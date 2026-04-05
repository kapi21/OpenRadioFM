package com.example.openradiofm.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.ui.main.MainActivity;
import com.example.openradiofm.util.HiHackBootReminder;

/**
 * Servicio de Accesibilidad para "secuestrar" el Intent de la radio de fábrica (K706 etc).
 * Cuando el usuario pulsa el botón físico RADIO, el sistema lanza com.android.fmradio.ext.
 * Este servicio detecta ese lanzamiento y pone OpenRadioFM por encima inmediatamente.
 */
public class FactoryRadioHijackerService extends AccessibilityService {

    private static final String TAG = "RadioHijackerService";
    private static final String PREFS = "RadioPresets";
    /** Si false, no interceptar teclas MEDIA (solo hijack de app de fábrica). */
    private static final String PREF_FORWARD_MEDIA_KEYS = "pref_a11y_forward_media_keys";
    private static final String TARGET_PACKAGE_K706 = "com.android.fmradio.ext";
    private static final String TARGET_PACKAGE_MT8163 = "com.hcn.autoradio";
    private static final String TARGET_PACKAGE_QS6 = "com.nwd.radio";
    private static final String TARGET_PACKAGE_QS6_ALT = "com.nwd.radio.service";
    private static final String TARGET_PACKAGE_TS = "com.ts.MainUI";
    private static final String TARGET_PACKAGE_MTK_GENERIC = "com.android.fmradio";
    private static final String TARGET_PACKAGE_MTK_MTK = "com.mediatek.fmradio";
    private static final String TARGET_PACKAGE_FYT = "com.syu.radio";
    
    // Evitar lanzar múltiples Intents seguidos
    private long lastLaunchTime = 0;
    private static final long COOLDOWN_MS = 2000;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        // Solo nos interesa cuando la ventana cambia a estado activo (app se abre)
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            
            if (TARGET_PACKAGE_K706.equals(packageName) || 
                TARGET_PACKAGE_MT8163.equals(packageName) ||
                TARGET_PACKAGE_QS6.equals(packageName) ||
                TARGET_PACKAGE_QS6_ALT.equals(packageName) ||
                TARGET_PACKAGE_TS.equals(packageName) ||
                TARGET_PACKAGE_MTK_GENERIC.equals(packageName) ||
                TARGET_PACKAGE_MTK_MTK.equals(packageName) ||
                TARGET_PACKAGE_FYT.equals(packageName)) {
                long currentTime = System.currentTimeMillis();
                
                // Evitamos rebotes o múltiples llamadas rápidas
                if (currentTime - lastLaunchTime > COOLDOWN_MS) {
                    Log.i(TAG, "¡Radio de fábrica detectada! Interceptando e iniciando OpenRadioFM...");
                    lastLaunchTime = currentTime;
                    
                    launchOpenRadioFM();
                }
            }
        }
    }

    private void launchOpenRadioFM() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            // Flags críticos para traer la actividad al frente y limpiar pilas anteriores
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                            Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            
            // Opcional: Extra para saber que venimos del hijacker (útil por si toca mutear la otra app, 
            // aunque en MTK el HW de radio FM suena directamente y simplemente tomamos control de UI)
            intent.putExtra("from_hijacker", true);
            
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error lanzando OpenRadioFM desde el Hijacker", e);
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Servicio Hijacker interrumpido");
    }

    /**
     * K706 / QS6 (NWD): con el launcher al frente las teclas MEDIA suelen ir como {@code KeyEvent}
     * al foco, no como {@code ACTION_MEDIA_BUTTON} a {@link RadioMediaService}. Reenviamos solo cuando
     * {@link MainActivity} no está en ciclo started (app en segundo plano) y el motor es K706 o QS6.
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        try {
            if (event == null) return false;
            if (MainActivity.sMainActivityStarted) {
                return false;
            }
            if (!MainActivity.sWheelMediaBridgeActive) {
                return false;
            }
            SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
            if (!p.getBoolean(PREF_FORWARD_MEDIA_KEYS, true)) {
                return false;
            }
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            int code = event.getKeyCode();
            if (code != KeyEvent.KEYCODE_MEDIA_NEXT
                    && code != KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    && code != KeyEvent.KEYCODE_MEDIA_PLAY
                    && code != KeyEvent.KEYCODE_MEDIA_PAUSE
                    && code != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    && code != KeyEvent.KEYCODE_MEDIA_STOP
                    && code != KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                    && code != KeyEvent.KEYCODE_MEDIA_REWIND) {
                return false;
            }

            Intent svc = new Intent(this, RadioMediaService.class);
            svc.setAction(Intent.ACTION_MEDIA_BUTTON);
            svc.putExtra(Intent.EXTRA_KEY_EVENT, event);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            Log.d(TAG, "MEDIA key reenviada a RadioMediaService (keyCode=" + code + ")");
            return true;
        } catch (Exception e) {
            Log.w(TAG, "onKeyEvent forward falló", e);
            return false;
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        HiHackBootReminder.persistEverEnabled(this);
        Log.i(TAG, "FactoryRadioHijackerService conectado y monitorizando");
    }
}
