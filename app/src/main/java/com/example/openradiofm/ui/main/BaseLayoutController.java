package com.example.openradiofm.ui.main;

import android.view.View;

/**
 * V21.0: Base para los controladores de UI específicos de cada layout.
 * Ayuda a desacoplar MainActivity de la manipulación directa de vistas.
 */
public abstract class BaseLayoutController {
    protected final MainActivity mActivity;

    public BaseLayoutController(MainActivity activity) {
        this.mActivity = activity;
    }

    public abstract void initViews(View root);
    public abstract void updateFrequency(int freq, String ps, boolean isAm);
    public abstract void updateRDS(String text);
    public abstract void updateRDSText(String text);
    public abstract void updatePTY(String pty);
    public abstract void updateRdsStatus(boolean af, boolean ta, boolean tp);
    public abstract void updateSignal(int level, String label, String color);
    public abstract void updateStereo(boolean stereo);
    public abstract void applySkin(boolean isNight);
    public abstract void updateFavoriteIndicator(boolean isFavorite, int presetIdx, boolean isNight);
    public abstract void updateBandIndicator(int band);
    public abstract void updateMute(boolean muted);
    
    /**
     * {@code null}: reset de arte de emisora antes de una nueva carga (vía {@link LogoManager#clearLogo()} en cada layout).
     * {@code non-null}: reservado; la app carga logos con Glide desde {@link LogoManager}.
     */
    public void updateLogo(android.graphics.Bitmap bitmap) {}

    /**
     * V21.0: Cleanup to avoid memory leaks.
     */
    public void release() {}
}
