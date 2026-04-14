package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Parcel;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;
import com.example.openradiofm.engine.QFTunerAdapter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioFocusRequest;
import android.media.AudioAttributes;
import android.Manifest;
import androidx.core.content.ContextCompat;

/**
 * V7.1: Implementación de la interfaz de radio para K706 (MT8163).
 * 
 * Correcciones:
 * - Sub-comandos MCU corregidos según TunerCmdFactory.smali
 * - DX/Local toggle con estado real
 * - Frecuencia se actualiza vía callback MCU (no solo RPC_GetChannel)
 * - AutoScan usa sub-comando correcto (0x08)
 * - Logging detallado para diagnóstico
 */
public class K706RadioManager extends IRadioServiceAPI.Stub {

    private static final String TAG = "K706RadioManager";
    private static final String SERVICE_NAME = "mcu_service";

    // Broadcast interno para sincronizar MediaSession/Android Auto con el AudioFocus real del SoC
    public static final String ACTION_OEM_AUDIO_FOCUS = "com.example.openradiofm.OEM_AUDIO_FOCUS";
    public static final String EXTRA_FOCUS_EVENT = "event";
    public static final String EVENT_LOSS = "LOSS";
    public static final String EVENT_LOSS_TRANSIENT = "LOSS_TRANSIENT";
    public static final String EVENT_GAIN = "GAIN";
    
    // === Sub-comandos MCU (V9.6: Oficiales extraídos de TunerCmdFactory.smali) ===
    private static final int CMD_TUNER = 0xA0; // Comando base tuner
    private static final byte SUB_TUNE_FREQUENCY = 0x00;
    private static final byte SUB_SEEK_UP        = 0x01;
    private static final byte SUB_SEEK_DOWN      = 0x02;
    private static final byte SUB_FINE_UP        = 0x03;
    private static final byte SUB_FINE_DOWN      = 0x04;
    private static final byte SUB_AUTO_SCAN_BGN  = 0x05;
    private static final byte SUB_SWITCH_BAND    = 0x06;
    private static final byte SUB_SWITCH_LOC     = 0x07;
    private static final byte SUB_TUNE_AS        = 0x08;
    private static final byte SUB_TUNE_PS        = 0x09;
    private static final byte SUB_TUNE_AREA      = 0x0A;
    private static final byte SUB_PRESET_SAVE    = 0x0B;
    private static final byte SUB_AUTO_SCAN_STOP = 0x0C; // Stop Scan
    private static final byte SUB_PRESET_SELECT  = 0x0D;
    private static final byte SUB_TUNE_NEXT      = 0x0E;
    private static final byte SUB_TUNE_PREV      = 0x0F;
    private static final byte SUB_TUNE_ST        = 0x10;
    private static final byte SUB_RDS_AF         = 0x11;
    private static final byte SUB_RDS_TA         = 0x12;
    private static final byte SUB_RDS_REG        = 0x13;
    private static final byte SUB_RDS_EON        = 0x14;
    private static final byte SUB_RDS_PTY        = 0x15;

    // Bandas K706
    private static final byte BAND_FM1 = 0;
    private static final byte BAND_FM2 = 1;
    private static final byte BAND_FM3 = 2;
    private static final byte BAND_AM1 = 3;
    private static final byte BAND_AM2 = 4;

    private Context mContext;
    private Object mMcuManager;
    private Method mSendMcuMsgData;
    private Method mSendMcuCmdData; // V9.7: Canal de comandos (prefijo 0x40)
    private Method mSetMute; // RPC_SetVolumeMute
    private Method mGetChannel; // RPC_GetChannel (audio channel, NOT frequency)
    private Method mSetChannel; // RPC_SetChannel (Audio routing MCU)

    // --- Broadcom FmReceiverService Reflection (V9.9: RDS Silencioso) ---
    private Object mFmReceiverService;
    private Method mBroadcomSetRdsMode;

    // V9.6: QFTunerManager - Canal de alto nivel para seek/scan/RDS
    private QFTunerAdapter mQfAdapter;

    private IRadioCallBack mCallback;
    /** Cache en unidades OpenRadioFM (×1000 kHz, ej. 87500 = 87.50 MHz). Debe coincidir con {@link #updateFrequency}. */
    private int mCurrentFreq = 87500;
    public int mLastFreq = -1;
    public boolean mIsStereo = false; // V12.3: Estado real de Stereo
    public boolean mHasRdsLock = false;
    private int mCurrentBand = BAND_FM1;
    private boolean mIsDxLocal = false; // V7.1: Estado DX/Local real
    private boolean mIsScanning = false; // V7.1: Estado de scan
    private boolean mIsSeeking = false; // V7.1: Estado de seek
    private boolean mIsTaEnabled = false; // V9.6: Estado TA habilitado
    private boolean mIsTpEnabled = false; // V9.6: Estado TP disponible
    private boolean mIsAfEnabled = false; // V9.6: Estado AF habilitado
    private boolean mIsRdsEnabled = true; // V7.2e: Estado RDS Global
    private byte[] mLastSignalData = null; // V15.7: Último paquete 0x41 (Telemetría RSSI/SNR)
    private AudioManager mAudioManager;
    private OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioFocusRequest mAudioFocusRequest;
    private boolean mIsAudioFocusHeld = false;
    private boolean mIsOnlineStreamingActive = false; // V18.3: Evita mutes en AudioFocusChange si el stream está OK
    private boolean mIsRadioActive = false;
    private boolean mIsInCall = false; // V11.5: Flag para estado de llamada telefónica
    private boolean mIsTransientFocusLoss = false; // V17.0: Spotify/Android Auto
    
    // V24.3: RAW DATA LISTENERS (Engineering)
    public interface RawMcuListener {
        void onRawData(byte[] data);
    }
    private final java.util.List<RawMcuListener> mRawListeners = new java.util.ArrayList<>();
    public void addRawMcuListener(RawMcuListener l) { synchronized(mRawListeners) { mRawListeners.add(l); } }
    public void removeRawMcuListener(RawMcuListener l) { synchronized(mRawListeners) { mRawListeners.remove(l); } }
    
    /** V24.8: Enviar comando RAW a la MCU (canal 0x40). */
    public void sendRawMcuCommand(byte[] data) {
        if (data == null || data.length == 0 || mMcuManager == null) return;
        try {
            if (mSendMcuCmdData != null) {
                mSendMcuCmdData.invoke(mMcuManager, (Object) data);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending raw MCU command", e);
        }
    }

    /** V24.8: Ajustar sensibilidad del sintonizador (0-15). */
    public void setTunerSensitivity(int level) {
        // Por ahora lo dejamos como log, hasta confirmar sub-comando exacto.
        Log.d(TAG, "setTunerSensitivity: " + level);
    }
    
    // OEM fix: evitar "mute inicial" por pérdidas espurias de AudioFocus (Zlink/Auto al enganchar)
    private long mIgnoreFocusLossUntilUptimeMs = 0L;
    private boolean mWasRadioActiveBeforeFocusLoss = false;
    
    // OEM fix: recuperar audio sin depender de recreación de layout.
    // En K706 el sistema puede forzar MUTE_EQ + SetChannel(4) tras un LOSS.
    // La app debe reintentar recuperar canal FM si el usuario "quiere FM".
    private boolean mUserWantsFmAudio = false;
    private final android.os.Handler mAudioRecoveryHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private int mAutoRecoveryAttempts = 0;
    private final Runnable mAutoRecoveryRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mUserWantsFmAudio) return;
            if (mIsOnlineStreamingActive) return;
            if (mIsInCall) return;
            
            // Si ya estamos activos, no martilleamos el MCU.
            if (mIsRadioActive) return;
            
            // Backoff simple: 1.5s, 3s, 6s, 10s (máx 4 intentos)
            if (mAutoRecoveryAttempts >= 4) return;
            mAutoRecoveryAttempts++;
            long delayMs;
            switch (mAutoRecoveryAttempts) {
                case 1: delayMs = 1500L; break;
                case 2: delayMs = 3000L; break;
                case 3: delayMs = 6000L; break;
                default: delayMs = 10000L; break;
            }
            
            try {
                // V18.5: CRÍTICO - Solo recuperar si REALMENTE tenemos el foco de audio.
                // Si el sistema nos dio LOSS, no debemos intentar recuperar el canal de radio.
                if (!mIsAudioFocusHeld) {
                    Log.d(TAG, "OEM AutoRecovery: abortado (no tenemos el foco de audio)");
                    return;
                }

                Log.d(TAG, "OEM AutoRecovery: intento " + mAutoRecoveryAttempts + " (recuperando canal FM)");
                // Forza SetChannel(2) + setAudioParams(true) + setMute(false) internamente
                enforceAudioChannelRecovery();
                try { setMute(false); } catch (Exception ignored) {}
                mIsRadioActive = true;
            } catch (Exception e) {
                Log.w(TAG, "OEM AutoRecovery falló (intento " + mAutoRecoveryAttempts + ")", e);
            }
            
