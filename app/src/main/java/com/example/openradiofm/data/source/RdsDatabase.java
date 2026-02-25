package com.example.openradiofm.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * V1.0: Base de datos ligera para mapear códigos RDS PI a nombres PS.
 * Permite identificación instantánea antes de recibir el texto por aire.
 */
public class RdsDatabase {
    private static final String TAG = "RdsDatabase";
    private static final String PREF_NAME = "rds_pi_database";
    private final SharedPreferences mPrefs;

    public RdsDatabase(Context context) {
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Obtiene el nombre guardado para un código PI.
     */
    public String getNameForPi(String piCode) {
        if (piCode == null || piCode.isEmpty()) return null;
        String name = mPrefs.getString(piCode, null);
        if (name != null) {
            Log.d(TAG, "PI Lookup HIT: " + piCode + " -> " + name);
        }
        return name;
    }

    /**
     * Guarda o actualiza el nombre asociado a un código PI.
     */
    public void savePiName(String piCode, String name) {
        if (piCode == null || piCode.isEmpty() || name == null || name.isEmpty()) return;
        
        // Evitar guardar textos genéricos o errores
        if (name.trim().length() < 2 || name.contains("----")) return;

        String oldName = mPrefs.getString(piCode, null);
        if (name.equals(oldName)) return;

        mPrefs.edit().putString(piCode, name).apply();
        Log.d(TAG, "PI Learned: " + piCode + " -> " + name + (oldName != null ? " (was " + oldName + ")" : ""));
    }

    /**
     * Guarda la ruta del logo asociada a un código PI.
     */
    public void savePiLogo(String piCode, String logoPath) {
        if (piCode == null || piCode.isEmpty() || logoPath == null) return;
        mPrefs.edit().putString(piCode + "_logo", logoPath).apply();
        Log.d(TAG, "PI Logo Associated: " + piCode + " -> " + logoPath);
    }

    /**
     * Obtiene la ruta del logo guardada para un código PI.
     */
    public String getLogoForPi(String piCode) {
        if (piCode == null || piCode.isEmpty()) return null;
        return mPrefs.getString(piCode + "_logo", null);
    }
    
    /**
     * Limpia la base de datos (para mantenimiento).
     */
    public void clear() {
        mPrefs.edit().clear().apply();
    }
}
