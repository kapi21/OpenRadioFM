package com.example.openradiofm.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Sistema de Skins para OpenRadioFM.
 * 
 * Permite cambiar dinámicamente el color de acento de la interfaz.
 * Los colores se guardan en SharedPreferences y se aplican en tiempo de
 * ejecución.
 */
public class ThemeManager {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_SKIN = "selected_skin";

    // Skins disponibles
    public enum Skin {
        NIGHT_MODE("Night Mode", "#2A2A2A"),
        CLASSIC_GRAY("Classic Gray", "#B0B0B0"),
        ORANGE("Orange", "#FF8C00"),
        BLUE("Blue", "#00A8FF"),
        GREEN("Green", "#00D68F"),
        PURPLE("Purple", "#A855F7"),
        RED("Red", "#FF4444"),
        YELLOW("Yellow", "#FFD700"),
        CYAN("Cyan", "#00CED1"),
        PINK("Pink", "#FF69B4"),
        WHITE("White", "#FFFFFF");

        public final String displayName;
        public final String colorHex;

        Skin(String displayName, String colorHex) {
            this.displayName = displayName;
            this.colorHex = colorHex;
        }
    }

    private final SharedPreferences prefs;

    public ThemeManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Obtiene el skin actualmente seleccionado.
     */
    public Skin getCurrentSkin() {
        String skinName = prefs.getString(KEY_SKIN, Skin.CLASSIC_GRAY.name());
        try {
            return Skin.valueOf(skinName);
        } catch (IllegalArgumentException e) {
            return Skin.CLASSIC_GRAY;
        }
    }

    /**
     * Guarda el skin seleccionado.
     */
    public void setSkin(Skin skin) {
        prefs.edit().putString(KEY_SKIN, skin.name()).apply();
    }

    /**
     * Cicla al siguiente skin disponible.
     */
    public Skin cycleSkin() {
        Skin current = getCurrentSkin();
        Skin[] all = Skin.values();
        int nextIndex = (current.ordinal() + 1) % all.length;
        Skin next = all[nextIndex];
        setSkin(next);
        return next;
    }

    /**
     * Obtiene el color de acento actual en formato int (para usar en código).
     */
    public int getAccentColor() {
        return android.graphics.Color.parseColor(getCurrentSkin().colorHex);
    }
}
