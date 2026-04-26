package com.example.openradiofm.data.source;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
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
    /** Evita que {@link #enforceAudioRecovery()} vuelva a abrir la FM vía {@code OpenRadioCh()} durante streaming. */
    private boolean mOnlineStreamingActive = false;
    
    // V21.1: Polling fuera del hilo UI para evitar jank
    private HandlerThread mPollThread;
    private Handler mHandler;
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
                mManager.forceUnmute(); // V20.0: Asegurar sonido al arrancar
                ensurePollThread();
                startRdsPolling();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "init failed", e);
        }
        return false;
    }

    private void ensurePollThread() {
        if (mHandler != null) return;
        mPollThread = new HandlerThread("MTK8259_8667-RdsPoll");
        mPollThread.start();
        mHandler = new Handler(mPollThread.getLooper());
    }

    private void startRdsPolling() {
        ensurePollThread();
        mHandler.removeCallbacks(mRdsRunnable);
        mHandler.postDelayed(mRdsRunnable, 1000);
    }

    private void stopRdsPolling() {
        if (mHandler != null) mHandler.removeCallbacks(mRdsRunnable);
    }

    @Override
    public void release() {
        release(false); // Por defecto, liberación completa
    }

    @Override
    public void release(boolean persist) {
        if (persist) {
            Log.d(TAG, "release(persist=true): Recreación detectada. Manteniendo MTK vivo pero pausando polling.");
            stopRdsPolling(); // V18.6.4: Evitar que el hilo intente actualizar vistas destruidas
            return;
        }

        Log.d(TAG, "release(persist=false): Soltando recursos MTK8259/8667");
        stopRdsPolling();
        if (mPollThread != null) {
            try {
                mPollThread.quitSafely();
            } catch (Exception ignored) {}
            mPollThread = null;
            mHandler = null;
        }
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
    public void setBand(int band) {
        if (mManager != null) {
            try {
                mManager.setBand(band);
                if (mCallback != null) mCallback.onBandChanged(band);
            } catch (Exception ignored) {}
        }
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
        if (mOnlineStreamingActive) {
            Log.d(TAG, "enforceAudioRecovery: omitido (Radio Online activo; evitar OpenRadioCh + stream a la vez)");
            return;
        }
        if (mManager != null) {
            mManager.switchMixerToFmAudio();
        }
    }

    @Override
    public void switchToAndroidAudio() {
        if (mManager != null) {
            mManager.switchMixerToAndroidAudio();
        }
    }

    @Override
    public void switchToFmAudio() {
        enforceAudioRecovery();
    }

    @Override
    public boolean isOnlineStreamingActive() {
        return mOnlineStreamingActive;
    }

    @Override
    public void setOnlineStreamingActive(boolean active) {
        mOnlineStreamingActive = active;
        Log.d(TAG, "setOnlineStreamingActive: " + active);
        if (active && mManager != null) {
            mManager.switchMixerToAndroidAudio();
        }
    }

    @Override
    public void toggleRdsFeature(int type) {
        if (mManager == null) return;
        
        switch (type) {
            case 1: // AF
                mManager.toggleAfSafe();
                break;
            case 2: // TA
                mManager.toggleTaSafe();
                break;
            case 99: // Código interno para AM/FM toggle (existente)
                try {
                    mManager.toggleAmFm();
                    if (mCallback != null) mCallback.onBandChanged(getCurrentBand());
                } catch (Exception ignored) {}
                break;
        }
    }

    @Override
    public boolean isAfEnabled() { 
        return mManager != null && mManager.isAfActiveSafe(); 
    }

    @Override
    public boolean isTaEnabled() { 
        return mManager != null && mManager.isTaActiveSafe(); 
    }

    @Override
    public boolean isTpEnabled() { 
        return mManager != null && mManager.isTpActiveSafe(); 
    }

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
        // V18.6.4: Al reconectar el callback (ej. tras recreación de layout), reanudamos el polling
        if (cb != null && mManager != null && mManager.isConnected()) {
            startRdsPolling();
        }
    }

    private void pollRdsAndDispatch() {
        MTK8259_8667RadioManager mTK8259_8667RadioManager = this.mManager;
        if (mTK8259_8667RadioManager == null || this.mCallback == null) {
            return;
        }

        // 1. RDS PS Name
        String psNameSafe = mTK8259_8667RadioManager.getPsNameSafe();
        if (psNameSafe != null) {
            psNameSafe = psNameSafe.trim();
        }
        if (psNameSafe != null && !psNameSafe.isEmpty() && !psNameSafe.equals(this.mLastPs)) {
            this.mLastPs = psNameSafe;
            this.mCallback.onRdsName(psNameSafe);
        }

        // 2. PTY (Program Type)
        // V18.9: El análisis confirma que GetCategory() devuelve el PTY.
        String ptySafe = mTK8259_8667RadioManager.getPtySafe();
        if (ptySafe != null && !ptySafe.isEmpty()) {
            this.mCallback.onRdsPty(ptySafe);
        }

        // 3. RT (Radio Text)
        // V18.9: El análisis confirma que GetPtyStr() devuelve el Radio Text (RT).
        String rtSafe = mTK8259_8667RadioManager.getRtSafe();
        if (rtSafe != null && !rtSafe.isEmpty() && !rtSafe.equals(this.mLastRt)) {
            this.mLastRt = rtSafe;
            this.mCallback.onRdsText(rtSafe);
        }

        // 4. RDS Status (AF, TA, TP bits) - V18.9
        this.mCallback.onRdsStatus(
                mTK8259_8667RadioManager.isAfActiveSafe(),
                mTK8259_8667RadioManager.isTaActiveSafe(),
                mTK8259_8667RadioManager.isTpActiveSafe()
        );
    }
}
