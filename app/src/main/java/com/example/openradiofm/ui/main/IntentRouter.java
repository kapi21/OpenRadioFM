package com.example.openradiofm.ui.main;

import android.content.pm.PackageManager;
import android.util.Log;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.K706Engine;
import com.example.openradiofm.widget.OpenRadioFmWidgetProvider;

/**
 * Rutas de entrada: {@code onNewIntent} (widget / hijacker K706) y resultados de permisos
 * (Fase 5 refactor 5.2.0.MCU).
 */
public final class IntentRouter {

    /** Import/export favoritos (almacenamiento). */
    public static final int REQ_STORAGE_IMPORT_EXPORT = 1001;
    /** Silenciar FM en llamadas (K706): {@link android.Manifest.permission#READ_PHONE_STATE} */
    public static final int REQ_READ_PHONE_STATE_K706 = 1003;

    private IntentRouter() {}

    static void dispatchNewIntent(MainActivity a, android.content.Intent intent) {
        if (intent == null) return;
        handleK706McuReassertFromHijacker(a, intent);
        handleWidgetDeepLinks(a, intent);
    }

    /**
     * La radio OEM registra su propio {@code IMcuListener}; al volver con HiHack hay que volver a
     * pedir telemetría MCU (RDS 0xB6/B7/…) para OpenRadioFM.
     */
    private static void handleK706McuReassertFromHijacker(MainActivity a, android.content.Intent intent) {
        if (intent == null || a.mMode != MainActivity.FmMode.FM_K706) return;
        if (!intent.getBooleanExtra(
                com.example.openradiofm.services.FactoryRadioHijackerService.EXTRA_FROM_HIJACKER, false)) {
            return;
        }
        intent.removeExtra(com.example.openradiofm.services.FactoryRadioHijackerService.EXTRA_FROM_HIJACKER);
        scheduleK706McuListenerReassertAfterOem(a, "from_hijacker_warm", 400L);
    }

    static void scheduleK706McuListenerReassertAfterOem(MainActivity a, String reason, long delayMs) {
        a.mMainHandler.postDelayed(() -> {
            if (a.isFinishing() || a.isDestroyed()) return;
            if (a.mMode != MainActivity.FmMode.FM_K706 || !(a.mEngine instanceof K706Engine)) return;
            try {
                ((K706Engine) a.mEngine).reassertMcuTelemetryListener();
                Log.i(MainActivity.TAG, "K706: reassert MCU listener (" + reason + ")");
            } catch (Exception e) {
                Log.w(MainActivity.TAG, "K706: reassert MCU listener falló (" + reason + ")", e);
            }
        }, delayMs);
    }

    private static void handleWidgetDeepLinks(MainActivity a, android.content.Intent intent) {
        if (intent == null) return;
        try {
            if (intent.getBooleanExtra(OpenRadioFmWidgetProvider.EXTRA_WIDGET_SHOW_INFO, false)) {
                int freq = intent.getIntExtra("freq_khz", 0);
                int band = intent.getIntExtra("band", 0);
                String ps = intent.getStringExtra("ps");
                String bandTxt;
                if (band == MainActivity.BAND_FM1) bandTxt = "FM1";
                else if (band == MainActivity.BAND_FM2) bandTxt = "FM2";
                else if (band == MainActivity.BAND_FM3) bandTxt = "FM3";
                else if (band == MainActivity.BAND_AM1) bandTxt = "AM1";
                else if (band == MainActivity.BAND_AM2) bandTxt = "AM2";
                else bandTxt = "FM1";
                String freqTxt = (freq > 0)
                        ? ((band == MainActivity.BAND_AM1 || band == MainActivity.BAND_AM2)
                        ? (freq + " kHz")
                        : String.format(java.util.Locale.US, "%.2f MHz", freq / 1000.0))
                        : "—";
                String psTxt = (ps != null && !ps.trim().isEmpty()) ? ps.trim() : "—";
                new android.app.AlertDialog.Builder(a)
                        .setTitle("OpenRadioFM")
                        .setMessage(bandTxt + " · " + freqTxt + "\n" + psTxt)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
            if (intent.getBooleanExtra(OpenRadioFmWidgetProvider.EXTRA_WIDGET_OPEN_FAVORITES_DIALOG, false)) {
                if (a.mDialogManager != null) {
                    a.mDialogManager.showSaveLoadFavoritesDialog();
                }
            }
        } catch (Exception ignored) {}
    }

    static void dispatchPermissionsResult(MainActivity a, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_STORAGE_IMPORT_EXPORT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                a.showToast(a.getString(R.string.toast_permissions_granted));
                if (a.mDialogManager != null) {
                    a.mDialogManager.showSaveLoadFavoritesDialog();
                }
            } else {
                a.showToast(a.getString(R.string.toast_storage_permission_needed));
            }
        } else if (requestCode == REQ_READ_PHONE_STATE_K706) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (a.mEngine instanceof K706Engine) {
                    ((K706Engine) a.mEngine).registerPhoneStateListenerIfPermitted();
                }
                a.showToast(a.getString(R.string.toast_phone_mute_on_call));
            } else {
                a.showToast(a.getString(R.string.toast_phone_no_permission_fm));
            }
        }
    }
}