            // Programar siguiente intento por si el sistema nos vuelve a tumbar (máximo 4 intentos total)
            if (mAutoRecoveryAttempts < 4) {
                mAudioRecoveryHandler.postDelayed(this, delayMs);
            }
        }
    };
    
    // V13.1: Estabilización de Dial / Debouncing
    private long mLastFreqUpdateTime = 0;
    private int mPendingFreq = -1;
    private final android.os.Handler mFreqHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable mFreqDebounceRunnable = () -> {
        if (mPendingFreq != -1) {
            updateFrequencyDelayed(mPendingFreq);
            mPendingFreq = -1;
        }
    };

    /** Requiere {@link Manifest.permission#READ_PHONE_STATE} concedido en Android 6+. */
    private android.telephony.PhoneStateListener mPhoneStateListener;
    private volatile boolean mPhoneListenerRegistered;

    public K706RadioManager(Context context) {
        this.mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d(TAG, "onAudioFocusChange: " + focusChange);
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.d(TAG, "--->>onAudioFocusChange()  ----AUDIOFOCUS_LOSS----");
                        broadcastOemFocus(EVENT_LOSS);
                        if (mIsOnlineStreamingActive) {
                            Log.d(TAG, "onAudioFocusChange(LOSS): Ignorando mute porque el Streaming Online está activo");
                            break;
                        }
                        // K706 OEM FIX (V18.5): Solo ignorar LOSS si es extremadamente reciente tras pedir foco (ventana de 2.5s)
                        // y el usuario quiere FM. Esto evita que la app "muera" al arrancar en algunos firmwares chinos,
                        // pero permite que Spotify/Música tomen el control después.
                        if (mUserWantsFmAudio && android.os.SystemClock.uptimeMillis() < mIgnoreFocusLossUntilUptimeMs) {
                            Log.d(TAG, "AUDIOFOCUS_LOSS (Glitch Protect): mUserWantsFmAudio=true -> manteniendo FM, forzando recovery");
                            mIsRadioActive = true;
                            mWasRadioActiveBeforeFocusLoss = true;
                            mAudioRecoveryHandler.removeCallbacks(mAutoRecoveryRunnable);
                            mAutoRecoveryAttempts = 0;
                            mAudioRecoveryHandler.postDelayed(mAutoRecoveryRunnable, 500L);
                            break;
                        }
                        // Ignorar pérdidas inmediatas tras pedir foco si todavía no estábamos activos
                        if (android.os.SystemClock.uptimeMillis() < mIgnoreFocusLossUntilUptimeMs && !mIsRadioActive) {
                            Log.d(TAG, "onAudioFocusChange(LOSS): Ignorado (startup/espurio). mIsRadioActive=false");
                            break;
                        }
                        try {
                            // Secuencia de salida segura
                            mWasRadioActiveBeforeFocusLoss = mIsRadioActive;
                            mIsRadioActive = false; // Detener Heartbeat inmediatamente
                            // Solo silenciar si realmente estaba sonando/activo
                            if (mWasRadioActiveBeforeFocusLoss) {
                                setMute(true);
                            } else {
                                Log.d(TAG, "AUDIOFOCUS_LOSS: Skip setMute(true) (radio no activa)");
                            }
                            if (mSetChannel != null && mMcuManager != null) {
                                mSetChannel.invoke(mMcuManager, (byte) 4); // Devolver contexto
                            }
                            setAudioParams(false); // Apagar flag radio
                            
                            // V18.4: ¡IMPORTANTE! Desmutear para que la nueva app que tomó el foco (Spotify/YouTube) se oiga.
                            setMute(false);
                        } catch (Exception e) {
                            Log.e(TAG, "Error on AUDIOFOCUS_LOSS", e);
                        }
                        // OEM: no dependas de recreación de layout para volver a sonar.
                        // Programamos intentos de autorecuperación si el usuario quería FM.
                        if (mUserWantsFmAudio) {
                            mAutoRecoveryAttempts = 0;
                            mAudioRecoveryHandler.removeCallbacks(mAutoRecoveryRunnable);
                            mAudioRecoveryHandler.postDelayed(mAutoRecoveryRunnable, 1500L);
                        }
                        abandonAudioFocus();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.d(TAG, "--->>onAudioFocusChange()  ----AUDIOFOCUS_LOSS_TRANSIENT (" + focusChange + ")----");
                        broadcastOemFocus(EVENT_LOSS_TRANSIENT);
                        if (mIsOnlineStreamingActive) {
                            Log.d(TAG, "onAudioFocusChange(LOSS_T): Ignorando mute porque el Streaming Online está activo");
                            break;
                        }
                        // V18.5: En LOSS_TRANSIENT (llamadas/navegación), respetamos SIEMPRE la interrupción.
                        // Solo usamos recovery si es un glitch de inicio.
                        if (mUserWantsFmAudio && android.os.SystemClock.uptimeMillis() < mIgnoreFocusLossUntilUptimeMs) {
                            Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT (Glitch Protect): forzando recovery");
                            mIsRadioActive = true;
                            mWasRadioActiveBeforeFocusLoss = true;
                            mAudioRecoveryHandler.removeCallbacks(mAutoRecoveryRunnable);
                            mAutoRecoveryAttempts = 0;
                            mAudioRecoveryHandler.postDelayed(mAutoRecoveryRunnable, 500L);
                            break;
                        }
                        if (android.os.SystemClock.uptimeMillis() < mIgnoreFocusLossUntilUptimeMs && !mIsRadioActive) {
                            Log.d(TAG, "onAudioFocusChange(LOSS_T): Ignorado (startup/espurio). mIsRadioActive=false");
                            break;
                        }
                        mIsAudioFocusHeld = true;
                        
                        // V17.0: Diferenciar llamada real de interrupción de música
                        if (mAudioManager.isMusicActive()) {
                             mIsTransientFocusLoss = true;
                             mIsInCall = false;
                        } else {
                             mIsInCall = true;
                             mIsTransientFocusLoss = false;
                        }
                        
                        try {
                            mWasRadioActiveBeforeFocusLoss = mIsRadioActive;
                            mIsRadioActive = false; // Detener Heartbeat temporalmente
                            if (mWasRadioActiveBeforeFocusLoss) {
                                setMute(true);
                            } else {
                                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT: Skip setMute(true) (radio no activa)");
                            }
                            // V11.5: Soltar canal MCU para que BT/teléfono suene limpio
                            if (mSetChannel != null && mMcuManager != null) {
                                mSetChannel.invoke(mMcuManager, (byte) 4);
                                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT: RPC_SetChannel(4) - canal FM liberado (mIsInCall=" + mIsInCall + ")");
                            }
                            // V18.4: Desmutear canal 4 para que Android/BT se oiga
                            setMute(false);
                        } catch (Exception e) {
                            Log.e(TAG, "Error on AUDIOFOCUS_LOSS_TRANSIENT", e);
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.d(TAG, "--->>onAudioFocusChange()  ----AUDIOFOCUS_GAIN----");
                        broadcastOemFocus(EVENT_GAIN);
                        mIsAudioFocusHeld = true;
                        mIsInCall = false;
                        mIsTransientFocusLoss = false;
                        // Rehabilitar heartbeat solo si veníamos de una radio activa
                        if (mWasRadioActiveBeforeFocusLoss) {
                            mIsRadioActive = true;
                            mWasRadioActiveBeforeFocusLoss = false;
                        }
                        // Al recuperar foco, cancelamos backoff y forzamos recuperación inmediata.
                        mAudioRecoveryHandler.removeCallbacks(mAutoRecoveryRunnable);
                        mAutoRecoveryAttempts = 0;
                        // Forzar recuperación inmediata
                        enforceAudioChannelRecovery();
                        break;
                }
            }
        };
        initMcuConnection();

        // V11.5 + runtime: READ_PHONE_STATE — ver registerPhoneStateListenerIfPermitted()
        registerPhoneStateListenerIfPermitted();
    }

    /**
     * Silencia FM durante llamadas y restaura al colgar. Requiere permiso en Android 6+.
     * Llamar de nuevo tras {@link android.app.Activity#onRequestPermissionsResult} si se concede.
     */
    public void registerPhoneStateListenerIfPermitted() {
        if (mContext == null) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "READ_PHONE_STATE no concedido: concede permiso para silenciar FM en llamadas");
                return;
            }
        }
        if (mPhoneListenerRegistered) return;
        try {
            android.telephony.TelephonyManager tm =
                    (android.telephony.TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) return;

            if (mPhoneStateListener == null) {
                mPhoneStateListener = new android.telephony.PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String phoneNumber) {
                        switch (state) {
                            case android.telephony.TelephonyManager.CALL_STATE_RINGING:
                            case android.telephony.TelephonyManager.CALL_STATE_OFFHOOK:
                                if (!mIsInCall) {
                                    mIsInCall = true;
                                    mIsTransientFocusLoss = false;
                                    mIsRadioActive = false;
                                    Log.d(TAG, "📞 Llamada detectada - silenciando radio FM");
                                    try {
                                        setMute(true);
                                        if (mSetChannel != null && mMcuManager != null) {
                                            mSetChannel.invoke(mMcuManager, (byte) 4);
                                            Log.d(TAG, "📞 RPC_SetChannel(4) - canal FM liberado");
                                        }
                                        setMute(false);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error silenciando radio en llamada", e);
                                    }
                                }
                                break;
                            case android.telephony.TelephonyManager.CALL_STATE_IDLE:
                                if (mIsInCall) {
                                    mIsInCall = false;
                                    Log.d(TAG, "📞 Llamada finalizada - restaurando radio FM");
                                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                        try {
                                            if (mSetChannel != null && mMcuManager != null) {
                                                mSetChannel.invoke(mMcuManager, (byte) 2);
                                                Log.d(TAG, "📞 RPC_SetChannel(2) - canal FM recuperado");
                                            }
                                            setAudioParams(true);
                                            setMute(false);
                                            mUserWantsFmAudio = true;
                                            try {
                                                requestPlayAudio();
                                            } catch (RemoteException ignored) {
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error restaurando radio tras llamada", e);
                                        }
                                    }, 800);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                };
            }
            tm.listen(mPhoneStateListener, android.telephony.PhoneStateListener.LISTEN_CALL_STATE);
            mPhoneListenerRegistered = true;
            Log.d(TAG, "PhoneStateListener registrado (silencio FM en llamadas)");
        } catch (Exception e) {
            Log.e(TAG, "registerPhoneStateListenerIfPermitted", e);
        }
    }

    private void broadcastOemFocus(String event) {
        try {
            if (mContext == null) return;
            Intent i = new Intent(ACTION_OEM_AUDIO_FOCUS);
            i.setPackage(mContext.getPackageName()); // solo nuestra app
            i.putExtra(EXTRA_FOCUS_EVENT, event);
            mContext.sendBroadcast(i);
        } catch (Exception e) {
            Log.w(TAG, "broadcastOemFocus falló: " + event, e);
        }
    }

    /**
     * Transact 1001: permiso de paquete para recibir {@code onMcuInfoChanged} (RDS, telemetría, etc.).
     */
    private void requestMcuPermissionHandshake(IBinder mcuServiceBinder) {
        if (mcuServiceBinder == null || mContext == null) return;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.qf.mcu.IMcuManager");
            data.writeString(mContext.getPackageName());
            mcuServiceBinder.transact(1001, data, reply, 0);
            reply.readException();
            Log.d(TAG, "Mcu permission handshake (1001) OK");
        } catch (Exception e) {
            Log.e(TAG, "Mcu permission handshake failed", e);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    /**
     * La radio OEM suele volver a registrar su {@code IMcuListener} y deja de entregar RDS a OpenRadioFM.
     * Vuelve a ejecutar el handshake 1001 y {@link #registerMcuListener()} sin recrear todo el manager.
     */
    public void reassertMcuInfoListener() {
        if (mMcuManager == null) {
            Log.w(TAG, "reassertMcuInfoListener: mMcuManager null (motor no inicializado)");
            return;
        }
        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, SERVICE_NAME);
            if (binder != null) {
                requestMcuPermissionHandshake(binder);
            }
        } catch (Exception e) {
            Log.w(TAG, "reassertMcuInfoListener: handshake opcional falló", e);
        }
        registerMcuListener();
        Log.i(TAG, "reassertMcuInfoListener: listener re-registrado");
    }

    private void initMcuConnection() {
        try {
            // 1. Get ServiceManager -> mcu_service
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, SERVICE_NAME);

            if (binder == null) {
                Log.e(TAG, "Failed to get mcu_service binder");
                return;
            }
            // 2. Get IMcuManager Interface
            Class<?> stubClass = Class.forName("android.qf.mcu.IMcuManager$Stub");
            Method asInterface = stubClass.getMethod("asInterface", IBinder.class);
            mMcuManager = asInterface.invoke(null, binder);
            
            Log.d(TAG, "Got IMcuManager instance: " + mMcuManager);

            // 3. Send "Request Permission" Transaction (1001) - Critical for receiving events
            requestMcuPermissionHandshake(binder);

            // 4. Register McuListener via Proxy to receive callbacks
            registerMcuListener();

            // 5. Reflect RPC methods for control
            Class<?> mcuManagerClass = mMcuManager.getClass();
            try {
                mSendMcuMsgData = mcuManagerClass.getMethod("RPC_SendMcuMsgData", byte.class, byte[].class, int.class);
                Log.d(TAG, "RPC_SendMcuMsgData: OK");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "RPC_SendMcuMsgData not found");
            }

            try {
                mSendMcuCmdData = mcuManagerClass.getMethod("RPC_SendMcuCmdData", byte.class, byte[].class, int.class);
                Log.d(TAG, "RPC_SendMcuCmdData: OK");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "RPC_SendMcuCmdData not found");
            }

            try {
                mSetMute = mcuManagerClass.getMethod("RPC_SetVolumeMute", int.class);
                Log.d(TAG, "RPC_SetVolumeMute(int): OK");
            } catch (NoSuchMethodException e) {
                 try {
                     mSetMute = mcuManagerClass.getMethod("RPC_SetVolumeMute", boolean.class);
                     Log.d(TAG, "RPC_SetVolumeMute(boolean): OK");
                 } catch (Exception ex) {
                     Log.w(TAG, "RPC_SetVolumeMute not found");
                 }
            }

            try {
                mGetChannel = mcuManagerClass.getMethod("RPC_GetChannel"); 
                Log.d(TAG, "RPC_GetChannel: OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "RPC_GetChannel not found, will rely on listener events");
            }

            // V7.0: Reflect RPC_SetChannel (critical for audio routing)
            // V7.2: CONFIRMADO por logcat: la firma real es RPC_SetChannel(byte), NO int
            try {
                mSetChannel = mcuManagerClass.getMethod("RPC_SetChannel", byte.class);
                Log.d(TAG, "RPC_SetChannel(byte): OK");
            } catch (NoSuchMethodException e) {
                // Fallback a int si byte no funciona
                try {
                    mSetChannel = mcuManagerClass.getMethod("RPC_SetChannel", int.class);
                    Log.d(TAG, "RPC_SetChannel(int): OK (fallback)");
                } catch (NoSuchMethodException e2) {
                    Log.e(TAG, "RPC_SetChannel not found!");
                }
            }

            // V9.6: Reflect QFTunerManager (canal de alto nivel para seek/scan/RDS)
            initQFTunerManager();
            
            // V9.9: Broadcom FmReceiverService (RDS silente / alternativo a QFTuner)
            initBroadcomFmReceiverService();

            // V7.1: Log ALL available methods for debugging
            Log.d(TAG, "=== Métodos disponibles en IMcuManager ===");
            for (Method m : mcuManagerClass.getMethods()) {
                if (m.getName().startsWith("RPC_")) {
                    StringBuilder params = new StringBuilder();
                    for (Class<?> p : m.getParameterTypes()) {
                        if (params.length() > 0) params.append(", ");
                        params.append(p.getSimpleName());
                    }
                    Log.d(TAG, "  " + m.getName() + "(" + params + ") -> " + m.getReturnType().getSimpleName());
                }
            }

            // === SECUENCIA DE INICIO DE AUDIO FM ===
            startFmAudioSequence();

            Log.d(TAG, "K706RadioManager initialized and connected.");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing McuManager", e);
        }
    }

    private void registerMcuListener() {
        // V7.2c: SOLUCIÓN DEFINITIVA
        // El Proxy.newProxyInstance NO funciona con AIDL porque no genera un IBinder válido.
        // IMcuListener es una interfaz AIDL, así que necesitamos un IMcuListener.Stub real.
        // Error anterior: "need eiter listener" = el servicio rechaza el proxy porque
        // no es un Binder válido.
        
        // Estrategia: Crear instancia de IMcuListener.Stub (clase abstracta) via bytecode proxy
        // usando android.os.Binder directamente.
        
        try {
            // Paso 1: Obtener la clase IMcuListener.Stub
            Class<?> stubClass = Class.forName("android.qf.mcu.IMcuListener$Stub");
            Log.d(TAG, "IMcuListener.Stub class found: " + stubClass.getName());
            
            // Paso 2: Listar métodos abstractos para debug
            for (Method m : stubClass.getDeclaredMethods()) {
                Log.d(TAG, "  Stub method: " + m.getName() + " modifiers=" + m.getModifiers());
            }
            
            // Paso 3: Crear una subclase dinámica no es posible en Android sin dex-gen.
            // Alternativa: Usar Binder directamente con el descriptor AIDL correcto.
            // Creamos un Binder que responda a la transacción onMcuInfoChanged.
            
            // El descriptor AIDL de IMcuListener
            String descriptor = "android.qf.mcu.IMcuListener";
            
            android.os.Binder listenerBinder = new android.os.Binder() {
                @Override
                protected boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws RemoteException {
                    Log.d(TAG, "IMcuListener.onTransact: code=" + code + " flags=" + flags);
                    
                    switch (code) {
                        case android.os.IBinder.INTERFACE_TRANSACTION:
                            reply.writeString(descriptor);
                            return true;
                        case 1: // TRANSACTION_onMcuInfoChanged (primer método AIDL = code 1)
                            data.enforceInterface(descriptor);
                            byte[] mcuData = data.createByteArray();
                            Log.d(TAG, ">>> IMcuListener.onMcuInfoChanged received! data=" + 
                                (mcuData != null ? bytesToHex(mcuData) : "null"));
                            if (mcuData != null) {
                                handleMcuData(mcuData);
                            }
                            if (reply != null) {
                                reply.writeNoException();
                            }
                            return true;
                        default:
                            Log.d(TAG, "IMcuListener.onTransact: unhandled code=" + code);
                            return super.onTransact(code, data, reply, flags);
                    }
                }
            };
            listenerBinder.attachInterface(null, descriptor);
            
            // Paso 4: Registrar el Binder. El stub proxy del otro lado hará
            // IMcuListener.Stub.asInterface(binder) que aceptará nuestro Binder real.
            Class<?> listenerInterface = Class.forName("android.qf.mcu.IMcuListener");
            Method requestListener = mMcuManager.getClass().getMethod(
                "RPC_RequestMcuInfoChangedListener", listenerInterface, String.class);
            
            // Necesitamos pasar un objeto que implemente IMcuListener.
            // Usamos IMcuListener.Stub.asInterface(listenerBinder) para envolver nuestro Binder.
            Method asInterface = stubClass.getMethod("asInterface", android.os.IBinder.class);
            Object listenerObj = asInterface.invoke(null, listenerBinder);
            Log.d(TAG, "IMcuListener proxy created via Stub.asInterface: " + listenerObj.getClass().getName());
            
            requestListener.invoke(mMcuManager, listenerObj, "com.example.openradiofm");
            Log.d(TAG, "✓ Registered IMcuListener (AIDL Binder) successfully!");
            
        } catch (Exception e) {
            Log.e(TAG, "Error registering IMcuListener (Binder approach)", e);
            // Último recurso: intentar con Binder directo sin asInterface
            try {
                registerMcuListenerDirect();
            } catch (Exception e2) {
                Log.e(TAG, "Direct Binder approach also failed", e2);
            }
        }
    }
    
    private void registerMcuListenerDirect() throws Exception {
        // Enfoque alternativo: pasar el Binder directamente sin Stub.asInterface
        String descriptor = "android.qf.mcu.IMcuListener";
        
        android.os.Binder listenerBinder = new android.os.Binder() {
            @Override
            protected boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws RemoteException {
                Log.d(TAG, "IMcuListener(direct).onTransact: code=" + code);
                if (code == 1) { // onMcuInfoChanged
                    data.enforceInterface(descriptor);
                    byte[] mcuData = data.createByteArray();
                    if (mcuData != null) {
                        Log.d(TAG, ">>> Direct: onMcuInfoChanged data=" + bytesToHex(mcuData));
                        handleMcuData(mcuData);
                    }
                    if (reply != null) reply.writeNoException();
                    return true;
                }
                return super.onTransact(code, data, reply, flags);
            }
        };
        listenerBinder.attachInterface(null, descriptor);
        
        // Intentar envolver en IMcuListener via Stub.asInterface
        Class<?> stubClass = Class.forName("android.qf.mcu.IMcuListener$Stub");
        Method asInterface = stubClass.getMethod("asInterface", android.os.IBinder.class);
        Object listenerObj = asInterface.invoke(null, listenerBinder);
        
        Class<?> listenerInterface = Class.forName("android.qf.mcu.IMcuListener");
        Method requestListener = mMcuManager.getClass().getMethod(
            "RPC_RequestMcuInfoChangedListener", listenerInterface, String.class);
        requestListener.invoke(mMcuManager, listenerObj, "com.example.openradiofm");
        Log.d(TAG, "✓ Registered IMcuListener (direct Binder) successfully!");
    }

    // ==========================================
    // MCU DATA HANDLING (0xB0, 0xB1, 0xB6, etc.)
    // ==========================================

    private void handleMcuData(byte[] data) {
        if (data == null || data.length == 0) return;
        
        int packetType = data[0] & 0xFF;
        
        // V7.1: Log TODOS los paquetes MCU para diagnóstico
        StringBuilder hexDump = new StringBuilder();
        for (byte b : data) hexDump.append(String.format("%02X ", b));
        
        String researchPrefix = "";
        if (packetType == 0xB0 || packetType == 0xB5 || packetType == 0xB6 || packetType == 0xB7 || packetType == 0xBC) {
            researchPrefix = "🔬 [RESEARCH] ";
        }
        Log.d(TAG, researchPrefix + "MCU[0x" + String.format("%02X", packetType) + "] len=" + data.length + " : " + hexDump.toString());

        try {
            switch (packetType) {
                case 0xB0: // Tuner Info (Flags + Frequency)
                    handleTunerInfo(data);
                    break;
                case 0xB1: // Preset List / Current Station
                    handlePresetList(data);
                    break;
                case 0xB3: // RDS RT Info (algunos firmwares) o flags AF/TA (otros firmwares)
                    // PROTOCOLO_MCU_K706.md: 0xB3 = RDS RT (Radio Text).
                    // Observación de campo: en ciertos K706/QF, 0xB3 se usa como flags AF/TA.
                    // Estrategia: si el paquete parece texto, parsear RT; si no, tratarlo como flags.
                    if (data.length > 2 && looksLikeAsciiTextPayload(data, 1)) {
                        fireEvent(110, "B3 RT?: " + bytesToHex(data));
                        handleRdsRt(data);
                        break;
                    }
                    if (data.length > 1) {
                        int rdsFlagsB3 = data[1] & 0xFF;
                        Log.d(TAG, "RDS B3 Flags: 0x" + String.format("%02X", rdsFlagsB3));
                        
                        // V9.9: Según TunerManagerForExt$UITunerObserver.onTuneRdsInfo:
                        // bit 0: AF Switch
                        // bit 1: TA Switch
                        boolean afState = (rdsFlagsB3 & 0x01) != 0;
                        boolean taState = (rdsFlagsB3 & 0x02) != 0;

                        if (mIsAfEnabled != afState) {
                            mIsAfEnabled = afState;
                            fireEvent(111, "AF:" + (afState ? 1 : 0));
                        }
                        if (mIsTaEnabled != taState) {
                            mIsTaEnabled = taState;
                            fireEvent(112, "TA_SWITCH:" + (taState ? 1 : 0));
                            Log.d(TAG, "TA Switch state updated from MCU B3: " + taState);
                        }
                    }
                    fireEvent(110, "B3: " + bytesToHex(data));
                    break;
                case 0xB4: // RDS Indicate Info (solo TP, NO TA - TA se controla desde 0xB3)
                    if (data.length > 1) {
                        int rdsFlagsB4 = data[1] & 0xFF;
                        Log.d(TAG, "RDS B4 Flags: 0x" + String.format("%02X", rdsFlagsB4));
                        
                        // V4.6.1: Solo TP desde B4. TA se maneja exclusivamente en B3.
                        boolean tpState = (rdsFlagsB4 & 0x10) != 0 || (rdsFlagsB4 & 0x01) != 0;
                        
                        if (mIsTpEnabled != tpState) {
                            mIsTpEnabled = tpState;
                            fireEvent(111, "TP:" + (tpState ? 1 : 0));
                        }
                    }
                    fireEvent(110, "B4: " + bytesToHex(data));
                    break;
                case 0xB5: // RDS PTY Type
                    fireEvent(110, "B5 PTY: " + bytesToHex(data));
                    if (data.length > 2) {
                        int pty = data[2] & 0xFF;
                        fireEvent(105, String.valueOf(pty)); // PTY = 105
                    }
                    break;
                case 0xB6: // RDS PS
                    fireEvent(110, "B6 PS:  " + bytesToHex(data));
                    handleRdsPs(data);
                    break;
                case 0xB7: // RDS RT Info
                    Log.d(TAG, "MCU[0xB7] RT RAW: " + bytesToHex(data));
                    fireEvent(110, "B7 RT:  " + bytesToHex(data));
                    handleRdsRt(data);
                    break;
                case 0xB8: // RDS PS Preset List (Research)
                    Log.d(TAG, "MCU[0xB8] PS Preset List RAW: " + bytesToHex(data));
                    break;
                case 0x41: // 🔬 Telemetría de señal (RSSI/SNR)
                    mLastSignalData = data.clone();
                    handleSignalQuality(data);
                    break;
                case 0x22: // 🚗 V24.5: Lights status (01=ON, 00=OFF)
                    if (data.length > 1) {
                        int lights = data[1] & 0xFF;
                        fireEvent(122, String.valueOf(lights));
                        Log.d(TAG, "🚗 MCU[0x22] LIGHTS: " + (lights == 1 ? "ON" : "OFF"));
                    }
                    break;
                case 0x23: // 🚗 V24.5: Reverse gear status (01=ON, 00=OFF)
                    if (data.length > 1) {
                        int reverse = data[1] & 0xFF;
                        fireEvent(123, String.valueOf(reverse));
                        Log.d(TAG, "🚗 MCU[0x23] REVERSE: " + (reverse == 1 ? "ACTIVE" : "INACTIVE"));
                    }
                    break;
                case 0x24: // 🚗 V24.5: ACC status (01=ON, 00=OFF)
                    if (data.length > 1) {
                        int acc = data[1] & 0xFF;
                        fireEvent(125, String.valueOf(acc));
                        Log.d(TAG, "🚗 MCU[0x24] ACC: " + (acc == 1 ? "ON" : "OFF"));
                    }
                    break;
                case 0x29: // V13.1: Heartbeat silencioso del MCU (29 6D)
                    // Ignoramos para evitar que se interprete como frecuencia
                    break; 
                default:
                    // Log unknown packets for research
                    if (packetType >= 0xB0 && packetType <= 0xBF) {
                        Log.d(TAG, "🔬 [RESEARCH] NEW RDS/TUNER PACKET FOUND: 0x" + String.format("%02X", packetType) + " -> " + bytesToHex(data));
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing MCU data", e);
        }

        // V24.3: Notify raw data listeners
        synchronized(mRawListeners) {
            for (RawMcuListener l : mRawListeners) {
                l.onRawData(data);
            }
        }
    }

    private void handleTunerInfo(byte[] data) {
        // V9.9: Mapeo CORREGIDO según TunerManagerForExt.smali ($UITunerObserver.onTunerInfoChanged)
        // Paquete 0xB0 [B0, flags]
        // Flags (byte[1]):
        //   bit 0: asFlag (AutoScan) -> 0x01
        //   bit 1: scanFlag          -> 0x02
        //   bit 2: psFlag            -> 0x04
        //   bit 3: seekFlag          -> 0x08
        //   bit 4: stFlag (Stereo)   -> 0x10
        //   bit 5: locFlag (Local)    -> 0x20
        //   bit 6: searching         -> 0x40
        //   bit 7: tempSearching     -> 0x80
        
        if (data.length < 2) return;
        
        int flags = data[1] & 0xFF;
        
        boolean asFlag     = (flags & 0x01) != 0; 
        boolean scanFlag   = (flags & 0x02) != 0;
        boolean flagsSeek   = (flags & 0x08) != 0;
        boolean stFlag     = (flags & 0x10) != 0; // V9.9: Bit 4 es Stereo
        boolean locFlag    = (flags & 0x20) != 0; // V9.9: Bit 5 es Local (1=Local, 0=DX)
        
        // V12.4: El estado de escaneo real para la UI solo debe ser AutoStore o Scan.
        // NO incluimos flagsSeek aquí para evitar que PresetManager oculte los nombres RDS.
        boolean newScanState = asFlag || scanFlag;
        if (mIsScanning != newScanState) {
            mIsScanning = newScanState;
            fireEvent(108, String.valueOf(mIsScanning ? 1 : 0));
        }
        mIsSeeking = flagsSeek;
        
        // V9.9: Solo actualizamos si cambia para evitar spam de eventos
        if (mIsDxLocal != locFlag) {
            mIsDxLocal = locFlag;
            Log.d(TAG, "DX/Local State Changed: " + (locFlag ? "LOCAL" : "DX"));
        }
        
        mIsStereo = stFlag; // V12.3: Actualizar estado real
        
        Log.d(TAG, "TunerInfo: flags=0x" + String.format("%02X", flags) + 
                    " AS=" + asFlag + " Scan=" + scanFlag + " Seek=" + flagsSeek +
                    " ST=" + stFlag + " LOC=" + locFlag);
        
        // V15.5 Research: Log extra bytes for Signal Quality (RSSI/SNR)
        if (data.length > 2) {
            StringBuilder extra = new StringBuilder();
            for (int i=2; i<data.length; i++) extra.append(String.format("[%d]=%02X ", i, data[i]));
            Log.d(TAG, "RESEARCH: MCU[0xB0] Extra Bytes (Potential RSSI/SNR): " + extra.toString());
        }
        
        // Notificar UI
        fireEvent(102, String.valueOf(stFlag ? 1 : 0)); // Stereo = 102
        fireEvent(106, String.valueOf(locFlag ? 1 : 0)); // DX/Local = 106
    }

    private void handlePresetList(byte[] data) {
        // V7.2: CONFIRMADO por logcat:
        // Formato 0xB1: [B1, band, presetIdx, freq1_hi, freq1_lo, freq2_hi, freq2_lo, ...]
        // Ejemplo real: [B1 00 05 24 04 22 38 22 CE 23 32 23 AA 24 04 24 68]
        //   band=0 (FM1), presetIdx=5 (actual)
        //   Presets: 0x2404=9220(92.2), 0x2238=8760(87.6), 0x22CE=8910(89.1), etc.
        // Frecuencias en Big-Endian, formato ×100 (9220 = 92.20 MHz)
        if (data.length < 5) return;
        
        int band = data[1] & 0xFF;
        int presetIdx = data[2] & 0xFF;
        
        // La frecuencia ACTUAL es la que está en la posición presetIdx
        // Cada frecuencia ocupa 2 bytes a partir de byte[3]
        // freq[0] = bytes[3..4], freq[1] = bytes[5..6], ...
        // freq[presetIdx] = bytes[3 + presetIdx*2 .. 4 + presetIdx*2]
        
        // Extraer frecuencia actual
        int freqOffset = 3 + presetIdx * 2;
        int currentFreq = -1;
        if (freqOffset + 1 < data.length) {
            int hi = data[freqOffset] & 0xFF;
            int lo = data[freqOffset + 1] & 0xFF;
            currentFreq = (hi << 8) | lo; // Big-Endian ×100
        }
        
        // También extraer la primera frecuencia como backup
        int firstFreqHi = data[3] & 0xFF;
        int firstFreqLo = data[4] & 0xFF;
        int firstFreq = (firstFreqHi << 8) | firstFreqLo;
        
        Log.d(TAG, "PresetList: band=" + band + " presetIdx=" + presetIdx + 
                    " currentFreq=" + currentFreq + " firstFreq=" + firstFreq +
                    " (offset=" + freqOffset + ")");
        
        // V9.4d: NO actualizar mCurrentBand desde 0xB1.
        // La MCU siempre reporta band=0 (FM1) en el preset list,
        // lo que causaba el reset a FM1 al hacer Seek.
        // La banda solo debe cambiar por acción explícita del usuario (onBandEvent).
        Log.d(TAG, "PresetList: band reportada=" + band + " (ignorada, mCurrentBand=" + mCurrentBand + ")");
        
        // Actualizar frecuencia (normalizar de ×100 a ×1000 para OpenRadioFM)
        if (currentFreq > 0 && isValidFreqMcu(currentFreq)) {
            updateFrequency(currentFreq);
        } else if (isValidFreqMcu(firstFreq)) {
            updateFrequency(firstFreq);
        }
    }

    private void handleRdsPs(byte[] data) {
        if (data.length < 3) return;
        try {
            // El offset del nombre PS varía. Probar desde byte 1.
            int startOffset = 1;
            int maxLen = Math.min(8, data.length - startOffset);
            if (maxLen <= 0) return;
            
            String psName = new String(data, startOffset, maxLen, "UTF-8").trim(); 
            psName = psName.replaceAll("[^\\x20-\\x7E]", "");
            
            if (!psName.isEmpty()) {
                Log.d(TAG, "RDS PS: '" + psName + "'");
                fireEvent(103, psName); // PS Name = 103
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RDS PS", e);
        }
    }

    private void handleRdsRt(byte[] data) {
        if (data.length <= 1) return;
        try {
            // V9.1: Logging extendido para depurar Radio Text
            // A veces el RadioText viene precedido por un flag o offset extra.
            String rtRaw = new String(data, 1, data.length - 1, "ISO-8859-1");
            Log.d(TAG, "RDS RT Try(1): '" + rtRaw + "'");
            
            // Usualmente RT empieza en el offset 1 o offset 2 en los sistemas chinos (0xB7 0x01 [Texto])
            int startOffset = 1;
            
            // Check if byte 1 is just a length or status, and text starts at 2
            if (data.length > 2 && data[1] < 10) {
                startOffset = 2; // skip status/length byte
            }
            
            int len = data.length - startOffset;
            if (len > 0) {
                String rtText = new String(data, startOffset, len, "ISO-8859-1");
                rtText = rtText.replace('\u0000', ' ').trim();
                rtText = rtText.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", " ");
                rtText = rtText.replaceAll("\\s{2,}", " ").trim();
                
                if (!rtText.isEmpty() && !rtText.equals("               ")) {
                    Log.d(TAG, "RDS RT Extracted: '" + rtText + "'");
                    fireEvent(104, rtText); // RT Text = 104
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RDS RT", e);
        }
    }

    /**
     * Heurística: muchos paquetes de RT vienen como ASCII/Latin1 legible con pocos bytes de control.
     */
    private static boolean looksLikeAsciiTextPayload(byte[] data, int offset) {
        if (data == null || data.length <= offset) return false;
        int printable = 0;
        int checked = 0;
        for (int i = offset; i < data.length && checked < 18; i++, checked++) {
            int b = data[i] & 0xFF;
            if (b == 0x00) break;
            if (b == 0x09 || b == 0x0A || b == 0x0D) { printable++; continue; }
            if (b >= 0x20 && b <= 0x7E) { printable++; continue; }
            // Latin1 printable range (áéíóúñ etc.)
            if (b >= 0xA0 && b <= 0xFF) { printable++; continue; }
        }
        return checked >= 6 && printable >= (checked * 3) / 4;
    }
    
    /**
     * V7.2e: Procesa datos de calidad de señal (0x41)
     * data[1] = RSSI (0-100 aprox)
     * data[2] = SNR / Quality
     */
    private void handleSignalQuality(byte[] data) {
        if (data.length < 3) return;
        
        int rssi = data[1] & 0xFF; // Fuerza de señal
        int snr  = data[2] & 0xFF; // Calidad / Relación señal-ruido
        
        // Enviar a la UI para pintar barras de colores
        // 120 -> RSSI, 121 -> SNR/Quality
        fireEvent(120, String.valueOf(rssi));
        fireEvent(121, String.valueOf(snr));
        
        Log.d(TAG, "Telemetry 0x41 -> RSSI: " + rssi + " | SNR: " + snr);
    }

    // V7.1: Helper para enviar eventos al callback de forma segura
    private void fireEvent(int code, String data) {
        if (mCallback != null) {
            try {
                mCallback.onEvent(code, data);
            } catch (RemoteException e) {
                Log.e(TAG, "Error firing event " + code, e);
            }
        }
    }

    /**
     * Valida frecuencia en formato MCU (×100).
     * Ejemplo: 8750 = 87.50 MHz, 9220 = 92.20 MHz
     */
    private boolean isValidFreqMcu(int freq) {
        // FM: 8750-10800 (87.5 - 108.0 MHz en ×100)
        // AM: 522-1710 kHz
        return (freq >= 8750 && freq <= 10800) || // FM
               (freq >= 522 && freq <= 1710);     // AM kHz
    }

    /**
     * V7.2: Recibe frecuencia en formato MCU (×100, ej: 9220=92.20MHz)
     * y la convierte al formato OpenRadioFM (×1000, ej: 92200)
     * para que la UI la muestre correctamente como 92.20
     */
    private void updateFrequency(int mcuFreq) {
        // Convertir de MCU×100 a OpenRadioFM×1000 
        int freqForUI;
        if (mcuFreq >= 8750 && mcuFreq <= 10800) {
            // V12.4: Redondear al múltiplo de 100 kHz (10 en MCU units) más cercano
            mcuFreq = ((mcuFreq + 5) / 10) * 10;
            freqForUI = mcuFreq * 10;
        } else if (mcuFreq >= 522 && mcuFreq <= 1710) {
            freqForUI = mcuFreq;
        } else {
            return;
        }
        
        if (freqForUI == mCurrentFreq) {
            mPendingFreq = -1;
            mFreqHandler.removeCallbacks(mFreqDebounceRunnable);
            return;
        }

        long now = android.os.SystemClock.elapsedRealtime();
        int diff = Math.abs(freqForUI - mCurrentFreq);
        
        // V13.1: Lógica de Suavizado (Histeresis/Debounce)
        // Si el cambio es el mínimo (100 kHz en OpenRadioFM units) y estamos en sintonía fina
        // o jitter de hardware, esperamos un poco para confirmar que se asienta.
        if (diff <= 100 && !mIsScanning && !mIsSeeking && (now - mLastFreqUpdateTime < 500)) {
            mPendingFreq = freqForUI;
            mFreqHandler.removeCallbacks(mFreqDebounceRunnable);
            mFreqHandler.postDelayed(mFreqDebounceRunnable, 150); // 150ms para estabilizar
            return;
        }

        // Si es un cambio mayor o el tiempo de gracia pasó, actualizamos inmediatamente
        updateFrequencyDelayed(freqForUI);
    }

    private void updateFrequencyDelayed(int freqForUI) {
        mFreqHandler.removeCallbacks(mFreqDebounceRunnable);
        int oldFreq = mCurrentFreq;
        mCurrentFreq = freqForUI;
        mLastFreqUpdateTime = android.os.SystemClock.elapsedRealtime();
        
        Log.d(TAG, ">>> FREQ STABLE: " + oldFreq + " -> " + freqForUI);
        notifyFreqUpdate();
    }
    
    private void notifyFreqUpdate() {
        fireEvent(100, String.valueOf(mCurrentFreq)); // Frequency = 100
        fireEvent(101, String.valueOf(mCurrentBand)); // Band = 101
    }
    
    private void sendMcuCmd(byte cmdId, byte[] data) {
        if (mMcuManager == null || mSendMcuCmdData == null) return;
        try {
            mSendMcuCmdData.invoke(mMcuManager, cmdId, data, data.length);
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMcuCmd", e);
        }
    }

    public String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    // V15.7: Getter para el Diálogo de Ingeniería
    public byte[] getLastSignalData() {
        return mLastSignalData;
    }

    // ==========================================
    // V9.6: QFTunerManager INIT (Alto nivel)
    // ==========================================

    /**
     * V9.6: Conecta con QFTunerManager del QF SDK via reflection.
     * Este es el canal que la app nativa usa para seek, scan y RDS.
     * sendCmd() vía RPC_SendMcuMsgData solo funciona fiable para tune/band/fine.
     */
    private void initQFTunerManager() {
        mQfAdapter = QFTunerAdapter.getInstance(mContext);
        if (mQfAdapter == null) {
            Log.w(TAG, "QFTunerManager SDK not available - hardware control will use raw MCU commands.");
            return;
        }

        Log.d(TAG, "QFTunerAdapter initialized successfully.");

        // V7.2c+: Registrar ITunerTool (QFTunerManager callbacks).
        try {
            Class<?> itunerToolClass = Class.forName("com.qf.clientsdk.ITunerTool");
            Object proxyTunerTool = Proxy.newProxyInstance(
                itunerToolClass.getClassLoader(),
                new Class<?>[] { itunerToolClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        final String name = method.getName();
                        
                        // Log call for research
                        StringBuilder argStr = new StringBuilder();
                        if (args != null) {
                            for (Object arg : args) argStr.append(arg != null ? arg.toString() : "null").append(" ");
                        }
                        Log.v(TAG, "🔬 ITunerTool: " + name + "(" + argStr.toString().trim() + ")");

                        // PI Code
                        if ("onCurrentFrequencyPICodeChange".equals(name)) {
                            if (args != null && args.length > 0 && args[0] instanceof Integer) {
                                int pi = (Integer) args[0];
                                fireEvent(107, String.valueOf(pi));
                            }
                        }
                        // PS
                        else if ("onTuneRdsPSInfo".equals(name)) {
                            if (args != null && args.length > 0 && args[0] instanceof byte[]) {
                                String ps = safeDecodeRdsBytes((byte[]) args[0], 1);
                                if (ps != null && !ps.isEmpty()) fireEvent(103, ps);
                            }
                        }
                        // RT
                        else if ("onTuneRdsRTInfo".equals(name) || "rds_stationRawTextChange".equals(name)) {
                            if (args != null && args.length > 0) {
                                String rt = null;
                                if (args[0] instanceof byte[]) rt = safeDecodeRdsBytes((byte[]) args[0], 1);
                                else if (args[0] instanceof String) rt = (String) args[0];
                                if (rt != null && !rt.isEmpty()) fireEvent(104, rt);
                            }
                        }
                        // PTY
                        else if ("onTuneRdsPtyTypeInfo".equals(name)) {
                            if (args != null && args.length > 0 && args[0] instanceof byte[]) {
                                byte[] b = (byte[]) args[0];
                                if (b.length > 2) fireEvent(105, String.valueOf(b[2] & 0xFF));
                            }
                        }
                        return null;
                    }
                });
            
            // mQfAdapter.setTunerTool(proxyTunerTool);
            Log.d(TAG, "V7.2e: ITunerTool Proxy DISABLED (Centralized RDS via handleMcuData)");
        } catch (Exception e) {
            Log.w(TAG, "Failed to register ITunerTool: " + e.getMessage());
        }
    }

    /**
     * Decodifica payloads RDS de QF/MCU intentando conservar caracteres (evitar filtro ASCII agresivo).
     * RDS clásico suele estar en ISO-8859-1; algunos firmwares empaquetan un byte de estado/len al inicio.
     */
    private String safeDecodeRdsBytes(byte[] data, int defaultStartOffset) {
        if (data == null || data.length <= defaultStartOffset) return null;
        try {
            int startOffset = Math.max(0, Math.min(defaultStartOffset, data.length - 1));

            // Heurística OEM: si el primer byte tras el header parece "len/status" pequeño, saltamos uno más.
            if (data.length > startOffset + 1) {
                int b = data[startOffset] & 0xFF;
                if (b < 10 && data.length > startOffset + 2) {
                    startOffset += 1;
                }
            }

            int len = data.length - startOffset;
            if (len <= 0) return null;

            String s;
            try {
                s = new String(data, startOffset, len, "ISO-8859-1");
            } catch (Exception e) {
                s = new String(data, startOffset, len, "UTF-8");
            }

            // Limpiar NULs y control chars, pero mantener caracteres extendidos.
            s = s.replace('\u0000', ' ').trim();
            s = s.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
            return s.trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Helper to initialize android.broadcom.IFmReceiverService via reflection for silent RDS setting.
     */
    private void initBroadcomFmReceiverService() {
        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, "fm_receiver");
            if (binder != null) {
                // IFmReceiverService.Stub.asInterface
                Class<?> stubClass = Class.forName("android.broadcom.IFmReceiverService$Stub");
                Method asInterface = stubClass.getMethod("asInterface", IBinder.class);
                mFmReceiverService = asInterface.invoke(null, binder);
                Log.d(TAG, "Broadcom FmReceiverService: Obtenido con éxito.");

                try {
                    // signature: setRdsMode(int rdsMode, int rdsFeatures, int afMode, int afThreshold)
                    mBroadcomSetRdsMode = mFmReceiverService.getClass().getMethod("setRdsMode", int.class, int.class, int.class, int.class);
                    Log.d(TAG, "Broadcom setRdsMode: CREADO con éxito.");
                } catch (NoSuchMethodException ex) {
                    Log.w(TAG, "Broadcom setRdsMode NO DISPONIBLE.");
                }
            } else {
                Log.w(TAG, "Broadcom FmReceiverService NOT FOUND (fm_receiver is null)");
            }
        } catch (Exception e) {
            Log.w(TAG, "Error localizando Broadcom FmReceiverService para RDS Silencioso.", e);
        }
    }

    // ==========================================
    // COMMAND SENDING
    // ==========================================

    private void sendCmd(byte subCmd, byte param1, byte param2) {
        if (mMcuManager == null || mSendMcuMsgData == null) {
            Log.e(TAG, "sendCmd FAILED: mMcuManager=" + (mMcuManager != null) + " mSendMcuMsgData=" + (mSendMcuMsgData != null));
            return;
        }
        try {
            byte[] payload = new byte[] { subCmd, param1, param2 };
            mSendMcuMsgData.invoke(mMcuManager, (byte) CMD_TUNER, payload, 3);
            Log.d(TAG, "CMD Enviado: sub=0x" + String.format("%02X", subCmd) + 
                        " p1=0x" + String.format("%02X", param1) + 
                        " p2=0x" + String.format("%02X", param2));
        } catch (Exception e) {
            Log.e(TAG, "Error enviando comando 0x" + String.format("%02X", subCmd), e);
        }
    }

    /**
     * V7.2f: Envía comando de hardware para forzar modo Mono o permitir Stereo.
     * @param enable true para Stereo (1), false para Mono (0).
     */
    public void setStereoMode(boolean enable) throws RemoteException {
        // Comando 0x10: 1=Stereo, 0=Mono
        sendCmd((byte) 0x10, (byte) (enable ? 1 : 0), (byte) 0);
        Log.d(TAG, "Direct MCU -> StereoMode changed to: " + (enable ? "STEREO" : "MONO") + " (cmd 0xA0 10)");
    }

    // ==========================================
    // AIDL INTERFACE IMPLEMENTATION
    // ==========================================

    @Override
    public void registerRadioClientBinder(IBinder binder) throws RemoteException {}

    @Override
    public void unRegisterRadioClientBinder() throws RemoteException {}

    @Override
    public void registerRadioCallback(IRadioCallBack cb) throws RemoteException {
        this.mCallback = cb;
        Log.d(TAG, "Callback registrado");
    }

    @Override
    public void unRegisterRadioCallback(IRadioCallBack cb) throws RemoteException {
        this.mCallback = null;
        Log.d(TAG, "Callback desregistrado");
    }

    @Override
    public void onBandEvent() throws RemoteException {
        mCurrentBand++;
        if (mCurrentBand > BAND_AM2) mCurrentBand = BAND_FM1;
        
        // V7.2e: Comunicación directa con MCU (Independencia total del SDK OEM)
        // El comando para cambiar la banda en MCU es 0x06 (SUB_SWITCH_BAND)
        sendCmd(SUB_SWITCH_BAND, (byte) mCurrentBand, (byte) 0);
        Log.d(TAG, "Direct MCU -> Change Band to: " + mCurrentBand + " (cmd 0xA0 06)");
        
        // Notificar inmediatamente el cambio a la UI
        fireEvent(101, String.valueOf(mCurrentBand));
    }

    public void closeDevice() throws RemoteException {
        Log.d(TAG, "Cerrando radio FM y restaurando contexto de audio");
        
        // V9.5: Emula secuencia estricta de salida de la app nativa 
        stopFmAudioSequence();
        
        abandonAudioFocus();
        if (mAudioManager != null && mContext != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(
                    new ComponentName(mContext.getPackageName(), "MediaButtonReceiver"));
        }
    }

    private void requestAudioFocus() {
        if (mAudioManager != null && !mIsAudioFocusHeld) {
            // Ventana anti-LOSS espurio tras pedir foco (Zlink/Auto)
            mIgnoreFocusLossUntilUptimeMs = android.os.SystemClock.uptimeMillis() + 2500L;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(attributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                        .build();

                int result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mIsAudioFocusHeld = true;
                    Log.d(TAG, "Audio Focus Granted (API 26+)");
                } else {
                    Log.w(TAG, "Audio Focus Failed (API 26+)");
                }
            } else {
                int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, 
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mIsAudioFocusHeld = true;
                    Log.d(TAG, "Audio Focus Granted (Legacy)");
                } else {
                    Log.w(TAG, "Audio Focus Failed (Legacy)");
                }
            }
        }
    }

    private void abandonAudioFocus() {
        if (mAudioManager != null && mIsAudioFocusHeld) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && mAudioFocusRequest != null) {
                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
                Log.d(TAG, "Audio Focus Abandoned (API 26+)");
            } else {
                mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
                Log.d(TAG, "Audio Focus Abandoned (Legacy)");
            }
            mIsAudioFocusHeld = false;
        }
    }

        /**
     * V7.2d: Secuencia de inicio de audio FM.
     * CORREGIDA basándose en el análisis del log de la app nativa (TunerManagerForExt/FmService).
     * 
     * Descubrimiento clave: NO debemos llamar RPC_SetChannel(2) directamente.
     * PID 4140 (framework) lo sobreescribe con SetChannel(4) inmediatamente.
     * 
     * La app nativa hace:
     * 1. Envía comando MCU 0xA0 sub=0x0A params=0x01,0x00 (onRadioArea: radioArea: 1)
     * 2. Pide AudioFocus (FmService.requestAudioFocus)
     * 3. PID 4140 RESPONDE con RPC_SetChannel(2) -> radio_type activado
     * 
     * Nuestro error anterior: llamar SetChannel(2) ANTES de AudioFocus,
     * causando que PID 4140 lo sobreescriba con SetChannel(4).
     */
    private void startFmAudioSequence() {
        Log.d(TAG, "=== INICIO SECUENCIA AUDIO FM V7.2d ===");

        // 1. Silenciar primero
        try {
            setMute(true);
            Log.d(TAG, "[1/7] setMute(true) OK");
        } catch (Exception e) {
            Log.w(TAG, "[1/7] setMute(true) failed", e);
        }

        // 2. CLAVE: Enviar comando "Radio Area" al MCU
        // V9.6: Usar QFTunerAdapter si disponible
        if (mQfAdapter != null) {
            mQfAdapter.setLoc(1); // mode=1 (Local)
            Log.d(TAG, "[2/9] LOC(1) via QFTunerAdapter");
        } else {
            sendCmd(SUB_TUNE_AREA, (byte) 0x01, (byte) 0x00);
            Log.d(TAG, "[2/9] Radio Area/LOC notification sent (sendCmd fallback)");
        }

        // 3. Pedir foco de audio ANTES de SetChannel
        // Esto es CRÍTICO: el framework (PID 4140) responde al AudioFocus
        // llamando RPC_SetChannel(2) automáticamente si la radio area está activa
        requestAudioFocus();
        Log.d(TAG, "[3/7] requestAudioFocus - PID 4140 debería responder con SetChannel(2)");

        // 4. Esperar un poco para que PID 4140 procese el AudioFocus
        // y llame SetChannel(2) por nosotros
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }

        // 5. Verificar si PID 4140 ya activó el canal FM
        // Si no, hacerlo nosotros como fallback
        if (mMcuManager != null && mSetChannel != null) {
            try {
                Method getChannel = mMcuManager.getClass().getMethod("RPC_GetChannel");
                byte currentChannel = (byte) getChannel.invoke(mMcuManager);
                Log.d(TAG, "[5/7] Canal actual: " + currentChannel);
                if (currentChannel != 2) {
                    Log.d(TAG, "[5/7] Canal NO es FM, forzando SetChannel(2)...");
                    mSetChannel.invoke(mMcuManager, (byte) 2);
                    Log.d(TAG, "[5/7] RPC_SetChannel(2) forzado OK");
                } else {
                    Log.d(TAG, "[5/7] Canal ya es FM (2), OK!");
                }
            } catch (Exception e) {
                // Fallback: forzar SetChannel(2) si falla GetChannel
                try {
                    mSetChannel.invoke(mMcuManager, (byte) 2);
                    Log.d(TAG, "[5/7] RPC_SetChannel(2) fallback OK");
                } catch (Exception e2) {
                    Log.e(TAG, "[5/7] SetChannel(2) FAILED", e2);
                }
            }
        }

        // 6. Parámetros de audio FM
        setAudioParams(true);
        Log.d(TAG, "[6/7] setAudioParams(on)");

        // 7. Desmutear
        try {
            setMute(false);
            Log.d(TAG, "[7/7] setMute(false) OK");
        } catch (Exception e) {
            Log.w(TAG, "[7/7] setMute(false) failed", e);
        }
        
        // OEM: el usuario quiere FM (aunque el sistema intente tumbar canal/mute)
        mUserWantsFmAudio = true;

        // 8. RDS - V7.2e: Re-habilitado vía comando maestro directo
        sendRdsCmd((byte) (mIsRdsEnabled ? 1 : 0));
        Log.d(TAG, "[8/9] RDS Master set to " + mIsRdsEnabled + " via cmd 0xA2");

        // 9. V7.2e: RESET FILTRO PTY (cmd 0x15)
        sendCmd(SUB_RDS_PTY, (byte) 0, (byte) 0);
        Log.d(TAG, "[9/9] Resetting PTY filter via direct cmd 0x15");
        
        // V11.6: setRdsTASwitch() ya NO se llama al inicio - lanzaba un TA SEEK scan
        // TA se maneja puramente en software con mIsTaEnabled
        mIsTaEnabled = false; // TA desactivado por defecto
        
        // 10. V9.9: ULTIMO RECURSO - BROADCOM DIRECT OVERRIDE
        // Si el MCU no emite los paquetes B5 y B7, forzamos al chip Broadcom a encender
        // sus features RDS de bajo nivel saltándonos al MCU mediante la API de sistema.
        enableBroadcomRdsFeatures();

        mIsRadioActive = true; // V9.9: Activar el flag para el Heartbeat
        Log.d(TAG, "=== FIN SECUENCIA AUDIO FM V7.2d ===");
    }
    
    /**
     * Intenta conectar con el servicio FmProxy y habilita a la fuerza 
     * todas las características del RDS (PTY, RT, etc.) usando reflection.
     */
    private void enableBroadcomRdsFeatures() {
        try {
            // Buscamos FmProxy de Broadcom 
            Class<?> fmProxyClass = Class.forName("com.broadcom.bt.app.fm.FmProxy");
            Method getProxyMethod = fmProxyClass.getMethod("getProxy", Context.class, Class.forName("com.broadcom.bt.app.fm.IFmProxyCallback"));
            
            Log.d(TAG, "[Broadcom] Encontrado FmProxy. Intentando activar features RDS...");
            
            // Instalar un proxy dinámico para el callback no es trivial sin la interfaz cargada en nuestra app
            // Por lo que trataremos de hacer bindService directo al IFmReceiverService si está disponible
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.broadcom.bt.app.fm", "com.broadcom.bt.app.fm.FmReceiverService"));
            boolean bound = mContext.bindService(intent, new android.content.ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    try {
                        Log.d(TAG, "[Broadcom] OK Conectado a FmReceiverService. Forzando RDS...");
                        // Obtener interfaz AIDL
                        Class<?> stubClass = Class.forName("com.broadcom.bt.app.fm.IFmReceiverService$Stub");
                        Method asInterface = stubClass.getMethod("asInterface", IBinder.class);
                        Object fmService = asInterface.invoke(null, service);
                        
                        // setRdsMode(int rdsMode, int rdsFeatures, int afMode, int afThreshold)
                        // Features Mask: PS(0x04) | PTY(0x08) | RT(0x01) | PTYN(0x20) | TP(0x02) = 0x2F
                        Method setRdsMode = fmService.getClass().getMethod("setRdsMode", int.class, int.class, int.class, int.class);
                        setRdsMode.invoke(fmService, 1, 0x2F, 0, 0); 
                        Log.d(TAG, "[Broadcom] ¡setRdsMode(1, 0x2F, 0, 0) EJECUTADO!");
                        
                    } catch (Exception e) {
                        Log.w(TAG, "[Broadcom] Error ejecutando invoke en FmReceiverService", e);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "[Broadcom] Desconectado de FmReceiverService");
                }
            }, Context.BIND_AUTO_CREATE);
            
            if (!bound) {
                Log.w(TAG, "[Broadcom] bindService falló. El servicio FmReceiverService no está accesible para la app.");
            }
            
        } catch (Exception e) {
            Log.w(TAG, "[Broadcom] Broadcom FmProxy / FmReceiverService no disponible o inaccesible vía Reflection.", e);
        }
    }

    private void stopFmAudioSequence() {
        Log.d(TAG, "=== FIN SECUENCIA AUDIO FM (Teardown V9.5) ===");
        mIsRadioActive = false; // V9.9: Limpiar flag activo inmediatamente
        mUserWantsFmAudio = false;
        mAudioRecoveryHandler.removeCallbacks(mAutoRecoveryRunnable);
        mAutoRecoveryAttempts = 0;
        try {
            // 1. Silenciar temporalmente para evitar transitorios
            setMute(true);
            setAudioParams(false); // V18.4: Apagar radio hardware
            
            // 2. Devolver canal a MPU (Media = 4)
            if (mSetChannel != null && mMcuManager != null) {
                mSetChannel.invoke(mMcuManager, (byte) 4);
                Log.d(TAG, "[Teardown] RPC_SetChannel(4) - Contexto devuelto a Android MPU");
            }
            
            // 3. V18.4: DESMUTEAR GLOBAL. Si no, YouTube/Spotify no sonarán tras cerrar la app.
            setMute(false);
            Log.d(TAG, "[Teardown] setMute(false) finalizado");
            
        } catch (Exception e) {
            Log.w(TAG, "Teardown error", e);
            try { setMute(false); } catch (Exception ignored) {}
        }
    }

    private void setAudioParams(boolean on) {
        if (mAudioManager != null) {
            String params = on ? "fm_radio_on=1;fm_mute=0" : "fm_radio_on=0;fm_mute=1";
            try {
                mAudioManager.setParameters(params);
                Log.d(TAG, "Set Audio Params: " + params);
            } catch (Exception e) {
                Log.e(TAG, "Error setting audio params", e);
            }
        }
    }

    private void setForceUse(boolean useSpeaker) {
        try {
            Class<?> audioSystemClass = Class.forName("android.media.AudioSystem");
            Method setForceUseMethod = audioSystemClass.getMethod("setForceUse", int.class, int.class);
            int usage = 1; // FOR_MEDIA
            int mode = useSpeaker ? 1 : 0; // FORCE_SPEAKER or FORCE_NONE
            setForceUseMethod.invoke(null, usage, mode);
            Log.d(TAG, "AudioSystem.setForceUse(" + usage + ", " + mode + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error calling AudioSystem.setForceUse", e);
        }
    }

    // ==========================================
    // RADIO CONTROL COMMANDS
    // ==========================================

    @Override
    public void onASEvent() throws RemoteException {
        // V7.2e: BLOQUEADO Auto Store nativo (0x08). 
        // No queremos que la MCU machaque sus presets internos.
        Log.d(TAG, "Native AutoStore (0x08) BLOCKED. Triggering Software AutoScan...");
        
        mIsScanning = true;
        // Evento 108 (2) -> Escaneo por Software iniciado
        fireEvent(108, "2"); 
        // Evento 116 -> Comando para que el Engine empiece la secuencia de Seek & Save
        fireEvent(116, "START_SOFT_AS");
    }

    @Override
    public void onPSEvent() throws RemoteException {
        // V7.2e: Comunicación directa con MCU (Independencia total del SDK OEM)
        // Stop Scan es el comando 0x0C (SUB_AUTO_SCAN_STOP)
        sendCmd(SUB_AUTO_SCAN_STOP, (byte) 0, (byte) 0);
        mIsScanning = false;
        Log.d(TAG, "Direct MCU -> Stop Scan requested (cmd 0xA0 0C)");
    }

    @Override
    public void onLocDxEvent() throws RemoteException {
        mIsDxLocal = !mIsDxLocal;
        int mode = mIsDxLocal ? 1 : 0;
        
        // V7.2e: Comunicación directa con MCU (Independencia total del SDK OEM)
        // Comando 0xA0 0x07 [mode] 0x00
        sendCmd(SUB_SWITCH_LOC, (byte) mode, (byte) 0x00);
        Log.d(TAG, "Direct MCU -> DX/Local set to: " + (mIsDxLocal ? "LOCAL" : "DX") + " (cmd 0xA0 07)");
        
        fireEvent(106, String.valueOf(mode));
    }

    @Override
    public void onSeekDownEvent() throws RemoteException {
        // V7.2e: Comunicación directa con MCU (Independencia total del SDK OEM)
        // Seek Down es el comando 0x02 (SUB_SEEK_DOWN)
        sendCmd(SUB_SEEK_DOWN, (byte) 0x02, (byte) 0); // param1=0x02 suele indicar dirección down en este MCU
        mIsSeeking = true;
        Log.d(TAG, "Direct MCU -> Seek Down initiated (cmd 0xA0 02)");
    }

    @Override
    public void onSeekUpEvent() throws RemoteException {
        // V7.2e: Comunicación directa con MCU (Independencia total del SDK OEM)
        // Seek Up es el comando 0x01 (SUB_SEEK_UP)
        sendCmd(SUB_SEEK_UP, (byte) 0x01, (byte) 0); // param1=0x01 suele indicar dirección up
        mIsSeeking = true;
        Log.d(TAG, "Direct MCU -> Seek Up initiated (cmd 0xA0 01)");
    }

    @Override
    public void onManualUpEvent() throws RemoteException {
        // V9.6: Fine = 0x03 (Up)
        sendCmd(SUB_FINE_UP, (byte) 0, (byte) 0);
    }

    @Override
    public void onManualDownEvent() throws RemoteException {
        // V9.6: Fine = 0x04 (Down)
        sendCmd(SUB_FINE_DOWN, (byte) 0, (byte) 0);
    }

    @Override
    public void onScanEvent() throws RemoteException {
        // V7.2e: BLOQUEADO Scan nativo. Redirigiendo a Software Scan.
        Log.d(TAG, "Native Scan BLOCKED. Using OpenRadioFM SoftScan...");
        
        mIsScanning = true;
        fireEvent(108, "2"); 
        fireEvent(116, "START_SOFT_SCAN");
    }

    @Override
    public void gotoFreq(int freq) throws RemoteException {
        // La freq de OpenRadioFM viene en formato x1000 (ej. 96900 = 96.9 MHz)
        int freqMcu = (freq >= 522 && freq <= 1710) ? freq : (freq / 10); 
        mCurrentFreq = freq;
        
        // V7.2e: Comunicación directa con MCU (Independencia total del SDK OEM)
        // Comando 0xA0 0x00 [hi] [lo]
        sendCmd(SUB_TUNE_FREQUENCY, (byte) ((freqMcu >> 8) & 0xFF), (byte) (freqMcu & 0xFF));
        Log.d(TAG, "Direct MCU -> Tune to " + freq + " Hz (MCU binary: " + freqMcu + ")");
        
        // V13.1: Feedback inmediato a la UI al deslizar el dial
        notifyFreqUpdate();
    }

    @Override
    public void gotoFreq2(String freq) throws RemoteException {
        try {
            float f = Float.parseFloat(freq);
            // f = 92.2 → gotoFreq espera ×1000 → 92200
            gotoFreq((int)(f * 1000));
        } catch (NumberFormatException e) {
            Log.e(TAG, "gotoFreq2: invalid freq string: " + freq);
        }
    }

    @Override
    public void gotoFreqIndex(int index) throws RemoteException {
        // V7.2e: DESACTIVADO el comando MCU 0x0D para independizarnos de su memoria interna.
        // Ahora OpenRadioFM gestiona sus propios favoritos por base de datos.
        // La UI sintonizará por frecuencia directa usando gotoFreq(int freq).
        Log.d(TAG, "Software Preset Redirect: Index " + index + " requested (MCU 0x0D ignored)");
        
        // Opcional: Notificar a la UI que use su propia memoria si el index viene de un mando al volante
        fireEvent(115, String.valueOf(index)); // Código 115: 'Cargar favorito de la app por índice'
    }

    public void onNextFavoriteEvent() throws RemoteException {
        // V13.5: Comando nativo para siguiente favorito (0x0E)
        sendCmd(SUB_TUNE_NEXT, (byte) 0, (byte) 0);
        Log.d(TAG, "Next Favorite (0x0E) command sent");
        
        // V13.1: Feedback UI
        notifyFreqUpdate();
    }

    public void onPreFavoriteEvent() throws RemoteException {
        // V13.5: Comando nativo para favorito anterior (0x0F)
        sendCmd(SUB_TUNE_PREV, (byte) 0, (byte) 0);
        Log.d(TAG, "Previous Favorite (0x0F) command sent");
        
        // V13.1: Feedback UI
        notifyFreqUpdate();
    }

    @Override
    public int getCurrentBand() throws RemoteException {
        return mCurrentBand;
    }

    @Override
    public int getCurrentFreq() throws RemoteException {
        // V7.2: La frecuencia se actualiza vía callback MCU (handlePresetList).
        // RPC_GetChannel retorna el CANAL DE AUDIO (2=FM, 4=Android), NO la frecuencia.
        // mCurrentFreq ya está en formato OpenRadioFM (×1000) gracias a updateFrequency().
        
        // V9.9: Aprovechamos este polling (1 vez por seg) para vigilar que el coche no nos haya robado el canal.
        // Durante streaming online el canal deseado es 4 (Android); no forzar recuperación a FM aquí.
        if (mIsRadioActive && !mIsOnlineStreamingActive) {
            checkAndRecoverAudio();
        }

        return mCurrentFreq; 
    }

    @Override
    public boolean IsAS() throws RemoteException { return false; }

    @Override
    public boolean IsPS() throws RemoteException { return false; }

    @Override
    public boolean IsScan() throws RemoteException { return mIsScanning; }

    @Override
    public boolean IsSeek() throws RemoteException { return mIsSeeking; }

    @Override
    public boolean IsStereo() throws RemoteException { return mIsStereo; }

    @Override
    public boolean IsDxLocal() throws RemoteException { 
        return mIsDxLocal; // V7.1: Retorna estado real en vez de siempre false
    }

    @Override
    public boolean requestPlayAudio() throws RemoteException {
        // V17.0: Limpiar estados de interrupcion al forzar play
        mIsInCall = false;
        mIsTransientFocusLoss = false;
        
        requestAudioFocus();
        mIsRadioActive = true;
        Log.d(TAG, "requestPlayAudio: focus=" + mIsAudioFocusHeld + " radioActive=true");
        // Forzar canal 2 por si acaso
        enforceAudioChannelRecovery();
        return mIsAudioFocusHeld;
    }

    public void setMute(boolean mute) throws RemoteException {
        // V7.2e: Independencia total: Usamos únicamente el método RPC_SetVolumeMute del mcu_service
        // para asegurar el silencio a nivel de amplificador de hardware.
        if (mSetMute == null) {
            Log.w(TAG, "setMute: mSetMute is null (McuService NOT ready)");
            return;
        }
        try {
            if (mSetMute.getParameterTypes()[0] == int.class) {
                mSetMute.invoke(mMcuManager, mute ? 1 : 0);
            } else {
                mSetMute.invoke(mMcuManager, mute);
            }
            Log.d(TAG, "Direct MCU -> Volume Mute set to: " + mute);
        } catch (Exception e) {
            Log.e(TAG, "Error setting mute in McuService", e);
        }
    }
    
    /**
     * Sincroniza el estado interno sin enviar comandos al hardware.
     */
    public void syncState(int freq, int band) {
        this.mCurrentFreq = freq;
        this.mCurrentBand = band;
        Log.d(TAG, "Estado sincronizado manual: " + freq + " / B" + band);
    }

    /**
     * Call the lowest-level Broadcom driver to configure Alternative Frequencies / Traffic Announcement 
     * WITHOUT triggering a scan/seek from the QFTuner API.
     */
    public void enableSilentlyRdsFeatures(boolean afOn, boolean taOn) {
        if (mFmReceiverService == null || mBroadcomSetRdsMode == null) {
            Log.w(TAG, "enableSilentlyRdsFeatures FAILED: Broadcom FmReceiverService not initialized. Try native method.");
            // Fallback (might trigger scan combo, user beware)
            if (afOn) sendCmd((byte) 0x10, (byte) 0x01, (byte) 0x00);
            if (taOn) sendCmd((byte) 0x12, (byte) 0x01, (byte) 0x00);
            return;
        }

        try {
            int rdsMode = 1;      // RDS always listening
            int rdsFeatures = taOn ? 1 : 0; // We guess TA is matched to bitmask in Broadcom? Or just set to 0. Actually, TS = Traffic Service might be part of rdsFeatures. Let's use standard mode.
            int afMode = afOn ? 1 : 0;
            int afThreshold = 10; // Default RSSI threshold for AF jumping
            
            // setRdsMode(int rdsMode, int rdsFeatures, int afMode, int afThreshold)
            mBroadcomSetRdsMode.invoke(mFmReceiverService, rdsMode, rdsFeatures, afMode, afThreshold);
            Log.d(TAG, "Broadcom setRdsMode invoked: AF=" + afOn + ", TA=" + taOn + " (Silently!)");
        } catch (Exception e) {
            Log.e(TAG, "Error invoking Broadcom setRdsMode.", e);
        }
    }

    @Override
    public void toggleRdsFeature(int type) {
        try {
            switch (type) {
                case 0: // RDS Switch (Global)
                    mIsRdsEnabled = !mIsRdsEnabled;
                    // V7.2e: Comando maestro 0xA2. 0x01 = ON, 0x00 = OFF
                    sendRdsCmd((byte) (mIsRdsEnabled ? 1 : 0));
                    Log.d(TAG, "Direct MCU -> RDS Global Switch set to: " + mIsRdsEnabled + " (cmd 0xA2)");
                    
                    // Sincronización Broadcom silente opcional
                    if (mBroadcomSetRdsMode != null && mFmReceiverService != null) {
                        mBroadcomSetRdsMode.invoke(mFmReceiverService, mIsRdsEnabled ? 1 : 0, 0x0F, mIsAfEnabled ? 1 : 0, 10);
                    }
                    break;
                case 1: // AF Switch
                    boolean nextAfState = !mIsAfEnabled;
                    mIsAfEnabled = nextAfState; 
                    
                    // V7.2e: Comando directo 0xA0 0x11 [state] 0x00
                    sendCmd(SUB_RDS_AF, (byte) (nextAfState ? 1 : 0), (byte) 0);
                    Log.d(TAG, "Direct MCU -> RDS AF set to: " + nextAfState + " (cmd 0xA0 11)");

                    // Notificar a la UI
                    fireEvent(111, "AF:" + (nextAfState ? "1" : "0"));

                    // Sincronizar con Broadcom silente
                    if (mBroadcomSetRdsMode != null && mFmReceiverService != null) {
                        enableSilentlyRdsFeatures(nextAfState, mIsTaEnabled);
                    }
                    break;
                case 2: // TA Switch
                    mIsTaEnabled = !mIsTaEnabled;
                    
                    // V7.2e: Comando directo 0xA0 0x12 [state] 0x00
                    sendCmd(SUB_RDS_TA, (byte) (mIsTaEnabled ? 1 : 0), (byte) 0);
                    Log.d(TAG, "Direct MCU -> RDS TA set to: " + mIsTaEnabled + " (cmd 0xA0 12)");

                    // Notificar a la UI
                    fireEvent(112, "TA_SW:" + (mIsTaEnabled ? "1" : "0"));
                    break;
                case 3: // DX/Local Toggle
                    mIsDxLocal = !mIsDxLocal;
                    byte nextLocMode = mIsDxLocal ? (byte) 1 : (byte) 0;
                    
                    // V7.2e: Comando directo 0xA0 0x07
                    sendCmd(SUB_SWITCH_LOC, nextLocMode, (byte) 0);
                    Log.d(TAG, "Direct MCU -> DX/Local set to: " + (mIsDxLocal ? "LOCAL" : "DX") + " (cmd 0xA0 07)");
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling RDS feature " + type, e);
        }
    }

    /**
     * V9.5: Getter público para estado de scanning (AutoStore o Scan).
     * V18.5: Excluimos deliberadamente el estado de Seek para que los textos de favoritos
     * no se conviertan en frecuencia durante un simple seek.
     */
    public boolean isScanning() {
        return mIsScanning;
    }

    /**
     * V9.5: Envía comando RDS (prefijo 0xA2) al MCU.
     * Usado para setRdsPtyType y otros controles RDS.
     */
    private void sendRdsCmd(byte ptyType) {
        if (mMcuManager == null || mSendMcuMsgData == null) {
            Log.e(TAG, "sendRdsCmd FAILED: MCU no disponible");
            return;
        }
        try {
            byte[] payload = new byte[] { ptyType };
            mSendMcuMsgData.invoke(mMcuManager, (byte) 0xA2, payload, 1);
            Log.d(TAG, "RDS CMD Enviado: 0xA2 ptyType=" + (ptyType & 0xFF));
        } catch (Exception e) {
            Log.e(TAG, "Error sending RDS PTY cmd", e);
        }
    }


    public void enforceAudioRecovery() {
        enforceAudioChannelRecovery();
    }

    // V9.9: Hack for Bluetooth recovery. The system's MediaFocusControl "steals" the audio channel
    // but never gives it back to us via normal OnAudioFocusChange because it uses an OEM "abandonCustomAudioFocus"
    public void enforceAudioChannelRecovery() {
        if (mIsOnlineStreamingActive) {
            Log.d(TAG, "enforceAudioChannelRecovery: omitido (streaming online activo)");
            return;
        }
        Log.d(TAG, "enforceAudioChannelRecovery: Forzando SetChannel(2) tras desconexión BT");
        try {
            // Repetimos la secuencia vital para asegurar el audio FM
            requestAudioFocus(); // Asegurarnos de tener el foco estándar Android
            
            if (mSetChannel != null && mMcuManager != null) {
                mSetChannel.invoke(mMcuManager, (byte) 2);
                Log.d(TAG, "enforceAudioChannelRecovery: mSetChannel(2) enviado");
            }
            
            // Refrescar el Mute al estado actual (si estábamos desmuteados, que suene)
            setMute(false);
            setAudioParams(true); // V18.1: Restaurar flag de FM activo en el mixer de Android
        } catch (Exception e) {
            Log.e(TAG, "enforceAudioChannelRecovery FAILED", e);
        }
    }

    // V9.9: Helper to gracefully surrender the audio channel to the system (Media = 4)
    public void returnAudioChannel() {
        mIsAudioFocusHeld = false; // V17.2: Previene que checkAndRecoverAudio() robe el canal 4 de Android
        abandonAudioFocus();       // V17.2: Soltamos el control de Android explícitamente para el MediaPlayer
        setAudioParams(false);     // V18.1: Avisar al OS de que FM ya no suena (libera el mixer)
        try {
            if (mSetChannel != null && mMcuManager != null) {
                mSetChannel.invoke(mMcuManager, (byte) 4); // V13.5: RPC_SetChannel(4) - Restore to Android
                Log.d(TAG, "returnAudioChannel: Radio Channel 4 (MPU) restore requested");
        
                setMute(false);
                
                // V18.3: Segundo desmuteo retardado para ganar cualquier carrera contra el sistema
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (mIsOnlineStreamingActive) {
                        try {
                            setMute(false);
                            setAudioParams(false);
                            Log.d(TAG, "returnAudioChannel: Desmuteo de seguridad REFORZADO ejecutado");
                        } catch (Exception e) {
                            Log.e(TAG, "Error in delayed un-mute", e);
                        }
                    }
                }, 500);
            }
        } catch (Exception e) {
            Log.e(TAG, "returnAudioChannel FAILED", e);
        }
    }

    // V9.9: Heartbeat agresivo para evitar que MediaFocusControl o la Marcha Atrás nos roben el audio
    private void checkAndRecoverAudio() {
        if (mGetChannel == null || mSetChannel == null || mMcuManager == null) return;
        
        try {
            // Le preguntamos a la placa base en qué canal de audio está ahora mismo
            byte currentChannel = (byte) mGetChannel.invoke(mMcuManager);
            
            // 2 = FM Radio, 4 = Android Media, 6 = Bluetooth, etc.
            if (currentChannel != 2) {
                // EXCEPCIÓN 1: Si acabamos de ceder el foco conscientemente, no peleamos
                if (!mIsAudioFocusHeld) return;
                
                // EXCEPCIÓN 1.1: Si el streaming online está activo, el canal deseado es 4 (Android), no el 2 (Radio)
                if (mIsOnlineStreamingActive) return;
                
                // EXCEPCIÓN 2: Si estamos en LLAMADA REAL, no peleamos
                if (mIsInCall) return;
                
                // V17.0: Si es pérdida transitoria (Spotify), permitimos recuperación 
                // si la música ya no suena o si el usuario ha interactuado
                if (mIsTransientFocusLoss) {
                    // Si la música de Android sigue sonando fuera de nuestra app, no le robamos el audio aún
                    if (mAudioManager.isMusicActive()) return;
                    
                    Log.d(TAG, "HEARTBEAT: Recuperando audio FM tras pausa de música transitoria.");
                    mIsTransientFocusLoss = false;
                }

                Log.w(TAG, "HEARTBEAT WARNING: Canal de audio secuestrado (Canal actual: " + currentChannel + "). Forzando recuperación a 2 (Radio).");
                mSetChannel.invoke(mMcuManager, (byte) 2);
                try {
                    setMute(false); // Asegurarnos de desmutear
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            Log.e(TAG, "checkAndRecoverAudio FAILED", e);
        }
    }

    public void setOnlineStreamingActive(boolean active) {
        this.mIsOnlineStreamingActive = active;
        Log.d(TAG, "setOnlineStreamingActive: " + active);
        if (active) {
            // V18.3: Desmutear inmediatamente si el streaming empieza,
            // por si AudioFocusChange disparó un mute justo antes.
            try {
                setMute(false);
                setAudioParams(false);
            } catch (Exception e) {
                Log.e(TAG, "Error setting mute in setOnlineStreamingActive", e);
            }
        }
    }

    public void switchToFmAudio() {
        Log.d(TAG, "switchToFmAudio requested");
        enforceAudioChannelRecovery();
    }

    public void switchToAndroidAudio() {
        Log.d(TAG, "switchToAndroidAudio requested");
        returnAudioChannel();
    }

    public boolean isOnlineStreamingActive() {
        return mIsOnlineStreamingActive;
    }
}
