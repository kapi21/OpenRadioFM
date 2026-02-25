package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.hcn.autoradio.IRadioCallBack;
import com.hcn.autoradio.IRadioServiceAPI;

/**
 * V5.0: Motor K706 — Wrapper sobre K706RadioManager.
 *
 * K706RadioManager ya implementa IRadioServiceAPI (AIDL).
 * Esta clase lo envuelve en la interfaz RadioEngine unificada
 * y traduce los callbacks del AIDL a RadioEngineCallback.
 */
public class K706Engine implements RadioEngine {

    private static final String TAG = "K706Engine";

    private K706RadioManager mManager;
    private RadioEngineCallback mCallback;
    // Track AF/TA state internally (K706RadioManager fields are private)
    private boolean mAfEnabled = false;
    private boolean mTaEnabled = false;

    @Override
    public boolean init(Context context) {
        try {
            mManager = new K706RadioManager(context);

            // Registrar callback AIDL que traduce a RadioEngineCallback
            mManager.registerRadioCallback(new IRadioCallBack.Stub() {
                @Override
                public void onEvent(int code, String data) {
                    if (mCallback == null) return;
                    handleCallback(code, data);
                }
            });

            Log.d(TAG, "K706Engine inicializado correctamente.");
            
            // V11.5: Solicitar AudioFocus para que Android nos notifique
            // de llamadas telefónicas y otras interrupciones de audio
            mManager.requestPlayAudio();
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando K706Engine: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void release() {
        if (mManager != null) {
            try { mManager.closeDevice(); } catch (Exception ignored) {}
        }
        mCallback = null;
        mManager = null;
    }

    @Override
    public String getEngineName() {
        return "K706";
    }

    // === Tuning ===

    @Override
    public void tune(int freqKhz) {
        if (mManager == null) return;
        try { mManager.gotoFreq(freqKhz); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public int getCurrentFreq() {
        if (mManager == null) return 0;
        try { return mManager.getCurrentFreq(); } catch (RemoteException e) { return 0; }
    }

    @Override
    public int getCurrentBand() {
        if (mManager == null) return 0;
        try { return mManager.getCurrentBand(); } catch (RemoteException e) { return 0; }
    }

    @Override
    public void seekUp() {
        if (mManager == null) return;
        try { mManager.onSeekUpEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void seekDown() {
        if (mManager == null) return;
        try { mManager.onSeekDownEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void stepUp() {
        if (mManager == null) return;
        try { mManager.onManualUpEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void stepDown() {
        if (mManager == null) return;
        try { mManager.onManualDownEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void scan() {
        if (mManager == null) return;
        try { mManager.onScanEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void stopScan() {
        if (mManager == null) return;
        try { mManager.onPSEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void bandCycle() {
        if (mManager == null) return;
        try { mManager.onBandEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    // === Audio ===

    @Override
    public boolean isStereo() {
        if (mManager == null) return false;
        try { return mManager.IsStereo(); } catch (RemoteException e) { return false; }
    }

    @Override
    public void setStereo(boolean enable) {
        // K706 no expone setStereo directo en AIDL
        Log.d(TAG, "setStereo no disponible en K706");
    }

    @Override
    public void setMute(boolean mute) {
        if (mManager == null) return;
        try { mManager.setMute(mute); } catch (Exception e) { Log.e(TAG, "setMute error", e); }
    }

    @Override
    public void openEq(Context context) {
        try {
            android.content.Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.qf.soundeffect");
            if (launchIntent != null) {
                launchIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            } else {
                // Fallback: abrir ajustes de sonido de Android
                android.content.Intent intent = new android.content.Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.Settings$SoundSettingsActivity");
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "No se pudo abrir el EQ", e);
        }
    }

    @Override
    public boolean requestPlayAudio() {
        if (mManager == null) return false;
        try { return mManager.requestPlayAudio(); } catch (RemoteException e) { return false; }
    }

    // === RDS ===

    @Override
    public void toggleRdsFeature(int type) {
        if (mManager == null) return;
        try { mManager.toggleRdsFeature(type); } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public boolean isAfEnabled() {
        return mAfEnabled;
    }

    @Override
    public boolean isTaEnabled() {
        return mTaEnabled;
    }

    // === DX/Local ===

    @Override
    public void toggleDxLocal() {
        if (mManager == null) return;
        try { mManager.onLocDxEvent(); } catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public boolean isDxLocal() {
        if (mManager == null) return false;
        try { return mManager.IsDxLocal(); } catch (RemoteException e) { return false; }
    }

    // === Presets ===

    @Override
    public void gotoPreset(int index) {
        if (mManager == null) return;
        try { mManager.gotoFreqIndex(index); } catch (RemoteException e) { e.printStackTrace(); }
    }

    // === Callbacks ===

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    /**
     * Traduce los códigos de callback del AIDL K706 a RadioEngineCallback.
     * Los códigos vienen de IRadioCallBack.RadioCallback(code, data).
     */
    private void handleCallback(int code, String data) {
        if (mCallback == null) return;

        switch (code) {
            case 100: // Frequency changed
                try {
                    mCallback.onFrequencyChanged(Integer.parseInt(data));
                } catch (NumberFormatException ignored) {}
                break;
            case 101: // Band changed
                try {
                    mCallback.onBandChanged(Integer.parseInt(data));
                } catch (NumberFormatException ignored) {}
                break;
            case 102: // Stereo changed
                mCallback.onStereoChanged("1".equals(data));
                break;
            case 103: // RDS PS (name)
                mCallback.onRdsName(data);
                break;
            case 104: // RDS RT (text)
                mCallback.onRdsText(data);
                break;
            case 105: // RDS PTY
                mCallback.onRdsPty(data);
                break;
            case 106: // DX/Local changed
                mCallback.onDxLocalChanged("1".equals(data));
                break;
            case 107: // PI Code Detected
                mCallback.onRdsPi(data);
                break;
            case 111: // AF/TP status indicators
                if (data != null && data.startsWith("AF:")) {
                    mAfEnabled = data.contains("1");
                    mCallback.onRdsAfTaStatus(mAfEnabled, mTaEnabled);
                }
                break;
            case 112: // TA switch status from B3
                if (data != null && data.contains(":1")) {
                    mTaEnabled = true;
                    mCallback.onRdsAfTaStatus(mAfEnabled, mTaEnabled);
                } else if (data != null && data.contains(":0")) {
                    mTaEnabled = false;
                    mCallback.onRdsAfTaStatus(mAfEnabled, mTaEnabled);
                }
                break;
            default:
                mCallback.onRawEvent(code, data);
                break;
        }
    }

    /**
     * Acceso directo al K706RadioManager para Engineering Dialog.
     */
    public K706RadioManager getManager() {
        return mManager;
    }

    /**
     * Acceso como IRadioServiceAPI para compatibilidad temporal.
     */
    public IRadioServiceAPI asAidl() {
        return mManager;
    }
}
