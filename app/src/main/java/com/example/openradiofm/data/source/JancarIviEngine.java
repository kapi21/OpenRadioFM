package com.example.openradiofm.data.source;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.jancar.services.radio.IRadio;
import com.jancar.services.radio.IRadioCallback;

/**
 * Motor Jancar IVI (p. ej. MTK8227L / head units con {@code com.jancar.services}).
 * Usa el AIDL {@link IRadio} del servicio OEM — sin cargar {@code libfmjni.so} desde la app
 * (mute vía {@link IRadio#mute} / {@link IRadio#unMute}).
 * <p>
 * AIDL alineado con ivi-radio / ivi-services descompilados (orden de transacciones Binder).
 */
public class JancarIviEngine implements RadioEngine {

    private static final String TAG = "JancarIviEngine";

    public static final String PACKAGE_IVI_SERVICES = "com.jancar.services";
    private static final String ACTION_MAIN = "com.jancar.services.action.main";
    private static final String ACTION_RADIO = "com.jancar.services.action.radio";

    // 8227L OEM FMService (com.jancar.radio) control intents
    private static final String PACKAGE_FACTORY_RADIO = "com.jancar.radio";
    private static final String CLASS_FACTORY_FM_SERVICE = "com.jancar.radio.FmService";
    private static final String EXTRA_FM_FREQ_VALID = "fmradio.freq.valid"; // expects: freqKhz/10 (e.g. 87500 -> 8750)
    private static final String ACTION_FM_SEEK_NEXT = "fmradio.seek.next";
    private static final String ACTION_FM_SEEK_PREVIOUS = "fmradio.seek.previous";
    private static final String ACTION_FM_TURN_OFF = "fmradio.turnoff";

    /** Jancar {@code IVIRadio.Band}: FM=1, AM=0 */
    private static final int J_BAND_FM = 1;
    private static final int J_BAND_AM = 0;

    private Context mContext;
    private RadioEngineCallback mCallback;
    private IRadio mRadio;
    private boolean mBound;
    private boolean mOpened;
    private boolean mMute;
    private boolean mScanning;
    private boolean mOnlineStreamingActive;
    private boolean mIsAfEnabled;
    private boolean mIsTaEnabled;
    private boolean mIsTpEnabled;
    private boolean mDxLocal;
    private final android.os.Handler mMainHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    private final IRadioCallback mRadioCallback = new IRadioCallback.Stub() {
        @Override
        public void onSetFreq(int freq) {
            dispatchFreq(freq);
        }

        @Override
        public void onFreqChanged(int freq) {
            dispatchFreq(freq);
        }

        @Override
        public void onScanStart(boolean scanAll) {
            mScanning = true;
            postCb(() -> {
                if (mCallback != null) mCallback.onScanStatusChanged(true);
            });
        }

        @Override
        public void onScanEnd(boolean scanAll) {
            mScanning = false;
            postCb(() -> {
                if (mCallback != null) mCallback.onScanStatusChanged(false);
            });
        }

        @Override
        public void onScanAbort(boolean scanAll) {
            mScanning = false;
            postCb(() -> {
                if (mCallback != null) mCallback.onScanStatusChanged(false);
            });
        }

        @Override
        public void onSignalUpdate(int freq, int strength) {
            postCb(() -> {
                if (mCallback != null) mCallback.onSignalUpdate(strength, 0);
            });
        }

        @Override
        public void onStereo(int freq, boolean stereo) {
            postCb(() -> {
                if (mCallback != null) mCallback.onStereoChanged(stereo);
            });
        }

        @Override
        public void onRdsPsChanged(int pi, int freq, String text) {
            postCb(() -> {
                if (mCallback != null) {
                    mCallback.onRdsPi(pi > 0 ? String.format(java.util.Locale.US, "%04X", pi) : "");
                    mCallback.onRdsName(text != null ? text : "");
                }
            });
        }

        @Override
        public void onRdsRtChanged(int pi, int freq, String text) {
            postCb(() -> {
                if (mCallback != null) {
                    mCallback.onRdsText(text != null ? text : "");
                }
            });
        }

        @Override
        public void onRdsMaskChanged(int pi, int freq, int pty, int tp, int ta) {
            mIsTpEnabled = tp == 1;
            mIsTaEnabled = ta == 1;
            postCb(() -> {
                if (mCallback != null) {
                    mCallback.onRdsPty(pty >= 0 ? String.valueOf(pty) : "");
                    mCallback.onRdsStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                }
            });
        }

        @Override
        public void onPowerOn() {
        }

        @Override
        public void onPowerOff() {
        }

        @Override
        public void onScanResult(int freq, int signal) {
            dispatchFreq(freq);
        }

        @Override
        public void suspend() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void play() {
        }

        @Override
        public void playPause() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void next() {
        }

        @Override
        public void prev() {
        }

        @Override
        public void quitApp() {
        }

        @Override
        public void select(int index) {
        }

        @Override
        public void setFavour(boolean favour) {
        }

        @Override
        public void onTuneRotate(boolean clockwise) {
        }

        @Override
        public void scanUp() {
        }

        @Override
        public void scanDown() {
        }

        @Override
        public void scanAll() {
        }

        @Override
        public void setNumberkey(int key) {
        }

        @Override
        public void onCMDServiceToApp(int cmd, int[] ints, float[] floats, String[] strings) {
        }
    };

