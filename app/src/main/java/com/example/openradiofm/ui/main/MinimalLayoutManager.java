package com.example.openradiofm.ui.main;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.palette.graphics.Palette;
import com.example.openradiofm.R;

/**
 * V19.2: Manager para el Layout 4 - Minimalista Premium.
 * Centrado en gestos, logos gigantes y animaciones fluidas ("Breathing").
 */
public class MinimalLayoutManager {

    private final MainActivity mActivity;
    private ImageView ivDynamicBackground, ivDynamicBackgroundExtra;
    private ImageView ivMainLogo;
    private TextView tvFrequency;
    private TextView tvRdsInfo;
    private View boxLogoMinimal;
    
    private boolean useExtraBackground = false;

    public MinimalLayoutManager(MainActivity activity) {
        this.mActivity = activity;
    }

    public void initViews(View root) {
        ivDynamicBackground = root.findViewById(R.id.ivDynamicBackground);
        ivDynamicBackgroundExtra = root.findViewById(R.id.ivDynamicBackgroundExtra);
        ivMainLogo = root.findViewById(R.id.ivMainLogo);
        tvFrequency = root.findViewById(R.id.tvFrequency);
        tvRdsInfo = root.findViewById(R.id.tvRdsInfo);
        boxLogoMinimal = root.findViewById(R.id.boxLogoMinimal);

        setupListeners();
        setDefaultState();
        startBreathingAnimation();
    }

    private void setupListeners() {
        if (ivMainLogo != null) {
            ivMainLogo.setOnClickListener(v -> {
                // Toggle Mute en Minimal
                if (mActivity.mPlaybackManager != null) {
                    mActivity.mPlaybackManager.setMute(!mActivity.mPlaybackManager.isMuted());
                } else if (mActivity.mEngine != null) {
                    boolean isMuted = !mActivity.mMuteState;
                    mActivity.mEngine.setMute(isMuted);
                    mActivity.mMuteState = isMuted;
                }
            });
            
            ivMainLogo.setOnLongClickListener(v -> {
                // Volver a modo estándar o settings
                mActivity.showToast(mActivity.getString(R.string.toast_back_standard_layout));
                mActivity.mPrefs.edit().putBoolean("pref_layout_minimal", false).apply();
                mActivity.recreate();
                return true;
            });
        }
    }

    public void updateFrequency(int freqKhz, String stationName) {
        if (tvFrequency != null) {
            if (stationName != null && !stationName.isEmpty()) {
                tvFrequency.setText(stationName);
            } else {
                tvFrequency.setText(String.format(java.util.Locale.US, "%.1f", freqKhz / 1000.0f));
            }
        }
    }

    public void updateRds(String text) {
        if (tvRdsInfo != null) {
            tvRdsInfo.setText(text);
            tvRdsInfo.setSelected(true);
        }
    }

    public void updateLogo(Bitmap bitmap) {
        if (ivMainLogo != null && bitmap != null) {
            ivMainLogo.setImageBitmap(bitmap);
            ivMainLogo.setAlpha(1.0f);
            applyPaletteToBackground(bitmap);
        }
    }

    private void startBreathingAnimation() {
        if (ivMainLogo == null) return;
        
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(ivMainLogo, "scaleX", 1.0f, 1.05f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(ivMainLogo, "scaleY", 1.0f, 1.05f);
        
        scaleX.setDuration(4000);
        scaleY.setDuration(4000);
        scaleX.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        scaleX.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        
        scaleX.start();
        scaleY.start();
    }

    public void setDefaultState() {
        if (ivMainLogo != null) {
            ivMainLogo.setImageResource(R.drawable.ic_app_logo);
            ivMainLogo.setAlpha(0.2f);
        }
        if (ivDynamicBackground != null) ivDynamicBackground.setAlpha(0.0f);
        if (ivDynamicBackgroundExtra != null) ivDynamicBackgroundExtra.setAlpha(0.0f);
    }

    private void applyPaletteToBackground(Bitmap bitmap) {
        Palette.from(bitmap).generate(palette -> {
            if (palette == null) return;
            int color = palette.getVibrantColor(palette.getDominantColor(0xFF444444));
            int darkColor = palette.getDarkMutedColor(0xFF000000);

            GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { color | 0xFF000000, darkColor | 0xFF000000, 0xFF000000 }
            );

            mActivity.runOnUiThread(() -> {
                performCrossfade(gd);
                // Glow effect para el logo central
                if (ivMainLogo != null) {
                    ivMainLogo.setElevation(30f);
                }
            });
        });
    }

    private void performCrossfade(GradientDrawable newDrawable) {
        ImageView invisible = useExtraBackground ? ivDynamicBackground : ivDynamicBackgroundExtra;
        ImageView visible = useExtraBackground ? ivDynamicBackgroundExtra : ivDynamicBackground;

        invisible.setImageDrawable(newDrawable);
        invisible.animate().alpha(0.35f).setDuration(1500).start();
        visible.animate().alpha(0.0f).setDuration(1500).start();
        
        useExtraBackground = !useExtraBackground;
    }
}
