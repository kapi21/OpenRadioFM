package com.example.openradiofm.ui.main;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

public class StandardLayoutManager {

    private final MainActivity mActivity;

    // UI Elements
    public TextView tvFrequency;
    public TextView tvRdsName;
    public TextView tvRdsInfo;
    public TextView tvPty;
    public TextView ivBandIndicator;
    public TextView ivUnitLabel;
    public ImageView ivSignalLevel;
    public ImageView ivAfIcon, ivTaIcon, ivTpIcon;
    public ImageView ivFavoriteIndicator;
    public TextView ivStereoIcon;
    public ImageButton btnPowerOff; // Actually ImageView/ImageButton

    public StandardLayoutManager(MainActivity activity) {
        this.mActivity = activity;
    }

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
        btnPowerOff = root.findViewById(R.id.btnPowerOff);
        
        setupRdsIcons();
    }

    private void setupRdsIcons() {
        if (ivAfIcon != null) {
            ivAfIcon.setAlpha(0.2f);
            ivAfIcon.setOnClickListener(v -> {
                mActivity.animateButton(ivAfIcon);
                if (mActivity.mEngine != null) mActivity.mEngine.toggleRdsFeature(1);
            });
        }
        if (ivTaIcon != null) {
            ivTaIcon.setAlpha(0.2f);
            ivTaIcon.setOnClickListener(v -> {
                mActivity.animateButton(ivTaIcon);
                if (mActivity.mEngine != null) mActivity.mEngine.toggleRdsFeature(2);
            });
        }
        if (ivTpIcon != null) {
            ivTpIcon.setAlpha(0.2f);
            ivTpIcon.setOnClickListener(v -> {
                mActivity.animateButton(ivTpIcon);
                if (mActivity.mEngine != null) mActivity.mEngine.toggleRdsFeature(0);
            });
        }
    }

    public void updateFrequencyDisplay(int freq, boolean isNight, boolean isFavorite, int presetIdx, String currentBandStr, int currentBandInt) {
        if (freq <= 0) return;

        applyColors(isNight);
        updateFavoriteIndicator(isFavorite, presetIdx, isNight);
        updateSignalLevel(currentBandInt);
        updateStereoIcon();
    }

    public void setFrequencyText(int freq, String text, int currentBandInt) {
        if (tvFrequency != null) {
            String targetText;
            if (text != null && !text.isEmpty()) {
                targetText = text;
            } else if (currentBandInt >= 3) { // AM
                targetText = String.valueOf(freq);
            } else {
                targetText = String.format(java.util.Locale.US, "%.1f", freq / 1000.0);
            }
            MainActivity.setTextIfChanged(tvFrequency, targetText);
        }
    }

    public void applyColors(boolean isNight) {
        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
        boolean isDay = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.DAY_MODE);
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normalText = (isLight || isDay) ? Color.BLACK : Color.WHITE;

        if (isNight) {
            if (tvFrequency != null) tvFrequency.setTextColor(nightBlue);
            if (ivUnitLabel != null) ivUnitLabel.setTextColor(nightBlue);
            if (tvRdsName != null) tvRdsName.setTextColor(nightBlue);
            if (tvRdsInfo != null) tvRdsInfo.setTextColor(nightBlue);
            if (tvPty != null) tvPty.setTextColor(nightBlue);
            if (btnPowerOff != null) btnPowerOff.setColorFilter(nightBlue, PorterDuff.Mode.SRC_IN);
        } else {
            if (tvFrequency != null) tvFrequency.setTextColor(normalText);
            if (ivUnitLabel != null) ivUnitLabel.setTextColor(normalText);
            if (tvRdsName != null) tvRdsName.setTextColor(normalText);
            if (tvRdsInfo != null) tvRdsInfo.setTextColor(normalText);
            if (tvPty != null) tvPty.setTextColor(normalText);
            if (btnPowerOff != null) btnPowerOff.clearColorFilter();
        }
    }

    private void updateFavoriteIndicator(boolean isFavorite, int idx, boolean isNight) {
        if (ivFavoriteIndicator != null) {
            if (isFavorite && idx > 0) {
                ivFavoriteIndicator.setVisibility(View.VISIBLE);
                android.graphics.drawable.Drawable d = mActivity.getPresetNumberDrawable(idx);
                if (d != null) {
                    ivFavoriteIndicator.setImageDrawable(d);
                } else {
                    ivFavoriteIndicator.setImageResource(mActivity.getPresetNumberResId(idx));
                }

                int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
                if (isNight) {
                    ivFavoriteIndicator.setColorFilter(nightBlue, PorterDuff.Mode.SRC_IN);
                } else {
                    boolean isDay = (mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.DAY_MODE);
                    boolean isLight = (mActivity.mThemeManager != null
                            && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
                    ivFavoriteIndicator.setColorFilter((isLight || isDay) ? Color.BLACK : Color.WHITE, PorterDuff.Mode.SRC_IN);
                }
            } else {
                // INVISIBLE: hueco fijo junto a la nube (layout 2); no usar GONE para no colapsar.
                ivFavoriteIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void updateSignalLevel(int currentBand) {
        if (ivSignalLevel != null) {
            int signalColor;
            boolean hasStereo = mActivity.mEngine != null && mActivity.mEngine.isStereo();
            boolean hasRdsLock = mActivity.mHasRdsLock;

            if (hasRdsLock && hasStereo) {
                signalColor = Color.parseColor("#00E676"); // Green
            } else if (hasStereo || currentBand >= 3) {
                signalColor = Color.parseColor("#FFD600"); // Yellow
            } else {
                signalColor = Color.parseColor("#FF5252"); // Red
            }
            ivSignalLevel.setColorFilter(signalColor, PorterDuff.Mode.SRC_IN);
        }
    }

    public void updateStereoIcon() {
        mActivity.refreshStereoIndicatorUi(null);
    }

    public void updateBandImage(int band) {
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
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.DAY_MODE);
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normal = (isLight || isDay) ? Color.BLACK : Color.WHITE;
        tv.setTextColor(isNight ? nightBlue : normal);
    }

    public void updateStatusIndicator(boolean active, String type) {
        // En lugar de pasar el TextView directamente desde MainActivity,
        // lo buscamos aquí si necesitamos
        // Add others if needed like TA, TP text
    }
}
