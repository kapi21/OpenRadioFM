package com.example.openradiofm.ui.main;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.example.openradiofm.ui.theme.ThemeManager;
import com.example.openradiofm.ui.widget.SignalBarsView;

/**
 * Alterna entre icono clásico de señal y barras segmentadas; sincroniza con RSSI/SNR y flags de calidad.
 */
public final class SignalMeterCoordinator {

    public static final String PREF_USE_BARS = "pref_signal_meter_bars";

    private final MainActivity mActivity;
    private ImageView mIcon;
    private SignalBarsView mBars;
    private int mLastLit = Integer.MIN_VALUE;

    public SignalMeterCoordinator(MainActivity activity) {
        mActivity = activity;
    }

    public void bind(ImageView icon, SignalBarsView bars) {
        mIcon = icon;
        mBars = bars;
    }

    public boolean useBars() {
        try {
            return mActivity.mPrefs != null && mActivity.mPrefs.getBoolean(PREF_USE_BARS, false);
        } catch (Exception e) {
            return false;
        }
    }

    public void applyModeVisibility() {
        if (mIcon == null || mBars == null) return;
        boolean bars = useBars();
        mIcon.setVisibility(bars ? View.GONE : View.VISIBLE);
        mBars.setVisibility(bars ? View.VISIBLE : View.GONE);
        if (bars) {
            applyBarsAppearanceFromSkin();
            // Solo semilla desde estéreo/RDS al activar barras; no pisar en cada applyMode (piel/noche)
            // para no anular niveles RSSI ya recibidos.
            if (mLastLit == Integer.MIN_VALUE) {
                refreshFromEngineFlags();
            }
        } else {
            mLastLit = Integer.MIN_VALUE;
        }
    }

    public void applyBarsAppearanceFromSkin() {
        if (mBars == null) return;
        ThemeManager.Skin s = mActivity.mThemeManager != null
                ? mActivity.mThemeManager.getActiveSkin() : ThemeManager.Skin.CLASSIC;
        if (s == ThemeManager.Skin.NIGHT_MODE) {
            mBars.setAppearance(SignalBarsView.APPEAR_NIGHT);
        } else if (s == ThemeManager.Skin.DAY_MODE || s == ThemeManager.Skin.CLEAR) {
            mBars.setAppearance(SignalBarsView.APPEAR_DAY);
        } else {
            mBars.setAppearance(SignalBarsView.APPEAR_CLASSIC);
        }
    }

    public void onRssiSnr(int rssi, int snr) {
        if (useBars() && mBars != null) {
            int lit = mapRssiSnrToLitCount(rssi, snr);
            if (lit != mLastLit) {
                mLastLit = lit;
                mBars.setLitCount(lit);
            }
        }
    }

    /** Sincroniza barras con estéreo / RDS / banda cuando no hay telemetría numérica reciente. */
    public void refreshFromEngineFlags() {
        if (!useBars() || mBars == null) return;
        boolean hasStereo = mActivity.mEngine != null && mActivity.mEngine.isStereo();
        boolean hasRdsLock = mActivity.mHasRdsLock;
        int band = mActivity.mCurrentBand;
        int lit;
        if (hasRdsLock && hasStereo) {
            lit = 5;
        } else if (hasStereo || band >= 3) {
            lit = 3;
        } else {
            lit = 1;
        }
        if (lit != mLastLit) {
            mLastLit = lit;
            mBars.setLitCount(lit);
        }
    }

    /**
     * Actualiza barras a partir del color legado del icono (verde / amarillo / rojo).
     */
    public void updateFromLegacyIconColor(int color) {
        if (!useBars() || mBars == null) return;
        int lit = legacyColorToLit(color);
        if (lit != mLastLit) {
            mLastLit = lit;
            mBars.setLitCount(lit);
        }
    }

    /** Verde fuerte → 5, ámbar/amarillo → 3, rojo débil → 1. */
    static int legacyColorToLit(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        if (g >= 200 && r < 130) {
            return 5;
        }
        if (r >= 200 && g >= 160 && b < 140) {
            return 3;
        }
        if (r >= 200 && g < 130) {
            return 1;
        }
        return 3;
    }

    private static int mapRssiSnrToLitCount(int rssi, int snr) {
        // Negativos: tratar como dBm (típico -120…-40); “mejor” = menos negativo.
        if (rssi < 0 || snr < 0) {
            int dbm = Integer.MIN_VALUE;
            if (rssi < 0) {
                dbm = Math.max(dbm, rssi);
            }
            if (snr < 0) {
                dbm = Math.max(dbm, snr);
            }
            if (dbm == Integer.MIN_VALUE) {
                return 1;
            }
            if (dbm >= -55) {
                return 5;
            }
            if (dbm >= -65) {
                return 4;
            }
            if (dbm >= -75) {
                return 3;
            }
            if (dbm >= -88) {
                return 2;
            }
            return 1;
        }
        // QS6 / NWD: en ingeniería y en runtime el stack suele reportar RSSI y SNR en escala **0–5**
        // (una “barra” de 5 posiciones). Con la fórmula 0–15, valores 1–3 casi siempre caían en 1 segmento.
        if (rssi <= 5 && snr <= 5) {
            int x = Math.max(rssi, snr);
            if (x < 0) {
                return 1;
            }
            return Math.min(5, Math.max(0, x));
        }
        int rr = Math.min(15, Math.max(0, rssi));
        int ss = Math.min(15, Math.max(0, snr));
        int x = Math.max(rr, ss);
        if (x <= 0) {
            return 1;
        }
        int lit = (x * 5 + 14) / 15;
        if (lit < 1) {
            lit = 1;
        }
        if (lit > 5) {
            lit = 5;
        }
        return lit;
    }
}
