package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * Encapsula la orquestación de skins (modos visuales) en la Activity principal.
 * Coordina las llamadas a ThemeManager, NightModeManager, DayModeManager, 
 * y actualiza atributos visuales base de UI.
 */
public class SkinCoordinator {

    private final MainActivity mActivity;

    public SkinCoordinator(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Tipografía global (V15.6) y presets; antes en {@link MainActivity#applyFonts()}.
     */
    public void applyFonts() {
        Typeface typeface = mActivity.getSystemTypeface();
        applyRecursiveFont(mActivity.findViewById(android.R.id.content), typeface);
        if (mActivity.mPresetManager != null) {
            mActivity.mPresetManager.applyFonts(typeface);
        }
        if (mActivity.mUiMediator.tvDigitalClock != null) {
            mActivity.mUiMediator.tvDigitalClock.setTypeface(typeface);
        }
    }

    /**
     * Aplica {@code tf} a TextViews bajo {@code v}; usado por diálogos y escaneo.
     */
    public void applyRecursiveFont(View v, Typeface tf) {
        if (v == null) return;
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyRecursiveFont(vg.getChildAt(i), tf);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(tf);
        }
    }

    public void applySkin(ThemeManager.Skin skin) {
        ThemeManager tm = mActivity.mThemeManager;
        ThemeManager.Skin prevSkinForBg = mActivity.mLastSkinAppliedForBackground;
        ThemeManager.Skin activeBefore = tm != null ? tm.getActiveSkin() : null;

        if (tm != null) tm.applySkin(skin);
        ThemeManager.Skin activeAfter = tm != null ? tm.getActiveSkin() : null;

        if (activeBefore != activeAfter) {
            try { mActivity.mRdsLogoTransition.logoUiGeneration.incrementAndGet(); } catch (Exception ignored) {}
        }

        boolean isNight = (skin == ThemeManager.Skin.NIGHT_MODE);
        boolean isClear = (skin == ThemeManager.Skin.CLEAR);
        boolean isDay = (skin == ThemeManager.Skin.DAY_MODE);

        applyVisualStateForSkin(prevSkinForBg, skin);

        try {
            if (mActivity.mLogoManager != null && 
               (isDay || prevSkinForBg == ThemeManager.Skin.DAY_MODE)) {
                mActivity.mLogoManager.loadCustomBackground();
            }
        } catch (Exception ignored) {}
        
        mActivity.mLastSkinAppliedForBackground = skin;
    }

    /**
     * Color del reloj digital según skin activa (una sola fuente de verdad; antes duplicado
     * al final de {@link #applySkin} y dentro de {@link #applyVisualStateForSkin}).
     */
    private void applyDigitalClockTextColor(ThemeManager tm, ThemeManager.Skin skin) {
        TextView tvDigitalClock = mActivity.findViewById(R.id.tvDigitalClock);
        if (tvDigitalClock == null) return;
        if (skin == ThemeManager.Skin.NIGHT_MODE) {
            tvDigitalClock.setTextColor(mActivity.getResources().getColor(R.color.night_blue_primary, null));
            return;
        }
        boolean isLight = (tm != null && tm.getActiveSkin() == ThemeManager.Skin.CLEAR);
        if (skin == ThemeManager.Skin.DAY_MODE) {
            tvDigitalClock.setTextColor(Color.BLACK);
        } else {
            tvDigitalClock.setTextColor(isLight ? Color.BLACK : Color.WHITE);
        }
    }

    private void applyVisualStateForSkin(ThemeManager.Skin prevSkin, ThemeManager.Skin skin) {
        boolean isNight = (skin == ThemeManager.Skin.NIGHT_MODE);
        boolean isClear = (skin == ThemeManager.Skin.CLEAR);
        boolean isDay = (skin == ThemeManager.Skin.DAY_MODE);

        try {
            if (mActivity.isV3LayoutActive()) {
                View v = mActivity.findViewById(R.id.ivMainLogo);
                if (v instanceof ImageView) {
                    MainActivity.setVisibilityIfChanged(v, View.GONE);
                    ((ImageView) v).setImageDrawable(null);
                }
            }
        } catch (Exception ignored) {}

        if (mActivity.mUiController != null) {
            mActivity.mUiController.applySkin(isNight);
        } else if (mActivity.mIsSimpleLayout && mActivity.mSimpleLayoutManager != null) {
            mActivity.mSimpleLayoutManager.applyColors(isNight);
        }

        if (mActivity.mNightModeManager != null) {
            if (isNight) mActivity.mNightModeManager.applyNightModeColors(mActivity.mLastFreq);
            else mActivity.mNightModeManager.resetNightModeColors(mActivity.mLastFreq);
        }

        try {
            if (mActivity.mDayModeManager != null) {
                if (isDay) {
                    mActivity.mDayModeManager.applyDayModeColors(mActivity.mLastFreq);
                } else if (prevSkin == ThemeManager.Skin.DAY_MODE) {
                    mActivity.mDayModeManager.resetDayModeColors(mActivity.mLastFreq);
                }
            }
        } catch (Exception ignored) {}

        if (!isNight && !isDay) {
            applyClearButtonIconTint(isClear);
        }

        mActivity.updateDataActivityUI();

        try {
            applyDigitalClockTextColor(mActivity.mThemeManager, skin);
        } catch (Exception ignored) {}

        try {
            if (mActivity.mSignalMeterCoordinator != null) {
                mActivity.mSignalMeterCoordinator.applyModeVisibility();
                mActivity.mSignalMeterCoordinator.applyBarsAppearanceFromSkin();
            }
        } catch (Exception ignored) {}

        try {
            if (mActivity.mPresetManager != null) {
                mActivity.mPresetManager.syncLoopMirrorPresetVisualsWithMainSlots();
            }
        } catch (Exception ignored) {}
    }

    public void reapplyVisualStateForCurrentSkin() {
        ThemeManager tm = mActivity.mThemeManager;
        if (tm == null) {
            mActivity.updateDataActivityUI();
            return;
        }
        ThemeManager.Skin s = tm.getActiveSkin();
        applyVisualStateForSkin(s, s);
    }

    private void applyClearButtonIconTint(boolean enabled) {
        final int tint = Color.BLACK;
        final PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;

        int[] buttonIds = {
                R.id.btnSeekUp, R.id.btnSeekDown,
                R.id.btnFavPrev, R.id.btnFavNext,
                R.id.btnBand, R.id.btnAutoScan,
                R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnGps,
                R.id.btnExtra1, R.id.btnExtra2, R.id.btnPowerOff
        };

        for (int id : buttonIds) {
            View v = mActivity.findViewById(id);
            if (v instanceof ImageView) {
                if (enabled) MainActivity.setColorFilterIfChanged((ImageView) v, tint, mode);
                else MainActivity.setColorFilterIfChanged((ImageView) v, null, null);
            }
        }

        int[] iconIds = {
                R.id.ivAfIcon, R.id.ivTaIcon, R.id.ivTpIcon,
                R.id.ivStereoIcon
        };
        for (int id : iconIds) {
            View v = mActivity.findViewById(id);
            if (v instanceof ImageView) {
                if (enabled) MainActivity.setColorFilterIfChanged((ImageView) v, tint, mode);
                else MainActivity.setColorFilterIfChanged((ImageView) v, null, null);
            }
        }
    }

    public void cycleClassicNightDay() {
        ThemeManager tm = mActivity.mThemeManager;
        if (tm == null) return;
        try {
            ThemeManager.Skin active = tm.getActiveSkin();
            boolean dayEnabled = true;
            try {
                SharedPreferences prefs = mActivity.mPrefs;
                dayEnabled = (prefs == null) || prefs.getBoolean("pref_dev_day_mode_enabled", true);
            } catch (Exception ignored) {}

            ThemeManager.Skin next;
            if (active == ThemeManager.Skin.NIGHT_MODE) {
                next = dayEnabled ? ThemeManager.Skin.DAY_MODE : ThemeManager.Skin.CLASSIC;
            } else if (active == ThemeManager.Skin.DAY_MODE) {
                next = ThemeManager.Skin.CLASSIC;
            } else {
                next = ThemeManager.Skin.NIGHT_MODE;
            }

            tm.setSkin(next);
            applySkin(next);
            mActivity.showStyledToast(mActivity.getString(R.string.toast_skin_colon, next.displayName));
        } catch (Exception ignored) {}
    }

    public void toggleNightMode() {
        ThemeManager tm = mActivity.mThemeManager;
        if (tm == null) return;
        try {
            ThemeManager.Skin active = tm.getActiveSkin();
            SharedPreferences tp = mActivity.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
            final String KEY_PREV = "prev_skin_before_night";

            if (active == ThemeManager.Skin.NIGHT_MODE) {
                String prevName = tp.getString(KEY_PREV, null);
                ThemeManager.Skin prev = null;
                if (prevName != null) {
                    try { prev = ThemeManager.Skin.valueOf(prevName); } catch (Exception ignored) {}
                }
                if (prev == null || prev == ThemeManager.Skin.NIGHT_MODE || prev == ThemeManager.Skin.CLEAR) {
                    prev = tm.getCurrentSkin();
                }
                if (prev == null || prev == ThemeManager.Skin.NIGHT_MODE || prev == ThemeManager.Skin.CLEAR) {
                    prev = ThemeManager.Skin.CLASSIC;
                }
                tm.setSkin(prev);
                applySkin(prev);
                mActivity.showStyledToast(mActivity.getString(R.string.toast_skin_colon, prev.displayName));
            } else {
                ThemeManager.Skin current = tm.getCurrentSkin();
                if (current != null && current != ThemeManager.Skin.NIGHT_MODE && current != ThemeManager.Skin.CLEAR) {
                    tp.edit().putString(KEY_PREV, current.name()).apply();
                }
                tm.setSkin(ThemeManager.Skin.NIGHT_MODE);
                applySkin(ThemeManager.Skin.NIGHT_MODE);
                mActivity.showStyledToast(mActivity.getString(R.string.toast_skin_night_mode));
            }
        } catch (Exception ignored) {}
    }
}
