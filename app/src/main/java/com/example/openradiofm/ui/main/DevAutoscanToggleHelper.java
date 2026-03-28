package com.example.openradiofm.ui.main;

import android.content.Context;

import androidx.appcompat.widget.SwitchCompat;

/**
 * Activa el botón AutoScan de la UI principal (experimental). Por defecto desactivado.
 */
public final class DevAutoscanToggleHelper {

    public static final String PREF_DEV_AUTOSCAN_ENABLED = "pref_dev_autoscan_enabled";

    private DevAutoscanToggleHelper() {}

    public static void bind(SwitchCompat sw, MainActivity activity) {
        if (sw == null || activity == null) {
            return;
        }
        android.content.SharedPreferences prefs =
                activity.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
        sw.setChecked(prefs.getBoolean(PREF_DEV_AUTOSCAN_ENABLED, false));
        sw.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(PREF_DEV_AUTOSCAN_ENABLED, checked).apply();
            activity.applyDevAutoScanButtonState();
        });
    }
}
