package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ts.main.common.ITsCommon;
import com.ts.tsspeechlib.radio.ITsSpeechRadio;

/**
 * Engine para unidades Topway/TS (MTK8259/8667).
 * 
 * Implementa la interfaz RadioEngine de OpenRadioFM V5.0.
 */
public class MTK8259_8667Engine implements RadioEngine {

    private static final String TAG = "MTK8259_8667Engine";
    
    private final ITsCommon mTsCommon;
    private final ITsSpeechRadio mTsSpeechRadio;
    
    private MTK8259_8667RadioManager mManager;
    private RadioEngineCallback mCallback;
    
    private String mLastPs = null;
    private String mLastRt = null;
    
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRdsRunnable = new Runnable() {
        @Override
        public void run() {
            pollRdsAndDispatch();
            mHandler.postDelayed(this, 2000); // Polling cada 2 segundos
        }
    };

    public MTK8259_8667Engine(ITsCommon tsCommon, ITsSpeechRadio tsSpeechRadio) {
        this.mTsCommon = tsCommon;
        this.mTsSpeechRadio = tsSpeechRadio;
    }

    @Override
    public boolean init(Context context) {
        try {
            mManager = new MTK8259_8667RadioManager(context, mTsCommon, mTsSpeechRadio);
            mManager.openDevice();
            
            // Iniciar el bucle de RDS si el manager está conectado
            if (mManager.isConnected()) {
                startRdsPolling();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "init failed", e);
        }
        return false;
    }

    private void startRdsPolling() {
        mHandler.removeCallbacks(mRdsRunnable);
        mHandler.postDelayed(mRdsRunnable, 1000);
    }

    private void stopRdsPolling() {
        mHandler.removeCallbacks(mRdsRunnable);
    }

    @Override
    public void release() {
        stopRdsPolling();
        if (mManager != null) {
            try {
                mManager.closeDevice();
            } catch (Exception ignored) {}
        }
        mManager = null;
        mCallback = null;
    }

    @Override
    public void closeDevice() {
        release();
    }

    @Override
    public String getEngineName() {
        return "MTK8259_8667";
    }

    @Override
    public void tune(int freqKhz) {
        if (mManager == null) return;
        try {
            mManager.gotoFreq(freqKhz);
            if (mCallback != null) mCallback.onFrequencyChanged(freqKhz);
            // Limpiar RDS al sintonizar
            mLastPs = null;
            mLastRt = null;
        } catch (Exception e) {
            Log.e(TAG, "tune failed", e);
        }
    }

    @Override
    public int getCurrentFreq() {
        if (mManager == null) return 87500;
        try {
            return mManager.getCurrentFreq();
        } catch (Exception e) {
            return 87500;
        }
    }

    @Override
    public int getCurrentBand() {
        if (mManager == null) return 0;
        try {
            return mManager.getCurrentBand();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void seekUp() {
        if (mManager != null) {
            try { mManager.onSeekUpEvent(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void seekDown() {
        if (mManager != null) {
            try { mManager.onSeekDownEvent(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void stepUp() {
        if (mManager != null) {
            try { mManager.onManualUpEvent(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void stepDown() {
        if (mManager != null) {
            try { mManager.onManualDownEvent(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void scan() {
        seekUp();
    }

    @Override
    public void stopScan() {
        // No hay API nativa para parar el escaneo en TS
    }

    @Override
    public void bandCycle() {
        if (mManager != null) {
            try {
                mManager.onBandCycle();
                if (mCallback != null) mCallback.onBandChanged(getCurrentBand());
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean isScanning() {
        return false;
    }

    @Override
    public boolean isStereo() {
        // Usar GetRadioSTState sugerido por Csaba a través del manager
        return mManager != null && mManager.isStereo();
    }

    @Override
    public void setStereo(boolean enable) {}

    @Override
    public void setMute(boolean mute) {
        if (mManager != null) mManager.setMute(mute);
    }

    @Override
    public void openEq(Context context) {
        if (mManager != null) mManager.openEq();
    }

    @Override
    public boolean requestPlayAudio() {
        return true;
    }

    @Override
    public void enforceAudioRecovery() {
        if (mManager != null) {
            try { mManager.getRawService().OpenRadioCh(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void switchToAndroidAudio() {
        if (mManager != null) {
            try { mManager.getRawService().CloseRadioCh(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void switchToFmAudio() {
        enforceAudioRecovery();
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {}

    @Override
    public void toggleRdsFeature(int type) {
        // En Topway es un intercambio entre AM/FM si detectamos el nuevo tipo de toggle
        if (type == 99 && mManager != null) { // Código interno para AM/FM toggle
            try {
                mManager.toggleAmFm();
                if (mCallback != null) mCallback.onBandChanged(getCurrentBand());
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean isAfEnabled() { return false; }

    @Override
    public boolean isTaEnabled() { return false; }

    @Override
    public boolean isTpEnabled() { return false; }

    @Override
    public void toggleDxLocal() {}

    @Override
    public boolean isDxLocal() { return false; }

    @Override
    public void gotoPreset(int index) {
        if (mManager != null) {
            try { mManager.getRawService().onSelectedFreq(index); } catch (Exception ignored) {}
        }
    }

    @Override
    public void nextFavorite() {
        stepUp();
    }

    @Override
    public void prevFavorite() {
        stepDown();
    }

    @Override
    public void setCallback(RadioEngineCallback cb) {
        this.mCallback = cb;
    }

    private void pollRdsAndDispatch() {
        if (mManager == null || mCallback == null) return;

        // RDS Safety Check (Csaba suggestion): Query PS once or handle NULL
        // If the MainUI doesn't support the GDUCK methods, they catch errors and return null.
        
        // PS Name
        String ps = mManager.getPsNameSafe();
        if (ps != null) ps = ps.trim();
        
        // Si ps es nulo, significa que el hardware/MainUI no responde o no tiene el MOD.
        // Csaba sugiere no activar RDS si es NULL.
        if (ps == null) {
            return;
        }

        if (!ps.isEmpty() && !ps.equals(mLastPs)) {
            mLastPs = ps;
            mCallback.onRdsName(ps);
        }

        // PTY / Category Text (Csaba suggestion: use GetCategory())
        String cat = mManager.getCategorySafe();
        if (cat != null) cat = cat.trim();
        
        if (cat != null && !cat.isEmpty()) {
            mCallback.onRdsPty(cat);
        }
        
        // RT Tradicional
        String rt = mManager.getPtyStrSafe();
        if (rt != null) rt = rt.trim();
        if (rt != null && !rt.isEmpty() && !rt.equals(mLastRt)) {
            mLastRt = rt;
            mCallback.onRdsText(rt);
        }
    }
}
