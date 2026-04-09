package com.example.openradiofm.ui.main;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * Modo Día (skin): texto/íconos oscuros sobre fondo blanco.
 * Se mantiene separado de NightModeManager para poder deshabilitarlo fácilmente.
 */
public final class DayModeManager {
    private static final String TAG = "DayModeManager";

    private final Activity mActivity;
    private final android.content.SharedPreferences mPrefs;
    private final NightModeManager.NightModeListener mListener;

    public DayModeManager(Activity activity,
                          android.content.SharedPreferences prefs,
                          NightModeManager.NightModeListener listener) {
        this.mActivity = activity;
        this.mPrefs = prefs;
        this.mListener = listener;
    }

    /** Aplica tintes negros en DAY_MODE, siguiendo la misma filosofía que NightModeManager. */
    public void applyDayModeColors(int lastFreq) {
        if (!(mActivity instanceof MainActivity)) return;
        MainActivity main = (MainActivity) mActivity;
        if (main.mThemeManager == null || main.mThemeManager.getActiveSkin() != ThemeManager.Skin.DAY_MODE) return;
        // No tocar Simple Layout (mantener comportamiento original). Layout 2 y Layout 3 sí.
        if (main.mIsSimpleLayout) return;

        final int dayBlack = Color.BLACK;
        final int dayDim = Color.parseColor("#FF555555");

        // Refrescar frecuencia (igual que noche, pero a negro)
        if (lastFreq != -1 && mListener != null) {
            try { mListener.onRefreshFrequencyDisplay(lastFreq); } catch (Exception ignored) {}
        }

        // Textos principales
        tintText(R.id.tvFrequency, dayBlack); // En V3 el PS se muestra aquí
        tintText(R.id.tvRdsName, dayBlack);
        tintText(R.id.tvRdsInfo, dayBlack);
        tintText(R.id.tvPty, dayBlack);
        tintText(R.id.ivBandIndicator, dayBlack);
        tintText(R.id.ivStereoIcon, dayBlack);
        tintText(R.id.ivUnitLabel, dayBlack);

        // Iconos estado
        tintImage(R.id.ivAfIcon, dayDim);
        tintImage(R.id.ivTaIcon, dayDim);
        tintImage(R.id.ivTpIcon, dayDim);
        tintImage(R.id.ivSignalLevel, dayBlack);
        tintImage(R.id.ivFavoriteIndicator, dayBlack);
        // Cloud: color lo decide MainActivity.updateDataActivityUI() para no pisar streaming.

        // Botones
        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff
        };
        for (int id : buttonIds) {
            View btn = mActivity.findViewById(id);
            if (btn instanceof ImageView) {
                ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) btn, dayBlack, PorterDuff.Mode.SRC_IN);
            }
        }

        // Presets: texto negro; logos no se tiñen (se mantienen según icon pack).
        for (int i = 1; i <= 18; i++) {
            int tvId = mActivity.getResources().getIdentifier("tvP" + i, "id", mActivity.getPackageName());
            TextView tv = mActivity.findViewById(tvId);
            if (tv != null) tv.setTextColor(dayBlack);
        }
    }

    public void resetDayModeColors(int lastFreq) {
        if (!(mActivity instanceof MainActivity)) return;
        MainActivity main = (MainActivity) mActivity;
        if (main.mIsSimpleLayout) return;

        // Al salir de DAY_MODE, el resto de skins siguen el esquema actual (texto blanco salvo CLEAR).
        boolean isLight = (main.mThemeManager != null && main.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normalText = isLight ? Color.BLACK : mActivity.getResources().getColor(R.color.white, null);

        if (lastFreq != -1 && mListener != null) {
            try { mListener.onRefreshFrequencyDisplay(lastFreq); } catch (Exception ignored) {}
        }

        tintText(R.id.tvRdsName, normalText);
        tintText(R.id.tvRdsInfo, normalText);
        tintText(R.id.tvPty, normalText);
        tintText(R.id.ivBandIndicator, normalText);
        tintText(R.id.ivStereoIcon, normalText);
        tintText(R.id.ivUnitLabel, normalText);
        tintText(R.id.tvFrequency, normalText);

        clearImage(R.id.ivAfIcon);
        clearImage(R.id.ivTaIcon);
        clearImage(R.id.ivTpIcon);
        clearImage(R.id.ivSignalLevel);
        clearImage(R.id.ivFavoriteIndicator);

        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown, R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff
        };
        for (int id : buttonIds) {
            View btn = mActivity.findViewById(id);
            if (btn instanceof ImageView) {
                ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) btn, null, null);
            }
        }

        for (int i = 1; i <= 18; i++) {
            int tvId = mActivity.getResources().getIdentifier("tvP" + i, "id", mActivity.getPackageName());
            TextView tv = mActivity.findViewById(tvId);
            if (tv != null) tv.setTextColor(normalText);
        }
    }

    private void tintText(int id, int color) {
        View v = mActivity.findViewById(id);
        if (v instanceof TextView) ((TextView) v).setTextColor(color);
    }

    private void tintImage(int id, int color) {
        View v = mActivity.findViewById(id);
        if (v instanceof ImageView && mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) v, color, PorterDuff.Mode.SRC_IN);
        } else if (v instanceof ImageView) {
            ((ImageView) v).setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    private void clearImage(int id) {
        View v = mActivity.findViewById(id);
        if (v instanceof ImageView && mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setColorFilterIfChanged((ImageView) v, null, null);
        } else if (v instanceof ImageView) {
            ((ImageView) v).clearColorFilter();
        }
    }
}

