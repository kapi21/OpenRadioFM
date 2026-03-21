package com.example.openradiofm.data.source;

/**
 * Reenvía eventos del {@link RadioEngine} a dos receptores (p. ej. MainActivity + RadioMediaService)
 * cuando un único {@link QS6Engine} compartido necesita actualizar UI y sesión de medios.
 */
public final class CompositeRadioEngineCallback implements RadioEngineCallback {

    private final RadioEngineCallback first;
    private final RadioEngineCallback second;

    public CompositeRadioEngineCallback(RadioEngineCallback first, RadioEngineCallback second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void onFrequencyChanged(int freqKhz) {
        if (first != null) first.onFrequencyChanged(freqKhz);
        if (second != null) second.onFrequencyChanged(freqKhz);
    }

    @Override
    public void onBandChanged(int band) {
        if (first != null) first.onBandChanged(band);
        if (second != null) second.onBandChanged(band);
    }

    @Override
    public void onStereoChanged(boolean stereo) {
        if (first != null) first.onStereoChanged(stereo);
        if (second != null) second.onStereoChanged(stereo);
    }

    @Override
    public void onRdsName(String name) {
        if (first != null) first.onRdsName(name);
        if (second != null) second.onRdsName(name);
    }

    @Override
    public void onRdsText(String text) {
        if (first != null) first.onRdsText(text);
        if (second != null) second.onRdsText(text);
    }

    @Override
    public void onRdsPty(String pty) {
        if (first != null) first.onRdsPty(pty);
        if (second != null) second.onRdsPty(pty);
    }

    @Override
    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        if (first != null) first.onRdsStatus(afEnabled, taEnabled, tpEnabled);
        if (second != null) second.onRdsStatus(afEnabled, taEnabled, tpEnabled);
    }

    @Override
    public void onRdsPi(String piCode) {
        if (first != null) first.onRdsPi(piCode);
        if (second != null) second.onRdsPi(piCode);
    }

    @Override
    public void onDxLocalChanged(boolean isLocal) {
        if (first != null) first.onDxLocalChanged(isLocal);
        if (second != null) second.onDxLocalChanged(isLocal);
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        if (first != null) first.onScanStatusChanged(scanning);
        if (second != null) second.onScanStatusChanged(scanning);
    }

    @Override
    public void onRawEvent(int code, String data) {
        if (first != null) first.onRawEvent(code, data);
        if (second != null) second.onRawEvent(code, data);
    }

    @Override
    public void onSignalUpdate(int rssi, int snr) {
        if (first != null) first.onSignalUpdate(rssi, snr);
        if (second != null) second.onSignalUpdate(rssi, snr);
    }
}
