package com.example.openradiofm.ui.main;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * V21.0: Controlador para el Layout V3 (Horizontal Premium).
 */
public class V3LayoutController extends BaseLayoutController {
    private TextView tvFrequency, tvRdsInfo, tvPty;
    private TextView ivBandIndicator;
    private ImageView ivSignalLevel;
    private TextView ivUnitLabel;
    private ImageView ivAfIcon, ivTaIcon, ivTpIcon;
    private ImageView ivFavoriteIndicator;
    private TextView ivStereoIcon;
    private ImageView ivMainLogo;

    public V3LayoutController(MainActivity activity) {
        super(activity);
    }

    @Override
    public void initViews(View root) {
        tvFrequency = root.findViewById(R.id.tvFrequency);
        tvRdsInfo = root.findViewById(R.id.tvRdsInfo);
        tvPty = root.findViewById(R.id.tvPty);

        ivBandIndicator = root.findViewById(R.id.ivBandIndicator);
        ivSignalLevel = root.findViewById(R.id.ivSignalLevel);
        ivUnitLabel = root.findViewById(R.id.ivUnitLabel);
        
        ivAfIcon = root.findViewById(R.id.ivAfIcon);
        ivTaIcon = root.findViewById(R.id.ivTaIcon);
        ivTpIcon = root.findViewById(R.id.ivTpIcon);

        ivFavoriteIndicator = root.findViewById(R.id.ivFavoriteIndicator);
        ivStereoIcon = root.findViewById(R.id.ivStereoIcon);
        ivMainLogo = root.findViewById(R.id.ivMainLogo);

        // V3: el PS se renderiza en tvFrequency; nunca queremos arte debajo.
        if (ivMainLogo != null) {
            ivMainLogo.setImageDrawable(null);
            ivMainLogo.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateFrequency(int freq, String ps, boolean isAm) {
        if (tvFrequency != null) {
            String text;
            if (ps != null && !ps.isEmpty()) {
                text = ps;
            } else if (isAm) {
                text = String.valueOf(freq);
            } else {
                text = String.format(java.util.Locale.US, "%.1f", freq / 1000.0);
            }
            MainActivity.setTextIfChanged(tvFrequency, text);
        }
    }

    @Override
    public void updateRDS(String text) {
        // En V3Layout el nombre PS ya se muestra en el campo de frecuencia
        // via updateFrequency(). Ignoramos aquí para evitar duplicación.
    }

    @Override
    public void updateRDSText(String text) {
        MainActivity.setTextIfChanged(tvRdsInfo, text);
    }

    @Override
    public void updatePTY(String pty) {
        String label = com.example.openradiofm.utils.PtyManager.getPtyDisplayLabel(mActivity, pty);
        MainActivity.setTextIfChanged(tvPty, label);
    }

    @Override
    public void updateRdsStatus(boolean af, boolean ta, boolean tp) {
        if (ivAfIcon != null) ivAfIcon.setAlpha(af ? 1.0f : 0.2f);
        if (ivTaIcon != null) ivTaIcon.setAlpha(ta ? 1.0f : 0.2f);
        if (ivTpIcon != null) ivTpIcon.setAlpha(tp ? 1.0f : 0.2f);
    }

    @Override
    public void updateSignal(int level, String label, String color) {
        if (mActivity.mSignalMeterCoordinator != null && mActivity.mSignalMeterCoordinator.useBars()
                && color != null) {
            try {
                mActivity.mSignalMeterCoordinator.updateFromLegacyIconColor(Color.parseColor(color));
            } catch (Exception ignored) {}
            return;
        }
        if (ivSignalLevel != null && color != null) {
            try {
                ivSignalLevel.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
            } catch (Exception e) {}
        }
    }

    @Override
    public void updateStereo(boolean stereo) {
        if (ivStereoIcon != null) {
            ivStereoIcon.setVisibility(stereo ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void applySkin(boolean isNight) {
        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
        boolean isDay = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.DAY_MODE);
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normalColor = (isLight || isDay) ? Color.BLACK : Color.WHITE;
        int color = isNight ? nightBlue : normalColor;

        MainActivity.setTextColorIfChanged(tvFrequency, color);
        MainActivity.setTextColorIfChanged(tvRdsInfo, color);
        MainActivity.setTextColorIfChanged(tvPty, color);
        MainActivity.setTextColorIfChanged(ivBandIndicator, color);
        MainActivity.setTextColorIfChanged(ivStereoIcon, color);
        
        if (ivUnitLabel != null) {
            MainActivity.setTextColorIfChanged(ivUnitLabel, color);
        }
    }

    @Override
    public void updateFavoriteIndicator(boolean isFavorite, int presetIdx, boolean isNight) {
        if (ivFavoriteIndicator == null) return;
        if (isFavorite && presetIdx > 0) {
            ivFavoriteIndicator.setVisibility(View.VISIBLE);
            android.graphics.drawable.Drawable d = mActivity.getPresetNumberDrawable(presetIdx);
            if (d != null) {
                ivFavoriteIndicator.setImageDrawable(d);
            } else {
                ivFavoriteIndicator.setImageResource(mActivity.getPresetNumberResId(presetIdx));
            }
            
            if (isNight) {
                ivFavoriteIndicator.setColorFilter(mActivity.getResources().getColor(R.color.night_blue_primary, null), PorterDuff.Mode.SRC_IN);
            } else {
                boolean isDay = (mActivity.mThemeManager != null
                        && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.DAY_MODE);
                boolean isLight = (mActivity.mThemeManager != null
                        && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
                ivFavoriteIndicator.setColorFilter((isLight || isDay) ? Color.BLACK : Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        } else {
            ivFavoriteIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateBandIndicator(int band) {
        if (ivBandIndicator == null) return;
        MainActivity.setTextIfChanged(ivBandIndicator, bandText(band));
        applyBandTextColorForCurrentSkin(ivBandIndicator);
    }

    private static String bandText(int band) {
        switch (band) {
            case 0: return "FM1";
            case 1: return "FM2";
            case 2: return "FM3";
            case 3: return "AM1";
            case 4: return "AM2";
            default: return "FM1";
        }
    }

    private void applyBandTextColorForCurrentSkin(TextView tv) {
        if (tv == null) return;
        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
        boolean isNight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.NIGHT_MODE);
        boolean isDay = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.DAY_MODE);
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normal = (isLight || isDay) ? Color.BLACK : Color.WHITE;
        tv.setTextColor(isNight ? nightBlue : normal);
    }

    @Override
    public void updateMute(boolean muted) {
        // En V3 el mute suele ser un ImageButton en el MainActivity
    }

    @Override
    public void updateLogo(android.graphics.Bitmap bitmap) {
        // En V3, el logo de estación se gestiona como fondo dinámico; evitar solapes con PS.
        if (ivMainLogo != null) {
            ivMainLogo.setImageDrawable(null);
            ivMainLogo.setVisibility(View.GONE);
        }
    }
}
