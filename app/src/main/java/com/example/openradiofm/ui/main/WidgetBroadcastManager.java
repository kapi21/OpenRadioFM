package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.openradiofm.data.repository.RadioRepository;
import com.example.openradiofm.data.source.RadioEngine;

/**
 * V22: Gestiona todos los broadcasts OEM (K706, MTK, Topway) y la actualización
 * del widget propio de OpenRadioFM.
 * <p>
 * Extraído de {@link MainActivity} para:
 * <ul>
 *   <li>Eliminar ~80 líneas de lógica de bajo nivel del hilo principal.</li>
 *   <li>Centralizar la guarda anti-spam de broadcasts duplicados.</li>
 *   <li>Facilitar pruebas unitarias del formateo de frecuencia/banda.</li>
 * </ul>
 */
public class WidgetBroadcastManager {

    private static final String TAG = "WidgetBroadcastMgr";
    private static final QfBroadcastGuard QF_GUARD = new QfBroadcastGuard();

    // Constantes de banda (coinciden con MainActivity)
    private static final int BAND_AM1 = 3;
    private static final int BAND_AM2 = 4;

    // --- Guarda anti-spam ---
    private int mLastBroadcastFreq = -1;
    private int mLastBroadcastBand = -1;
    private String mLastBroadcastPs = "";

    /**
     * Resetea las guardas de broadcast para forzar la próxima emisión.
     * Útil tras recrear la Activity o cambios forzados de estado.
     */
    public void invalidate() {
        mLastBroadcastFreq = -1;
        mLastBroadcastBand = -1;
        mLastBroadcastPs = "";
    }

    /**
     * Envía broadcasts a los widgets OEM del launcher (K706/QuickFish, MTK, Topway)
     * y actualiza el widget propio de OpenRadioFM.
     *
     * @param context   Context para sendBroadcast y acceso a recursos.
     * @param freq      Frecuencia en kHz.
     * @param band      Índice de banda (0=FM1, 1=FM2, 2=FM3, 3=AM1, 4=AM2).
     * @param rdsName   Nombre RDS (puede ser null).
     * @param presetIdx Índice del preset memorizado (-1 si no memorizado).
     * @param isStereo  Si la recepción es estéreo.
     * @param repo      Repositorio de datos para obtener el logo (puede ser null).
     * @param engine    Motor activo (puede ser null). Si implementa notifyWidgetUpdate(),
     *                  el broadcast K706 se delega al propio motor en vez de enviarse aquí.
     */
    public void sendUpdate(Context context, int freq, int band,
                           String rdsName, int presetIdx, boolean isStereo,
                           RadioRepository repo, RadioEngine engine) {
        // Guarda anti-spam (evita Binder flood / "Permission Denial")
        if (freq == mLastBroadcastFreq && band == mLastBroadcastBand
                && strEquals(rdsName, mLastBroadcastPs)) {
            return;
        }

        mLastBroadcastFreq = freq;
        mLastBroadcastBand = band;
        mLastBroadcastPs = rdsName;

        try {
            String freqStr = formatFrequency(freq, band);
            int nativeFreqInt = (band == BAND_AM1 || band == BAND_AM2) ? freq : freq / 10;
            String widgetName = sanitizeWidgetName(rdsName);

            // 1. K706 / QuickFish — delegado al engine si está disponible (desacopla com.qf.* de la UI)
            if (engine != null) {
                engine.notifyWidgetUpdate(context, freq, band, presetIdx, rdsName);
            } else {
                // Fallback: sin engine, enviar directamente (compatibilidad)
                broadcastK706(context, freqStr, nativeFreqInt, band, presetIdx, widgetName);
            }

            // 2. Launcher MTK
            broadcastMtk(context, freqStr, band, widgetName, isStereo);

            // 3. Topway / TS
            broadcastTopway(context, nativeFreqInt, band, widgetName);

            // 4. Fuente de sistema
            broadcastSource(context);

            // 5. Widget propio con inyección de logo asíncrona
            updateOwnWidget(context, freq, band, rdsName, repo);

        } catch (Exception ex) {
            Log.e(TAG, "Error updating launcher widgets", ex);
        }
    }

