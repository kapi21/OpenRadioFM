package com.example.openradiofm.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logger ligero para diagnósticos SPD (Junsun V9 Plus).
 * Evita dependencias externas; escribe en externalFilesDir si existe.
 */
public final class SpdDiagLogger {
    private static final String TAG = "SpdDiagLogger";
    private static volatile File sLogFile;
    private static final Object LOCK = new Object();

    private SpdDiagLogger() {}

    public static void init(Context ctx) {
        if (sLogFile != null || ctx == null) return;
        synchronized (LOCK) {
            if (sLogFile != null) return;
            try {
                File base = ctx.getExternalFilesDir(null);
                if (base == null) base = ctx.getFilesDir();
                File dir = new File(base, "Diagnostics");
                if (!dir.exists() && !dir.mkdirs()) {
                    Log.w(TAG, "Could not create diagnostics directory: " + dir.getAbsolutePath());
                }
                sLogFile = new File(dir, "spd_engine.log");
            } catch (Exception e) {
                Log.w(TAG, "init failed", e);
            }
        }
    }

    public static String getLogPath() {
        File f = sLogFile;
        return f != null ? f.getAbsolutePath() : "";
    }

    public static void i(String event, String msg) {
        write("I", event, msg, null);
    }

    public static void e(String event, String msg, Throwable t) {
        write("E", event, msg, t);
    }

    private static void write(String level, String event, String msg, Throwable t) {
        try {
            File f = sLogFile;
            if (f == null) return;
            String ts = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date());
            String line = ts + " " + level + " " + (event != null ? event : "") + " " + (msg != null ? msg : "") + "\n";
            synchronized (LOCK) {
                try (FileWriter w = new FileWriter(f, true)) {
                    w.write(line);
                    if (t != null) {
                        w.write(Log.getStackTraceString(t));
                        w.write("\n");
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}

