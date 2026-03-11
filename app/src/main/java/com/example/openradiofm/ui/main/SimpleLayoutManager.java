package com.example.openradiofm.ui.main;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.palette.graphics.Palette;
import com.example.openradiofm.R;

/**
 * Manager para el Layout Simple (Minimalista).
 * Encapsula la lógica de Palette API y actualizaciones de UI específicas.
 */
public class SimpleLayoutManager {

    private final MainActivity mActivity;
    private ImageView ivDynamicBackground, ivDynamicBackgroundExtra;
    private ImageView ivMainLogo;
    private TextView tvFrequency;
    private TextView tvRdsInfo;
    private View boxLogoSimple;
    private ImageButton btnSeekDown, btnMute, btnSeekUp;
    
    // V18.6: Estado para el fundido cruzado (Cross-fade)
    private boolean useExtraBackground = false;

    public SimpleLayoutManager(MainActivity activity) {
        this.mActivity = activity;
    }

    public void initViews(View root) {
        ivDynamicBackground = root.findViewById(R.id.ivDynamicBackground);
        ivDynamicBackgroundExtra = root.findViewById(R.id.ivDynamicBackgroundExtra);
        ivMainLogo = root.findViewById(R.id.ivMainLogo);
        tvFrequency = mActivity.findViewById(R.id.tvFrequency);
        if (tvFrequency != null) {
            tvFrequency.setEllipsize(null);
            tvFrequency.setSingleLine(false); // Use maxLines instead (already in XML)
            tvFrequency.setMaxLines(1);
        }
        tvRdsInfo = mActivity.findViewById(R.id.tvRdsInfo);
        boxLogoSimple = root.findViewById(R.id.boxLogoSimple);
        
        btnSeekDown = root.findViewById(R.id.btnSeekDown);
        btnMute = root.findViewById(R.id.btnMute);
        btnSeekUp = root.findViewById(R.id.btnSeekUp);

        setupListeners();
        setDefaultState(); 
    }

    private void setupListeners() {
        if (btnSeekDown != null) btnSeekDown.setOnClickListener(v -> mActivity.mEngine.seekDown());
        if (btnSeekUp != null) btnSeekUp.setOnClickListener(v -> mActivity.mEngine.seekUp());
        if (btnMute != null) btnMute.setOnClickListener(v -> {
            mActivity.mMuteState = !mActivity.mMuteState;
            mActivity.mEngine.setMute(mActivity.mMuteState);
            btnMute.setImageResource(mActivity.mMuteState ? R.drawable.radio_mute_p : R.drawable.radio_mute_n);
        });

        if (ivMainLogo != null) {
            ivMainLogo.setOnLongClickListener(v -> {
                mActivity.toggleLayoutMode();
                return true;
            });
        }
        
        if (boxLogoSimple != null) {
            boxLogoSimple.setOnLongClickListener(v -> {
                mActivity.toggleLayoutMode();
                return true;
            });
        }
    }

    public void updateFrequency(int freqKhz, String stationName) {
        if (tvFrequency != null) {
            String targetText;
            if (stationName != null && !stationName.isEmpty()) {
                targetText = stationName;
            } else {
                targetText = String.format(java.util.Locale.US, "%.1f", freqKhz / 1000.0f);
            }
            MainActivity.setTextIfChanged(tvFrequency, targetText);
            // V18.6: Aplicar gradiente tras actualizar texto
            applyGradientToTexts();
        }
    }

    public void updateRds(String text) {
        if (tvRdsInfo != null) {
            MainActivity.setTextIfChanged(tvRdsInfo, text);
            tvRdsInfo.setSelected(true); 
            applyGradientToTexts();
        }
    }

    public void updateLogo(Bitmap bitmap) {
        if (ivMainLogo != null && bitmap != null) {
            ivMainLogo.setImageBitmap(bitmap);
            ivMainLogo.setColorFilter(null);
            ivMainLogo.setAlpha(1.0f);

            // V18.6: Si el fondo dinámico está activo (bgMode == 2), ocultamos el box para evitar mezclas
            // Accedemos a las SharedPreferences de la actividad directamente
            android.content.SharedPreferences prefs = mActivity.getSharedPreferences("LayoutPrefs", android.content.Context.MODE_PRIVATE);
            int bgMode = prefs.getInt("pref_bg_mode", 1);
            
            if (bgMode == 2 && boxLogoSimple != null) {
                boxLogoSimple.setVisibility(View.GONE);
            } else if (boxLogoSimple != null) {
                boxLogoSimple.setVisibility(View.VISIBLE);
            }

            applyPaletteToBackground(bitmap);
        }
    }

