package com.example.openradiofm.ui.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * V16: Gestor del Modo Nocturno.
 * Encapsula la detección horaria/sistema y la aplicación de colores azul noche
 * estilo Android Auto / Google Maps.
 */
public class NightModeManager {
    private static final String TAG = "NightModeManager";

    private final Activity mActivity;
    private final SharedPreferences mPrefs;
    private final NightModeListener mListener;

    /**
     * Listener para que MainActivity ejecute acciones que requieren su contexto directo
     * (como updateFrequencyDisplay que depende de lógica interna de favoritos/presets).
     */
    public interface NightModeListener {
        void onRefreshFrequencyDisplay(int freq);
    }

    public NightModeManager(Activity activity, SharedPreferences prefs, NightModeListener listener) {
        this.mActivity = activity;
        this.mPrefs = prefs;
        this.mListener = listener;
    }

    /**
     * Comprueba si el modo noche automático está activo y, si es de noche,
     * aplica el skin NIGHT_MODE.
     */
    public void checkAndApplyNightMode() {
        boolean autoNight = mPrefs.getBoolean("pref_night_mode_auto", false);

        // V16.2: Usar instancia compartida de ThemeManager
        if (mActivity instanceof MainActivity) {
            MainActivity main = (MainActivity) mActivity;
            if (main.mThemeManager != null) {
                ThemeManager.Skin savedSkin = main.mThemeManager.getCurrentSkin();

                if (autoNight && isNightTime()) {
                    // Apply without overwriting the user's saved preference
                    main.applySkin(ThemeManager.Skin.NIGHT_MODE);
                } else {
                    // Revert to user's saved skin when day time or auto disabled
                    main.applySkin(savedSkin);
                }
            }
        }
    }

    /**
     * Determina si es horario nocturno:
     * 1. Prioridad: modo noche del sistema Android.
     * 2. Fallback: rango horario configurable (por defecto 19h-7h).
     */
    public boolean isNightTime() {
        // 1. Time Based (configurable, default 19h-7h) - HIGHER PRIORITY to follow user prefs
        int startHour = mPrefs.getInt("pref_night_start", 19);
        int endHour = mPrefs.getInt("pref_night_end", 7);
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        
        boolean isTimeRangeNight;
        if (startHour > endHour) {
            // Overnight range (e.g. 19-7)
            isTimeRangeNight = (hour >= startHour || hour < endHour);
        } else {
            // Same-day range (e.g. 22-23)
            isTimeRangeNight = (hour >= startHour && hour < endHour);
        }
        
        if (isTimeRangeNight) return true;

        // 2. Fallback: Check System UI Mode (ILL cable trigger)
        int nightModeFlags = mActivity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
    }

    /**
     * Aplica colores azul noche a textos, iconos y botones.
     * Debe llamarse cuando el skin activo es NIGHT_MODE.
     *
     * @param lastFreq Última frecuencia conocida para refrescar la visualización.
     */
    public void applyNightModeColors(int lastFreq) {
        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);

