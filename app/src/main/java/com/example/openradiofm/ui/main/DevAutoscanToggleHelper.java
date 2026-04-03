package com.example.openradiofm.ui.main;

import android.content.Context;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

/**
 * Modo desarrollo: activar botón AutoScan en la UI principal y umbral RSSI/SNR sin RDS.
 * El AutoScan en UI está activo por defecto en instalaciones nuevas.
 */
public final class DevAutoscanToggleHelper {

    public static final String PREF_DEV_AUTOSCAN_ENABLED = "pref_dev_autoscan_enabled";
    /** Mínimo RSSI o SNR (el que cumpla) para aceptar emisora sin RDS; 0–15 según chip. */
    public static final String PREF_DEV_AUTOSCAN_SIGNAL_THRESHOLD = "pref_dev_autoscan_signal_threshold";
    public static final int DEFAULT_AUTOSCAN_SIGNAL_THRESHOLD = 2;
    /** Por defecto el modo AutoScan (botón escaneo) está activo. */
    public static final boolean DEFAULT_DEV_AUTOSCAN_ENABLED = true;

    private DevAutoscanToggleHelper() {}

    public static int getAutoScanSignalThreshold(Context context) {
        if (context == null) return DEFAULT_AUTOSCAN_SIGNAL_THRESHOLD;
        try {
            int v = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE)
                    .getInt(PREF_DEV_AUTOSCAN_SIGNAL_THRESHOLD, DEFAULT_AUTOSCAN_SIGNAL_THRESHOLD);
            if (v < 0) return 0;
            if (v > 15) return 15;
            return v;
        } catch (Exception e) {
            return DEFAULT_AUTOSCAN_SIGNAL_THRESHOLD;
        }
    }

    public static void bind(SwitchCompat sw, MainActivity activity) {
        if (sw == null || activity == null) {
            return;
        }
        android.content.SharedPreferences prefs =
                activity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
        sw.setChecked(prefs.getBoolean(PREF_DEV_AUTOSCAN_ENABLED, DEFAULT_DEV_AUTOSCAN_ENABLED));
        sw.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(PREF_DEV_AUTOSCAN_ENABLED, checked).apply();
            activity.applyDevAutoScanButtonState();
        });
    }

    public static void bindThresholdSeekBar(SeekBar seek, TextView valueDisplay, MainActivity activity) {
        if (seek == null || activity == null) {
            return;
        }
        android.content.SharedPreferences prefs =
                activity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
        int v = getAutoScanSignalThreshold(activity);
        seek.setMax(15);
        seek.setProgress(Math.min(15, Math.max(0, v)));
        updateThresholdValueText(valueDisplay, seek.getProgress());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateThresholdValueText(valueDisplay, progress);
                if (fromUser) {
                    prefs.edit().putInt(PREF_DEV_AUTOSCAN_SIGNAL_THRESHOLD, progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private static void updateThresholdValueText(TextView tv, int v) {
        if (tv != null) {
            tv.setText(String.valueOf(v));
        }
    }
}
