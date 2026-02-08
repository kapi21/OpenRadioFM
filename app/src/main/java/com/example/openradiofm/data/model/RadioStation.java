package com.example.openradiofm.data.model;

public class RadioStation {
    private int freqKHz; // e.g. 96900
    private String name; // e.g. "LOS40"
    private String logoUrl; // e.g. "http://.../los40.png"
    private boolean isFavorite;
    private String pty; // e.g. "POP MUSIC"

    public RadioStation(int freqKHz, String name) {
        this.freqKHz = freqKHz;
        this.name = name;
        this.logoUrl = null;
        this.isFavorite = false;
        this.pty = null;
    }

    public int getFreqKHz() {
        return freqKHz;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getPty() {
        return pty;
    }

    public void setPty(String pty) {
        this.pty = pty;
    }

    public String getFreqLabel() {
        return String.format("%.1f FM", freqKHz / 1000.0);
    }
}
