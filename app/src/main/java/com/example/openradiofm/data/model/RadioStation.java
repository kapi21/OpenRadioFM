package com.example.openradiofm.data.model;

public class RadioStation {
    private int freqKHz; // e.g. 96900
    private String name; // e.g. "LOS40"
    private String logoUrl; // e.g. "http://.../los40.png"
    private boolean isFavorite;

    public RadioStation(int freqKHz, String name) {
        this.freqKHz = freqKHz;
        this.name = name;
        this.logoUrl = null;
        this.isFavorite = false;
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

    public String getFreqLabel() {
        return String.format("%.1f FM", freqKHz / 1000.0);
    }
}
