package com.example.openradiofm.data.source;

/**
 * V5.1: Callbacks del motor de radio hacia la UI.
 *
 * Todas las implementaciones de RadioEngine notifican cambios
 * a través de esta interfaz. MainActivity la implementa para
 * actualizar frecuencia, RDS, iconos, etc.
 */
public interface RadioEngineCallback {

    /** Frecuencia cambiada (en kHz, ej: 92200 = 92.2 MHz) */
    void onFrequencyChanged(int freqKhz);

    /** Banda cambiada (0=FM1, 1=FM2, 2=FM3, 3=AM1, etc.) */
    void onBandChanged(int band);

    /** Estado estéreo cambiado */
    void onStereoChanged(boolean stereo);

    /** RDS: Nombre de emisora (PS) */
    void onRdsName(String name);

    /** RDS: Texto informativo (RT) */
    void onRdsText(String text);

    /** RDS: Tipo de programa (PTY) */
    void onRdsPty(String pty);

    /** RDS: Estado AF/TA actualizado */
    void onRdsAfTaStatus(boolean afEnabled, boolean taEnabled);

    /** RDS: Program Identification (PI) Code */
    void onRdsPi(String piCode);

    /** DX/Local cambiado */
    void onDxLocalChanged(boolean isLocal);

    /** Evento crudo para Engineering Mode */
    void onRawEvent(int code, String data);
}