        // Textos principales en azul noche
        TextView tvRdsName = mActivity.findViewById(R.id.tvRdsName);
        TextView tvRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);
        TextView tvPty = mActivity.findViewById(R.id.tvPty);

        // Refreshar frecuencia (respeta colores de favoritos en Night Mode)
        if (lastFreq != -1 && mListener != null) {
            mListener.onRefreshFrequencyDisplay(lastFreq);
        }

        if (tvRdsName != null) tvRdsName.setTextColor(nightBlue);
        if (tvRdsInfo != null) tvRdsInfo.setTextColor(nightBlue);
        if (tvPty != null) tvPty.setTextColor(nightBlue);

        // V18.6: Reloj Digital en azul noche
        TextView tvClock = mActivity.findViewById(R.id.tvDigitalClock);
        if (tvClock != null) tvClock.setTextColor(nightBlue);

        // Icono de banda FM
        ImageView ivBandIndicator = mActivity.findViewById(R.id.ivBandIndicator);
        if (ivBandIndicator != null) {
            ivBandIndicator.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Iconos de estado y PTY
        tintImageView(R.id.ivStereoIcon, nightBlue);
        tintImageView(R.id.ivAfIcon, nightBlue);
        tintImageView(R.id.ivTaIcon, nightBlue);
        tintImageView(R.id.ivTpIcon, nightBlue);
        
        // V17: El ID real del icono es ivDataActivityIcon, el wrapper FrameLayout es ivDataActivity.
        // tintImageView ahora es seguro contra casts.
        tintImageView(R.id.ivDataActivityIcon, nightBlue);
        tintImageView(R.id.ivDataActivity, nightBlue);

        // Botones de control
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff // V16.1: PowerOff night
        };
        for (int id : buttonIds) {
            ImageButton btn = mActivity.findViewById(id);
            if (btn != null) {
                btn.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        // Presets en azul noche
        for (int i = 1; i <= 12; i++) {
            int tvId = mActivity.getResources().getIdentifier("tvP" + i, "id", mActivity.getPackageName());
            TextView tv = mActivity.findViewById(tvId);
            if (tv != null) tv.setTextColor(nightBlue);
        }
    }

    /**
     * Restaura colores originales cuando se sale del modo nocturno.
     *
     * @param lastFreq Última frecuencia conocida para refrescar la visualización.
     */
    public void resetNightModeColors(int lastFreq) {
        int white = mActivity.getResources().getColor(R.color.white, null);

        // Restaurar textos
        TextView tvRdsName = mActivity.findViewById(R.id.tvRdsName);
        TextView tvRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);
        TextView tvPty = mActivity.findViewById(R.id.tvPty);

        // Refrescar frecuencia
        if (lastFreq != -1 && mListener != null) {
            mListener.onRefreshFrequencyDisplay(lastFreq);
        }

        if (tvRdsName != null) tvRdsName.setTextColor(white);
        if (tvRdsInfo != null) tvRdsInfo.setTextColor(white);
        if (tvPty != null) tvPty.setTextColor(white);

        // V18.6: Restaurar reloj a blanco
        TextView tvClock = mActivity.findViewById(R.id.tvDigitalClock);
        if (tvClock != null) tvClock.setTextColor(white);

        // Restaurar iconos
        ImageView ivBandIndicator = mActivity.findViewById(R.id.ivBandIndicator);
        if (ivBandIndicator != null) ivBandIndicator.clearColorFilter();

        ImageView ivFavoriteIndicator = mActivity.findViewById(R.id.ivFavoriteIndicator);
        if (ivFavoriteIndicator != null) ivFavoriteIndicator.clearColorFilter();

        ImageView ivUnitLabel = mActivity.findViewById(R.id.ivUnitLabel);
        if (ivUnitLabel != null) ivUnitLabel.clearColorFilter();

        clearImageViewFilter(R.id.ivStereoIcon);
        clearImageViewFilter(R.id.ivAfIcon);
        clearImageViewFilter(R.id.ivTaIcon);
        clearImageViewFilter(R.id.ivTpIcon);
        
        // V17: El ID real del icono es ivDataActivityIcon, el wrapper FrameLayout es ivDataActivity
        clearImageViewFilter(R.id.ivDataActivityIcon);
        clearImageViewFilter(R.id.ivDataActivity); // Safe helper handles FrameLayout too now

        // Botones
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff // V16.1: PowerOff night
        };
        for (int id : buttonIds) {
            ImageButton btn = mActivity.findViewById(id);
            if (btn != null) btn.clearColorFilter();
        }

        // Restaurar presets a blanco
        for (int i = 1; i <= 12; i++) {
            int tvId = mActivity.getResources().getIdentifier("tvP" + i, "id", mActivity.getPackageName());
            TextView tv = mActivity.findViewById(tvId);
            if (tv != null) tv.setTextColor(white);
        }
    }

    // --- Helpers privados ---

    private void tintImageView(int resId, int color) {
        android.view.View v = mActivity.findViewById(resId);
        if (v instanceof ImageView) {
            ((ImageView) v).setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void clearImageViewFilter(int resId) {
        android.view.View v = mActivity.findViewById(resId);
        if (v instanceof ImageView) {
            ((ImageView) v).clearColorFilter();
        }
    }
}
