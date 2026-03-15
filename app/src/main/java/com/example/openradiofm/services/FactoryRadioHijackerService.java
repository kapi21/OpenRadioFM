package com.example.openradiofm.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.openradiofm.ui.main.MainActivity;

/**
 * Servicio de Accesibilidad para "secuestrar" el Intent de la radio de fábrica (K706 etc).
 * Cuando el usuario pulsa el botón físico RADIO, el sistema lanza com.android.fmradio.ext.
 * Este servicio detecta ese lanzamiento y pone OpenRadioFM por encima inmediatamente.
 */
public class FactoryRadioHijackerService extends AccessibilityService {

    private static final String TAG = "RadioHijackerService";
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

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "FactoryRadioHijackerService conectado y monitorizando");
    }
}
