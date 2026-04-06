package com.example.openradiofm.utils;

import android.content.Context;

/**
 * Preferencia de país usada para filtrar búsquedas de logos/streaming.
 * Se guarda en "RadioPresets" para que esté disponible desde el primer arranque.
 */
public final class CountryPrefs {
    private CountryPrefs() {}

    public static final String PREF_COUNTRY_CODE = "pref_country_code";

    /** Último recurso si no hay valor configurado. */
    public static final String DEFAULT_COUNTRY = "ES";

    public static String getCountry(Context context) {
        if (context == null) return DEFAULT_COUNTRY;
        try {
            android.content.SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            String v = p.getString(PREF_COUNTRY_CODE, null);
            if (v == null || v.trim().isEmpty()) return DEFAULT_COUNTRY;
            return v.trim().toUpperCase();
        } catch (Exception e) {
            return DEFAULT_COUNTRY;
        }
    }

    public static boolean isCountrySet(Context context) {
        if (context == null) return false;
        try {
            android.content.SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            String v = p.getString(PREF_COUNTRY_CODE, null);
            return v != null && !v.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static void setCountry(Context context, String countryCode) {
        if (context == null) return;
        String v = (countryCode == null) ? "" : countryCode.trim().toUpperCase();
        if (v.isEmpty()) return;
        try {
            android.content.SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            p.edit().putString(PREF_COUNTRY_CODE, v).apply();
        } catch (Exception ignored) {}
    }
}

