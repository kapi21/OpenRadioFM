package com.example.openradiofm.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityServiceInfo;

import com.example.openradiofm.service.RadioMediaService;
import com.example.openradiofm.ui.main.MainActivity;
import com.example.openradiofm.util.HiHackBootReminder;

/**
 * Servicio de Accesibilidad para "secuestrar" el Intent de la radio de fábrica (K706 etc).
 * Cuando el usuario pulsa el botón físico RADIO, el sistema lanza com.android.fmradio.ext.
 * Este servicio detecta ese lanzamiento y pone OpenRadioFM por encima inmediatamente.
 */
public class FactoryRadioHijackerService extends AccessibilityService {

    /** Extra en el Intent hacia {@link com.example.openradiofm.ui.main.MainActivity} tras interceptar la radio OEM. */
    public static final String EXTRA_FROM_HIJACKER = "from_hijacker";

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

    /** Heartbeat para detectar “activo pero no funciona” en algunas ROM (Android 9 Junsun). */
    public static final String PREF_HIHACK_HEARTBEAT_MS = "pref_hihack_heartbeat_ms";
    /**
     * Tras cerrar OpenRadioFM ({@code isFinishing()}), la ROM a veces muestra un frame la radio OEM;
     * sin esto el hijacker re-lanza la app en bucle. No aplica al pulsar HOME (solo al salir con back / matar tarea).
     */
    public static final String PREF_HIHACK_SUPPRESS_UNTIL_MS = "pref_hihack_suppress_until_ms";
    /** Motivo del último cierre (para no “matar” el HiHack normal al salir al launcher). */
    public static final String PREF_HIHACK_SUPPRESS_REASON = "pref_hihack_suppress_reason";
    private static final String REASON_POWEROFF = "poweroff";
    /**
     * QS6: el “flash” a la OEM al cerrar suele ser de pocos segundos. Mantenerlo corto para que
     * el botón RADIO vuelva a abrir OpenRadioFM casi inmediatamente tras PowerOff.
     */
    private static final long SUPPRESS_AFTER_POWEROFF_MS = 6_000L;
    /** Menos frecuente = menos escrituras en flash (estabilidad en head units con eMMC lenta). */
    private static final long HEARTBEAT_INTERVAL_MS = 90_000L;
    private final android.os.Handler mHbHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final java.lang.Runnable mHeartbeat = new java.lang.Runnable() {
        @Override public void run() {
            try {
                SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
                p.edit().putLong(PREF_HIHACK_HEARTBEAT_MS, System.currentTimeMillis()).apply();
            } catch (Exception ignored) {}
            try {
                mHbHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
            } catch (Exception ignored) {}
        }
    };

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
                if (currentTime - lastLaunchTime <= COOLDOWN_MS) {
                    return;
                }
                if (shouldSuppressHijack()) {
                    return;
                }
                lastLaunchTime = currentTime;
                Log.i(TAG, "¡Radio de fábrica detectada! Interceptando e iniciando OpenRadioFM...");
                launchOpenRadioFM();
            }
        }
    }

    /**
     * Evita bucle al apagar/cerrar: pantalla ya en sleep, o el usuario acaba de cerrar OpenRadioFM
     * y la OEM parpadea un instante.
     */
    private boolean shouldSuppressHijack() {
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isInteractive()) {
                Log.d(TAG, "Hijack omitido: pantalla no interactiva (sleep/apagado)");
                return true;
            }
        } catch (Exception e) {
            Log.w(TAG, "shouldSuppressHijack: PowerManager", e);
        }
        try {
            SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
            long until = p.getLong(PREF_HIHACK_SUPPRESS_UNTIL_MS, 0L);
            if (until > 0L) {
                long now = System.currentTimeMillis();
                if (now < until) {
                    String reason = p.getString(PREF_HIHACK_SUPPRESS_REASON, "");
                    // Suprimir solo si el motivo fue PowerOff (cierre “total”).
                    if (REASON_POWEROFF.equals(reason)) {
                        Log.d(TAG, "Hijack omitido: ventana post-poweroff (evita bucle con OEM)");
                        return true;
                    }
                } else {
                    // Ventana expirada: limpiar claves para no dejar supresión latente.
                    p.edit()
                            .remove(PREF_HIHACK_SUPPRESS_UNTIL_MS)
                            .remove(PREF_HIHACK_SUPPRESS_REASON)
                            .apply();
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    /** Llamar al pulsar el botón PowerOff (cierre total). */
    public static void markPowerOffForHijack(Context context) {
        try {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(PREF_HIHACK_SUPPRESS_REASON, REASON_POWEROFF)
                    .putLong(PREF_HIHACK_SUPPRESS_UNTIL_MS, System.currentTimeMillis() + SUPPRESS_AFTER_POWEROFF_MS)
                    .apply();
        } catch (Exception ignored) {}
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
            intent.putExtra(EXTRA_FROM_HIJACKER, true);
            
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
     * {@link MainActivity#isMainActivityResumed()} es false (p. ej. launcher al frente) y el motor es K706 o QS6.
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        try {
            if (event == null) return false;
            final boolean resumed = MainActivity.isMainActivityResumed();
            final boolean bridge = MainActivity.isWheelMediaBridgeActive();
            SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
            if (!p.getBoolean(PREF_FORWARD_MEDIA_KEYS, true)) {
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

            // Log siempre visible: en muchas ROM el usuario filtra por INFO y no ve DEBUG.
            try {
                Log.i(TAG, "onKeyEvent MEDIA keyCode=" + code
                        + " action=" + event.getAction()
                        + " resumed=" + resumed
                        + " bridge=" + bridge);
            } catch (Exception ignored) {}

            // Solo reenviar cuando la UI no está al frente; si la Activity está en foreground,
            // dejamos que su propio onKeyDown/onKeyUp lo gestione.
            if (resumed) return false;
            if (!bridge) return false;
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

            Intent svc = new Intent(this, RadioMediaService.class);
            svc.setAction(Intent.ACTION_MEDIA_BUTTON);
            svc.putExtra(Intent.EXTRA_KEY_EVENT, event);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            Log.i(TAG, "MEDIA key reenviada a RadioMediaService (keyCode=" + code + ")");
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
        // K706/QS6 OEM: asegurar que se solicita filtrado de teclas aunque algunas ROM ignoren el XML.
        // Esto es imprescindible para capturar KEYCODE_MEDIA_* cuando el launcher los consume como KeyEvent.
        try {
            AccessibilityServiceInfo info = getServiceInfo();
            if (info != null) {
                info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
                // Algunas ROM solo envían onKeyEvent si el servicio no está “demasiado” limitado en eventTypes.
                // No cambia nuestra lógica (seguimos filtrando a TYPE_WINDOW_STATE_CHANGED), pero mejora compatibilidad.
                info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
                setServiceInfo(info);
            }
        } catch (Exception e) {
            Log.w(TAG, "onServiceConnected: no se pudo solicitar filterKeyEvents", e);
        }
        // Activar bridge por defecto: el guard basado en mode puede estar aún sin detectar en arranques en frío.
        try {
            MainActivity.setWheelMediaBridgeActive(true);
        } catch (Exception ignored) {}
        // Start heartbeat
        try { mHbHandler.removeCallbacks(mHeartbeat); } catch (Exception ignored) {}
        try { mHbHandler.post(mHeartbeat); } catch (Exception ignored) {}
        Log.i(TAG, "FactoryRadioHijackerService conectado y monitorizando");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try { mHbHandler.removeCallbacks(mHeartbeat); } catch (Exception ignored) {}
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        try { mHbHandler.removeCallbacks(mHeartbeat); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
