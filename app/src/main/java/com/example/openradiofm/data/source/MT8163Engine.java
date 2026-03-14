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

import android.os.Handler;
import android.os.Looper;

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

    // V16.2: Polling mechanism for continuous frequency updates during seek/scan
    private Handler mPollingHandler = new Handler(Looper.getMainLooper());
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
                
                // V18.6: Si el servicio fue matado por el sistema (ej. abriendo Youtube)
                // y teníamos una emisora sintonizada, la restauramos automáticamente
                if (mLastPolledFreq > 0 && !mIsOnlineStreamingActive) {
                    Log.d(TAG, "Restaurando frecuencia tras reconexión: " + mLastPolledFreq);
                    mService.gotoFreq(mLastPolledFreq);
                    mService.requestPlayAudio();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error registrando callback AIDL", e);
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
        if ((!mBound || mService == null) && !mExternalService) {
            Log.w(TAG, "Reconectando servicio HCN (Estaba muerto)...");
            try {
                Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
                intent.setPackage("com.hcn.autoradio");
                mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                Log.e(TAG, "Error intentando reconectar", e);
            }
        }
    }

    @Override
    public boolean init(Context context) {
        mContext = context;

        // 1. Conectar con servicio HCN del sistema (tune, seek, band)
        if (!mExternalService) {
            try {
                Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
                intent.setPackage("com.hcn.autoradio");
                context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                Log.d(TAG, "Binding al servicio HCN via Action...");
            } catch (Exception e) {
                Log.e(TAG, "No se pudo conectar al servicio HCN: " + e.getMessage());
            }
        } else {
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

        return true; 
    }

    @Override
    public void release() {
        release(false); // Por defecto, liberación completa y muteo
    }

    @Override
    public void release(boolean persist) {
        if (persist) {
            Log.d(TAG, "release(persist=true): Recreación detectada. Manteniendo hardware activo.");
            return;
        }

        Log.d(TAG, "release(persist=false): Soltando recursos MT8163 y silenciando");
        switchToAndroidAudio(); // Asegurar liberación de audio al salir
        
        if (mPollingRunnable != null) {
            mPollingHandler.removeCallbacks(mPollingRunnable);
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
    public void setOnlineStreamingActive(boolean active) {
        this.mIsOnlineStreamingActive = active;
        Log.d(TAG, "setOnlineStreamingActive: " + active);
        if (active) {
            switchToAndroidAudio();
        }
    }

    @Override
    public void closeDevice() {
        release();
    }

    @Override
    public String getEngineName() {
        return "MT8163";
    }

    // === Tuning (via AIDL service) ===

    @Override
    public void tune(int freqKhz) {
        if (mService == null) return;
        resetRdsUI();
        try { 
            mService.gotoFreq(freqKhz); 
        } catch (android.os.DeadObjectException e) {
            Log.e(TAG, "DeadObjectException en tune: " + e.getMessage());
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en tune: " + e.getMessage()); 
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

    @Override
    public int getCurrentFreq() {
        if (mIsOnlineStreamingActive && mLastPolledFreq > 0) return mLastPolledFreq;
        if (mService == null) return 0;
        try { 
            return mService.getCurrentFreq(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
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
            mService = null;
            return 0;
        } catch (RemoteException e) { 
            return 0; 
        }
    }
    
    @Override
    public void seekUp() {
        if (mService == null) return;
        resetRdsUI();
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current + 100);
        try { 
            mService.onSeekDownEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en seekUp"); 
        }
        startFreqPolling();
    }

    @Override
    public void seekDown() {
        if (mService == null) return;
        resetRdsUI();
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) mCallback.onFrequencyChanged(current - 100);
        try { 
            mService.onSeekUpEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en seekDown"); 
        }
        startFreqPolling();
    }

    @Override
    public void stepUp() {
        if (mService == null) return;
        resetRdsUI();
        // V16.2: Notificar frecuencia inmediata para sensación de movimiento
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) {
            mCallback.onFrequencyChanged(current + 100);
        }
        try { 
            mService.onManualUpEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en stepUp"); 
        }
        startFreqPolling();
    }

    @Override
    public void stepDown() {
        if (mService == null) return;
        resetRdsUI();
        // V16.2: Notificar frecuencia inmediata para sensación de movimiento
        int current = getCurrentFreq();
        if (current > 0 && mCallback != null) {
            mCallback.onFrequencyChanged(current - 100);
        }
        try { 
            mService.onManualDownEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en stepDown"); 
        }
        startFreqPolling();
    }

    @Override
    public void scan() {
        if (mService == null) return;
        resetRdsUI();
        try { 
            mService.onScanEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en scan"); 
        }
        startFreqPolling();
    }

    @Override
    public void stopScan() {
        if (mService == null) return;
        try { 
            mService.onPSEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en stopScan"); 
        }
    }

    @Override
    public void bandCycle() {
        if (mService == null) return;
        try { 
            mService.onBandEvent(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en bandCycle"); 
        }
    }

    // === Audio (via HiddenRadioPlayer) ===

    @Override
    public boolean isStereo() {
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
        
        // V18.6: Fallback para MT8163 enviando par\u00e1metros directos al AudioManager
        if (mAudioManager != null) {
            try {
                mAudioManager.setParameters("fm_radio_on=1;fm_mute=" + (mute ? "1" : "0"));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    mAudioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                            mute ? android.media.AudioManager.ADJUST_MUTE : android.media.AudioManager.ADJUST_UNMUTE, 0);
                } else {
                    mAudioManager.setStreamMute(android.media.AudioManager.STREAM_MUSIC, mute);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setMute (AudiorManager fallback)", e);
            }
        }
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
            Log.e(TAG, "Error injecting MCU key: " + e.getMessage());
        }
    }

    @Override
    public boolean requestPlayAudio() {
        if (mService == null) return false;
        try { 
            return mService.requestPlayAudio(); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
            return false;
        } catch (RemoteException e) { 
            return false; 
        }
    }

    @Override
    public void enforceAudioRecovery() {
        reconnectIfNeeded();
        // En MT8163 basta con volver a pedir el canal de audio al servicio AIDL
        requestPlayAudio();
        if (mHiddenPlayer != null) {
            mHiddenPlayer.setMute(false);
        }
    }

    @Override
    public void switchToAndroidAudio() {
        Log.d(TAG, "switchToAndroidAudio (MT8163) - Liberando canal para sistema");
        setMute(true);
        
        // V18.5: Ejecutar parámetros de audio en un hilo ligero para no congelar la UI si el hardware tarda
        new Thread(() -> {
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
        }).start();
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
        // V12.2: Implementación mínima para satisfacer la interfaz
        return false;
    }

    // === DX/Local (via AIDL) ===

    @Override
    public void toggleDxLocal() {
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
        if (mService == null) return;
        try { 
            mService.gotoFreqIndex(index); 
        } catch (android.os.DeadObjectException e) {
            mService = null;
        } catch (RemoteException e) { 
            Log.e(TAG, "RemoteException en gotoPreset"); 
        }
    }

    @Override
    public void nextFavorite() {
        if (mHiddenPlayer != null) mHiddenPlayer.next();
    }

    @Override
    public void prevFavorite() {
        if (mHiddenPlayer != null) mHiddenPlayer.prev();
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
