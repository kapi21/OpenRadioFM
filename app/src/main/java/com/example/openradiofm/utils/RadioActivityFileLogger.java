package com.example.openradiofm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.openradiofm.data.source.K706RadioManager;

import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logger ligero a fichero (opt-in) para diagnósticos en head units.
 * Escribe en la carpeta RadioLogos (legacy /sdcard o app-specific si no es writable).
 *
 * <p>Incluye heartbeat periódico (TICK) mientras el log está activo, para capturar estado durante
 * Android Auto sin ADB/logcat. Corrige carrera {@code apply()} vs nombre de fichero.</p>
 */
public final class RadioActivityFileLogger {
    public static final String TAG_FILE = "OpenRadioFM_FileLog";
    public static final String PREF_DEV_FILE_LOG_ENABLED = "pref_dev_activity_file_log_enabled";
    public static final String PREF_DEV_LOG_FILE_NAME = "pref_dev_activity_file_log_name";
    public static final String PREF_DEV_FILE_LOG_PROFILE = "pref_dev_activity_file_log_profile"; // 0..2

    private static final Object LOCK = new Object();
    private static final SimpleDateFormat TS_FILE = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private static final SimpleDateFormat TS_LINE = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    /** Nombre del .log activo; evita carrera si {@code SharedPreferences} aún no ha hecho {@code apply}. */
    private static volatile String sBoundLogFileName;

    private static final Handler HB_HANDLER = new Handler(Looper.getMainLooper());
    private static final long HEARTBEAT_MS = 8000L;
    /** Contexto app para ticks; débil para no retener la app al desactivar. */
    private static WeakReference<Context> sHeartbeatAppCtx;

