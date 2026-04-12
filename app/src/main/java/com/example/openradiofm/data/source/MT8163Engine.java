package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.openradiofm.engine.MT8163TunerAdapter;

/**
 * V22.3: Motor modular para MT8163 (HCN) — Delegando sintonía al MT8163TunerAdapter.
 * Corrección de firmas de métodos y completado de interfaz RadioEngine.
 */
public class MT8163Engine implements RadioEngine {
    private static final String TAG = "MT8163Engine";

    private Context mContext;
    private MT8163TunerAdapter mAdapter;
    private HiddenRadioPlayer mHiddenPlayer;
    private RadioEngineCallback mCallback;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0;
    private boolean mIsMute = false;
    private boolean mOnlineStreamingActive = false;

    @Override
    public boolean init(Context context) {
        mContext = context;
        mAdapter = MT8163TunerAdapter.getInstance(context);
        mAdapter.addCallback(mAdapterCallback);
        mAdapter.connect();

        mHiddenPlayer = new HiddenRadioPlayer(new HiddenRadioPlayer.Listener() {
            @Override public void onRdsText(String text) { if (mCallback != null) mCallback.onRdsText(text); }
            @Override public void onRdsName(String name) { if (mCallback != null) mCallback.onRdsName(name); }
            @Override public void onRdsPty(String pty) { if (mCallback != null) mCallback.onRdsPty(pty); }
            @Override public void onRawEvent(int code, Object info, String str) { if (mCallback != null) mCallback.onRawEvent(code, str); }
            @Override public void onRdsAfTaStatus(boolean af, boolean ta, boolean tp) { if (mCallback != null) mCallback.onRdsStatus(af, ta, tp); }
        });
        mHiddenPlayer.init();
        return true;
    }

    private final MT8163TunerAdapter.AdapterCallback mAdapterCallback = new MT8163TunerAdapter.AdapterCallback() {
        @Override
        public void onEvent(int code, String data) {
            handleAidlCallback(code, data);
        }

        @Override
        public void onServiceConnected() {
            Log.d(TAG, "MT8163 Engine: Adapter conectado.");
            mAdapter.requestPlayAudio();
            if (mHiddenPlayer != null) {
                mHiddenPlayer.setMute(false);
            }
        }

        @Override
        public void onServiceDisconnected() {}
    };

    private void handleAidlCallback(int code, String data) {
        if (mCallback == null) return;
        mMainHandler.post(() -> {
            try {
                if (code == 1) { // Freq
                    mCurrentFreq = Integer.parseInt(data);
                    mCallback.onFrequencyChanged(mCurrentFreq);
                } else if (code == 2) { // Band
                    mCurrentBand = Integer.parseInt(data);
                    mCallback.onBandChanged(mCurrentBand);
                }
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void release() {
        if (mAdapter != null) {
            mAdapter.removeCallback(mAdapterCallback);
        }
        if (mHiddenPlayer != null) {
            mHiddenPlayer.release();
        }
        mCallback = null;
    }

    @Override public void tune(int freqKhz) { mAdapter.gotoFreq(freqKhz); mCurrentFreq = freqKhz; }
    @Override public void seekUp() { mAdapter.seekUp(); }
    @Override public void seekDown() { mAdapter.seekDown(); }
    @Override public void setMute(boolean mute) { mAdapter.requestPlayAudio(); mIsMute = mute; if (mHiddenPlayer!=null) mHiddenPlayer.setMute(mute); }
    @Override public void setBand(int band) { mCurrentBand = band; }
    @Override public void bandCycle() { mAdapter.bandCycle(); }

    @Override public String getEngineName() { return "MT8163 (Modular)"; }
    @Override public int getCurrentFreq() { return mCurrentFreq; }
    @Override public int getCurrentBand() { return mCurrentBand; }
    @Override public void stopScan() { mAdapter.stopScan(); }

    @Override public boolean requestPlayAudio() { return mAdapter.requestPlayAudio(); }
    @Override public void enforceAudioRecovery() { requestPlayAudio(); tune(mCurrentFreq); }
    @Override public void switchToAndroidAudio() { setMute(true); }
    @Override public void switchToFmAudio() { setMute(false); requestPlayAudio(); }
    @Override public boolean isOnlineStreamingActive() { return mOnlineStreamingActive; }
    @Override public void setOnlineStreamingActive(boolean active) { 
        mOnlineStreamingActive = active;
        if (active) switchToAndroidAudio();
    }

    @Override public void toggleRdsFeature(int type) { mAdapter.toggleRdsFeature(type); }
    @Override public boolean isAfEnabled() { return false; }
    @Override public boolean isTaEnabled() { return false; }
    @Override public boolean isTpEnabled() { return false; }
    @Override public boolean isScanning() { return false; }
    @Override public void toggleDxLocal() {}
    @Override public boolean isDxLocal() { return false; }
    @Override public void setCallback(RadioEngineCallback cb) { this.mCallback = cb; }

    @Override public void stepUp() { mAdapter.stepUp(); }
    @Override public void stepDown() { mAdapter.stepDown(); }
    @Override public void scan() { mAdapter.scan(); }
    @Override public boolean isStereo() { return mAdapter.isStereo(); }
    @Override public void setStereo(boolean enable) {}
    @Override public void openEq(Context context) {}
    @Override public void closeDevice() {}
    @Override public void gotoPreset(int index) {}
    @Override public void nextFavorite() {}
    @Override public void prevFavorite() {}

    /** Mantenemos la compatibilidad con RadioServiceController por si acaso */
    public void updateService(Object service) {
        if (mAdapter != null) mAdapter.connect();
    }
    
    /** Flags de compatibilidad legado */
    public static boolean isHcnServiceBindBlockedAfterStreamEnd() { return false; }
    public static void setBlockHcnServiceBindAfterStreamEnd(boolean block) {}
    public static final long HCN_BIND_BLOCK_AFTER_STREAM_MS = 2000L;
}
