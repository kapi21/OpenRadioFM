package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;

import com.example.openradiofm.util.AppIoExecutor;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * V5.0: Motor MT8163 — Combina IRadioServiceAPI (servicio nativo del coche)
 * con HiddenRadioPlayer (API oculta del chip para RDS AF/TA).
 *
 * Sintonización, seek, band → van por IRadioServiceAPI (servicio HCN del sistema)
 * RDS (AF, TA, stereo, mute) → van por HiddenRadioPlayer (reflexión RadioPlayer)
 */
public class MT8163Engine implements RadioEngine {

    private static final String TAG = "MT8163Engine";

    private Context mContext;
    private IRadioServiceAPI mService;
    private HiddenRadioPlayer mHiddenPlayer;
    private RadioEngineCallback mCallback;
    private boolean mBound = false;
    private boolean mExternalService = false;
    /** true: no usar FMPlugService (com.hcn.autoradio); controlar vía RadioPlayer directo. */
    private boolean mPreferDirectRadioPlayer = false;
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

    /**
     * Antes se reactivaba {@code requestPlayAudio AIDL} a los 4s tras streaming; al reconectar el bind
     * HCN minutos después, eso dispara {@code forceStopPackage} en SourceService. Ya no se programa.
     */
    private final Runnable mClearDeferAidlRunnable = new Runnable() {
        @Override
        public void run() {
            mDeferAidlRequestPlayAudioAfterStream = false;
            Log.d(TAG, "requestPlayAudio AIDL re-permitido (solo tras streaming online)");
        }
    };

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
                    applyFmHardwareRouteOnly();
                    if (mHiddenPlayer != null) {
                        mHiddenPlayer.setMute(false);
                    }
                    mPendingAudioRecovery = false;
                    return;
                }
                mService.requestPlayAudio();
                if (mHiddenPlayer != null) {
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

    private final Runnable mReconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContext == null || mExternalService) return;
            if (mPreferDirectRadioPlayer) {
                Log.i(TAG, "Reconexión HCN omitida (pref_mt8163_mcu_direct=true)");
                return;
            }
            if (mBound && mService != null) return;
            Log.w(TAG, "Reconectando servicio HCN (Estaba muerto)...");
            try {
                Intent wake = new Intent("com.hcn.autoradio.FMRADIO_START");
                wake.setPackage("com.hcn.autoradio");
                mContext.sendBroadcast(wake);
                Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
                intent.setPackage("com.hcn.autoradio");
                mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                Log.e(TAG, "Error intentando reconectar", e);
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
    
    // OEM safety: evitar mutear todo el dispositivo (STREAM_MUSIC) salvo compatibilidad explícita
    private boolean mAllowGlobalStreamMute = false;

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
                
                // V18.6: Si el servicio fue matado por el sistema (ej. abriendo Youtube)
                // y teníamos una emisora sintonizada, la restauramos automáticamente
                if ((mPendingAudioRecovery || mLastPolledFreq > 0) && !mIsOnlineStreamingActive) {
                    Log.i(TAG, "Restaurando estado tras conexión (Re-init RDS y Audio). Freq: " + mLastPolledFreq);
                    if (mLastPolledFreq > 0) mService.gotoFreq(mLastPolledFreq);
                    if (mDeferAidlRequestPlayAudioAfterStream) {
                        Log.i(TAG, "onServiceConnected: omitiendo requestPlayAudio AIDL (handoff streaming->FM)");
                        applyFmHardwareRouteOnly();
                    } else {
                        mService.requestPlayAudio();
                    }
                    
                    // V21.3: Re-inicializar canal RDS para evitar referencias a proceso muerto
                    if (mHiddenPlayer != null) {
                        mHiddenPlayer.init();
                        mHiddenPlayer.setMute(false);
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
        mContext = context;

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
                Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
                intent.setPackage("com.hcn.autoradio");
                context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                Log.d(TAG, "Binding al servicio HCN via Action...");
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

        // Preferencia de compatibilidad: algunas ROMs antiguas dependían de mutear STREAM_MUSIC.
        // Por defecto OFF para no silenciar Spotify/BT/Android Auto.
        try {
            mAllowGlobalStreamMute = context
                    .getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_mt8163_global_stream_mute", false);
        } catch (Exception ignored) {}

        ensurePollingThread();
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
        switchToAndroidAudio(); // Asegurar liberación de audio al salir
        
        if (mPollingRunnable != null && mPollingHandler != null) {
            mPollingHandler.removeCallbacks(mPollingRunnable);
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
            ensurePollingThread();
            if (mPollingHandler != null) {
                mPollingHandler.removeCallbacks(mClearDeferAidlRunnable);
            }
            mDeferAidlRequestPlayAudioAfterStream = false;
            clearHcnBindReconnectDefer();
            setBlockHcnServiceBindAfterStreamEnd(false);
            switchToAndroidAudio();
            return;
        }
        if (!active) {
            mDeferAidlRequestPlayAudioAfterStream = true;
            ensurePollingThread();
            if (mPollingHandler != null) {
                mPollingHandler.removeCallbacks(mClearDeferAidlRunnable);
            }
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
        // En MT8163 legado no hay un comando AIDL para setBand directo (solo onBandEvent)
        // Se actualiza localmente para mantener coherencia en la UI
        Log.d(TAG, "setBand (legado): " + band);
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

    private void startFreqPolling() {
        ensurePollingThread();
        if (mPollingRunnable != null) {
            mPollingHandler.removeCallbacks(mPollingRunnable);
        }
        mPollingTicks = 0;
        mPollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (mService == null) return;
                int f = getCurrentFreq();
                if (f > 0 && f != mLastPolledFreq) {
                    mLastPolledFreq = f;
                    if (mCallback != null) mCallback.onFrequencyChanged(f);
                }
                
                mPollingTicks++;
                // Poll for 4 seconds (40 * 100ms)
                if (mPollingTicks < 40) {
                    mPollingHandler.postDelayed(this, 100);
                }
            }
        };
        mPollingHandler.postDelayed(mPollingRunnable, 100);
    }

    private void startDirectFreqPolling() {
        ensurePollingThread();
        if (mPollingHandler == null) return;
        if (mPollingRunnable != null) {
            mPollingHandler.removeCallbacks(mPollingRunnable);
        }
        mPollingTicks = 0;
        mPollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mPreferDirectRadioPlayer || mHiddenPlayer == null) return;
                Integer f = mHiddenPlayer.getCurrentFreqKhz();
                if (f != null && f > 0 && f != mLastPolledFreq) {
                    mLastPolledFreq = f;
                    if (mCallback != null) mCallback.onFrequencyChanged(f);
                }
                // ventana corta (4s) igual que AIDL
                mPollingTicks++;
                if (mPollingTicks < 40) {
                    mPollingHandler.postDelayed(this, 100);
                }
            }
        };
        mPollingHandler.postDelayed(mPollingRunnable, 100);
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
        if (mIsOnlineStreamingActive) return 0;
        if (mService == null) return 0;
        try { 
            return mService.getCurrentBand(); 
        } catch (android.os.DeadObjectException e) {
            handleDeadService("getCurrentBand", e);
            return 0;
        } catch (RemoteException e) { 
            return 0; 
        }
    }
    
    @Override
    public void seekUp() {
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
            // MT8163: en AIDL, seekUp debe llamar onSeekUpEvent (estaba invertido).
            svc.onSeekUpEvent();
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
            // MT8163: en AIDL, seekDown debe llamar onSeekDownEvent (estaba invertido).
            svc.onSeekDownEvent();
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
            // Notificar inmediata para “sensación” + polling para corrección.
            int current = getCurrentFreq();
            if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current + 100);
            if (mHiddenPlayer.stepUp()) {
                startDirectFreqPolling();
                return;
            }
        }
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        // V16.2: Notificar frecuencia inmediata para sensación de movimiento
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) {
            mCallback.onFrequencyChanged(current + 100);
        }
        try {
            svc.onManualUpEvent();
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
            if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current - 100);
            if (mHiddenPlayer.stepDown()) {
                startDirectFreqPolling();
                return;
            }
        }
        IRadioServiceAPI svc = mService;
        if (svc == null) return;
        // V16.2: Notificar frecuencia inmediata para sensación de movimiento
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) {
            mCallback.onFrequencyChanged(current - 100);
        }
        try {
            svc.onManualDownEvent();
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
        if (mPreferDirectRadioPlayer && mHiddenPlayer != null) {
            // MCU-direct (HiddenRadioPlayer): algunos firmwares MT8163 no tienen FM3.
            // Ciclo tolerante: FM1 -> FM2 -> (FM3 si existe) -> FM1. Si falla, caer a FM1 o AM1 si existe.
            String cur = mHiddenPlayer.getUiBandName();
            String[] candidates;
            if ("FM2".equals(cur)) {
                candidates = new String[] { "FM3", "FM1", "AM1", "FM2" };
            } else if ("FM3".equals(cur)) {
                candidates = new String[] { "FM1", "FM2", "AM1", "FM3" };
            } else if ("AM1".equals(cur) || "AM".equals(cur)) {
                candidates = new String[] { "FM1", "FM2", "FM3", "AM1" };
            } else {
                // FM1 o null -> FM2 (comportamiento histórico)
                candidates = new String[] { "FM2", "FM3", "FM1", "AM1" };
            }
            Integer freq = mHiddenPlayer.getCurrentFreqKhz();
            for (String next : candidates) {
                if (mHiddenPlayer.setUiBandKeepFreq(next, freq)) {
                    if ("FM1".equals(next)) mLastUiBand = 0;
                    else if ("FM2".equals(next)) mLastUiBand = 1;
                    else if ("FM3".equals(next)) mLastUiBand = 2;
                    else if ("AM1".equals(next) || "AM".equals(next)) mLastUiBand = 3;
                    if (mCallback != null) mCallback.onBandChanged(mLastUiBand);
                    startDirectFreqPolling();
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
        if (mHiddenPlayer != null) mHiddenPlayer.setMute(mute);
        
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
            Log.w(TAG, "enforceAudioRecovery: Servicio nulo, marcando recuperación pendiente.");
            mPendingAudioRecovery = true;
            reconnectIfNeeded();
            return;
        }

        if (mDeferAidlRequestPlayAudioAfterStream) {
            Log.i(TAG, "enforceAudioRecovery: omitiendo requestPlayAudio AIDL (handoff streaming->FM OEM)");
            try {
                applyFmHardwareRouteOnly();
            } catch (Exception ignored) {}
            if (mHiddenPlayer != null) {
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
        
        if (mHiddenPlayer != null) {
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
                try { mCallback.onBandChanged(Integer.parseInt(data)); }
                catch (NumberFormatException ignored) {}
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
