package com.example.openradiofm.data.source;

import java.util.ArrayList;
import java.util.List;

/**
 * Reenvía eventos del {@link RadioEngine} a varios receptores (p. ej. MainActivity + RadioMediaService)
 * cuando un único motor compartido (QS6/K706/MT8163) necesita actualizar UI y sesión de medios.
 *
 * <p>Usar {@link #combine} en lugar del constructor para no anidar composites: al recrear la
 * Activity (o re-entregar el motor al servicio) se sustituye el receptor anterior del mismo rol
 * en vez de envolverlo, evitando leaks de Activities muertas y eventos duplicados.</p>
 */
public final class CompositeRadioEngineCallback implements RadioEngineCallback {

    private final RadioEngineCallback[] callbacks;

    public CompositeRadioEngineCallback(RadioEngineCallback first, RadioEngineCallback second) {
        this.callbacks = new RadioEngineCallback[] { first, second };
    }

    private CompositeRadioEngineCallback(List<RadioEngineCallback> list) {
        this.callbacks = list.toArray(new RadioEngineCallback[0]);
    }

    /**
     * Combina {@code added} con el callback ya registrado en el motor sin anidar.
     * Aplana composites existentes y descarta cualquier receptor de la misma clase que
     * {@code added} (instancia anterior del mismo rol: Activity recreada / servicio re-enganchado).
     *
     * @param addedFirst true si {@code added} debe recibir los eventos antes que el resto
     *                   (UI primero, como hasta ahora).
     */
    public static RadioEngineCallback combine(RadioEngineCallback existing,
                                              RadioEngineCallback added,
                                              boolean addedFirst) {
        if (added == null) return existing;
        List<RadioEngineCallback> others = new ArrayList<>();
        flattenInto(existing, others);
        for (int i = others.size() - 1; i >= 0; i--) {
            RadioEngineCallback cb = others.get(i);
            if (cb == null || cb == added || cb.getClass() == added.getClass()) {
                others.remove(i);
            }
        }
        if (others.isEmpty()) return added;
        if (addedFirst) others.add(0, added); else others.add(added);
        return new CompositeRadioEngineCallback(others);
    }

    private static void flattenInto(RadioEngineCallback cb, List<RadioEngineCallback> out) {
        if (cb == null) return;
        if (cb instanceof CompositeRadioEngineCallback) {
            for (RadioEngineCallback inner : ((CompositeRadioEngineCallback) cb).callbacks) {
                flattenInto(inner, out);
            }
        } else if (!out.contains(cb)) {
            out.add(cb);
        }
    }

    @Override
    public void onFrequencyChanged(int freqKhz) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onFrequencyChanged(freqKhz);
    }

    @Override
    public void onBandChanged(int band) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onBandChanged(band);
    }

    @Override
    public void onStereoChanged(boolean stereo) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onStereoChanged(stereo);
    }

    @Override
    public void onRdsName(String name) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onRdsName(name);
    }

    @Override
    public void onRdsText(String text) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onRdsText(text);
    }

    @Override
    public void onRdsPty(String pty) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onRdsPty(pty);
    }

    @Override
    public void onRdsStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onRdsStatus(afEnabled, taEnabled, tpEnabled);
    }

    @Override
    public void onRdsPi(String piCode) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onRdsPi(piCode);
    }

    @Override
    public void onDxLocalChanged(boolean isLocal) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onDxLocalChanged(isLocal);
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onScanStatusChanged(scanning);
    }

    @Override
    public void onRawEvent(int code, String data) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onRawEvent(code, data);
    }

    @Override
    public void onSignalUpdate(int rssi, int snr) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onSignalUpdate(rssi, snr);
    }

    @Override
    public void onHwAutomationEvent(int type, boolean active) {
        for (RadioEngineCallback cb : callbacks) if (cb != null) cb.onHwAutomationEvent(type, active);
    }
}
