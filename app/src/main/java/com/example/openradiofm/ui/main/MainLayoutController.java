package com.example.openradiofm.ui.main;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * V21.0: Controlador para el Layout Estándar (V2 - Vertical).
 */
public class MainLayoutController extends BaseLayoutController {
    private TextView tvFrequency, tvRdsName, tvRdsInfo, tvPty;
    private TextView ivBandIndicator;
    private TextView ivUnitLabel;
    private ImageView ivSignalLevel;
    private ImageView ivAfIcon, ivTaIcon, ivTpIcon;
    private ImageView ivFavoriteIndicator;
    private TextView ivStereoIcon;

    public MainLayoutController(MainActivity activity) {
        super(activity);
    }

    @Override
    public void initViews(View root) {
        tvFrequency = root.findViewById(R.id.tvFrequency);
        tvRdsName = root.findViewById(R.id.tvRdsName);
        tvRdsInfo = root.findViewById(R.id.tvRdsInfo);
        tvPty = root.findViewById(R.id.tvPty);

        ivBandIndicator = root.findViewById(R.id.ivBandIndicator);
        ivUnitLabel = root.findViewById(R.id.ivUnitLabel);
        ivSignalLevel = root.findViewById(R.id.ivSignalLevel);
        
        ivAfIcon = root.findViewById(R.id.ivAfIcon);
        ivTaIcon = root.findViewById(R.id.ivTaIcon);
        ivTpIcon = root.findViewById(R.id.ivTpIcon);

        ivFavoriteIndicator = root.findViewById(R.id.ivFavoriteIndicator);
        ivStereoIcon = root.findViewById(R.id.ivStereoIcon);
        
        setupRdsListeners();
    }

    private void setupRdsListeners() {
        if (ivAfIcon != null) ivAfIcon.setOnClickListener(v -> {
            mActivity.animateButton(ivAfIcon);
            if (mActivity.mEngine != null) mActivity.mEngine.toggleRdsFeature(1);
        });
        if (ivTaIcon != null) ivTaIcon.setOnClickListener(v -> {
            mActivity.animateButton(ivTaIcon);
            if (mActivity.mEngine != null) mActivity.mEngine.toggleRdsFeature(2);
        });
        if (ivTpIcon != null) ivTpIcon.setOnClickListener(v -> {
            mActivity.animateButton(ivTpIcon);
            if (mActivity.mEngine != null) mActivity.mEngine.toggleRdsFeature(0);
        });
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
    public void updateRDS(String name) {
        MainActivity.setTextIfChanged(tvRdsName, name);
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
        mActivity.refreshStereoIndicatorUi(null);
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
        MainActivity.setTextColorIfChanged(tvRdsName, color);
        MainActivity.setTextColorIfChanged(tvRdsInfo, color);
        MainActivity.setTextColorIfChanged(tvPty, color);
        
        mActivity.refreshStereoIndicatorUi(null);
        
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
            ivFavoriteIndicator.setVisibility(View.INVISIBLE);
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
                && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
        boolean isDay = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE);
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
        int normal = (isLight || isDay) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE;
        tv.setTextColor(isNight ? nightBlue : normal);
    }

    @Override
    public void updateMute(boolean muted) {
        // En V2 el mute suele ser un ImageButton en el MainActivity que ya se gestiona
        // Pero si el layout tuviera su propio indicador, lo pondríamos aquí.
    }

    /**
     * {@code refreshRadioStatus()} y {@link MainActivity#clearStationLogoUi()} llaman aquí con {@code null}
     * para limpiar antes de la carga asíncrona (delegación directa a {@link LogoManager#clearLogo()}).
     */
    @Override
    public void updateLogo(Bitmap bitmap) {
        if (bitmap != null) {
            return;
        }
        if (mActivity.mLogoManager != null) {
            mActivity.mLogoManager.clearLogo();
        }
    }
}
