package com.example.openradiofm.ui.main;

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
    private ImageView ivBandIndicator, ivUnitLabel, ivSignalLevel;
    private ImageView ivAfIcon, ivTaIcon, ivTpIcon;
    private ImageView ivFavoriteIndicator, ivStereoIcon;

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
        boolean isLight = (mActivity.mThemeManager != null
                && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
        int normalColor = isLight ? Color.BLACK : Color.WHITE;
        int color = isNight ? nightBlue : normalColor;

        MainActivity.setTextColorIfChanged(tvFrequency, color);
        MainActivity.setTextColorIfChanged(tvRdsName, color);
        MainActivity.setTextColorIfChanged(tvRdsInfo, color);
        MainActivity.setTextColorIfChanged(tvPty, color);
        
        if (ivUnitLabel != null) {
            if (isNight) ivUnitLabel.setColorFilter(nightBlue, PorterDuff.Mode.SRC_IN);
            else ivUnitLabel.clearColorFilter();
        }
    }

    @Override
    public void updateFavoriteIndicator(boolean isFavorite, int presetIdx, boolean isNight) {
        if (ivFavoriteIndicator == null) return;
        if (isFavorite && presetIdx > 0) {
            ivFavoriteIndicator.setVisibility(View.VISIBLE);
            int resId = mActivity.getResources().getIdentifier("radio_icon_p" + String.format("%02d", presetIdx), "drawable", mActivity.getPackageName());
            ivFavoriteIndicator.setImageResource(resId != 0 ? resId : R.drawable.radio_icon_p01);
            
            if (isNight) {
                ivFavoriteIndicator.setColorFilter(mActivity.getResources().getColor(R.color.night_blue_primary, null), PorterDuff.Mode.SRC_IN);
            } else {
                boolean isLight = (mActivity.mThemeManager != null
                        && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.CLEAR);
                ivFavoriteIndicator.setColorFilter(isLight ? Color.BLACK : Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        } else {
            ivFavoriteIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateBandIndicator(int band) {
        if (ivBandIndicator == null) return;
        int drawId;
        switch (band) {
            case 0: drawId = R.drawable.radio_fm1; break;
            case 1: drawId = R.drawable.radio_fm2; break;
            case 2: drawId = R.drawable.radio_fm3; break;
            case 3: drawId = R.drawable.radio_am1; break;
            case 4: drawId = R.drawable.radio_am2; break;
            default: drawId = R.drawable.radio_fm1; break;
        }
        
        // V2.5: Usar el helper centralizado para preservar el tinte (filtros)
        MainActivity.setImageResourceIfChanged(ivBandIndicator, drawId);
        
        // Re-asegurar tinte si es modo noche (por si el helper no lo detectó en el primer frame)
        if (mActivity.mThemeManager != null && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.NIGHT_MODE) {
            ivBandIndicator.setColorFilter(mActivity.getResources().getColor(R.color.night_blue_primary, null), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            ivBandIndicator.clearColorFilter();
        }
    }

    @Override
    public void updateMute(boolean muted) {
        // En V2 el mute suele ser un ImageButton en el MainActivity que ya se gestiona
        // Pero si el layout tuviera su propio indicador, lo pondríamos aquí.
    }
}
