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
            } catch (RemoteException e) {
                Log.e(TAG, "Error registrando callback AIDL", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.w(TAG, "Servicio HCN desconectado");
        }
    };

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

        return true; // Incluso si RDS falla, el servicio AIDL puede funcionar
    }

    @Override
    public void release() {
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
        try { mService.gotoFreq(freqKhz); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public int getCurrentFreq() {
        if (mService == null) return 0;
        try { return mService.getCurrentFreq(); } catch (RemoteException e) { return 0; }
    }

    @Override
    public int getCurrentBand() {
        if (mService == null) return 0;
        try { return mService.getCurrentBand(); } catch (RemoteException e) { return 0; }
    }

    @Override
    public void seekUp() {
        if (mService == null) return;
        try { mService.onSeekDownEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void seekDown() {
        if (mService == null) return;
        try { mService.onSeekUpEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void stepUp() {
        if (mService == null) return;
        try { mService.onManualUpEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void stepDown() {
        if (mService == null) return;
        try { mService.onManualDownEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void scan() {
        if (mService == null) return;
        try { mService.onScanEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void stopScan() {
        if (mService == null) return;
        try { mService.onPSEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void bandCycle() {
        if (mService == null) return;
        try { mService.onBandEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    // === Audio (via HiddenRadioPlayer) ===

    @Override
    public boolean isStereo() {
        if (mService == null) return false;
        try { return mService.IsStereo(); } catch (RemoteException e) { return false; }
    }

    @Override
    public void setStereo(boolean enable) {
        if (mHiddenPlayer != null) mHiddenPlayer.setStereo(enable);
    }

    @Override
    public void setMute(boolean mute) {
        if (mHiddenPlayer != null) mHiddenPlayer.setMute(mute);
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
        try { return mService.requestPlayAudio(); } catch (RemoteException e) { return false; }
    }

    @Override
    public void enforceAudioRecovery() {
        // En MT8163 basta con volver a pedir el canal de audio al servicio AIDL
        requestPlayAudio();
        if (mHiddenPlayer != null) {
            mHiddenPlayer.setMute(false);
        }
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
        try { mService.onLocDxEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public boolean isDxLocal() {
        if (mService == null) return false;
        try { return mService.IsDxLocal(); } catch (RemoteException e) { return false; }
    }

    // === Presets ===

    @Override
    public void gotoPreset(int index) {
        if (mService == null) return;
        try { mService.gotoFreqIndex(index); } catch (RemoteException e) { e.printStackTrace(); }
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
