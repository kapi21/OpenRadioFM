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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioFocusRequest;
import android.media.AudioAttributes;

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
    // sendCmd (MCU directo) solo funciona fiable para tune/band/fine.
    // Para seek, scan y RDS la app nativa usa QFTunerManager.
    private Object mTunerManager; // QFTunerManager singleton
    private Method mTunerOnSeek;   // onSeek(boolean up)
    private Method mTunerAutoScan;  // autoScan()
    private Method mTunerStopScan;  // stopScan()
    private Method mTunerOnBand;    // onBand(int band)
    private Method mTunerOnLoc;     // onLoc(int mode)
    private Method mTunerSetRdsSwitch;   // setRdsSwitch(int state)
    private Method mTunerSetRdsAF;       // setRdsAFSwitch()
    private Method mTunerSetRdsTA;       // setRdsTASwitch()
    private Method mTunerSetRdsPtyType;  // setRdsPtyType(int type)

    private IRadioCallBack mCallback;
    private int mCurrentFreq = 8750; // Cache (en unidades x10, ej: 8750 = 87.50 MHz)
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
    private byte[] mLastSignalData = null; // V15.7: Último paquete 0x41 (Telemetría RSSI/SNR)
    private AudioManager mAudioManager;
    private OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioFocusRequest mAudioFocusRequest;
    private boolean mIsAudioFocusHeld = false;
    private boolean mIsRadioActive = false;
    private boolean mIsInCall = false; // V11.5: Flag para estado de llamada telefónica
    
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
                        // Alguien más (ej. otra app, Bluetooth largo) tomó el foco permanentemente.
                        // Debemos silenciarnos y soltar el foco.
                        try {
                            setMute(true);
                            if (mSetChannel != null && mMcuManager != null) {
                                mSetChannel.invoke(mMcuManager, (byte) 4); // Devolver contexto
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error on AUDIOFOCUS_LOSS", e);
                        }
                        abandonAudioFocus();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.d(TAG, "--->>onAudioFocusChange()  ----AUDIOFOCUS_LOSS_TRANSIENT (" + focusChange + ")----");
                        // Interrupción corta (Llamada, Notificación, Android Auto, GPS).
                        mIsAudioFocusHeld = true; // Retenemos para poder recuperar
                        mIsInCall = true; // V11.5: Marcar como en llamada para que heartbeat no pelee
                        try {
                            setMute(true);
                            // V11.5: Soltar canal MCU para que BT/teléfono suene limpio
                            if (mSetChannel != null && mMcuManager != null) {
                                mSetChannel.invoke(mMcuManager, (byte) 4);
                                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT: RPC_SetChannel(4) - canal FM liberado");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error muting on AUDIOFOCUS_LOSS_TRANSIENT", e);
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.d(TAG, "--->>onAudioFocusChange()  ----AUDIOFOCUS_GAIN----");
                        // Recuperamos el foco (terminó la llamada/notificación o se desconectó BT).
                        // Volvemos a levantar nuestro canal de radio.
                        mIsAudioFocusHeld = true;
                        mIsInCall = false; // V11.5: Ya no estamos en llamada
                        
                        // Si estábamos en modo activo, forzamos recuperar
                        if (mIsRadioActive) {
                            try {
                                if (mMcuManager != null && mSetChannel != null) {
                                    mSetChannel.invoke(mMcuManager, (byte) 2);
                                    Log.d(TAG, "AUDIOFOCUS_GAIN: Forced RPC_SetChannel(2)");
                                }
                                setAudioParams(true);
                                setMute(false);
                                Log.d(TAG, "AUDIOFOCUS_GAIN: Radio unmuted and params restored");
                            } catch (Exception e) {
                                Log.e(TAG, "Error restoring audio on AUDIOFOCUS_GAIN", e);
                            }
                        }
                        break;
                }
            }
        };
        initMcuConnection();
        
        // V11.5: Detectar llamadas telefónicas para silenciar la radio
        // AudioFocus no funciona en K706 porque el MCU enruta FM directo.
        // PhoneStateListener detecta llamadas a nivel de SO.
        try {
            android.telephony.TelephonyManager tm = 
                (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                tm.listen(new android.telephony.PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String phoneNumber) {
                        switch (state) {
                            case android.telephony.TelephonyManager.CALL_STATE_RINGING:
                            case android.telephony.TelephonyManager.CALL_STATE_OFFHOOK:
                                if (!mIsInCall) {
                                    mIsInCall = true;
                                    Log.d(TAG, "📞 Llamada detectada - silenciando radio FM");
                                    try {
                                        setMute(true);
                                        // Soltar el canal de audio MCU para que el BT/teléfono suene
                                        if (mSetChannel != null && mMcuManager != null) {
                                            mSetChannel.invoke(mMcuManager, (byte) 4);
                                            Log.d(TAG, "📞 RPC_SetChannel(4) - canal FM liberado");
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error silenciando radio en llamada", e);
                                    }
                                }
                                break;
                            case android.telephony.TelephonyManager.CALL_STATE_IDLE:
                                if (mIsInCall) {
                                    mIsInCall = false;
                                    Log.d(TAG, "📞 Llamada finalizada - restaurando radio FM");
                                    // Esperar un momento para que el sistema asimile
                                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                        try {
                                            if (mSetChannel != null && mMcuManager != null) {
                                                mSetChannel.invoke(mMcuManager, (byte) 2);
                                                Log.d(TAG, "📞 RPC_SetChannel(2) - canal FM recuperado");
                                            }
                                            setAudioParams(true);
                                            setMute(false);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error restaurando radio tras llamada", e);
                                        }
                                    }, 800);
                                }
                                break;
                        }
                    }
                }, android.telephony.PhoneStateListener.LISTEN_CALL_STATE);
                Log.d(TAG, "V11.5: PhoneStateListener registrado OK");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registrando PhoneStateListener", e);
        }
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
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.qf.mcu.IMcuManager");
                data.writeString(mContext.getPackageName());
                binder.transact(1001, data, reply, 0);
                reply.readException();
                Log.d(TAG, "Permissions requested successfully (Transact 1001)");
            } catch (Exception e) {
                Log.e(TAG, "Error requesting permissions", e);
            } finally {
                data.recycle();
                reply.recycle();
            }

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
                case 0xB3: // RDS AF/TA Status Flags (Interruptores)
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
                case 0x41: // 🔬 RESEARCH: Posible telemetría de señal (RSSI/SNR)
                    mLastSignalData = data.clone(); // V15.7: Guardar para Menú Ingeniería
                    StringBuilder sb41 = new StringBuilder();
                    for (int i = 1; i < data.length; i++) {
                        sb41.append(String.format("[%d]=%02X ", i, data[i]));
                    }
                    Log.d(TAG, "🔬 [RESEARCH] MCU[0x41] SIGNAL DATA: " + sb41.toString());
                    break;
                case 0x29: // V13.1: Heartbeat silencioso del MCU (29 6D)
                    // Ignoramos para evitar que se interprete como frecuencia (0x296D hex = 10605 decimal -> 106.10 MHz)
                    return; 
                default:
                    // Log unknown packets for research
                    if (packetType >= 0xB0 && packetType <= 0xBF) {
                        Log.d(TAG, "🔬 [RESEARCH] NEW RDS/TUNER PACKET FOUND: 0x" + String.format("%02X", packetType) + " -> " + bytesToHex(data));
                    } else {
                        Log.d(TAG, "Unknown MCU packet type: 0x" + String.format("%02X", packetType));
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing MCU data", e);
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
        
        // V12.4: Incluir seekFlag en el estado de escaneo para que la UI sepa cuando se detiene un Seek Up/Down
        boolean newScanState = asFlag || scanFlag || flagsSeek;
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
            String rtRaw = new String(data, 1, data.length - 1, "UTF-8");
            Log.d(TAG, "RDS RT Try(1): '" + rtRaw + "'");
            
            // Usualmente RT empieza en el offset 1 o offset 2 en los sistemas chinos (0xB7 0x01 [Texto])
            int startOffset = 1;
            
            // Check if byte 1 is just a length or status, and text starts at 2
            if (data.length > 2 && data[1] < 10) {
                startOffset = 2; // skip status/length byte
            }
            
            int len = data.length - startOffset;
            if (len > 0) {
                String rtText = new String(data, startOffset, len, "UTF-8").trim();
                rtText = rtText.replaceAll("[^\\x20-\\x7E]", "");
                
                if (!rtText.isEmpty() && !rtText.equals("               ")) {
                    Log.d(TAG, "RDS RT Extracted: '" + rtText + "'");
                    fireEvent(104, rtText); // RT Text = 104
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RDS RT", e);
        }
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
        try {
            Class<?> clazz = Class.forName("com.qf.clientsdk.QFTunerManager");
            Method getInstance = clazz.getMethod("getInstance");
            mTunerManager = getInstance.invoke(null);
            Log.d(TAG, "QFTunerManager: CONNECTED (" + mTunerManager + ")");

            // Seek: onSeek(boolean up) — true=up, false=down
            try {
                mTunerOnSeek = clazz.getMethod("onSeek", boolean.class);
                Log.d(TAG, "  → onSeek(boolean): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → onSeek not found, trying alternatives");
                // Fallback: quizás use int (1=up, 0=down)
                try {
                    mTunerOnSeek = clazz.getMethod("onSeek", int.class);
                    Log.d(TAG, "  → onSeek(int): OK (fallback)");
                } catch (NoSuchMethodException e2) {
                    Log.w(TAG, "  → onSeek NOT AVAILABLE");
                }
            }

            // AutoScan & Stop
            try {
                mTunerAutoScan = clazz.getMethod("autoScan");
                Log.d(TAG, "  → autoScan(): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → autoScan NOT AVAILABLE");
            }

            try {
                mTunerStopScan = clazz.getMethod("stopScan");
                Log.d(TAG, "  → stopScan(): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → stopScan NOT AVAILABLE");
            }

            // Band
            try {
                mTunerOnBand = clazz.getMethod("onBand", int.class);
                Log.d(TAG, "  → onBand(int): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → onBand NOT AVAILABLE");
            }

            // LOC mode
            try {
                mTunerOnLoc = clazz.getMethod("onLoc", int.class);
                Log.d(TAG, "  → onLoc(int): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → onLoc NOT AVAILABLE");
            }

            // RDS Switch
            try {
                mTunerSetRdsSwitch = clazz.getMethod("setRdsSwitch", int.class);
                Log.d(TAG, "  → setRdsSwitch(int): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → setRdsSwitch NOT AVAILABLE");
            }

            // RDS AF Switch
            try {
                mTunerSetRdsAF = clazz.getMethod("setRdsAFSwitch");
                Log.d(TAG, "  → setRdsAFSwitch(): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → setRdsAFSwitch NOT AVAILABLE");
            }

            // RDS TA Switch
            try {
                mTunerSetRdsTA = clazz.getMethod("setRdsTASwitch");
                Log.d(TAG, "  → setRdsTASwitch(): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → setRdsTASwitch NOT AVAILABLE");
            }

            // RDS PTY Type
            try {
                mTunerSetRdsPtyType = clazz.getMethod("setRdsPtyType", int.class);
                Log.d(TAG, "  → setRdsPtyType(int): OK");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "  → setRdsPtyType NOT AVAILABLE");
            }

            // Log TODOS los métodos del TunerManager para exploración
            Log.d(TAG, "=== Métodos QFTunerManager ===");
            for (Method m : clazz.getMethods()) {
                if (!m.getDeclaringClass().equals(Object.class)) {
                    StringBuilder params = new StringBuilder();
                    for (Class<?> p : m.getParameterTypes()) {
                        if (params.length() > 0) params.append(", ");
                        params.append(p.getSimpleName());
                    }
                    Log.d(TAG, "  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + params + ")");
                }
            }

            // V7.2c: Registrar ITunerTool para capturar el código PI
            try {
                Class<?> itunerToolClass = Class.forName("com.qf.clientsdk.ITunerTool");
                Method setTunerTool = clazz.getMethod("setTunerTool", itunerToolClass);
                
                Object proxyTunerTool = Proxy.newProxyInstance(
                    itunerToolClass.getClassLoader(),
                    new Class<?>[] { itunerToolClass },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            StringBuilder argStr = new StringBuilder();
                            if (args != null) {
                                for (Object arg : args) {
                                    argStr.append(arg != null ? arg.toString() : "null").append(" ");
                                }
                            }
                            Log.d(TAG, "🔬 [RESEARCH] ITunerTool Call: " + method.getName() + "(" + argStr.toString().trim() + ")");

                            if (method.getName().equals("onCurrentFrequencyPICodeChange")) {
                                if (args != null && args.length > 0) {
                                    int pi = (Integer) args[0];
                                    Log.d(TAG, "PI Code Detected via ITunerTool: 0x" + Integer.toHexString(pi).toUpperCase());
                                    // Notificamos como evento 107 (PI Code)
                                    fireEvent(107, String.valueOf(pi));
                                }
                            }
                            return null;
                        }
                    });
                
                setTunerTool.invoke(mTunerManager, proxyTunerTool);
                Log.d(TAG, "  → ITunerTool Listener (PI Capture): REGISTERED");
            } catch (Exception e) {
                Log.w(TAG, "  → setTunerTool NOT AVAILABLE or Proxy error: " + e.getMessage());
            }

        } catch (ClassNotFoundException e) {
            Log.w(TAG, "QFTunerManager class not found - sendCmd MCU directo será el único canal");
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to QFTunerManager", e);
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
        // V9.6 (Fix AM band): El comando real para cambiar la banda en MCU es 0x06 (SWITCH_BAND)
        sendCmd(SUB_SWITCH_BAND, (byte) mCurrentBand, (byte) 0);
        Log.d(TAG, "Band -> " + mCurrentBand + " (sent [0xA0 06 " + String.format("%02X", mCurrentBand) + " 00])");
        
        // V13.1: Notificar INMEDIATAMENTE el cambio de banda para que la UI responda rápido
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
        // V9.6: Usar QFTunerManager.onLoc() si disponible
        if (mTunerOnLoc != null && mTunerManager != null) {
            try {
                mTunerOnLoc.invoke(mTunerManager, 1); // mode=1 (Local)
                Log.d(TAG, "[2/9] LOC(1) via QFTunerManager.onLoc()");
            } catch (Exception e) {
                sendCmd(SUB_TUNE_AREA, (byte) 0x01, (byte) 0x00);
                Log.d(TAG, "[2/9] LOC(1) fallback sendCmd");
            }
        } else {
            sendCmd(SUB_TUNE_AREA, (byte) 0x01, (byte) 0x00);
            Log.d(TAG, "[2/9] Radio Area/LOC notification sent (sendCmd)");
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

        // 8. RDS - V9.8: Re-habilitado de forma segura vía QFTunerManager
        if (mTunerSetRdsSwitch != null && mTunerManager != null) {
            try {
                mTunerSetRdsSwitch.invoke(mTunerManager, 1);
                Log.d(TAG, "[8/9] RDS ON via QFTunerManager.setRdsSwitch(1)");
            } catch (Exception e) {
                // Si falla el alto nivel, no enviamos el comando de bajo nivel (0x15) porque causa autoscan/cuelgue
                Log.w(TAG, "[8/9] Falló setRdsSwitch de QFTunerManager", e);
            }
        }

        // 9. V9.8: RESET FILTRO PTY - Vía alto nivel
        if (mTunerSetRdsPtyType != null && mTunerManager != null) {
            try {
                mTunerSetRdsPtyType.invoke(mTunerManager, 0); // 0 suele significar "sin filtro / todos"
                Log.d(TAG, "[9/9] Resetting PTY filter via QFTunerManager.setRdsPtyType(0)");
            } catch (Exception e) {
                Log.w(TAG, "[9/9] Falló setRdsPtyType de QFTunerManager", e);
            }
        }
        
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
        mIsRadioActive = false; // V9.9: Limpiar flag activo
        try {
            // 1. Silenciar (Mute true)
            setMute(true);
            Log.d(TAG, "[1/3] setMute(true)");
        } catch (Exception e) {}

        try {
            // 2. Devolver canal a MPU (Media = 4)
            if (mSetChannel != null && mMcuManager != null) {
                mSetChannel.invoke(mMcuManager, (byte) 4);
                Log.d(TAG, "[2/2] RPC_SetChannel(4) - Contexto devuelto a Android MPU");
            }
        } catch (Exception e) {
            Log.w(TAG, "[2/2] Failed to set channel 4", e);
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
        // V9.6: Auto Store via QFTunerManager
        if (mTunerAutoScan != null && mTunerManager != null) {
            try {
                mTunerAutoScan.invoke(mTunerManager);
                mIsScanning = true;
                Log.d(TAG, "AutoStore via QFTunerManager.autoScan()");
                return;
            } catch (Exception e) {
                Log.w(TAG, "QFTunerManager.autoScan fallback", e);
            }
        }
        sendCmd(SUB_TUNE_AS, (byte) 0, (byte) 0);
        mIsScanning = true;
        Log.d(TAG, "AutoStore fallback sendCmd (0x08)");
    }

    @Override
    public void onPSEvent() throws RemoteException {
        // V9.6: Stop scan via QFTunerManager
        if (mTunerStopScan != null && mTunerManager != null) {
            try {
                mTunerStopScan.invoke(mTunerManager);
                mIsScanning = false;
                Log.d(TAG, "StopScan via QFTunerManager.stopScan()");
                return;
            } catch (Exception e) {
                Log.w(TAG, "QFTunerManager.stopScan fallback", e);
            }
        }
        sendCmd(SUB_AUTO_SCAN_STOP, (byte) 0, (byte) 0);
        mIsScanning = false;
        Log.d(TAG, "StopScan fallback sendCmd (0x0C)");
    }

    @Override
    public void onLocDxEvent() throws RemoteException {
        // V9.6: Usar QFTunerManager.onLoc() para LOC/DX
        mIsDxLocal = !mIsDxLocal;
        int mode = mIsDxLocal ? 1 : 0;
        
        // V9.6: LOC/DX es el comando 0x07 (SWITCH_LOC) según TunerCmdFactory
        sendCmd(SUB_SWITCH_LOC, (byte) (mIsDxLocal ? 1 : 0), (byte) 0x00);
        Log.d(TAG, "DX/Local toggle -> " + (mIsDxLocal ? "LOCAL" : "DX") + " (sent [0xA0 07 " + (mIsDxLocal ? "01" : "00") + " 00])");
        fireEvent(106, String.valueOf(mIsDxLocal ? 1 : 0));
    }

    @Override
    public void onSeekDownEvent() throws RemoteException {
        // V9.6: Seek down real
        sendCmd(SUB_SEEK_DOWN, (byte) 0x02, (byte) 0);
        mIsSeeking = true;
        Log.d(TAG, "Seek Down (0x02) command sent");
    }

    @Override
    public void onSeekUpEvent() throws RemoteException {
        // V9.6: Seek up real (true=0x01/0x02 según param)
        sendCmd(SUB_SEEK_UP, (byte) 0x01, (byte) 0);
        mIsSeeking = true;
        Log.d(TAG, "Seek Up (0x01) command sent");
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
        // V9.6: Usar QFTunerManager
        if (mTunerAutoScan != null && mTunerManager != null) {
            try {
                mTunerAutoScan.invoke(mTunerManager);
                mIsScanning = true;
                Log.d(TAG, "AutoScan via QFTunerManager.autoScan()");
                return;
            } catch (Exception e) {
                Log.w(TAG, "QFTunerManager.autoScan fallback to sendCmd", e);
            }
        }
        sendCmd(SUB_TUNE_AS, (byte) 0, (byte) 0);
        mIsScanning = true;
        Log.d(TAG, "AutoScan fallback sendCmd (0x08)");
    }

    @Override
    public void gotoFreq(int freq) throws RemoteException {
        // La freq de OpenRadioFM viene en formato x1000 (ej. 96900 = 96.9 MHz)
        int freqMcu = (freq >= 522 && freq <= 1710) ? freq : (freq / 10); 
        mCurrentFreq = freq;
        sendCmd(SUB_TUNE_FREQUENCY, (byte) ((freqMcu >> 8) & 0xFF), (byte) (freqMcu & 0xFF));
        Log.d(TAG, "Tune -> " + freq + " (MCU: " + freqMcu + ")");
        
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
        // El index (0-5) corresponde al preset. Extraído: SUB_PRESET_SELECT = 0x0D
        sendCmd(SUB_PRESET_SELECT, (byte) index, (byte) 0);
        Log.d(TAG, "Select Preset Index -> " + index);
        
        // V13.1: Forzamos refresco del estado para que la UI detecte la nueva frecuencia y banda
        notifyFreqUpdate();
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
        
        // V9.9: Aprovechamos este polling (1 vez por seg) para vigilar que el coche no nos haya robado el canal
        if (mIsRadioActive) {
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
        requestAudioFocus();
        mIsRadioActive = true;
        Log.d(TAG, "requestPlayAudio: focus=" + mIsAudioFocusHeld + " radioActive=true");
        return mIsAudioFocusHeld;
    }

    public void setMute(boolean mute) throws RemoteException {
        if (mSetMute == null) {
            Log.w(TAG, "setMute: mSetMute is null");
            return;
        }
        try {
            if (mSetMute.getParameterTypes()[0] == int.class) {
                mSetMute.invoke(mMcuManager, mute ? 1 : 0);
            } else {
                mSetMute.invoke(mMcuManager, mute);
            }
            Log.d(TAG, "Mute set to: " + mute);
        } catch (Exception e) {
            Log.e(TAG, "Error setting mute", e);
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
                    // Intentamos Broadcom primero para el switch global silente
                    if (mBroadcomSetRdsMode != null && mFmReceiverService != null) {
                        // Toggle logic: we need current state. Using a local flag or assuming swap.
                        // Default to ON (1) if unknown for now, as RDS is almost always wanted.
                        mBroadcomSetRdsMode.invoke(mFmReceiverService, 1, 0x0F, mIsAfEnabled ? 1 : 0, 10);
                        Log.d(TAG, "Broadcom setRdsMode invoked for Global RDS Switch");
                    }
                    if (mTunerSetRdsSwitch != null && mTunerManager != null) {
                        mTunerSetRdsSwitch.invoke(mTunerManager, 1); // 1 = On
                        Log.d(TAG, "QFTunerManager.setRdsSwitch(1) invoked");
                    }
                    break;
                case 1: // AF Switch
                    boolean nextAfState = !mIsAfEnabled;
                    mIsAfEnabled = nextAfState; // V4.6.1: Actualizar estado local ANTES
                    if (mTunerSetRdsAF != null && mTunerManager != null) {
                        try {
                            mTunerSetRdsAF.invoke(mTunerManager);
                            Log.d(TAG, "QFTunerManager.setRdsAFSwitch() invoked. AF now=" + nextAfState);
                        } catch (Exception e) { e.printStackTrace(); }
                    } else {
                        // Fallback: Comando MCU directo 0x11
                        byte newState = (byte) (nextAfState ? 1 : 0);
                        sendCmd(SUB_RDS_AF, newState, (byte)0);
                        Log.d(TAG, "MCU Fallback: SUB_RDS_AF (0x11) sent with state " + newState);
                    }

                    // V11.6d: Notificar a la UI (vía K706Engine case 111) para sincronizar icono
                    if (mCallback != null) {
                        try {
                            mCallback.onEvent(111, "AF:" + (nextAfState ? "1" : "0"));
                        } catch (Exception ignored) {}
                    }

                    // Sincronizar con Broadcom (Silencioso)
                    enableSilentlyRdsFeatures(nextAfState, mIsTaEnabled);
                    break;
                case 2: // TA Switch — V11.6: Solo software, setRdsTASwitch() lanza un seek
                    mIsTaEnabled = !mIsTaEnabled;
                    Log.d(TAG, "TA toggled (software): mIsTaEnabled=" + mIsTaEnabled);
                    // Notificar a la UI via código 112 (el que handleCallback espera para TA)
                    if (mCallback != null) {
                        try {
                            mCallback.onEvent(112, "TA_SW:" + (mIsTaEnabled ? "1" : "0"));
                        } catch (Exception ignored) {}
                    }
                    break;
                case 3: // DX/Local Toggle (V9.9)
                    byte nextLocMode = mIsDxLocal ? (byte) 0 : (byte) 1; // Si es Local (1), pasar a DX (0)
                    if (mTunerOnLoc != null && mTunerManager != null) {
                        try {
                            mTunerOnLoc.invoke(mTunerManager, nextLocMode);
                            Log.d(TAG, "QFTunerManager.onLoc(" + nextLocMode + ") invoked");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Fallback MCU: 0xA0 0x07 [mode] 0x00
                        sendCmd(SUB_SWITCH_LOC, nextLocMode, (byte) 0);
                    }
                    break;

            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling RDS feature " + type, e);
        }
    }

    /**
     * V9.5: Getter público para estado de scanning.
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


    // V9.9: Hack for Bluetooth recovery. The system's MediaFocusControl "steals" the audio channel
    // but never gives it back to us via normal OnAudioFocusChange because it uses an OEM "abandonCustomAudioFocus"
    public void enforceAudioChannelRecovery() {
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
        } catch (Exception e) {
            Log.e(TAG, "enforceAudioChannelRecovery FAILED", e);
        }
    }

    // V9.9: Helper to gracefully surrender the audio channel to the system (Media = 4)
    public void returnAudioChannel() {
        Log.d(TAG, "returnAudioChannel: Cediendo canal al sistema (SetChannel 4)");
        try {
            if (mSetChannel != null && mMcuManager != null) {
                mSetChannel.invoke(mMcuManager, (byte) 4);
                Log.d(TAG, "returnAudioChannel: mSetChannel(4) enviado");
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
                // EXCEPCIÓN 2: Si estamos en llamada/interrupción, no peleamos
                if (mIsInCall) return;

                Log.w(TAG, "HEARTBEAT WARNING: Canal de audio secuestrado (Canal actual: " + currentChannel + "). Forzando recuperación a 2 (Radio).");
                mSetChannel.invoke(mMcuManager, (byte) 2);
                setMute(false); // Asegurarnos de desmutear
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en checkAndRecoverAudio", e);
        }
    }
}
