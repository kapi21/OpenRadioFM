package com.example.openradiofm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logger ligero a fichero (opt-in) para diagnósticos en head units.
 * Escribe en la carpeta RadioLogos (legacy /sdcard o app-specific si no es writable).
 */
public final class RadioActivityFileLogger {
    public static final String PREF_DEV_FILE_LOG_ENABLED = "pref_dev_activity_file_log_enabled";
    public static final String PREF_DEV_LOG_FILE_NAME = "pref_dev_activity_file_log_name";
    public static final String PREF_DEV_FILE_LOG_PROFILE = "pref_dev_activity_file_log_profile"; // 0..2

    private static final Object LOCK = new Object();
    private static final SimpleDateFormat TS_FILE = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private static final SimpleDateFormat TS_LINE = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static final int PROFILE_BASIC = 0;
    public static final int PROFILE_MEDIUM = 1;
    public static final int PROFILE_FULL = 2;

    private RadioActivityFileLogger() {}

    public static boolean isEnabled(Context context) {
        if (context == null) return false;
        try {
            SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            return p.getBoolean(PREF_DEV_FILE_LOG_ENABLED, false);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static int getProfile(Context context) {
        if (context == null) return PROFILE_BASIC;
        try {
            SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            int v = p.getInt(PREF_DEV_FILE_LOG_PROFILE, PROFILE_BASIC);
            if (v < PROFILE_BASIC) v = PROFILE_BASIC;
            if (v > PROFILE_FULL) v = PROFILE_FULL;
            return v;
        } catch (Exception ignored) {
            return PROFILE_BASIC;
        }
    }

    /** Llamar al activar/desactivar para rotar/limpiar el fichero. */
    public static void onToggleChanged(Context context, boolean enabled) {
        if (context == null) return;
        try {
            SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            if (!enabled) {
                p.edit().remove(PREF_DEV_LOG_FILE_NAME).apply();
                return;
            }
            String name = "radio_activity_" + TS_FILE.format(new Date()) + ".log";
            p.edit().putString(PREF_DEV_LOG_FILE_NAME, name).apply();
            // Crear fichero y cabecera.
            logBasic(context, "DEV", "FILE_LOG_ENABLED name=" + name + " profile=" + getProfile(context));
        } catch (Exception ignored) {}
    }

    /** Perfil BÁSICO: eventos imprescindibles para soporte. */
    public static void logBasic(Context context, String tag, String message) {
        log(context, PROFILE_BASIC, tag, message);
    }

    /** Perfil MEDIO: más detalle (estado + decisiones). */
    public static void logMedium(Context context, String tag, String message) {
        log(context, PROFILE_MEDIUM, tag, message);
    }

    /** Perfil COMPLETO: muy verboso (debug/trazas). */
    public static void logFull(Context context, String tag, String message) {
        log(context, PROFILE_FULL, tag, message);
    }

    public static void log(Context context, int requiredProfile, String tag, String message) {
        if (context == null) return;
        if (!isEnabled(context)) return;
        if (getProfile(context) < requiredProfile) return;
        String t = tag != null ? tag : "APP";
        String m = message != null ? message : "";

        synchronized (LOCK) {
            try {
                File dir = getPreferredRadioLogosDir(context);
                if (dir == null) return;
                try { dir.mkdirs(); } catch (Exception ignored) {}

                SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
                String fileName = p.getString(PREF_DEV_LOG_FILE_NAME, null);
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "radio_activity_" + TS_FILE.format(new Date()) + ".log";
                    p.edit().putString(PREF_DEV_LOG_FILE_NAME, fileName).apply();
                }
                File out = new File(dir, fileName);
                String line = "[" + TS_LINE.format(new Date()) + "] " + t + ": " + m + "\n";

                FileWriter w = new FileWriter(out, true);
                w.write(line);
                w.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static File getPreferredRadioLogosDir(Context context) {
        try {
            File legacy = new File("/sdcard/RadioLogos");
            if ((legacy.exists() || legacy.mkdirs()) && legacy.canWrite()) return legacy;
        } catch (Exception ignored) {}
        try {
            File external = context.getExternalFilesDir(null);
            File base = external != null ? external : context.getFilesDir();
            File appDir = new File(base, "RadioLogos");
            try { appDir.mkdirs(); } catch (Exception ignored) {}
            return appDir;
        } catch (Exception ignored) {
            return null;
        }
    }
}

