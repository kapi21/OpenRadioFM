package com.example.openradiofm.ui.main;

/**
 * Estado inmutable de la sesión de radio compartido entre Activity, servicios y UI.
 */
public final class RadioSessionState {

    public final int freqKhz;
    public final int band;
    public final boolean isPlaying;
    public final boolean isMuted;
    /** ACC (contacto) ON/OFF. Null si desconocido/no soportado. */
    public final Boolean accOn;
    public final String rdsName;
    public final String rdsText;
    public final String pty;
    public final String pi;

    public RadioSessionState(
            int freqKhz,
            int band,
            boolean isPlaying,
            boolean isMuted,
            Boolean accOn,
            String rdsName,
            String rdsText,
            String pty,
            String pi
    ) {
        this.freqKhz = freqKhz;
        this.band = band;
        this.isPlaying = isPlaying;
        this.isMuted = isMuted;
        this.accOn = accOn;
        this.rdsName = rdsName != null ? rdsName : "";
        this.rdsText = rdsText != null ? rdsText : "";
        this.pty = pty != null ? pty : "";
        this.pi = pi != null ? pi : "";
    }

    public static RadioSessionState initial() {
        return new RadioSessionState(
                -1,
                0,
                false,
                false,
                null,
                "",
                "",
                "",
                ""
        );
    }
}

