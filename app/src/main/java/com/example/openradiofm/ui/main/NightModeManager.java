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

        // V2.6: Si auto night NO está habilitado, no interferir con la selección manual.
        // El usuario puede haber seleccionado NIGHT_MODE manualmente y no debemos revertirlo.
        if (!autoNight) return;

        // V16.2: Usar instancia compartida de ThemeManager
        if (mActivity instanceof MainActivity) {
            MainActivity main = (MainActivity) mActivity;
            if (main.mThemeManager != null) {
                ThemeManager.Skin savedSkin = main.mThemeManager.getCurrentSkin();

                if (isNightTime()) {
                    // Apply without overwriting the user's saved preference
                    main.applySkin(ThemeManager.Skin.NIGHT_MODE);
                } else {
                    // Revert to user's saved skin when day time
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
        // Time Based (configurable, default 19:00-07:00, con minutos)
        int startHour = mPrefs.getInt("pref_night_start", 19);
        int startMin = mPrefs.getInt("pref_night_start_min", 0);
        int endHour = mPrefs.getInt("pref_night_end", 7);
        int endMin = mPrefs.getInt("pref_night_end_min", 0);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
        int startMinutes = startHour * 60 + startMin;
        int endMinutes = endHour * 60 + endMin;

        boolean isTimeRangeNight;
        if (startMinutes > endMinutes) {
            // Overnight range (e.g. 19:00-07:00)
            isTimeRangeNight = (nowMinutes >= startMinutes || nowMinutes < endMinutes);
        } else {
            // Same-day range (e.g. 22:00-23:30)
            isTimeRangeNight = (nowMinutes >= startMinutes && nowMinutes < endMinutes);
        }
        return isTimeRangeNight;
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
        
        // Cloud (ivDataActivityIcon): no teñir aquí — rojo/amarillo/azul en idle lo aplica
        // MainActivity.updateDataActivityUI() tras applyNightModeColors (evita pisar streaming).

        // Botones de control
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff // V16.1: PowerOff night
        };
        for (int id : buttonIds) {
            android.view.View btn = mActivity.findViewById(id);
            if (btn instanceof ImageView && mActivity instanceof MainActivity) {
                ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) btn, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (btn instanceof ImageButton) {
                ((ImageButton) btn).setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        // Presets: texto en azul noche, logos opcionalmente teñidos según preferencia
        boolean tintLogos = mPrefs.getBoolean("pref_night_logos", true);
        for (int i = 1; i <= 18; i++) {
            int tvId = mActivity.getResources().getIdentifier("tvP" + i, "id", mActivity.getPackageName());
            TextView tv = mActivity.findViewById(tvId);
            if (tv != null) tv.setTextColor(nightBlue);

            int ivId = mActivity.getResources().getIdentifier("ivP" + i, "id", mActivity.getPackageName());
            android.view.View ivView = mActivity.findViewById(ivId);
            if (ivView instanceof ImageView) {
                if (tintLogos) {
                    if (mActivity instanceof MainActivity) {
                        ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) ivView, nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        ((ImageView) ivView).setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    if (mActivity instanceof MainActivity) {
                        ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) ivView, null, null);
                    } else {
                        ((ImageView) ivView).clearColorFilter();
                    }
                }
            }
        }
    }

    /**
     * Restaura colores originales cuando se sale del modo nocturno.
     *
     * @param lastFreq Última frecuencia conocida para refrescar la visualización.
     */
    public void resetNightModeColors(int lastFreq) {
        boolean isLight = false;
        if (mActivity instanceof MainActivity) {
            MainActivity main = (MainActivity) mActivity;
            isLight = (main.mThemeManager != null && main.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        }
        int normalText = isLight ? android.graphics.Color.BLACK : mActivity.getResources().getColor(R.color.white, null);

        // Restaurar textos
        TextView tvRdsName = mActivity.findViewById(R.id.tvRdsName);
        TextView tvRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);
        TextView tvPty = mActivity.findViewById(R.id.tvPty);

        // Refrescar frecuencia
        if (lastFreq != -1 && mListener != null) {
            mListener.onRefreshFrequencyDisplay(lastFreq);
        }

        if (tvRdsName != null) tvRdsName.setTextColor(normalText);
        if (tvRdsInfo != null) tvRdsInfo.setTextColor(normalText);
        if (tvPty != null) tvPty.setTextColor(normalText);

        // V18.6: Restaurar reloj a blanco
        TextView tvClock = mActivity.findViewById(R.id.tvDigitalClock);
        if (tvClock != null) tvClock.setTextColor(normalText);

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
        
        // Cloud: estado (idle/streaming) en MainActivity.updateDataActivityUI()

        // Botones
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff // V16.1: PowerOff night
        };
        for (int id : buttonIds) {
            android.view.View btn = mActivity.findViewById(id);
            if (btn instanceof ImageView && mActivity instanceof MainActivity) {
                ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) btn, null, null);
            } else if (btn instanceof ImageButton) {
                ((ImageButton) btn).clearColorFilter();
            }
        }

        // Restaurar presets a blanco
        for (int i = 1; i <= 18; i++) {
            int tvId = mActivity.getResources().getIdentifier("tvP" + i, "id", mActivity.getPackageName());
            TextView tv = mActivity.findViewById(tvId);
            if (tv != null) tv.setTextColor(normalText);

             int ivId = mActivity.getResources().getIdentifier("ivP" + i, "id", mActivity.getPackageName());
             android.view.View ivView = mActivity.findViewById(ivId);
             if (ivView instanceof ImageView && mActivity instanceof MainActivity) {
                 ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) ivView, null, null);
             } else if (ivView instanceof ImageView) {
                 ((ImageView) ivView).clearColorFilter();
             }
        }
    }

    // --- Helpers privados ---

    private void tintImageView(int resId, int color) {
        android.view.View v = mActivity.findViewById(resId);
        if (v instanceof ImageView && mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) v, color, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (v instanceof ImageView) {
            ((ImageView) v).setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void clearImageViewFilter(int resId) {
        android.view.View v = mActivity.findViewById(resId);
        if (v instanceof ImageView && mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) v, null, null);
        } else if (v instanceof ImageView) {
            ((ImageView) v).clearColorFilter();
        }
    }

}