    public void setDefaultState() {
        if (ivMainLogo != null) {
            ivMainLogo.setImageResource(R.mipmap.ic_launcher);
            ivMainLogo.setColorFilter(null); 
            ivMainLogo.setAlpha(0.3f);
        }
        
        // Reset backgrounds and animations
        if (ivDynamicBackground != null) {
            ivDynamicBackground.animate().cancel();
            ivDynamicBackground.setImageDrawable(null);
            ivDynamicBackground.setAlpha(0.0f);
        }
        if (ivDynamicBackgroundExtra != null) {
            ivDynamicBackgroundExtra.animate().cancel();
            ivDynamicBackgroundExtra.setImageDrawable(null);
            ivDynamicBackgroundExtra.setAlpha(0.0f);
        }
        
        useExtraBackground = false;

        // Reset Glow
        if (tvFrequency != null) {
            tvFrequency.setShadowLayer(0, 0, 0, 0);
            tvFrequency.getPaint().setShader(null);
            tvFrequency.invalidate();
        }
    }

    /**
     * V18.6: Transición suave (Cross-fade) y Glow Dinámico.
     */
    public void applyPaletteToBackground(Bitmap bitmap) {
        if (bitmap == null) return;

        Palette.from(bitmap).maximumColorCount(32).generate(palette -> {
            if (palette == null) return;

            // Búsqueda de color con mayor "punch" visual
            int color = palette.getVibrantColor(
                palette.getLightVibrantColor(
                    palette.getDarkVibrantColor(
                        palette.getMutedColor(
                            palette.getDominantColor(0xFF444444)
                        )
                    )
                )
            );

            // Asegurar que no sea negro total si el logo es oscuro
            if ((color & 0xFFFFFF) == 0) color = 0xFF444444;

            int darkColor = palette.getDarkVibrantColor(0xFF000000);
            if (darkColor == 0xFF000000) darkColor = palette.getDarkMutedColor(0xFF000000);

            final int finalColor = color;
            final int finalDark = darkColor;
            
            GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { finalColor | 0xFF000000, finalDark | 0xFF000000, 0xFF000000 }
            );
            
            mActivity.runOnUiThread(() -> {
                performCrossfade(gd);
                applyGlowEffects(finalColor);
                applyGradientToTexts();
            });
        });
    }

    private void performCrossfade(GradientDrawable newDrawable) {
        if (ivDynamicBackground == null || ivDynamicBackgroundExtra == null) return;

        ImageView visibleView = useExtraBackground ? ivDynamicBackgroundExtra : ivDynamicBackground;
        ImageView hiddenView = useExtraBackground ? ivDynamicBackground : ivDynamicBackgroundExtra;

        // 1. Cancelar animaciones previas para evitar "saltos" si se cambia rápido
        visibleView.animate().cancel();
        hiddenView.animate().cancel();

        // 2. Preparar el que va a entrar (Hacerlo invisible primero)
        hiddenView.setImageDrawable(newDrawable);
        hiddenView.setAlpha(0.0f);
        hiddenView.setVisibility(View.VISIBLE);

        // 3. Animación cruzada: El oculto sube a 0.6, el visible baja a 0
        hiddenView.animate().alpha(0.60f).setDuration(1200).start();
        visibleView.animate().alpha(0.0f).setDuration(1200).withEndAction(() -> {
            visibleView.setVisibility(View.GONE);
            visibleView.setImageDrawable(null);
        }).start();

        useExtraBackground = !useExtraBackground;
    }

    public void applyColors(boolean isNight) {
        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
        int white = android.graphics.Color.WHITE;

        if (isNight) {
            if (tvFrequency != null) tvFrequency.getPaint().setShader(null); // Remove gradient for solid color or update it
            if (tvFrequency != null) tvFrequency.setTextColor(nightBlue);
            if (tvRdsInfo != null) tvRdsInfo.setTextColor(nightBlue);
            
            // Tint buttons
            if (btnSeekDown != null) btnSeekDown.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            if (btnSeekUp != null) btnSeekUp.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            if (btnMute != null) btnMute.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
            
            ImageView cloudIcon = mActivity.findViewById(R.id.ivDataActivityIcon);
            if (cloudIcon != null) cloudIcon.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            // Normal mode
            if (tvFrequency != null) tvFrequency.setTextColor(white);
            if (tvRdsInfo != null) tvRdsInfo.setTextColor(0xBBFFFFFF);
            
            if (btnSeekDown != null) btnSeekDown.clearColorFilter();
            if (btnSeekUp != null) btnSeekUp.clearColorFilter();
            if (btnMute != null) btnMute.clearColorFilter();
            
            ImageView cloudIcon = mActivity.findViewById(R.id.ivDataActivityIcon);
            if (cloudIcon != null) cloudIcon.clearColorFilter();
            
            applyGradientToTexts(); // Re-apply gradient
        }
    }

    private void applyGlowEffects(int color) {
        // V18.6: Modo Noche ignora el glow del logo para mantener el azul puro si así se prefiere,
        // pero aquí seguiremos la lógica de color si el skin no es NIGHT_MODE.
        boolean isNight = (mActivity.mThemeManager != null && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
        
        // Glow dinámico: Sombra grande y desenfocada con el color vibrante
        int glowColor = isNight ? mActivity.getResources().getColor(R.color.night_blue_primary, null) & 0x7F000000 : (color & 0x00FFFFFF) | 0x7F000000;
        
        if (tvFrequency != null) {
            tvFrequency.setShadowLayer(25, 0, 0, glowColor);
        }
        if (tvRdsInfo != null) {
            tvRdsInfo.setShadowLayer(25, 0, 0, glowColor);
        }

        android.graphics.Paint glowPaint = new android.graphics.Paint();
        glowPaint.setShadowLayer(25, 0, 0, glowColor);

        if (btnSeekDown != null) btnSeekDown.setLayerType(View.LAYER_TYPE_SOFTWARE, glowPaint);
        if (btnSeekUp != null) btnSeekUp.setLayerType(View.LAYER_TYPE_SOFTWARE, glowPaint);
        if (btnMute != null) btnMute.setLayerType(View.LAYER_TYPE_SOFTWARE, glowPaint);

        ImageView cloudIcon = mActivity.findViewById(R.id.ivDataActivityIcon);
        if (cloudIcon != null) {
            cloudIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, glowPaint);
        }
    }

    private void applyGradientToTexts() {
        boolean isNight = (mActivity.mThemeManager != null && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
        if (isNight) {
            if (tvFrequency != null) tvFrequency.getPaint().setShader(null);
            if (tvRdsInfo != null) tvRdsInfo.getPaint().setShader(null);
            return;
        }

        // V18.6: Aplicar gradiente lineal Vertical (Blanco a Gris Claro)
        if (tvFrequency != null) {
            tvFrequency.post(() -> {
                float height = tvFrequency.getHeight();
                if (height <= 0) height = 185; 
                
                android.graphics.Shader shader = new android.graphics.LinearGradient(
                    0, 0, 0, height,
                    new int[] { android.graphics.Color.WHITE, 0xFFBBBBBB },
                    null, android.graphics.Shader.TileMode.CLAMP
                );
                tvFrequency.getPaint().setShader(shader);
                tvFrequency.invalidate();
            });
        }
        if (tvRdsInfo != null) {
            tvRdsInfo.post(() -> {
                float height = tvRdsInfo.getHeight();
                if (height <= 0) height = 72; 
                
                android.graphics.Shader shader = new android.graphics.LinearGradient(
                    0, 0, 0, height,
                    new int[] { android.graphics.Color.WHITE, 0xFFBBBBBB },
                    null, android.graphics.Shader.TileMode.CLAMP
                );
                tvRdsInfo.getPaint().setShader(shader);
                tvRdsInfo.invalidate();
            });
        }
    }
}
