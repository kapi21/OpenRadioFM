package com.example.openradiofm.data.source;

import android.content.Context;
import android.util.Log;

import com.example.openradiofm.engine.TopwayTunerAdapter;

/**
 * V22.4: Motor modular para MTK8259/8667 (TopWay).
 * Sincronizado con la API real de TopwayTunerAdapter.
 */
public class MTK8259_8667Engine implements RadioEngine {
    private static final String TAG = "MTK8259Engine";

    private Context mContext;
    private TopwayTunerAdapter mAdapter;
    private RadioEngineCallback mCallback;
    private boolean mOnlineStreamingActive = false;

    private int mCurrentFreq = 87500;
    private int mCurrentBand = 0;

    @Override
    public boolean init(Context context) {
        mContext = context;
        try {
            this.mAdapter = TopwayTunerAdapter.getInstance(context);
            mAdapter.bind();
            
            mAdapter.addCallback(new TopwayTunerAdapter.AdapterCallback() {
                @Override
                public void onServiceConnected() {
                    Log.d(TAG, "Adapter conectado");
                }

                @Override
                public void onServiceDisconnected() {
                    Log.d(TAG, "Adapter desconectado");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "init failed", e);
        }
        return true;
    }

    @Override
    public void release() {
        if (mAdapter != null) mAdapter.unbind();
    }

    @Override public void release(boolean persist) {
        if (!persist) release();
    }

    @Override public void tune(int freqKhz) { 
        if (mAdapter != null) {
            mAdapter.turnBandAndFq(mCurrentBand, freqKhz);
            mCurrentFreq = freqKhz;
        }
    }

    @Override public void setBand(int band) {
        if (mAdapter != null) {
            mCurrentBand = band;
            mAdapter.turnBandAndFq(band, mCurrentFreq);
        }
    }

    @Override public int getCurrentFreq() { return mCurrentFreq; }
    @Override public int getCurrentBand() { return mCurrentBand; }
    @Override public void seekUp() { if (mAdapter != null) mAdapter.seekUp(); }
    @Override public void seekDown() { if (mAdapter != null) mAdapter.seekDown(); }
    @Override public void stepUp() { tune(mCurrentFreq + 50); }
    @Override public void stepDown() { tune(mCurrentFreq - 50); }
    @Override public void scan() {}
    @Override public void stopScan() {}
    @Override public void bandCycle() {
        mCurrentBand = (mCurrentBand + 1) % 5;
        setBand(mCurrentBand);
    }
    @Override public boolean isScanning() { return false; }

    @Override public boolean isStereo() { return mAdapter != null && mAdapter.getRadioSTState(); }
    @Override public void setStereo(boolean enable) {}
    @Override public void setMute(boolean mute) { if (mAdapter != null) mAdapter.mute(); }
    @Override public void openEq(Context context) { if (mAdapter != null) mAdapter.gotoEq(); }
    
    @Override public boolean requestPlayAudio() { 
        if (mAdapter != null) {
            mAdapter.openRadioCh();
            return true;
        }
        return false;
    }

    @Override public void enforceAudioRecovery() { requestPlayAudio(); }
    @Override public void switchToAndroidAudio() { if (mAdapter != null) mAdapter.closeRadioCh(); }
    @Override public void switchToFmAudio() { requestPlayAudio(); }
    
    @Override public void setOnlineStreamingActive(boolean active) { mOnlineStreamingActive = active; }
    @Override public boolean isOnlineStreamingActive() { return mOnlineStreamingActive; }

    @Override public void toggleRdsFeature(int type) { 
        if (mAdapter != null) {
            if (type == 1) mAdapter.rdsAfSwitch();
            else if (type == 2) mAdapter.rdsTaSwitch();
        }
    }
    @Override public boolean isAfEnabled() { return mAdapter != null && mAdapter.rdsAf(); }
    @Override public boolean isTaEnabled() { return mAdapter != null && mAdapter.rdsTa(); }
    @Override public boolean isTpEnabled() { return mAdapter != null && mAdapter.rdsTp(); }

    @Override public void toggleDxLocal() {}
    @Override public boolean isDxLocal() { return false; }

    @Override public void gotoPreset(int index) { if (mAdapter != null) mAdapter.onSelectedFreq(index); }
    @Override public void nextFavorite() {}
    @Override public void prevFavorite() {}
    @Override public void closeDevice() { release(); }

    @Override public void setCallback(RadioEngineCallback cb) { this.mCallback = cb; }
    @Override public String getEngineName() { return "MTK8259 (Modular)"; }
}
