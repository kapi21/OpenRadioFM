package com.example.openradiofm.data.source;

import android.content.Context;

/**
 * V5.0: Capa de abstracción de hardware para motores de radio.
 *
 * Cada motor (K706, MT8163, etc.) implementa esta interfaz.
 * MainActivity usa una sola referencia RadioEngine sin saber
 * qué hardware hay debajo.
 */
public interface RadioEngine {

    // === Lifecycle ===
    boolean init(Context context);
    void release();
    String getEngineName();

    // === Tuning ===
    void tune(int freqKhz);
    int getCurrentFreq();
    int getCurrentBand();
    void seekUp();
    void seekDown();
    void stepUp();
    void stepDown();
    void scan();
    void stopScan();
    void bandCycle();

    // === Audio ===
    boolean isStereo();
    void setStereo(boolean enable);
    void setMute(boolean mute);
    void openEq(Context context);
    boolean requestPlayAudio();

    // === RDS ===
    void toggleRdsFeature(int type); // 0=RDS global, 1=AF, 2=TA
    boolean isAfEnabled();
    boolean isTaEnabled();
    boolean isTpEnabled();

    // === DX/Local ===
    void toggleDxLocal();
    boolean isDxLocal();

    // === Presets ===
    void gotoPreset(int index);

    // === Callbacks ===
    void setCallback(RadioEngineCallback cb);
}
