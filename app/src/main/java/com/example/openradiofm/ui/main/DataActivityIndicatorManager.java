package com.example.openradiofm.ui.main;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.example.openradiofm.ui.theme.ThemeManager;

/**
 * UI-only manager para el indicador de actividad de datos (cloud).
 * Separa el renderizado (alpha, blink, tint) de la lógica de estado en MainActivity.
 */
public class DataActivityIndicatorManager {

    private static final int OFFLINE_WRAPPER_ANIM_MS = 200;
    private static final int BLINK_ANIM_MS = 650;

    private final FrameLayout wrapper;
    private final ImageView icon;

    @Nullable
    private ObjectAnimator blinkAnimator;

    public DataActivityIndicatorManager(FrameLayout wrapper, ImageView icon) {
        this.wrapper = wrapper;
        this.icon = icon;
    }

    public void render(
            boolean onlineEnabled,
            boolean isConnected,
            int activeOps,
            boolean isStreamingPlaying,
            boolean isStreamingLoading,
            @Nullable ThemeManager.Skin skin,
            float offlineAlpha,
            @ColorInt int nightBlueColor
    ) {
        if (!onlineEnabled) {
            setVisibilityIfChanged(wrapper, View.INVISIBLE);
            stopBlink();
            return;
        }

        setVisibilityIfChanged(wrapper, View.VISIBLE);
        setVisibilityIfChanged(icon, View.VISIBLE);

        // Wrapper alpha (offline dim) con transición suave.
        wrapper.animate().cancel();
        wrapper.animate()
                .alpha(isConnected ? 1.0f : offlineAlpha)
                .setDuration(OFFLINE_WRAPPER_ANIM_MS)
                .start();

        // Blink del icono solo si hay conectividad y hay operaciones activas.
        if (isConnected && activeOps > 0) {
            startBlink();
        } else {
            stopBlink();
        }

        // Color del icono: streaming gana (rojo/amarillo), si no, depende de skin.
        if (isStreamingPlaying) {
            setColorFilterIfChanged(icon, Color.RED, PorterDuff.Mode.SRC_IN);
            return;
        }
        if (isStreamingLoading) {
            setColorFilterIfChanged(icon, Color.YELLOW, PorterDuff.Mode.SRC_IN);
            return;
        }

        if (skin == ThemeManager.Skin.DAY_MODE) {
            int c = isConnected ? Color.BLACK : Color.parseColor("#FF555555");
            setColorFilterIfChanged(icon, c, PorterDuff.Mode.SRC_IN);
        } else if (skin == ThemeManager.Skin.NIGHT_MODE) {
            setColorFilterIfChanged(icon, nightBlueColor, PorterDuff.Mode.SRC_IN);
        } else if (skin == ThemeManager.Skin.CLEAR) {
            setColorFilterIfChanged(icon, Color.BLACK, PorterDuff.Mode.SRC_IN);
        } else {
            setColorFilterIfChanged(icon, null, null);
        }
    }

    public void stop() {
        stopBlink();
    }

    private void startBlink() {
        if (blinkAnimator != null && blinkAnimator.isRunning()) return;

        blinkAnimator = ObjectAnimator.ofFloat(icon, "alpha", 1.0f, 0.35f);
        blinkAnimator.setDuration(BLINK_ANIM_MS);
        blinkAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        blinkAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        blinkAnimator.start();
    }

    private void stopBlink() {
        if (blinkAnimator != null) {
            blinkAnimator.cancel();
            blinkAnimator = null;
        }
        icon.setAlpha(1.0f);
    }

    private static void setVisibilityIfChanged(View v, int visibility) {
        if (v.getVisibility() != visibility) v.setVisibility(visibility);
    }

    private static void setColorFilterIfChanged(ImageView v, @Nullable Integer color, @Nullable PorterDuff.Mode mode) {
        // No hay API pública para comparar "si ya es el mismo filter", pero evitar llamadas excesivas
        // no es crítico aquí. Aun así, si es limpiar (null), solo limpiamos si hay filtro.
        if (color == null || mode == null) {
            v.clearColorFilter();
            return;
        }
        v.setColorFilter(color, mode);
    }
}