    private void dispatchFreq(int freq) {
        if (freq <= 0) return;
        postCb(() -> {
            if (mCallback != null) mCallback.onFrequencyChanged(freq);
        });
    }

    private void postCb(Runnable r) {
        mMainHandler.post(r);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRadio = IRadio.Stub.asInterface(service);
            Log.d(TAG, "IRadio connected: " + name);
            tryOpenRadio();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "IRadio disconnected: " + name);
            mRadio = null;
            mOpened = false;
        }
    };

    /** True si el paquete del stack IVI está instalado (detección automática). */
    public static boolean isJancarIviAvailable(Context context) {
        if (context == null) return false;
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_IVI_SERVICES, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean init(Context context) {
        mContext = context != null ? context.getApplicationContext() : null;
        if (mContext == null) return false;
        if (!isJancarIviAvailable(mContext)) {
            Log.w(TAG, "init: " + PACKAGE_IVI_SERVICES + " not installed");
            return false;
        }
        try {
            Intent boot = new Intent(ACTION_MAIN);
            boot.setPackage(PACKAGE_IVI_SERVICES);
            try {
                mContext.startService(boot);
            } catch (Exception e) {
                Log.d(TAG, "startService(MAIN_ACTION): " + e.getMessage());
            }
            Intent bind = new Intent(ACTION_RADIO);
            bind.setPackage(PACKAGE_IVI_SERVICES);
            mBound = mContext.bindService(bind, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService(RADIO_ACTION) -> " + mBound);
            return mBound;
        } catch (Exception e) {
            Log.e(TAG, "init failed", e);
            return false;
        }
    }

    private void tryOpenRadio() {
        if (mRadio == null || mContext == null || mOpened) return;
        try {
            mRadio.open(mRadioCallback, mContext.getPackageName());
            mOpened = true;
            syncStateFromService();
            if (!mMute) {
                mRadio.unMute();
            }
            Log.d(TAG, "IRadio.open OK");
        } catch (RemoteException e) {
            Log.e(TAG, "IRadio.open failed", e);
        }
    }

    /**
     * 8227L quirk: en algunas ROMs, IRadio.setFreq() no cambia el tuner real.
     * La app OEM usa com.jancar.radio.FmService, que interpreta EXTRA_FM_FREQ_VALID como freqKhz/10.
     */
    private void sendFactoryFmServiceTune(int freqKhz) {
        if (mContext == null) return;
        if (freqKhz <= 0) return;
        try {
            int normalized = normalizeFreqFor8227L(freqKhz);
            int freqValid = normalized / 10; // 87500->8750, 101700->10170
            Intent i = new Intent();
            i.setComponent(new ComponentName(PACKAGE_FACTORY_RADIO, CLASS_FACTORY_FM_SERVICE));
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            i.putExtra(EXTRA_FM_FREQ_VALID, freqValid);
            mContext.startService(i);
            Log.d(TAG, "FmService tune: freqKhz=" + freqKhz + " normalized=" + normalized + " fmradio.freq.valid=" + freqValid);
        } catch (Exception e) {
            Log.w(TAG, "FmService tune failed", e);
        }
    }

    private void sendFactoryFmServiceAction(String action) {
        if (mContext == null) return;
        if (action == null || action.trim().isEmpty()) return;
        try {
            Intent i = new Intent(action);
            i.setComponent(new ComponentName(PACKAGE_FACTORY_RADIO, CLASS_FACTORY_FM_SERVICE));
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.startService(i);
            Log.d(TAG, "FmService action sent: " + action);
        } catch (Exception e) {
            Log.w(TAG, "FmService action failed: " + action, e);
        }
    }

    /**
     * Normaliza a pasos típicos de FM para evitar frecuencias "raras" (p.ej. 87.55)
     * que el stack OEM puede ignorar.
     *
     * OpenRadioFM usa freqKhz en escala ×1000 (87.50MHz -> 87500).
     */
    private static int normalizeFreqFor8227L(int freqKhz) {
        // FM range heuristic: > 20000 => FM in kHz×1 (87.50MHz => 87500)
        if (freqKhz > 20000) {
            // Redondeo a 100 kHz (100 en unidades OpenRadioFM)
            return ((freqKhz + 50) / 100) * 100;
        }
        // AM: dejar tal cual (kHz)
        return freqKhz;
    }

    private void syncStateFromService() {
        if (mRadio == null) return;
        try {
            int f = mRadio.getFreq();
            if (f > 0) {
                dispatchFreq(f);
            }
            int jb = mRadio.getBand();
            postCb(() -> {
                if (mCallback != null) mCallback.onBandChanged(jancarBandToUi(jb));
            });
        } catch (RemoteException e) {
            Log.w(TAG, "syncStateFromService", e);
        }
    }

    private static int jancarBandToUi(int jancarBand) {
        return jancarBand == J_BAND_FM ? 0 : 3;
    }

    @Override
    public void release() {
        release(false);
    }

    @Override
    public void release(boolean isChangingConfigurations) {
        try {
            if (mRadio != null && mOpened) {
                try {
                    mRadio.close();
                } catch (RemoteException ignored) {
                }
            }
        } finally {
            mOpened = false;
            mRadio = null;
        }
        // Best-effort: apagar el chip OEM si lo levantamos por FMService
        sendFactoryFmServiceAction(ACTION_FM_TURN_OFF);
        if (mContext != null && mBound) {
            try {
                mContext.unbindService(mConnection);
            } catch (Exception ignored) {
            }
        }
        mBound = false;
        mContext = null;
    }

    @Override
    public void closeDevice() {
        release();
    }

    @Override
    public String getEngineName() {
        return "JANCAR_IVI";
    }

    @Override
    public void tune(int freqKhz) {
        // 8227L: empujar también al FMService OEM (control real del tuner en algunas ROMs)
        sendFactoryFmServiceTune(freqKhz);
        if (mRadio == null) return;
        try {
            mRadio.setFreq(freqKhz);
        } catch (RemoteException e) {
            Log.w(TAG, "setFreq", e);
        }
    }

    @Override
    public void setBand(int band) {
        if (mRadio == null) return;
        try {
            int jBand = (band >= 3) ? J_BAND_AM : J_BAND_FM;
            mRadio.setBand(jBand);
        } catch (RemoteException e) {
            Log.w(TAG, "setBand", e);
        }
    }

    @Override
    public int getCurrentFreq() {
        if (mRadio == null) return 0;
        try {
            return mRadio.getFreq();
        } catch (RemoteException e) {
            return 0;
        }
    }

    @Override
    public int getCurrentBand() {
        if (mRadio == null) return 0;
        try {
            return jancarBandToUi(mRadio.getBand());
        } catch (RemoteException e) {
            return 0;
        }
    }

    @Override
    public void seekUp() {
        // OEM FmService path (más fiable en 8227L)
        sendFactoryFmServiceAction(ACTION_FM_SEEK_NEXT);
        if (mRadio == null) return;
        try {
            mRadio.scanUp(1);
        } catch (RemoteException e) {
            Log.w(TAG, "scanUp", e);
        }
    }

    @Override
    public void seekDown() {
        // OEM FmService path (más fiable en 8227L)
        sendFactoryFmServiceAction(ACTION_FM_SEEK_PREVIOUS);
        if (mRadio == null) return;
        try {
            mRadio.scanDown(1);
        } catch (RemoteException e) {
            Log.w(TAG, "scanDown", e);
        }
    }

    @Override
    public void stepUp() {
        if (mRadio == null) return;
        try {
            mRadio.step(1);
        } catch (RemoteException e) {
            Log.w(TAG, "stepUp", e);
        }
    }

    @Override
    public void stepDown() {
        if (mRadio == null) return;
        try {
            mRadio.step(-1);
        } catch (RemoteException e) {
            Log.w(TAG, "stepDown", e);
        }
    }

    @Override
    public void scan() {
        if (mRadio == null) return;
        try {
            mRadio.scanAll();
        } catch (RemoteException e) {
            Log.w(TAG, "scanAll", e);
        }
    }

    @Override
    public void stopScan() {
        if (mRadio == null) return;
        try {
            mRadio.scanStop();
        } catch (RemoteException e) {
            Log.w(TAG, "scanStop", e);
        }
        mScanning = false;
    }

    @Override
    public void bandCycle() {
        if (mRadio == null) return;
        try {
            int cur = mRadio.getBand();
            int next = (cur == J_BAND_FM) ? J_BAND_AM : J_BAND_FM;
            mRadio.setBand(next);
            postCb(() -> {
                if (mCallback != null) mCallback.onBandChanged(jancarBandToUi(next));
            });
        } catch (RemoteException e) {
            Log.w(TAG, "bandCycle", e);
        }
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    @Override
    public boolean isStereo() {
        if (mRadio == null) return true;
        try {
            return mRadio.isStereo();
        } catch (RemoteException e) {
            return true;
        }
    }

    @Override
    public void setStereo(boolean enable) {
        if (mRadio == null) return;
        try {
            mRadio.setStereo(enable);
        } catch (RemoteException e) {
            Log.w(TAG, "setStereo", e);
        }
    }

    @Override
    public void setMute(boolean mute) {
        mMute = mute;
        if (mRadio == null) return;
        try {
            if (mute) {
                mRadio.mute();
            } else {
                mRadio.unMute();
            }
        } catch (RemoteException e) {
            Log.w(TAG, "setMute", e);
        }
    }

    @Override
    public void openEq(Context context) {
        try {
            Intent i = mContext.getPackageManager().getLaunchIntentForPackage("com.jancar.radio");
            if (i != null && context != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        } catch (Exception e) {
            Log.w(TAG, "openEq", e);
        }
    }

    @Override
    public boolean requestPlayAudio() {
        setMute(false);
        return mRadio != null;
    }

    @Override
    public void enforceAudioRecovery() {
        setMute(false);
    }

    @Override
    public void switchToAndroidAudio() {
    }

    @Override
    public void switchToFmAudio() {
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        mOnlineStreamingActive = active;
    }

    @Override
    public boolean isOnlineStreamingActive() {
        return mOnlineStreamingActive;
    }

    @Override
    public void toggleRdsFeature(int type) {
        if (mRadio == null) return;
        try {
            if (type == 1) {
                mIsAfEnabled = !mIsAfEnabled;
                mRadio.selectRdsAf(mIsAfEnabled);
            } else if (type == 2) {
                mIsTaEnabled = !mIsTaEnabled;
                mRadio.selectRdsTa(mIsTaEnabled);
            } else if (type == 0) {
                mRadio.send(5, new int[]{1}, null, null);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "toggleRdsFeature", e);
        }
    }

    @Override
    public boolean isAfEnabled() {
        return mIsAfEnabled;
    }

    @Override
    public boolean isTaEnabled() {
        return mIsTaEnabled;
    }

    @Override
    public boolean isTpEnabled() {
        return mIsTpEnabled;
    }

    @Override
    public void toggleDxLocal() {
        if (mRadio == null) return;
        try {
            boolean dm = mRadio.getDistanceMode();
            mRadio.setDistanceMode(!dm);
            mDxLocal = mRadio.getDistanceMode();
        } catch (RemoteException e) {
            Log.w(TAG, "toggleDxLocal", e);
        }
        postCb(() -> {
            if (mCallback != null) mCallback.onDxLocalChanged(!mDxLocal);
        });
    }

    @Override
    public boolean isDxLocal() {
        if (mRadio == null) return true;
        try {
            return !mRadio.getDistanceMode();
        } catch (RemoteException e) {
            return true;
        }
    }

    @Override
    public void gotoPreset(int index) {
        Log.d(TAG, "gotoPreset(" + index + ") — no implementado en IVI genérico");
    }

    @Override
    public void nextFavorite() {
    }

    @Override
    public void prevFavorite() {
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        mCallback = cb;
    }
}
