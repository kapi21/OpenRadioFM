package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;

import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.util.AppIoExecutor;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * V5.0: Motor MT8163 — Combina IRadioServiceAPI (servicio nativo del coche)
 * con HiddenRadioPlayer (API oculta del chip para RDS AF/TA).
 *
 * Sintonización, seek, band → van por IRadioServiceAPI (servicio HCN del sistema)
 * RDS (AF, TA, stereo, mute) → van por HiddenRadioPlayer (reflexión RadioPlayer);
 * mute HAL → {@link android.media.AudioManager#setParameters}; el mux OEM ({@code ExtAudioMuxer})
 * solo tiene JNI en {@code system_server}, no es invocable desde el proceso de la app.
 */
public class MT8163Engine implements RadioEngine {

    private static final String TAG = "MT8163Engine";
    private static final String HCN_PKG = "com.hcn.autoradio";
    private static final String HCN_ACTION_CLASSIC = "com.hcn.autoradio.FM_PLUG_SERVICE";
    // Variante detectada en APK “8581a”
    private static final String HCN_ACTION_8581A = "com.hcn.radio.FM_PLUG_SERVICE";
    private static final String HCN_SERVICE_CLASS = "com.hcn.autoradio.service.FMPlugService";

    private Context mContext;
    private IRadioServiceAPI mService;
    private HiddenRadioPlayer mHiddenPlayer;
    private RadioEngineCallback mCallback;
    private boolean mBound = false;
    private boolean mExternalService = false;
    /** true: no usar FMPlugService (com.hcn.autoradio); controlar vía RadioPlayer directo. */
    private boolean mPreferDirectRadioPlayer = false;
    /** Evita doble {@link #init(Context)} (p. ej. MainActivity.onEngineReady con freq 0 tras init en onServiceConnected). */
    private boolean mInitCompleted = false;
    /**
     * Último estado de mute pedido por la app ({@link #setMute(boolean)}). Evita que {@link #switchToFmAudio()}
     * o la recuperación AIDL fuercen desmuteo mientras el usuario mantiene la radio silenciada (p. ej. tras inject OEM).
     */
    private volatile boolean mRadioMuteDesired = false;
    /** Última banda UI (0=FM1,1=FM2,2=FM3,3=AM1) cuando vamos por RadioPlayer. */
    private int mLastUiBand = 0;
    
    // V21.3: Estado de recuperación diferida para reconexión asíncrona
    private boolean mPendingAudioRecovery = false;

    /**
     * Tras streaming online, el AIDL {@code requestPlayAudio()} puede provocar que SourceService
     * haga force-stop de nuestra app al entregar el mux a com.hcn.autoradio. Durante unos
     * segundos solo usamos HAL (setParameters) + HiddenRadioPlayer.
     */
    private volatile boolean mDeferAidlRequestPlayAudioAfterStream;

    /**
     * El bind a {@code FMPlugService} vía {@code RadioServiceController.start()} justo después de
     * parar streaming dispara {@code forceStopPackage} en SourceService de algunos OEM (log:
     * muxMediaPlayer prepare to Kill openradiofm). Posponemos ese bind ~15s.
     */
    private static volatile long sHcnBindAllowedAfterElapsedMs;

    /**
     * Tras parar streaming, {@code conectarRadio()} inmediato puede provocar force-stop OEM.
     * Bloqueamos el bind solo una ventana corta; luego se permite reconectar AIDL (HAL sigue usable antes).
     */
    public static final long HCN_BIND_BLOCK_AFTER_STREAM_MS = 12_000L;

    private static volatile boolean sBlockHcnServiceBindAfterStreamEnd;
    /** {@link SystemClock#elapsedRealtime()} hasta el que no se debe llamar {@code conectarRadio()}. */
    private static volatile long sHcnBindBlockUntilElapsedMs;

    /** Evita doble bind + doble requestPlayAudio() al volver de streaming (mux OEM puede force-stop). */
    private static final long RECONNECT_DEBOUNCE_MS = 400L;

    private final Runnable mDeferredBinderRecoveryRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsOnlineStreamingActive) return;
            if (mService == null) return;
            try {
                if (mDeferAidlRequestPlayAudioAfterStream) {
                    Log.i(TAG, "deferredBinderRecovery: HAL sin requestPlayAudio AIDL (handoff OEM)");
                    if (!mRadioMuteDesired) {
                        applyFmHardwareRouteOnly();
                    }
                    if (mHiddenPlayer != null && !mRadioMuteDesired) {
                        mHiddenPlayer.setMute(false);
                    }
                    mPendingAudioRecovery = false;
                    return;
                }
                mService.requestPlayAudio();
                if (mHiddenPlayer != null && !mRadioMuteDesired) {
                    mHiddenPlayer.setMute(false);
                }
                mPendingAudioRecovery = false;
            } catch (Exception e) {
                Log.w(TAG, "Recuperación diferida post-updateService falló", e);
                handleDeadService("deferredBinderRecovery", e);
            }
        }
    };

    private void applyFmHardwareRouteOnly() {
        if (mAudioManager != null) {
            try {
                mAudioManager.setParameters("fm_radio_on=1;fm_mute=0");
            } catch (Exception e) {
                Log.w(TAG, "applyFmHardwareRouteOnly", e);
            }
        }
    }

    /** Tras parar streaming: no llamar a {@code RadioServiceController.start()} hasta pasado este margen. */
    public static void deferHcnServiceBindReconnect(long deferMs) {
        long now = android.os.SystemClock.elapsedRealtime();
        sHcnBindAllowedAfterElapsedMs = now + Math.max(0L, deferMs);
        Log.i(TAG, "deferHcnServiceBindReconnect: bind FMPlug pospuesto ~" + deferMs + "ms (OEM SourceService)");
    }

    public static void clearHcnBindReconnectDefer() {
        sHcnBindAllowedAfterElapsedMs = 0L;
    }

    public static boolean isHcnBindReconnectDeferred() {
        return android.os.SystemClock.elapsedRealtime() < sHcnBindAllowedAfterElapsedMs;
    }

    public static void setBlockHcnServiceBindAfterStreamEnd(boolean block) {
        if (block) {
            sBlockHcnServiceBindAfterStreamEnd = true;
            sHcnBindBlockUntilElapsedMs = android.os.SystemClock.elapsedRealtime() + HCN_BIND_BLOCK_AFTER_STREAM_MS;
            Log.i(TAG, "setBlockHcnServiceBindAfterStreamEnd: true (~" + (HCN_BIND_BLOCK_AFTER_STREAM_MS / 1000) + "s)");
        } else {
            sBlockHcnServiceBindAfterStreamEnd = false;
            sHcnBindBlockUntilElapsedMs = 0L;
        }
    }

    public static boolean isHcnServiceBindBlockedAfterStreamEnd() {
        if (!sBlockHcnServiceBindAfterStreamEnd) {
            return false;
        }
        long now = android.os.SystemClock.elapsedRealtime();
        if (sHcnBindBlockUntilElapsedMs > 0L && now >= sHcnBindBlockUntilElapsedMs) {
            Log.i(TAG, "HCN bind block: ventana OEM terminada; se permite conectarRadio");
            sBlockHcnServiceBindAfterStreamEnd = false;
            sHcnBindBlockUntilElapsedMs = 0L;
            return false;
        }
        return true;
    }

    /** Misma cadencia que MainActivity tras {@link RadioMediaService#ACTION_MT8163_FM_HANDOFF}. */
    private static final long HCN_RECONNECT_AFTER_HANDOFF_MS = 550L;

    private final Runnable mReconnectDoBindRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContext == null || mExternalService) return;
            if (mPreferDirectRadioPlayer) return;
            if (mBound && mService != null) return;
            Log.w(TAG, "Reconectando servicio HCN (bind tras handoff MediaSession)...");
            try {
                Intent wake = new Intent("com.hcn.autoradio.FMRADIO_START");
                wake.setPackage(HCN_PKG);
                mContext.sendBroadcast(wake);
                if (!bindHcnServiceBestEffort()) {
                    Log.w(TAG, "Reconexión HCN: bindService devolvió false (classic+8581a fallaron)");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error intentando reconectar", e);
            }
        }
    };

    /**
     * ROMs HCN/MT8163 varían la action del servicio. Probamos:
     * - action clásica por package
     * - variante “8581a” con ComponentName explícito (según APK nativa)
     */
    private boolean bindHcnServiceBestEffort() {
        if (mContext == null) return false;
        try {
            Intent i = new Intent(HCN_ACTION_CLASSIC);
            i.setPackage(HCN_PKG);
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            if (mContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE)) {
                return true;
            }
        } catch (Throwable ignored) {}
        try {
            Intent i2 = new Intent(HCN_ACTION_8581A);
            i2.setComponent(new ComponentName(HCN_PKG, HCN_SERVICE_CLASS));
            i2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            return mContext.bindService(i2, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Throwable ignored) {}
        return false;
    }

    private final Runnable mReconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContext == null || mExternalService) return;
            if (mPreferDirectRadioPlayer) {
                Log.i(TAG, "Reconexión HCN omitida (pref_mt8163_mcu_direct=true)");
                return;
            }
            if (isHcnServiceBindBlockedAfterStreamEnd()) {
                ensurePollingThread();
                if (mPollingHandler != null) {
                    mPollingHandler.postDelayed(this, 500L);
                    Log.d(TAG, "Reconexión HCN: esperando fin ventana post-streaming OEM (~12s)");
                }
                return;
            }
            if (mBound && mService != null) return;

            try {
                Intent h = new Intent(mContext, RadioMediaService.class);
                h.setAction(RadioMediaService.ACTION_MT8163_FM_HANDOFF);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(h);
                } else {
                    mContext.startService(h);
                }
            } catch (Exception e) {
                Log.w(TAG, "ACTION_MT8163_FM_HANDOFF antes de bind HCN falló", e);
            }
            ensurePollingThread();
            if (mPollingHandler != null) {
                mPollingHandler.removeCallbacks(mReconnectDoBindRunnable);
                mPollingHandler.postDelayed(mReconnectDoBindRunnable, HCN_RECONNECT_AFTER_HANDOFF_MS);
            } else {
                mReconnectDoBindRunnable.run();
            }
        }
    };

    // V16.2: Polling mechanism for continuous frequency updates during seek/scan
    // V21.1: Polling fuera del hilo UI para evitar jank
    private HandlerThread mPollingThread;
    private Handler mPollingHandler;
    private Runnable mPollingRunnable;
    private int mLastPolledFreq = -1;
    private int mPollingTicks = 0;
    
    // V18.4: AudioFocus y Streaming Guard para MT8163
    private boolean mIsOnlineStreamingActive = false;
    private android.media.AudioManager mAudioManager;
    private android.media.AudioManager.OnAudioFocusChangeListener mAudioFocusListener;
    
    public MT8163Engine() {}

    public MT8163Engine(IRadioServiceAPI service) {
        this.mService = service;
        this.mBound = true;
        this.mExternalService = true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = IRadioServiceAPI.Stub.asInterface(binder);
            mBound = true;
            Log.d(TAG, "Servicio HCN conectado: " + name);

            // Registrar callback AIDL
            try {
                mService.registerRadioCallback(new IRadioCallBack.Stub() {
                    @Override
                    public void onEvent(int code, String data) {
                        handleAidlCallback(code, data);
                    }
                });

                // Sincronía inicial de banda (8581a/HCN): algunos firmwares “viven” en FM2/FM3 pero el AIDL
                // devuelve banda poco fiable. Tomamos la banda real desde RadioPlayer oculto y la persistimos.
                syncUiBandFromHiddenPlayer("onServiceConnected");
                
                // V18.6: Si el servicio fue matado por el sistema (ej. abriendo Youtube)
                // y teníamos una emisora sintonizada, la restauramos automáticamente
                if ((mPendingAudioRecovery || mLastPolledFreq > 0) && !mIsOnlineStreamingActive) {
                    Log.i(TAG, "Restaurando estado tras conexión (Re-init RDS y Audio). Freq: " + mLastPolledFreq);
                    if (mLastPolledFreq > 0) mService.gotoFreq(mLastPolledFreq);
                    if (mDeferAidlRequestPlayAudioAfterStream) {
                        Log.i(TAG, "onServiceConnected: omitiendo requestPlayAudio AIDL (handoff streaming->FM)");
                        if (!mRadioMuteDesired) {
                            applyFmHardwareRouteOnly();
                        }
                    } else {
                        mService.requestPlayAudio();
                    }
                    
                    // V21.3: Re-inicializar canal RDS para evitar referencias a proceso muerto
                    if (mHiddenPlayer != null) {
                        mHiddenPlayer.init();
                        if (!mRadioMuteDesired) {
                            mHiddenPlayer.setMute(false);
                        }
                    }
                    mPendingAudioRecovery = false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error registrando callback AIDL o recuperando estado", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.w(TAG, "Servicio HCN desconectado (Posiblemente asesinado por SourceService)");
        }
    };

    private void reconnectIfNeeded() {
        if (mExternalService || mContext == null) return;
        if (mBound && mService != null) return;
        ensurePollingThread();
        if (mPollingHandler != null) {
            mPollingHandler.removeCallbacks(mReconnectRunnable);
            mPollingHandler.removeCallbacks(mReconnectDoBindRunnable);
            mPollingHandler.postDelayed(mReconnectRunnable, RECONNECT_DEBOUNCE_MS);
        } else {
            mReconnectRunnable.run();
        }
    }

    private void handleDeadService(String op, Exception e) {
        Log.w(TAG, "Servicio HCN muerto en " + op + " -> reconectando", e);
        mService = null;
        mBound = false;
        reconnectIfNeeded();
    }

    /**
     * V21.3: Permite actualizar el binder del servicio sin re-inicializar todo el motor.
     * Evita la duplicidad de instancias y fugas de hilos de polling.
     */
    public void updateService(IRadioServiceAPI service) {
        Log.i(TAG, "updateService: Actualizando binder del servicio AIDL legado.");
        this.mService = service;
        this.mBound = (service != null);
        // NO llamar enforceAudioRecovery() aquí en el mismo hilo: al volver streaming->FM
        // coincide con ExoPlayer.release() y con RadioServiceController.start(); el mux OEM
        // puede matar el proceso si requestPlayAudio() se dispara dos veces seguidas.
        ensurePollingThread();
        if (mPollingHandler != null) {
            mPollingHandler.removeCallbacks(mDeferredBinderRecoveryRunnable);
            mPollingHandler.postDelayed(mDeferredBinderRecoveryRunnable, 450);
        }
    }

    @Override
    public boolean init(Context context) {
        if (mInitCompleted) {
            Log.d(TAG, "init(): omitido (motor ya inicializado; evita doble bind/HiddenRadioPlayer)");
            return true;
        }
        mContext = context;

        // Banda UI coherente con MainActivity (presets P*_B<band>). IRadioServiceAPI.getCurrentBand()
        // en muchos firmwares HCN devuelve siempre 0; sin caché, StatusRefreshCoordinator pisaba mCurrentBand.
        try {
            int b = context.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getInt("pref_last_band", 0);
            if (b >= 0 && b <= 4) {
                mLastUiBand = b;
            }
        } catch (Exception ignored) {
        }

        // Preferencia: “MCU first” (sin dependencia del servicio com.hcn.autoradio).
        try {
            mPreferDirectRadioPlayer = context
                    .getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_mt8163_mcu_direct", false);
        } catch (Exception ignored) {
            mPreferDirectRadioPlayer = false;
        }

        // 1. Conectar con servicio HCN del sistema (tune, seek, band)
        if (!mExternalService && !mPreferDirectRadioPlayer) {
            try {
                boolean ok = bindHcnServiceBestEffort();
                Log.d(TAG, "Binding al servicio HCN (classic/8581a): " + ok);
            } catch (Exception e) {
                Log.e(TAG, "No se pudo conectar al servicio HCN", e);
            }
        } else if (mExternalService) {
            // Si el servicio es externo, registrar el callback de todas formas
            try {
                mService.registerRadioCallback(new IRadioCallBack.Stub() {
                    @Override
                    public void onEvent(int code, String data) {
                        handleAidlCallback(code, data);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "Error registrando callback AIDL externo", e);
            }
        } else {
            Log.i(TAG, "MT8163: pref_mt8163_mcu_direct=true -> omitiendo bind a com.hcn.autoradio");
        }

        // 2. Inicializar HiddenRadioPlayer (RDS AF/TA via RadioPlayer oculto)
        mHiddenPlayer = new HiddenRadioPlayer(new HiddenRadioPlayer.Listener() {
            @Override
            public void onRdsText(String text) {
                if (mCallback != null) mCallback.onRdsText(text);
            }

            @Override
            public void onRdsName(String name) {
                if (mCallback != null) mCallback.onRdsName(name);
            }

            @Override
            public void onRdsPty(String pty) {
                // V18.6.3: Some AIDL sources are limited, restoring HiddenRadioPlayer as primary source
                if (mCallback != null) mCallback.onRdsPty(pty);
            }

            @Override
            public void onRawEvent(int code, Object info, String str) {
                // Código 1: RadioInfo con mUiBand (FM-1…); el AIDL a menudo no notifica banda o devuelve 0.
                if (code == 1 && info != null && mHiddenPlayer != null) {
                    int b = mHiddenPlayer.readAppBandIndexFromRadioInfo(info);
                    if (b >= 0 && b <= 4 && b != mLastUiBand) {
                        mLastUiBand = b;
                        if (mCallback != null) {
                            mCallback.onBandChanged(b);
                        }
                    }
                }
                if (mCallback != null) mCallback.onRawEvent(code, str);
            }

            @Override
            public void onRdsAfTaStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
                if (mCallback != null) mCallback.onRdsStatus(afEnabled, taEnabled, tpEnabled);
            }
        });

        boolean rdsOk = mHiddenPlayer.init();
        Log.d(TAG, "HiddenRadioPlayer init: " + (rdsOk ? "OK" : "FAIL"));

        // V18.4: Setup AudioFocus Listener (igual que en K706)
        mAudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusListener = focusChange -> {
            switch (focusChange) {
                case android.media.AudioManager.AUDIOFOCUS_LOSS:
                    if (mIsOnlineStreamingActive) break;
                    switchToAndroidAudio();
                    break;
                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mIsOnlineStreamingActive) break;
                    setMute(true);
                    break;
                case android.media.AudioManager.AUDIOFOCUS_GAIN:
                    if (mIsOnlineStreamingActive) break;
                    switchToFmAudio();
                    break;
            }
        };

        ensurePollingThread();
        mInitCompleted = true;
        return true; 
    }

    private void ensurePollingThread() {
        if (mPollingHandler != null) return;
        mPollingThread = new HandlerThread("MT8163-FreqPoll");
        mPollingThread.start();
        mPollingHandler = new Handler(mPollingThread.getLooper());
    }

    @Override
    public void release() {
        release(false); // Por defecto, liberación completa y muteo
    }

    @Override
    public void release(boolean persist) {
        if (persist) {
            Log.d(TAG, "release(persist=true): Recreación detectada. Manteniendo hardware activo.");
            if (mPollingRunnable != null && mPollingHandler != null) {
                mPollingHandler.removeCallbacks(mPollingRunnable);
            }
            return;
        }

        Log.d(TAG, "release(persist=false): Soltando recursos MT8163 y silenciando");
        com.example.openradiofm.ui.main.RadioServiceController.clearSharedLocalEngineIfSame(this);
        mInitCompleted = false;
        mRadioMuteDesired = false;
        switchToAndroidAudio(); // Asegurar liberación de audio al salir
        
        if (mPollingRunnable != null && mPollingHandler != null) {
            mPollingHandler.removeCallbacks(mPollingRunnable);
        }
        if (mPollingHandler != null) {
            mPollingHandler.removeCallbacks(mReconnectRunnable);
            mPollingHandler.removeCallbacks(mReconnectDoBindRunnable);
        }
        if (mPollingThread != null) {
            try {
                mPollingThread.quitSafely();
            } catch (Exception ignored) {}
            mPollingThread = null;
            mPollingHandler = null;
        }
        if (mHiddenPlayer != null) {
            mHiddenPlayer.release();
            mHiddenPlayer = null;
        }
        if (mBound && mContext != null && !mExternalService) {
            try { mContext.unbindService(mConnection); } catch (Exception ignored) {}
            mBound = false;
        }
        mService = null;
        mCallback = null;
    }

    // V18.4: Permitir que OnlineStreamManager nos notifique el estado
    @Override
    public boolean isOnlineStreamingActive() {
        return mIsOnlineStreamingActive;
    }

    @Override
    public boolean shouldSkipMediaServiceForcePlayOnUnmute() {
        return mDeferAidlRequestPlayAudioAfterStream;
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        this.mIsOnlineStreamingActive = active;
        Log.d(TAG, "setOnlineStreamingActive: " + active);
        if (active) {
            mDeferAidlRequestPlayAudioAfterStream = false;
            clearHcnBindReconnectDefer();
            setBlockHcnServiceBindAfterStreamEnd(false);
            switchToAndroidAudio();
            return;
        }
        if (!active) {
            mDeferAidlRequestPlayAudioAfterStream = true;
            // NO reactivar requestPlayAudio AIDL por temporizador: tras reconectar FMPlug, el OEM
            // mata OpenRadioFM si se llama requestPlayAudio() (ver SourceService.forceStopPackage).
            // Solo HAL + HiddenRadioPlayer hasta el próximo streaming (active=true limpia el flag).
        }
    }

    @Override
    public void closeDevice() {
        release();
    }

    @Override
    public void setBand(int band) {
        // No hay setBand AIDL fiable en todos los firmwares. En ROMs HCN/8581a la UI band real
        // suele depender de RadioPlayer (oculto). Para que FM1 funcione igual que FM2/FM3,
        // intentamos fijar la banda real con HiddenRadioPlayer si está disponible.
        int target = (band >= 0 && band <= 4) ? band : 0;
        mLastUiBand = target;
        try {
            if (mContext != null) {
                mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                        .edit().putInt("pref_last_band", mLastUiBand).apply();
            }
        } catch (Exception ignored) {}

        if (mHiddenPlayer != null) {
            String bandName;
            if (target == 1) bandName = "FM2";
            else if (target == 2) bandName = "FM3";
            else if (target == 3 || target == 4) bandName = "AM1"; // AM2 no siempre existe en HCN
            else bandName = "FM1";
            Integer freq = mHiddenPlayer.getCurrentFreqKhz();
            if (freq == null || freq <= 0) freq = getCurrentFreq();
            boolean ok = mHiddenPlayer.setUiBandKeepFreq(bandName, freq);
            if (ok) {
                if (mCallback != null) mCallback.onBandChanged(mLastUiBand);
                ensurePollingThread();
                if (mPollingHandler != null) {
                    mPollingHandler.postDelayed(() -> syncUiBandFromHiddenPlayer("setBand"), 120);
                }
                Log.i(TAG, "setBand: RadioPlayer UI band -> " + bandName + " (target=" + target + ")");
                return;
            }
        }
        if (mCallback != null) mCallback.onBandChanged(mLastUiBand);
        Log.d(TAG, "setBand: cache UI band=" + mLastUiBand + " (sin RadioPlayer)");
    }

    @Override
    public String getEngineName() {
        return "MT8163";
    }

    // === Tuning (via AIDL service) ===

    @Override
    public void tune(int freqKhz) {
        resetRdsUI();
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            boolean ok = mHiddenPlayer.tune(freqKhz);
            if (ok) {
                if (mCallback != null) mCallback.onFrequencyChanged(freqKhz);
                return;
            }
        }
        // Snapshot antes de resetRdsUI(): el callback RDS puede llamar getCurrentFreq() y
        // handleDeadService() deja mService=null antes de llegar a gotoFreq (reentrada).
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        try {
            svc.gotoFreq(freqKhz);
        } catch (android.os.DeadObjectException e) {
            handleDeadService("tune", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en tune", e);
        }
        startFreqPolling();
    }

    private void resetRdsUI() {
        if (mCallback != null) {
            mCallback.onRdsName("");
            mCallback.onRdsText("");
            mCallback.onRdsPty("");
            mCallback.onRdsPi("");
            // Force a resync of logo by firing an empty name or explicitly triggering frequency unchanged? 
            // Wait, LogoManager handles logos based on frequency and RDS name. 
            // The empty RDS name triggered above will force a logo update next time freq is updated.
        }
    }

    /**
     * Ventana corta (~4 s) de polling tras seek/tune/step: una sola implementación, dos fuentes.
     *
     * @param sourceAlive si false, se aborta la cadena (servicio nulo o modo directo inválido).
     * @param freqKhz     frecuencia en kHz para este tick (≤0 = sin cambio que notificar).
     */
    private void startShortFreqPolling(BooleanSupplier sourceAlive, IntSupplier freqKhz) {
        ensurePollingThread();
        if (mPollingHandler == null) return;
        if (mPollingRunnable != null) {
            mPollingHandler.removeCallbacks(mPollingRunnable);
        }
        mPollingTicks = 0;
        mPollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!sourceAlive.getAsBoolean()) {
                    return;
                }
                int f = freqKhz.getAsInt();
                if (f > 0 && f != mLastPolledFreq) {
                    mLastPolledFreq = f;
                    if (mCallback != null) {
                        mCallback.onFrequencyChanged(f);
                    }
                }
                mPollingTicks++;
                if (mPollingTicks < 40 && mPollingHandler != null) {
                    mPollingHandler.postDelayed(this, 100);
                }
            }
        };
        mPollingHandler.postDelayed(mPollingRunnable, 100);
    }

    /** Polling tras comandos AIDL: lee solo el servicio HCN (evita mezclar con RadioPlayer). */
    private void startFreqPolling() {
        startShortFreqPolling(
                () -> mService != null,
                () -> {
                    if (mService == null) return 0;
                    try {
                        return mService.getCurrentFreq();
                    } catch (android.os.DeadObjectException e) {
                        handleDeadService("shortFreqPoll", e);
                        return 0;
                    } catch (RemoteException e) {
                        return 0;
                    }
                });
    }

    /** Polling tras comandos RadioPlayer directo. */
    private void startDirectFreqPolling() {
        startShortFreqPolling(
                () -> mPreferDirectRadioPlayer && mHiddenPlayer != null,
                () -> {
                    Integer f = mHiddenPlayer != null ? mHiddenPlayer.getCurrentFreqKhz() : null;
                    return (f != null && f > 0) ? f : 0;
                });
    }

    @Override
    public int getCurrentFreq() {
        if (mIsOnlineStreamingActive && mLastPolledFreq > 0) return mLastPolledFreq;
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            Integer f = mHiddenPlayer.getCurrentFreqKhz();
            if (f != null && f > 0) return f;
        }
        if (mService == null) return 0;
        try {
            return mService.getCurrentFreq();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("getCurrentFreq", e);
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    @Override
    public int getCurrentBand() {
        if (mIsOnlineStreamingActive) {
            return mLastUiBand;
        }
        // No usar mService.getCurrentBand(): en HCN suele ser 0 aunque el HU esté en FM2/FM3,
        // lo que vaciaba visualmente los presets de esa banda y mezclaba favoritos con FM1.
        return mLastUiBand;
    }
    
    @Override
    public void seekUp() {
        resetRdsUI();
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            // Misma convención que MT8163TunerAdapter / firmware: API “seekUp” de app = buscar siguiente emisora.
            if (mHiddenPlayer.seekDown()) {
                startDirectFreqPolling();
                return;
            }
        }
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current - 100);
        try {
            svc.onSeekDownEvent();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("seekUp", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en seekUp");
        }
        startFreqPolling();
    }

    @Override
    public void seekDown() {
        resetRdsUI();
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            if (mHiddenPlayer.seekUp()) {
                startDirectFreqPolling();
                return;
            }
        }
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current + 100);
        try {
            svc.onSeekUpEvent();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("seekDown", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en seekDown");
        }
        startFreqPolling();
    }

    @Override
    public void stepUp() {
        resetRdsUI();
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            // Alineado con seek: firmware intercambia sentido manual up/down respecto a la UI.
            int current = getCurrentFreq();
            if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current - 100);
            if (mHiddenPlayer.stepDown()) {
                startDirectFreqPolling();
                return;
            }
        }
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) {
            mCallback.onFrequencyChanged(current - 100);
        }
        try {
            svc.onManualDownEvent();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("stepUp", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en stepUp");
        }
        startFreqPolling();
    }

    @Override
    public void stepDown() {
        resetRdsUI();
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            int current = getCurrentFreq();
            if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current + 100);
            if (mHiddenPlayer.stepUp()) {
                startDirectFreqPolling();
                return;
            }
        }
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) {
            mCallback.onFrequencyChanged(current + 100);
        }
        try {
            svc.onManualUpEvent();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("stepDown", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en stepDown");
        }
        startFreqPolling();
    }

    @Override
    public void scan() {
        // Consigna: el autoscan lo gestiona ScanManager (software). No usar OEM (ni AIDL ni RadioPlayer).
        Log.d(TAG, "scan(): no-op (ScanManager propio)");
    }

    @Override
    public void stopScan() {
        if (mService == null) return;
        try { 
            mService.onPSEvent(); 
        } catch (android.os.DeadObjectException e) {
            handleDeadService("stopScan", e);
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en stopScan"); 
        }
    }

    @Override
    public void bandCycle() {
        // Para 8581a/HCN: incluso cuando NO estamos en modo "mcu_direct", la banda del sistema puede quedarse en FM2
        // y el AIDL no siempre notifica band changes. Si RadioPlayer está disponible, forzamos el ciclo aquí.
        if (mHiddenPlayer != null) {
            // Ciclo tolerante: FM1 -> FM2 -> (FM3 si existe) -> FM1. Si falla, caer a FM1 o AM1 si existe.
            String cur = mHiddenPlayer.getUiBandName();
            String curNorm = cur != null ? cur.trim().toUpperCase(java.util.Locale.US) : null;
            String[] candidates;
            if ("FM2".equals(curNorm) || "FM-2".equals(curNorm)) {
                candidates = new String[] { "FM3", "FM1", "AM1", "FM2" };
            } else if ("FM3".equals(curNorm) || "FM-3".equals(curNorm)) {
                candidates = new String[] { "FM1", "FM2", "AM1", "FM3" };
            } else if ("AM1".equals(curNorm) || "AM-1".equals(curNorm) || "AM".equals(curNorm)) {
                candidates = new String[] { "FM1", "FM2", "FM3", "AM1" };
            } else {
                // FM1 o null -> FM2 (comportamiento histórico)
                candidates = new String[] { "FM2", "FM3", "FM1", "AM1" };
            }
            Integer freq = mHiddenPlayer.getCurrentFreqKhz();
            if (freq == null || freq <= 0) freq = getCurrentFreq();
            for (String next : candidates) {
                if (mHiddenPlayer.setUiBandKeepFreq(next, freq)) {
                    if ("FM1".equals(next)) mLastUiBand = 0;
                    else if ("FM2".equals(next)) mLastUiBand = 1;
                    else if ("FM3".equals(next)) mLastUiBand = 2;
                    else if ("AM1".equals(next) || "AM".equals(next)) mLastUiBand = 3;
                    if (mCallback != null) mCallback.onBandChanged(mLastUiBand);
                    // Alinear persistencia para que el próximo arranque no caiga a FM1 vacío/FMx inconsistente.
                    try {
                        if (mContext != null) {
                            mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                                    .edit().putInt("pref_last_band", mLastUiBand).apply();
                        }
                    } catch (Exception ignored) {}
                    // Si estamos en modo directo, también hacemos polling directo de freq.
                    if (mPreferDirectRadioPlayer) {
                        startDirectFreqPolling();
                    } else {
                        // En modo AIDL, solo re-sincronizar banda tras un tick.
                        ensurePollingThread();
                        if (mPollingHandler != null) {
                            mPollingHandler.postDelayed(() -> syncUiBandFromHiddenPlayer("bandCycle"), 120);
                        }
                    }
                    return;
                }
            }
        }
        if (mService == null) return;
        try {
            mService.onBandEvent();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("bandCycle", e);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en bandCycle");
        }
        // Fallback: aunque el AIDL no notifique, intentamos leer banda real tras el ciclo.
        ensurePollingThread();
        if (mPollingHandler != null) {
            mPollingHandler.postDelayed(() -> syncUiBandFromHiddenPlayer("bandCycleAidl"), 160);
        }
    }

    // === Audio (via HiddenRadioPlayer) ===

    @Override
    public boolean isStereo() {
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            Boolean s = mHiddenPlayer.isStereo();
            if (s != null) return s;
        }
        if (mService == null) return false;
        try {
            return mService.IsStereo();
        } catch (android.os.DeadObjectException e) {
            mService = null;
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override
    public void setStereo(boolean enable) {
        if (mHiddenPlayer != null) mHiddenPlayer.setStereo(enable);
    }

    @Override
    public void setMute(boolean mute) {
        mRadioMuteDesired = mute;
        if (mHiddenPlayer != null) mHiddenPlayer.setMute(mute);

        // La app nativa HCN suele mutear vía framework carsource (ExtAudioMuxer cmd 10/11), no vía fm_mute= (cmd 04).
        // NO inyectar tecla MCU aquí: setMute() también lo llama audio focus / switchToAndroidAudio al arranque
        // y VOLUME_MUTE es toggle → cortes “va y viene”. El inject solo: {@link #applyUserOemMuteThroughMcuKey()}.
        tryHcnCarsourceMcuMute(mute);
        
        // Fallback para MT8163: parámetros directos al AudioManager (el audio FM suele ir por HAL, no por STREAM_MUSIC).
        if (mAudioManager != null) {
            try {
                // Algunas ROMs ignoran el formato con ';' pero aceptan claves sueltas. Probamos ambos.
                // Importante: NO apagar fm_radio_on durante el mute para evitar pops/demoras.
                final String v = mute ? "1" : "0";
                final String paramsA = "fm_radio_on=1;fm_mute=" + v;
                final String paramsB = "fm_mute=" + v;
                final String paramsC = "hcn_fm_mute=" + v;

                mAudioManager.setParameters(paramsA);
                mAudioManager.setParameters(paramsB);
                mAudioManager.setParameters(paramsC);
                
                // Reintento corto para asegurar que el DSP procesa el cambio
                if (mPollingHandler != null) {
                    mPollingHandler.postDelayed(() -> {
                        try {
                            mAudioManager.setParameters(paramsA);
                            mAudioManager.setParameters(paramsB);
                            mAudioManager.setParameters(paramsC);
                        } catch (Exception ignored) {}
                    }, 120);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setMute (AudiorManager fallback)", e);
            }
        }

        // Extra: algunos firmwares solo aplican mute correctamente si el routing FM está activo.
        // Reforzar el estado base del HAL sin tocar audio focus ni forzar requestPlayAudio.
        try {
            if (mAudioManager != null) {
                mAudioManager.setParameters("fm_radio_on=1");
            }
        } catch (Exception ignored) {}
    }

    /**
     * Busca en {@code android.carsource.McuManager} métodos OEM relacionados con mute (p. ej. radio/FM/source)
     * y los invoca por reflexión. Es la misma clase que usa {@link #sendMcuKey(int)}; en muchas ROM HCN el mute
     * del mux no pasa por {@code fm_mute=} sino por esta capa (logcat: ExtAudioMuxer cmd 10/11 vs 04).
     */
    private void tryHcnCarsourceMcuMute(boolean mute) {
        boolean reflectedOk = false;
        try {
            Class<?> cls = Class.forName("android.carsource.McuManager");
            Object inst;
            try {
                inst = cls.getMethod("getsInstance").invoke(null);
            } catch (NoSuchMethodException e) {
                inst = cls.getMethod("getInstance").invoke(null);
            }
            if (inst == null) {
                Log.d(TAG, "McuManager.getsInstance() == null");
            } else {
                final java.util.Locale loc = java.util.Locale.US;
                java.util.ArrayList<java.lang.reflect.Method> cands = new java.util.ArrayList<>();
                for (java.lang.reflect.Method m : cls.getMethods()) {
                    String n = m.getName();
                    if ("notify".equals(n) || "notifyAll".equals(n) || "wait".equals(n)) continue;
                    String nl = n.toLowerCase(loc);
                    if (!nl.contains("mute") && !nl.contains("silen")) continue;
                    Class<?>[] pt = m.getParameterTypes();
                    if (pt.length == 0) continue;
                    cands.add(m);
                }
                cands.sort((a, b) -> muteMethodPriority(b) - muteMethodPriority(a));
                for (java.lang.reflect.Method m : cands) {
                    Class<?>[] p = m.getParameterTypes();
                    try {
                        m.setAccessible(true);
                        if (p.length == 1 && p[0] == boolean.class) {
                            m.invoke(inst, mute);
                            Log.i(TAG, "HCN McuManager: OK " + m.getName() + "(boolean)");
                            reflectedOk = true;
                            break;
                        }
                        if (p.length == 1 && p[0] == int.class) {
                            m.invoke(inst, mute ? 1 : 0);
                            Log.i(TAG, "HCN McuManager: OK " + m.getName() + "(int)");
                            reflectedOk = true;
                            break;
                        }
                        if (p.length == 2 && p[0] == int.class && p[1] == boolean.class) {
                            for (int path : new int[] { 0, 4 }) {
                                try {
                                    m.invoke(inst, path, mute);
                                    Log.i(TAG, "HCN McuManager: OK " + m.getName() + "(" + path + "," + mute + ")");
                                    reflectedOk = true;
                                    break;
                                } catch (Throwable ignored) {
                                }
                            }
                            if (reflectedOk) break;
                        }
                        if (p.length == 2 && p[0] == boolean.class && p[1] == int.class) {
                            m.invoke(inst, mute, 0);
                            Log.i(TAG, "HCN McuManager: OK " + m.getName() + "(boolean,int)");
                            reflectedOk = true;
                            break;
                        }
                    } catch (Throwable ex) {
                        Log.d(TAG, "McuManager." + m.getName() + " omitido: " + ex.getClass().getSimpleName());
                    }
                }
                if (!reflectedOk) {
                    if (cands.isEmpty()) {
                        logMcuManagerMethodCatalogOnce(cls);
                        Log.i(TAG, "McuManager: sin API mute; inject VOLUME_MUTE solo vía gesto usuario (PlaybackManager)");
                    } else {
                        Log.d(TAG, "McuManager: ningún mute aplicó tras probar " + cands.size() + " candidatos");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "android.carsource.McuManager no presente");
        } catch (Throwable t) {
            Log.d(TAG, "tryHcnCarsourceMcuMute: " + t.getClass().getSimpleName());
        }
    }

    /**
     * Imita la tecla física de mute del HU (toggle). Llamar solo desde {@link com.example.openradiofm.ui.main.PlaybackManager}
     * cuando el usuario (o sesión media explícita) cambia mute, no desde audio focus ni al iniciar.
     */
    public void applyUserOemMuteThroughMcuKey() {
        if (!shouldTryMcuInjectMuteKey()) return;
        tryHcnMcuInjectHardwareMuteKey();
        tryMicrontekIrMuteBroadcast();
    }

    private boolean shouldTryMcuInjectMuteKey() {
        try {
            return mContext != null
                    && mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_mt8163_mcu_inject_mute_key", true);
        } catch (Exception e) {
            return true;
        }
    }

    private static volatile boolean sMcuManagerCatalogLogged;

    private static void logMcuManagerMethodCatalogOnce(Class<?> cls) {
        if (sMcuManagerCatalogLogged) return;
        sMcuManagerCatalogLogged = true;
        StringBuilder sb = new StringBuilder(512);
        for (java.lang.reflect.Method m : cls.getMethods()) {
            if (sb.length() > 2400) {
                sb.append("…");
                break;
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(m.getName());
            sb.append('(');
            Class<?>[] pt = m.getParameterTypes();
            for (int i = 0; i < pt.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(pt[i].getSimpleName());
            }
            sb.append(')');
        }
        Log.i(TAG, "McuManager métodos (diagnóstico 1×): " + sb);
    }

    /**
     * Simula pulsación de mute del panel (toggle en muchos HU); puede disparar ExtAudioMuxer 10/11 vía servicio MCU.
     */
    private void tryHcnMcuInjectHardwareMuteKey() {
        final int duration = 0x32;
        try {
            Class<?> mcuClass = Class.forName("android.carsource.McuManager");
            Object instance = mcuClass.getMethod("getsInstance").invoke(null);
            if (instance == null) return;
            java.lang.reflect.Method inject = mcuClass.getMethod("injectKeyEventTimeout", int.class, int.class);
            inject.invoke(instance, android.view.KeyEvent.KEYCODE_VOLUME_MUTE, duration);
            Log.i(TAG, "McuManager.injectKeyEventTimeout(VOLUME_MUTE=" + android.view.KeyEvent.KEYCODE_VOLUME_MUTE + ")");
        } catch (Throwable t) {
            Log.d(TAG, "inject VOLUME_MUTE falló: " + t.getClass().getSimpleName());
        }
    }

    /** Algunos firmwares Microntek/Topway reenvían IR por broadcast (opcional). */
    private void tryMicrontekIrMuteBroadcast() {
        if (mContext == null) return;
        try {
            android.content.Intent i = new android.content.Intent("com.microntek.irkeyDown");
            i.putExtra("keyCode", android.view.KeyEvent.KEYCODE_VOLUME_MUTE);
            mContext.sendBroadcast(i);
        } catch (Throwable ignored) {
        }
    }

    private static int muteMethodPriority(java.lang.reflect.Method m) {
        String n = m.getName().toLowerCase(java.util.Locale.US);
        int s = 0;
        if (n.contains("fm") || n.contains("radio") || n.contains("tuner")) s += 24;
        if (n.contains("source") || n.contains("path") || n.contains("mux")) s += 14;
        if (n.contains("mute") || n.contains("silen")) s += 8;
        if (n.startsWith("is") || n.startsWith("get") || n.startsWith("has")) s -= 35;
        if (n.contains("bt") || n.contains("bluetooth") || n.contains("hfp") || n.contains("phone"))
            s -= 28;
        return s;
    }

    @Override
    public void openEq(Context context) {
        // En MT8163, el ecualizador se suele lanzar con un código de tecla MCU específico
        sendMcuKey(0x134); // Keycode 308 for DSP in MT8163
    }

    private void sendMcuKey(int key) {
        try {
            Class<?> mcuClass = Class.forName("android.carsource.McuManager");
            java.lang.reflect.Method getInstance = mcuClass.getMethod("getsInstance");
            Object instance = getInstance.invoke(null);
            java.lang.reflect.Method injectKey = mcuClass.getMethod("injectKeyEventTimeout", int.class, int.class);
            injectKey.invoke(instance, key, 0x32);
            Log.d(TAG, "MCU Key injected: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error injecting MCU key", e);
        }
    }

    @Override
    public boolean requestPlayAudio() {
        if (mService == null) return false;
        if (mDeferAidlRequestPlayAudioAfterStream) {
            Log.d(TAG, "requestPlayAudio: omitido AIDL (post-streaming OEM; uso HAL)");
            try {
                applyFmHardwareRouteOnly();
            } catch (Exception ignored) {}
            return true;
        }
        try {
            return mService.requestPlayAudio();
        } catch (android.os.DeadObjectException e) {
            handleDeadService("requestPlayAudio", e);
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override
    public void enforceAudioRecovery() {
        if (mService == null) {
            if (mPreferDirectRadioPlayer) {
                try {
                    if (!mRadioMuteDesired) {
                        applyFmHardwareRouteOnly();
                    }
                } catch (Exception ignored) {}
                if (mHiddenPlayer != null && !mRadioMuteDesired) {
                    mHiddenPlayer.setMute(false);
                }
                return;
            }
            Log.w(TAG, "enforceAudioRecovery: Servicio nulo, marcando recuperación pendiente.");
            mPendingAudioRecovery = true;
            reconnectIfNeeded();
            return;
        }

        if (mDeferAidlRequestPlayAudioAfterStream) {
            Log.i(TAG, "enforceAudioRecovery: omitiendo requestPlayAudio AIDL (handoff streaming->FM OEM)");
            try {
                if (!mRadioMuteDesired) {
                    applyFmHardwareRouteOnly();
                }
            } catch (Exception ignored) {}
            if (mHiddenPlayer != null && !mRadioMuteDesired) {
                mHiddenPlayer.setMute(false);
            }
            return;
        }
        
        // En MT8163 basta con volver a pedir el canal de audio al servicio AIDL
        try {
            mService.requestPlayAudio();
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "enforceAudioRecovery: DeadObject (sin doble reconnect inmediato)", e);
            mPendingAudioRecovery = true;
            mService = null;
            mBound = false;
            reconnectIfNeeded();
        } catch (Exception e) {
            handleDeadService("enforceAudioRecovery", e);
        }
        
        if (mHiddenPlayer != null && !mRadioMuteDesired) {
            mHiddenPlayer.setMute(false);
        }
    }

    @Override
    public void switchToAndroidAudio() {
        Log.d(TAG, "switchToAndroidAudio (MT8163) - Liberando canal para sistema");
        setMute(true);
        
        // V18.5: Ejecutar parámetros de audio en un hilo ligero para no congelar la UI si el hardware tarda
        AppIoExecutor.execute(() -> {
            if (mAudioManager != null) {
                try {
                    mAudioManager.setParameters("fm_radio_on=1;fm_mute=1");
                } catch (Exception e) {
                    Log.e(TAG, "Error setting audio parameters", e);
                }
            }
            
            // Retorno al hilo principal para el resto de la secuencia
            mPollingHandler.postDelayed(() -> {
                // V18.5: NO desmutear aquí si estamos en streaming, el master debe controlarlo la app de origen
                // setMute(false); 
                // Ya no abandonamos el audio focus para no interferir con SourceService
            }, 300);
        });
    }

    @Override
    public void switchToFmAudio() {
        Log.d(TAG, "switchToFmAudio (MT8163)");
        if (mRadioMuteDesired) {
            Log.d(TAG, "switchToFmAudio: omitido (usuario mantiene mute; evita fm_mute=0 tras AUDIOFOCUS_GAIN)");
            return;
        }
        if (mAudioManager != null) {
            // Ya no solicitamos audio focus para no interferir con SourceService
            mAudioManager.setParameters("fm_radio_on=1;fm_mute=0");
        }
        enforceAudioRecovery();
    }

    // === RDS (via HiddenRadioPlayer) ===

    @Override
    public void toggleRdsFeature(int type) {
        if (mHiddenPlayer != null) mHiddenPlayer.toggleRdsFeature(type);
    }

    @Override
    public boolean isAfEnabled() {
        return mHiddenPlayer != null && mHiddenPlayer.isAfEnabled();
    }

    @Override
    public boolean isTaEnabled() {
        return mHiddenPlayer != null && mHiddenPlayer.isTaEnabled();
    }

    @Override
    public boolean isTpEnabled() {
        return mHiddenPlayer != null && mHiddenPlayer.isTpEnabled();
    }

    @Override
    public boolean isScanning() {
        if (mService == null) return false;
        try {
            return mService.IsScan();
        } catch (Exception e) {
            return false;
        }
    }

    // === DX/Local (via AIDL) ===

    @Override
    public void toggleDxLocal() {
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            Boolean cur = mHiddenPlayer.isLocal();
            boolean targetLocal = (cur == null) ? !isDxLocal() : !cur;
            if (mHiddenPlayer.setLocal(targetLocal)) {
                if (mCallback != null) mCallback.onDxLocalChanged(targetLocal);
                return;
            }
        }
        if (mService == null) return;
        try {
            mService.onLocDxEvent();
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException en toggleDxLocal");
        }
    }

    @Override
    public boolean isDxLocal() {
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            Boolean v = mHiddenPlayer.isLocal();
            if (v != null) return v;
            return false;
        }
        if (mService == null) return false;
        try {
            return mService.IsDxLocal();
        } catch (android.os.DeadObjectException e) {
            mService = null;
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    // === Presets ===

    @Override
    public void gotoPreset(int index) {
        // Consigna: presets los gestiona MainActivity/PresetManager (software), no el OEM.
        Log.d(TAG, "gotoPreset(" + index + "): no-op (usa MainActivity presets)");
    }

    @Override
    public void nextFavorite() {
        // Consigna: favoritos/presets son software; no navegar lista interna del chip/OEM.
        Log.d(TAG, "nextFavorite(): no-op (usa MainActivity presets)");
    }

    @Override
    public void prevFavorite() {
        Log.d(TAG, "prevFavorite(): no-op (usa MainActivity presets)");
    }

    // === Callbacks ===

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    /** Para {@link com.example.openradiofm.service.RadioMediaService}: componer con el callback de MainActivity. */
    public RadioEngineCallback getCallback() {
        return mCallback;
    }

    /**
     * Traduce callbacks del servicio AIDL (HCN nativo) a RadioEngineCallback.
     */
    private void handleAidlCallback(int code, String data) {
        if (mCallback == null) return;

        switch (code) {
            case 100:
                try { mCallback.onFrequencyChanged(Integer.parseInt(data)); }
                catch (NumberFormatException ignored) {}
                break;
            case 101:
                try {
                    int b = Integer.parseInt(data);
                    int clamped = Math.max(0, Math.min(4, b));
                    mLastUiBand = clamped;
                    mCallback.onBandChanged(clamped);
                } catch (NumberFormatException ignored) {}
                break;
            case 102:
                mCallback.onStereoChanged("1".equals(data));
                break;
            case 103:
                mCallback.onRdsName(data);
                break;
            case 104:
                mCallback.onRdsText(data);
                break;
            case 105:
                mCallback.onRdsPty(data);
                break;
            case 106:
                mCallback.onDxLocalChanged("1".equals(data));
                break;
            default:
                mCallback.onRawEvent(code, data);
                break;
        }
    }

    private void syncUiBandFromHiddenPlayer(String reason) {
        try {
            if (mContext == null || mHiddenPlayer == null) return;
            String ui = mHiddenPlayer.getUiBandName();
            int idx = parseUiBandToIndex(ui);
            if (idx < 0 || idx > 4) return;
            if (idx != mLastUiBand) {
                mLastUiBand = idx;
                try {
                    mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putInt("pref_last_band", idx)
                            .apply();
                } catch (Exception ignored) {}
                if (mCallback != null) {
                    mCallback.onBandChanged(idx);
                }
                Log.i(TAG, "Sync UI band desde HiddenRadioPlayer (" + reason + "): " + ui + " -> " + idx);
            }
        } catch (Exception ignored) {}
    }

    private static int parseUiBandToIndex(String uiBandName) {
        if (uiBandName == null) return -1;
        String s = uiBandName.trim().toUpperCase(java.util.Locale.US);
        // Variantes observadas: "FM1", "FM2", "FM3", "AM1" o con guión "FM-1"
        if (s.startsWith("FM")) {
            if (s.contains("3")) return 2;
            if (s.contains("2")) return 1;
            return 0;
        }
        if (s.startsWith("AM")) return 3;
        return -1;
    }

    /**
     * Acceso al servicio AIDL para compatibilidad temporal con Engineering Dialog.
     */
    public IRadioServiceAPI getService() {
        return mService;
    }

    /**
     * Acceso al HiddenRadioPlayer para Engineering Dialog.
     */
    public HiddenRadioPlayer getHiddenPlayer() {
        return mHiddenPlayer;
    }
}
