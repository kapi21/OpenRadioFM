package com.example.openradiofm.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.util.UUID;

/**
 * V19.5: Utilidad para obtener identificadores únicos del dispositivo.
 * Se utiliza para registrar las unidades en Supabase.
 */
public class DeviceMetadataUtils {
    private static final String TAG = "DeviceMetadataUtils";
    private static final String PREFS_NAME = "HardwareConfig";
    private static final String KEY_INSTALL_ID = "unique_install_id";

    /**
     * Obtiene un identificador único para la unidad actual.
     * Prioridad:
     * 1. Android ID (Settings.Secure)
     * 2. UUID persistente generado por la app (si fallan otros métodos)
     */
    public static String getUniqueDeviceId(Context context) {
        try {
            // 1. Android ID: Persistente tras reinicios, cambia con Factory Reset.
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId != null && !androidId.equals("9774d56d682e549c") && !androidId.isEmpty()) {
                return "AND_" + androidId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo Android ID", e);
        }

        // 2. Fallback: UUID persistente en SharedPreferences
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedId = prefs.getString(KEY_INSTALL_ID, null);
        
        if (savedId == null) {
            savedId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            prefs.edit().putString(KEY_INSTALL_ID, savedId).apply();
            Log.d(TAG, "Nuevo UUID generado para esta unidad: " + savedId);
        }
        
        return "UUID_" + savedId;
    }
}
