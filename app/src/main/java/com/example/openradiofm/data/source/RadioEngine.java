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
    
    /**
     * V20.0: Versión extendida de release que permite decidir si liberar recursos 
     * basándose en si la Activity se está recreando por un cambio de configuración 
     * (como un cambio de layout) o se está cerrando definitivamente.
     */
    default void release(boolean isChangingConfigurations) {
        release();
    }
    
    void closeDevice();
    String getEngineName();

    // === Tuning ===
    void tune(int freqKhz);
    void setBand(int band);
    int getCurrentFreq();
    int getCurrentBand();
    void seekUp();
    void seekDown();
    void stepUp();
    void stepDown();
    void scan();
    void stopScan();
    void bandCycle();
    boolean isScanning();

    // === Audio ===
    boolean isStereo();
    void setStereo(boolean enable);
    void setMute(boolean mute);
    void openEq(Context context);
    boolean requestPlayAudio();
    void enforceAudioRecovery(); // Fuerza la recuperación del hardware de audio (ej: canal MCU)
    void switchToAndroidAudio(); // Conmuta el MCU al canal de Android (MPU)
    void switchToFmAudio();      // Conmuta el MCU al canal de Radio FM
    void setOnlineStreamingActive(boolean active); // V18.4: Notificar modo streaming para protecciones de audio

    /**
     * {@code true} mientras haya streaming online activo (buffer o reproducción) para no forzar
     * recuperación de canal FM desde {@link PlaybackManager#setMute(boolean)} u otros caminos.
     */
    default boolean isOnlineStreamingActive() {
        return false;
    }

    /**
     * MT8163 OEM: durante el handoff streaming→FM, {@link PlaybackManager} no debe lanzar
     * {@code RadioMediaService} con {@code ACTION_FORCE_PLAY} al mismo tiempo que el bind a HCN
     * (SourceService puede forzar cierre del proceso).
     */
    default boolean shouldSkipMediaServiceForcePlayOnUnmute() {
        return false;
    }

    /**
     * QS6 (y futuros motores): al pasar la UI a segundo plano, dejar de competir por
     * {@link android.media.AudioManager#AUDIOFOCUS_GAIN} sin forzar cambio de fuente MCU.
     * Por defecto no hace nada.
     */
    default void releaseAudioFocusOnlyForBackground() {}

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
    void nextFavorite();
    void prevFavorite();

    // === Widget / Launcher OEM ===

    /**
     * Notifica al motor la frecuencia/banda/RDS actuales para que pueda enviar los
     * broadcasts OEM específicos de la plataforma (p. ej. {@code com.qf.radio.update_action}
     * en K706 o {@code com.ts.main.radio.update} en Topway).
     * <p>
     * La implementación por defecto es un no-op: los motores que no necesiten broadcasts
     * propios no deben sobrescribir este método.
     * </p>
     *
     * @param context    Context para sendBroadcast.
     * @param freqKhz    Frecuencia en kHz (ej. 87500 = 87.5 MHz).
     * @param band       Índice de banda (0=FM1, 1=FM2, 2=FM3, 3=AM1, 4=AM2).
     * @param presetIdx  Índice del preset memorizado (-1 si no memorizado).
     * @param rdsName    Nombre RDS (PS), puede ser null o vacío.
     */
    default void notifyWidgetUpdate(Context context, int freqKhz, int band,
                                    int presetIdx, String rdsName) {
        // No-op por defecto: los motores no-QF no necesitan broadcasts adicionales.
    }

    // === Callbacks ===
    void setCallback(RadioEngineCallback cb);

    /**
     * V24.8: Ajustar la sensibilidad del sintonizador (umbral de parada en escaneo).
     * @param level Nivel de sensibilidad (0-15).
     */
    default void setTunerSensitivity(int level) {}
}