    private static final Runnable HEARTBEAT_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Context app = sHeartbeatAppCtx != null ? sHeartbeatAppCtx.get() : null;
            if (app == null) {
                return;
            }
            if (!isEnabled(app)) {
                return;
            }
            K706RadioManager mgr = K706RadioManager.peekWeakRadioManagerForDevLog();
            String mgrLine = mgr != null ? mgr.buildDevFileLogTickLine() : "mgr=null";
            logBasic(app, "TICK", "uiResumed=" + sMainActivityResumed + " " + mgrLine);
            HB_HANDLER.postDelayed(HEARTBEAT_RUNNABLE, HEARTBEAT_MS);
        }
    };

    /** Último estado UI (MainActivity al frente); lo actualiza {@link com.example.openradiofm.ui.main.LifecycleCoordinator}. */
    private static volatile boolean sMainActivityResumed;

    public static final int PROFILE_BASIC = 0;
    public static final int PROFILE_MEDIUM = 1;
    public static final int PROFILE_FULL = 2;

    private RadioActivityFileLogger() {}

    public static void noteMainActivityResumed(boolean resumed) {
        sMainActivityResumed = resumed;
    }

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
        final Context app = context.getApplicationContext();
        try {
            SharedPreferences p = app.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
            // commit() antes de heartbeat / log: si usáramos solo apply() desde la UI, isEnabled()
            // podría seguir en false y el primer TICK no se reprogramaría.
            if (!p.edit().putBoolean(PREF_DEV_FILE_LOG_ENABLED, enabled).commit()) {
                Log.e(TAG_FILE, "commit PREF_DEV_FILE_LOG_ENABLED falló");
            }
            stopHeartbeat();
            if (!enabled) {
                sBoundLogFileName = null;
                sHeartbeatAppCtx = null;
                p.edit().remove(PREF_DEV_LOG_FILE_NAME).commit();
                return;
            }
            String name = "radio_activity_" + TS_FILE.format(new Date()) + ".log";
            sBoundLogFileName = name;
            if (!p.edit().putString(PREF_DEV_LOG_FILE_NAME, name).commit()) {
                Log.e(TAG_FILE, "commit nombre log falló");
            }
            logBasic(app, "DEV", "FILE_LOG_ENABLED name=" + name + " profile=" + getProfile(app));
            logBasic(app, "DEV", "Perfil 0=básico; 1=medio; 2=completo. TICK cada " + (HEARTBEAT_MS / 1000)
                    + "s (app en 2º plano / AA). Tras prueba: copia RadioLogos/*.log por USB.");
            logBasic(app, "DEV", "Opcional ingeniería: Volcar logcat buffer → mismo directorio.");
            verifyWritableProbe(app);
            startHeartbeat(app);
        } catch (Exception e) {
            Log.e(TAG_FILE, "onToggleChanged", e);
        }
    }

    private static void stopHeartbeat() {
        HB_HANDLER.removeCallbacks(HEARTBEAT_RUNNABLE);
    }

    /** Primer TICK enseguida; siguientes cada {@link #HEARTBEAT_MS}. */
    private static void startHeartbeat(Context app) {
        sHeartbeatAppCtx = new WeakReference<>(app.getApplicationContext());
        HB_HANDLER.removeCallbacks(HEARTBEAT_RUNNABLE);
        HB_HANDLER.post(HEARTBEAT_RUNNABLE);
    }

    private static void verifyWritableProbe(Context app) {
        logBasic(app, "DEV", "PROBE write_ok dir=" + getDiagnosticLogDirectory(app));
    }

    /**
     * Intenta volcar {@code logcat -d} (lo que permita la ROM sin root). Útil al volver del coche con USB.
     * No sustituye log en tiempo real de Z-Link; a veces solo salen líneas de la propia app.
     */
    public static void dumpLogcatBufferBestEffort(Context context) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        new Thread(() -> {
            File dir = getDiagnosticLogDirectory(app);
            if (dir == null) {
                Log.w(TAG_FILE, "dumpLogcat: sin directorio");
                return;
            }
            File out = new File(dir, "logcat_dump_" + TS_FILE.format(new Date()) + ".txt");
            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-t", "1200", "-v", "threadtime"});
                StringBuilder sb = new StringBuilder(65536);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                        if (sb.length() > 900_000) {
                            break;
                        }
                    }
                }
                proc.waitFor();
                try (FileWriter w = new FileWriter(out, false)) {
                    w.write(sb.toString());
                    w.flush();
                }
                logBasic(app, "DEV", "LOGCAT_DUMP bytes=" + sb.length() + " file=" + out.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG_FILE, "dumpLogcatBufferBestEffort", e);
                try {
                    logBasic(app, "DEV", "LOGCAT_DUMP_FAIL " + e.getMessage());
                } catch (Exception ignored) {
                }
            } finally {
                if (proc != null) {
                    proc.destroy();
                }
            }
        }, "logcat-dump").start();
    }

    public static void logBasic(Context context, String tag, String message) {
        log(context, PROFILE_BASIC, tag, message);
    }

    public static void logMedium(Context context, String tag, String message) {
        log(context, PROFILE_MEDIUM, tag, message);
    }

    public static void logFull(Context context, String tag, String message) {
        log(context, PROFILE_FULL, tag, message);
    }

    public static void log(Context context, int requiredProfile, String tag, String message) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        if (!isEnabled(app)) return;
        if (getProfile(app) < requiredProfile) return;
        String t = tag != null ? tag : "APP";
        String m = message != null ? message : "";

        synchronized (LOCK) {
            try {
                File dir = getDiagnosticLogDirectory(app);
                if (dir == null) {
                    Log.e(TAG_FILE, "log: dir null");
                    return;
                }
                try {
                    dir.mkdirs();
                } catch (Exception ignored) {
                }

                SharedPreferences p = app.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
                String fileName = sBoundLogFileName;
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = p.getString(PREF_DEV_LOG_FILE_NAME, null);
                }
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "radio_activity_" + TS_FILE.format(new Date()) + ".log";
                    sBoundLogFileName = fileName;
                    p.edit().putString(PREF_DEV_LOG_FILE_NAME, fileName).commit();
                }
                File out = new File(dir, fileName);
                String line = "[" + TS_LINE.format(new Date()) + "] " + t + ": " + m + "\n";

                FileWriter w = new FileWriter(out, true);
                w.write(line);
                w.flush();
                w.close();
            } catch (Exception e) {
                Log.e(TAG_FILE, "log write fail tag=" + t, e);
            }
        }
    }

    public static File getDiagnosticLogDirectory(Context context) {
        if (context == null) return null;
        try {
            File legacy = new File("/sdcard/RadioLogos");
            if ((legacy.exists() || legacy.mkdirs()) && legacy.canWrite()) return legacy;
        } catch (Exception ignored) {
        }
        try {
            Context app = context.getApplicationContext();
            File external = app.getExternalFilesDir(null);
            File base = external != null ? external : app.getFilesDir();
            File appDir = new File(base, "RadioLogos");
            try {
                appDir.mkdirs();
            } catch (Exception ignored) {
            }
            return appDir;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getActiveLogFilePathForDisplay(Context context) {
        if (context == null) return "";
        try {
            File dir = getDiagnosticLogDirectory(context);
            if (dir == null) return "";
            String fileName = sBoundLogFileName;
            if (fileName == null || fileName.trim().isEmpty()) {
                SharedPreferences p = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);
                fileName = p.getString(PREF_DEV_LOG_FILE_NAME, null);
            }
            if (fileName == null || fileName.trim().isEmpty()) {
                return dir.getAbsolutePath() + "\n→ (activa el switch; se crea radio_activity_YYYYMMDD_HHMMSS.log)";
            }
            return new File(dir, fileName).getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }
}
