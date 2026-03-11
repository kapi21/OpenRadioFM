package com.example.openradiofm.ui.theme;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import com.example.openradiofm.R;

/**
 * V16.2: Sistema de Skins para OpenRadioFM.
 *
 * Gestiona la persistencia, el drawable actual y la aplicación visual
 * del skin a todas las vistas de la interfaz.
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
        WHITE("White", "#FFFFFF"),
        GREY("Grey", "#757575");

        public final String displayName;
        public final String colorHex;

        Skin(String displayName, String colorHex) {
            this.displayName = displayName;
            this.colorHex = colorHex;
        }
    }

    /**
     * Listener para que el manager notifique al host cuando necesita
     * ejecutar lógica que depende del contexto de la Activity
     * (como aplicar night mode colors que vive en NightModeManager).
     */
    public interface SkinAppliedListener {
        void onSkinApplied(Skin skin);
    }

    private final SharedPreferences prefs;
    private final Activity mActivity;
    private SharedPreferences mLayoutPrefs;
    private SkinAppliedListener mListener;
    private Skin mCurrentSkin = Skin.CLASSIC_GRAY;

    public ThemeManager(android.content.Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        this.mActivity = (context instanceof Activity) ? (Activity) context : null;
        this.mLayoutPrefs = null; // Se establece con setLayoutPrefs()
    }

    /** V16.2: Establece las SharedPreferences del layout (las mismas que usa MainActivity). */
    public void setLayoutPrefs(SharedPreferences layoutPrefs) {
        this.mLayoutPrefs = layoutPrefs;
    }

    /** Registra listener para recibir notificación post-applySkin. */
    public void setSkinAppliedListener(SkinAppliedListener listener) {
        this.mListener = listener;
    }

    // ==================== Persistencia ====================

    public Skin getCurrentSkin() {
        String skinName = prefs.getString(KEY_SKIN, Skin.CLASSIC_GRAY.name());
        try {
            return Skin.valueOf(skinName);
        } catch (IllegalArgumentException e) {
            return Skin.CLASSIC_GRAY;
        }
    }

    public void setSkin(Skin skin) {
        prefs.edit().putString(KEY_SKIN, skin.name()).apply();
    }

    public Skin cycleSkin() {
        Skin current = getCurrentSkin();
        Skin[] all = Skin.values();
        int nextIndex = (current.ordinal() + 1) % all.length;
        Skin next = all[nextIndex];
        setSkin(next);
        return next;
    }

    public int getAccentColor() {
        return android.graphics.Color.parseColor(getCurrentSkin().colorHex);
    }

    // ==================== Skin activo en memoria ====================

    /** Devuelve el skin actualmente aplicado (en memoria). */
    public Skin getActiveSkin() {
        return mCurrentSkin;
    }

    // ==================== Drawable del Skin ====================

    /**
     * Devuelve el drawable resource ID correspondiente al skin activo.
     */
    public int getSkinDrawableId() {
        if (mCurrentSkin == null)
            return R.drawable.bg_glass_card_premium;
        switch (mCurrentSkin) {
            case NIGHT_MODE:
                return R.drawable.bg_glass_card_night;
            case ORANGE:
                return R.drawable.bg_glass_card_orange;
            case BLUE:
                return R.drawable.bg_glass_card_blue;
            case GREEN:
                return R.drawable.bg_glass_card_green;
            case PURPLE:
                return R.drawable.bg_glass_card_purple;
            case RED:
                return R.drawable.bg_glass_card_red;
            case YELLOW:
                return R.drawable.bg_glass_card_yellow;
            case CYAN:
                return R.drawable.bg_glass_card_cyan;
            case PINK:
                return R.drawable.bg_glass_card_pink;
            case WHITE:
                return R.drawable.bg_glass_card_white;
            case GREY:
                return R.drawable.bg_glass_card_classic;
            default:
                return R.drawable.bg_glass_card_premium;
        }
    }

    // ==================== Aplicación Visual del Skin ====================

    /**
     * V16.2: Aplica el skin seleccionado a todos los elementos de la interfaz.
     * Migrado desde MainActivity.applySkin().
     */
    public void applySkin(Skin skin) {
        if (mActivity == null) return;
        if (this.mCurrentSkin == skin && skin != Skin.NIGHT_MODE) {
            // V16.x: Optimización: si es el mismo skin, no re-aplicamos drawables.
            // Excepción: NIGHT_MODE siempre se aplica para refrescar tintes dinámicos.
            return;
        }

        this.mCurrentSkin = skin;
        int drawableId = getSkinDrawableId();

        // Detectar layout activo
        boolean isLayoutV3 = mLayoutPrefs != null && mLayoutPrefs.getBoolean("pref_layout_v3", false);
        boolean isSimpleLayout = mLayoutPrefs != null && mLayoutPrefs.getBoolean("pref_layout_simple", false);

        // Aplicar bordes del skin SOLO en Layout V2 (Vertical estándar)
        if (!isLayoutV3 && !isSimpleLayout) {
            int[] viewIds = {
                    R.id.boxFrequency, R.id.boxIconsTopLayout2, 
                    R.id.btnSeekUp, R.id.btnSeekDown,
                    R.id.btnFavPrev, R.id.btnFavNext,
                    R.id.tvRdsName, R.id.tvRdsInfo,
                    R.id.btnBand, R.id.btnAutoScan,
                    R.id.boxLogo,
                    R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                    R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff
            };

            for (int id : viewIds) {
                View v = mActivity.findViewById(id);
                if (v != null) {
                    int pL = v.getPaddingLeft();
                    int pT = v.getPaddingTop();
                    int pR = v.getPaddingRight();
                    int pB = v.getPaddingBottom();
                    v.setBackgroundResource(drawableId);
                    v.setPadding(pL, pT, pR, pB);
                }
            }
        } else {
            // Layout V3: RDS boxes sin borde (borderless)
            int[] v3BoxIds = { R.id.tvRdsName, R.id.tvRdsInfo };
            for (int id : v3BoxIds) {
                View v = mActivity.findViewById(id);
                if (v != null) {
                    int pL = v.getPaddingLeft();
                    int pT = v.getPaddingTop();
                    int pR = v.getPaddingRight();
                    int pB = v.getPaddingBottom();
                    v.setBackground(null);
                    v.setPadding(pL, pT, pR, pB);
                }
            }
        }

        // Aplicar a Presets P1-P12
        for (int i = 1; i <= 12; i++) {
            int id = mActivity.getResources().getIdentifier("cardP" + i, "id", mActivity.getPackageName());
            View v = mActivity.findViewById(id);
            if (v != null)
                v.setBackgroundResource(drawableId);
        }

        // Notificar al listener (Night Mode colors, etc.)
        if (mListener != null) {
            mListener.onSkinApplied(skin);
        }
    }
}