    /**
     * Sobrecarga de compatibilidad para llamadas que aún no pasan el engine.
     * @deprecated Usar {@link #sendUpdate(Context, int, int, String, int, boolean, RadioRepository, RadioEngine)} en su lugar.
     */
    @Deprecated
    public void sendUpdate(Context context, int freq, int band,
                           String rdsName, int presetIdx, boolean isStereo,
                           RadioRepository repo) {
        sendUpdate(context, freq, band, rdsName, presetIdx, isStereo, repo, null);
    }

    // ==================== Broadcasts individuales ====================

    private void broadcastK706(Context ctx, String freqStr, int nativeFreq,
                               int band, int presetIdx, String widgetName) {
        if (QF_GUARD.isDisabled()) return;
        Intent qf = new Intent("com.qf.radio.update_action");
        qf.putExtra("com.qf.radio.update_action_key", freqStr);
        qf.putExtra("com.qf.radio.update_action_freq_key", nativeFreq);
        qf.putExtra("com.qf.radio.update_action_band_key", band);
        qf.putExtra("com.qf.radio.update_action_preset_key", presetIdx);
        qf.putExtra("com.qf.radio.update_action_searching_key", false);
        qf.putExtra("com.qf.radio.update_action_name_key", widgetName);
        qf.setPackage("com.android.auto.autohome");
        try {
            ctx.sendBroadcast(qf);
        } catch (SecurityException se) {
            QF_GUARD.disable(se);
        }
    }

    private void broadcastMtk(Context ctx, String freqStr, int band,
                              String widgetName, boolean isStereo) {
        Intent mtk = new Intent("com.android.launcher.action.UPDATE_RADIO");
        mtk.putExtra("frequency", freqStr);
        mtk.putExtra("name", widgetName);
        mtk.putExtra("band", band < 3 ? "FM" : "AM");
        mtk.putExtra("isRadio", true);
        mtk.putExtra("stereo", isStereo);
        ctx.sendBroadcast(mtk);
    }

    private void broadcastTopway(Context ctx, int nativeFreq, int band,
                                 String widgetName) {
        Intent ts = new Intent("com.ts.main.radio.update");
        ts.putExtra("freq", nativeFreq);
        ts.putExtra("band", band);
        ts.putExtra("name", widgetName);
        ts.putExtra("isRadio", true);
        ctx.sendBroadcast(ts);
    }

    private void broadcastSource(Context ctx) {
        Intent src = new Intent("com.android.launcher.action.UPDATE_SOURCE");
        src.putExtra("source", 1); // 1 = Radio
        src.putExtra("sourceName", "Radio");
        ctx.sendBroadcast(src);
    }

    private void updateOwnWidget(Context ctx, int freq, int band,
                                 String rdsName, RadioRepository repo) {
        com.example.openradiofm.widget.OpenRadioFmWidgetProvider
                .updateStationDisplay(ctx, freq, band, rdsName);
        if (repo != null) {
            repo.getStationInfo(freq, logoUrl -> {
                com.example.openradiofm.widget.OpenRadioFmWidgetProvider
                        .updateStationDisplay(ctx, freq, band, rdsName, logoUrl);
            }, rdsName);
        }
    }

    // ==================== Helpers ====================

    /**
     * Formatea la frecuencia para los broadcasts OEM.
     * AM: valor entero; FM: formato "xxx.xx" con punto decimal US.
     */
    static String formatFrequency(int freqKhz, int band) {
        if (band == BAND_AM1 || band == BAND_AM2) {
            return String.valueOf(freqKhz);
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(java.util.Locale.US);
        df.setDecimalFormatSymbols(dfs);
        return df.format(freqKhz / 1000.0f);
    }

    /** Devuelve el nombre saneado para widgets (vacío si es texto genérico). */
    static String sanitizeWidgetName(String rdsName) {
        if (rdsName != null && !rdsName.isEmpty()
                && !"STATION NAME".equals(rdsName)
                && !"STATION".equals(rdsName)) {
            return rdsName;
        }
        return "";
    }

    private static boolean strEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Evita reintentos tras SecurityException ("Permission Denial") al broadcast QF.
     * En algunos K706 el broadcast está protegido por permisos de sistema.
     */
    private static final class QfBroadcastGuard {
        private volatile boolean disabled = false;
        private volatile boolean logged = false;

        boolean isDisabled() {
            return disabled;
        }

        void disable(SecurityException se) {
            disabled = true;
            if (!logged) {
                logged = true;
                Log.w(TAG, "broadcastK706: deshabilitado por permisos (SecurityException)", se);
            }
        }
    }
}
