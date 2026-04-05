package com.example.openradiofm.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import com.example.openradiofm.R;
import com.example.openradiofm.ui.main.MainActivity;

/**
 * Muchas ROM (cabeza K706/QS6) no persisten el servicio de accesibilidad tras un reinicio completo.
 * No se puede reactivar por código; este helper marca un recordatorio tras {@code BOOT_COMPLETED}
 * y muestra un diálogo la próxima vez que el usuario abre OpenRadioFM.
 */
public final class HiHackBootReminder {

    public static final String PREFS_NAME = "RadioPresets";
    /** El usuario llegó a tener HiHack activo al menos una vez (servicio conectado o detectado en resume). */
    public static final String PREF_EVER_ENABLED = "pref_hihack_ever_enabled";
    /** Si false, no marcar pendiente ni mostrar diálogo tras reinicio. */
    public static final String PREF_BOOT_REMINDER = "pref_hihack_boot_reminder";
    /** Tras boot: HiHack sigue apagado y debemos avisar en la siguiente apertura de la app. */
    public static final String PREF_PENDING_AFTER_BOOT = "pref_hihack_pending_after_boot";

    private HiHackBootReminder() {}

    public static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Llamar desde {@link com.example.openradiofm.services.FactoryRadioHijackerService#onServiceConnected()}. */
    public static void persistEverEnabled(Context context) {
        prefs(context).edit().putBoolean(PREF_EVER_ENABLED, true).apply();
    }

    /**
     * Tras arranque del sistema: si antes usaban HiHack y sigue desactivado, marcar aviso pendiente.
     */
    public static void markPendingIfHihackStillOff(Context context) {
        Context app = context.getApplicationContext();
        SharedPreferences p = prefs(app);
        if (!p.getBoolean(PREF_BOOT_REMINDER, true)) return;
        if (!p.getBoolean(PREF_EVER_ENABLED, false)) return;
        if (MainActivity.isFactoryRadioHijackerAccessibilityEnabled(app)) return;
        p.edit().putBoolean(PREF_PENDING_AFTER_BOOT, true).apply();
    }

    /**
     * {@link MainActivity#onResume()}: sincroniza “ever enabled” y muestra el diálogo si hace falta.
     */
    public static void onAppResumed(Activity activity, SharedPreferences prefs) {
        if (activity == null || activity.isFinishing() || prefs == null) return;
        if (MainActivity.isFactoryRadioHijackerAccessibilityEnabled(activity)) {
            prefs.edit()
                    .putBoolean(PREF_EVER_ENABLED, true)
                    .putBoolean(PREF_PENDING_AFTER_BOOT, false)
                    .apply();
            return;
        }
        if (!prefs.getBoolean(PREF_PENDING_AFTER_BOOT, false)) return;
        if (!prefs.getBoolean(PREF_BOOT_REMINDER, true)) {
            prefs.edit().putBoolean(PREF_PENDING_AFTER_BOOT, false).apply();
            return;
        }
        if (!prefs.getBoolean(PREF_EVER_ENABLED, false)) {
            prefs.edit().putBoolean(PREF_PENDING_AFTER_BOOT, false).apply();
            return;
        }
        prefs.edit().putBoolean(PREF_PENDING_AFTER_BOOT, false).apply();

        try {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.a11y_hihack_reboot_dialog_title))
                    .setMessage(activity.getString(R.string.a11y_hihack_reboot_dialog_message))
                    .setPositiveButton(activity.getString(R.string.a11y_hihack_reboot_dialog_open_settings), (d, w) -> {
                        try {
                            Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(i);
                        } catch (Exception ignored) {}
                    })
                    .setNegativeButton(activity.getString(R.string.a11y_hihack_reboot_dialog_later), null)
                    .show();
        } catch (Exception ignored) {
        }
    }
}
