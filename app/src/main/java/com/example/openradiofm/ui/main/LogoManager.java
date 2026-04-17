package com.example.openradiofm.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.core.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.example.openradiofm.R;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import androidx.annotation.Nullable;
import java.io.File;

/**
 * V1: Gestor de Logos y Fondos para OpenRadioFM.
 * Extraído de MainActivity para modularizar la carga de imágenes.
 */
public class LogoManager {
    private static final String TAG = "LogoManager";
    /** Fondo difuminado: no hace falta píxel a píxel; limitar decode mejora fluidez y RAM en head units lentas. */
    private static final int DYNAMIC_BG_MAX_EDGE_PX = 1600;
    private final MainActivity mActivity;
    
    // V21.1: Compatibilidad storage (preferir app-specific dir, fallback a legacy /sdcard)
    private final File mLegacyLogoDir = new File("/sdcard/RadioLogos/");
    private final File mAppLogoDir;
    private final File mActiveLogoDir;
    
    private CustomTarget<Drawable> mBackgroundTarget;
    private CustomTarget<Drawable> mDynamicBgTarget;

    // V2.4: State guards to avoid redundant reloads/flicker
    private int mLastFallbackResId = -1;
    private String mLastDynamicBgUrl = null;
    private String mLastStationLogoUrl = null;
    private int mLastBgMode = Integer.MIN_VALUE;
    private String mLastCustomBgPath = null;

    public LogoManager(MainActivity activity) {
        this.mActivity = activity;
        File external = activity.getExternalFilesDir(null);
        mAppLogoDir = new File(external != null ? external : activity.getFilesDir(), "RadioLogos");
        mActiveLogoDir = pickWritableDir(mLegacyLogoDir, mAppLogoDir);
        createRadioLogosFolder();
    }

    private boolean isActivityUsable() {
        try {
            if (mActivity == null) return false;
            if (mActivity.isFinishing()) return false;
            if (android.os.Build.VERSION.SDK_INT >= 17 && mActivity.isDestroyed()) return false;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
    
    private static File pickWritableDir(File legacy, File appDir) {
        try {
            if (legacy.exists() || legacy.mkdirs()) {
                if (legacy.canWrite()) return legacy;
            }
        } catch (Exception ignored) {}
        try { appDir.mkdirs(); } catch (Exception ignored) {}
        return appDir;
    }
    
    private File resolveExistingFile(String fileName) {
        File legacy = new File(mLegacyLogoDir, fileName);
        if (legacy.exists()) return legacy;
        File app = new File(mAppLogoDir, fileName);
        if (app.exists()) return app;
        return null;
    }

    /** Layout Simple: el logo vive dentro de {@link R.id#boxLogoSimple}; debe quedar visible al cargar emisora. */
    private void ensureSimpleLogoBoxVisible() {
        View box = mActivity.findViewById(R.id.boxLogoSimple);
        if (box != null) {
            MainActivity.setVisibilityIfChanged(box, View.VISIBLE);
        }
    }

    /**
     * Crea la carpeta /sdcard/RadioLogos/ si no existe.
     */
    public void createRadioLogosFolder() {
        try {
            File radioLogosDir = mActiveLogoDir;
            if (!radioLogosDir.exists()) {
                if (radioLogosDir.mkdirs()) {
                    Log.d(TAG, "Carpeta RadioLogos creada exitosamente");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al crear carpeta RadioLogos", e);
        }
    }

    /**
     * Carga el fondo según la preferencia del usuario.
     */
    public void loadCustomBackground() {
        if (mActivity.mPrefs == null) return;
        final int genAtStart = mActivity.mLogoUiGeneration.get();
        // DAY_MODE: fondo beige fijo (ignorar background.jpg/dinámico).
        try {
            if (mActivity.mThemeManager != null
                    && mActivity.mThemeManager.getActiveSkin() == com.example.openradiofm.ui.theme.ThemeManager.Skin.DAY_MODE) {
                View root = mActivity.findViewById(R.id.rootLayout);
                if (root != null) {
                    if (mBackgroundTarget != null) {
                        try { Glide.with(mActivity.getApplicationContext()).clear(mBackgroundTarget); } catch (Exception ignored) {}
                        mBackgroundTarget = null;
                    }
                    // Si veníamos de fondo dinámico, ocultarlo y limpiar el bitmap para evitar “mezclas”.
                    try {
                        ImageView ivDynamicBackground = mActivity.findViewById(R.id.ivDynamicBackground);
                        if (ivDynamicBackground != null) {
                            Glide.with(mActivity.getApplicationContext()).clear(ivDynamicBackground);
                            ivDynamicBackground.setImageDrawable(null);
                            MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.GONE);
                        }
                        mLastDynamicBgUrl = null;
                    } catch (Exception ignored) {}
                    // Modo Día: mismo beige que la ventana (@color/day_mode_background)
                    root.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.day_mode_background));
                    mLastBgMode = Integer.MIN_VALUE;
                    mLastCustomBgPath = null;
                }
                return;
            }
        } catch (Exception ignored) {}
        int bgMode = mActivity.mPrefs.getInt("pref_bg_mode", 1);
        
        View root = mActivity.findViewById(R.id.rootLayout);
        if (root == null) return;

        if (bgMode == 0) {
            if (mLastBgMode == 0) return;
            mLastBgMode = 0;
            mLastCustomBgPath = null;
            if (mBackgroundTarget != null) {
                try { Glide.with(mActivity.getApplicationContext()).clear(mBackgroundTarget); } catch (Exception ignored) {}
                mBackgroundTarget = null;
            }
            root.setBackgroundColor(Color.BLACK);
        } else if (bgMode == 1) {
            try {
                File bgJpg = resolveExistingFile("background.jpg");
                File bgPng = (bgJpg == null) ? resolveExistingFile("background.png") : null;
                File backgroundFile = (bgJpg != null) ? bgJpg : bgPng;

                if (backgroundFile != null) {
                    final String newPath = backgroundFile.getAbsolutePath();
                    // V2.6: Evitar flicker: si el fondo ya es el mismo, no reiniciar Glide ni limpiar el drawable.
                    if (mLastBgMode == 1 && newPath.equals(mLastCustomBgPath) && root.getBackground() != null) {
                        return;
                    }
                    mLastBgMode = 1;
                    mLastCustomBgPath = newPath;
                    if (mBackgroundTarget != null) {
                        // IMPORTANT: clear() dispara onLoadCleared y puede "parpadear" el fondo.
                        // Solo limpiar cuando realmente vamos a reemplazar el archivo de fondo.
                        try { Glide.with(mActivity.getApplicationContext()).clear(mBackgroundTarget); } catch (Exception ignored) {}
                        mBackgroundTarget = null;
                    }
                    mBackgroundTarget = new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                            if (genAtStart != mActivity.mLogoUiGeneration.get()) return;
                            root.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // CRÍTICO: si Glide limpia el drawable, el bitmap interno puede reciclarse.
                            // Si lo dejamos como background, puede crashear al dibujar.
                            try {
                                root.setBackgroundResource(R.drawable.bg_grainy_dark);
                            } catch (Exception ignored) {}
                        }
                    };
                    Glide.with(mActivity)
                            .load(backgroundFile)
                            .centerCrop()
                            // Archivo local estable: cachear decode (antes NONE recargaba desde disco siempre).
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(mBackgroundTarget);
                } else {
                    if (mLastBgMode == 1 && mLastCustomBgPath == null) return;
                    mLastBgMode = 1;
                    mLastCustomBgPath = null;
                    root.setBackgroundResource(R.drawable.bg_grainy_dark);
                }
            } catch (Exception e) {
                mLastBgMode = 1;
                mLastCustomBgPath = null;
                root.setBackgroundResource(R.drawable.bg_grainy_dark);
            }
        } else {
            if (mLastBgMode == bgMode) return;
            mLastBgMode = bgMode;
            mLastCustomBgPath = null;
            if (mBackgroundTarget != null) {
                try { Glide.with(mActivity.getApplicationContext()).clear(mBackgroundTarget); } catch (Exception ignored) {}
                mBackgroundTarget = null;
            }
            root.setBackgroundColor(Color.BLACK);
        }
    }

    /** Marca en {@link R.id#tag_logo_url} para el fallback del slot principal (no es URL remota). */
    private static final String FALLBACK_MAIN_LOGO_TAG = "__fallback_ic_toast__";

    /**
     * Logo de respaldo cuando no hay logo de emisora.
     * <ul>
     *   <li>{@link R.id#ivMainLogo} en V2/Simple: {@code ic_toast} (drawable nodpi; Glide fitCenter, sin recorte redondeado).</li>
     *   <li>{@link R.id#ivMainLogo} en V3: oculto (emisora va por fondo dinámico).</li>
     *   <li>Otros {@link ImageView}: {@code car_logo.png} si existe; si no, icono de la app.</li>
     * </ul>
     * El slot {@link R.id#ivCarLogo} (marca coche en V3) se carga solo en {@link #loadCarLogo()}.
     */
    public void applyFallbackLogo(ImageView iv) {
        if (iv == null) return;
        if (!isActivityUsable()) return;

        // V13.9.1: Ocultar logo principal en Layout 3 (V3)
        if (iv.getId() == R.id.ivMainLogo && mActivity.isV3LayoutActive()) {
            iv.setVisibility(View.GONE);
            return;
        }

        // V2/Simple: fallback de emisora = ic_toast (sin RoundedCorners: más nítido al escalar).
        if (iv.getId() == R.id.ivMainLogo) {
            iv.setVisibility(View.VISIBLE);
            iv.setAlpha(1.0f);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (mActivity.mIsSimpleLayout) {
                View box = mActivity.findViewById(R.id.boxLogoSimple);
                if (box != null) {
                    MainActivity.setVisibilityIfChanged(box, View.VISIBLE);
                }
            }
            iv.setTag(R.id.tag_logo_url, null);
            try {
                Glide.with(iv).clear(iv);
            } catch (Exception ignored) {
            }
            iv.setImageDrawable(null);
            Glide.with(iv)
                    .load(R.drawable.ic_toast)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(new RequestOptions()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .fitCenter()
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                    .transition(DrawableTransitionOptions.withCrossFade(0))
                    .into(iv);
            iv.setTag(R.id.tag_logo_url, FALLBACK_MAIN_LOGO_TAG);
            return;
        }

        File logoFile = resolveExistingFile("car_logo.png");
        if (logoFile != null && logoFile.exists()) {
            iv.setVisibility(View.VISIBLE);
            String path = logoFile.getAbsolutePath();
            iv.setTag(R.id.tag_logo_url, null);
            try {
                Glide.with(iv).clear(iv);
            } catch (Exception ignored) {
            }
            iv.setImageDrawable(null);
            Glide.with(iv)
                    .load(logoFile)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transform(new RoundedCorners(24))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv);
            iv.setTag(R.id.tag_logo_url, path);
        } else {
            MainActivity.setImageResourceIfChanged(iv, R.drawable.ic_app_logo);
            MainActivity.setVisibilityIfChanged(iv, View.VISIBLE);
        }
    }

    /**
     * Carga {@code car_logo.png} en {@link R.id#ivCarLogo} (Layout V3 / reloj vs coche).
     * El slot {@link R.id#ivMainLogo} en V2/Simple lo rellena {@link #applyFallbackLogo(ImageView)} (ic_toast).
     */
    public void loadCarLogo() {
        if (!isActivityUsable()) return;
        ImageView ivCarLogo = mActivity.findViewById(R.id.ivCarLogo);
        ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);

        File logoFile = resolveExistingFile("car_logo.png");
        boolean logoExists = logoFile != null && logoFile.exists();

        if (ivCarLogo != null) {
            if (logoExists) {
                ivCarLogo.setVisibility(View.VISIBLE);
                ivCarLogo.setImageDrawable(null); // Clear overlap
                Glide.with(ivCarLogo)
                        .load(logoFile)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .transform(new RoundedCorners(24))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivCarLogo);
            } else {
                ivCarLogo.setVisibility(View.INVISIBLE);
            }
        }

        if (ivMainLogo != null) {
            applyFallbackLogo(ivMainLogo);
        }
    }

    /**
     * Quita por completo el arte de emisora del {@link R.id#ivDynamicBackground} (Glide + visibilidad).
     * Evita “fantasmas” cuando {@code mLastDynamicBgUrl} ya era null pero el ImageView seguía con bitmap.
     */
    private void clearDynamicBackdropHard(ImageView ivDynamicBackground) {
        if (ivDynamicBackground == null) return;
        try {
            Glide.with(mActivity.getApplicationContext()).clear(ivDynamicBackground);
        } catch (Exception ignored) {
        }
        if (mDynamicBgTarget != null) {
            try { Glide.with(mActivity.getApplicationContext()).clear(mDynamicBgTarget); } catch (Exception ignored) {}
            mDynamicBgTarget = null;
        }
        try { ivDynamicBackground.setImageDrawable(null); } catch (Exception ignored) {}
        mLastDynamicBgUrl = null;
        MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.GONE);
        // V2.6: No forzar recarga del fondo estático aquí; en bgMode=1 provocaba parpadeo (clear+reload).
    }

    /**
     * Actualiza el fondo dinámico (difuminado).
     */
    public void updateDynamicBackground(String logoUrl) {
        if (!isActivityUsable()) return;
        // V18.6: El Simple Layout gestiona su propio fondo dinámico basado en paletas.
        // Evitamos que LogoManager interfiera para no mezclar diseños.
        if (mActivity.mIsSimpleLayout) return;

        ImageView ivDynamicBackground = mActivity.findViewById(R.id.ivDynamicBackground);
        if (ivDynamicBackground == null) return;

        int bgMode = mActivity.mPrefs.getInt("pref_bg_mode", 1);
        if (bgMode == 2) {
            if (logoUrl != null && !logoUrl.isEmpty()) {
                // Si la URL es la misma pero el fondo quedó vacío (p. ej. zapping → cancelación → onLoadCleared),
                // debemos reintentar la carga para evitar "a veces no aparece".
                boolean needsReload = !logoUrl.equals(mLastDynamicBgUrl)
                        || ivDynamicBackground.getDrawable() == null
                        || ivDynamicBackground.getVisibility() != View.VISIBLE
                        || mDynamicBgTarget == null;
                if (needsReload) {
                    final int genAtStart = mActivity.mLogoUiGeneration.get();
                    mLastDynamicBgUrl = logoUrl;
                    MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.VISIBLE);
                    if (mDynamicBgTarget != null) {
                        try { Glide.with(mActivity.getApplicationContext()).clear(mDynamicBgTarget); } catch (Exception ignored) {}
                        mDynamicBgTarget = null;
                    }
                    mDynamicBgTarget = new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                            if (genAtStart != mActivity.mLogoUiGeneration.get()) return;
                            ivDynamicBackground.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // Evitar “fantasmas” al invalidar por generación
                            try { ivDynamicBackground.setImageDrawable(null); } catch (Exception ignored) {}
                        }
                    };
                    Glide.with(ivDynamicBackground)
                            .load(logoUrl)
                            .apply(new RequestOptions()
                                    .format(DecodeFormat.PREFER_ARGB_8888)
                                    .override(DYNAMIC_BG_MAX_EDGE_PX, DYNAMIC_BG_MAX_EDGE_PX))
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(mDynamicBgTarget);
                }
            } else {
                if (mLastDynamicBgUrl != null
                        || ivDynamicBackground.getVisibility() == View.VISIBLE
                        || ivDynamicBackground.getDrawable() != null) {
                    clearDynamicBackdropHard(ivDynamicBackground);
                }
            }
        } else {
            if (mLastDynamicBgUrl != null || ivDynamicBackground.getVisibility() == View.VISIBLE) {
                clearDynamicBackdropHard(ivDynamicBackground);
            }
        }
    }

    /**
     * Limpia el logo actual y restablece el estado visual.
     * Útil al cambiar de frecuencia para evitar persistencia.
     */
    public void clearLogo() {
        if (!isActivityUsable()) return;
        mLastStationLogoUrl = null;

        // Layout V3: el logo de emisora no se muestra en ivMainLogo (GONE), pero Glide/ivDynamicBackground
        // podían dejar arte detrás del TextView de frecuencia (QS6, zapping). Reset duro al limpiar.
        if (mActivity.isV3LayoutActive()) {
            mActivity.mLastLogoUrl = "";
            ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
            if (ivMainLogo != null) {
                try {
                    Glide.with(mActivity.getApplicationContext()).clear(ivMainLogo);
                } catch (Exception ignored) {
                }
                ivMainLogo.setTag(R.id.tag_logo_url, null);
                ivMainLogo.setImageDrawable(null);
                MainActivity.setVisibilityIfChanged(ivMainLogo, View.GONE);
            }
            ImageView ivDyn = mActivity.findViewById(R.id.ivDynamicBackground);
            clearDynamicBackdropHard(ivDyn);
            return;
        }

        ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
        if (ivMainLogo != null) {
            ivMainLogo.setTag(R.id.tag_logo_url, null);
            // Fallback de emisora (ic_toast); quita arte Glide anterior sin depender de car_logo.
            applyFallbackLogo(ivMainLogo);
        }
        updateDynamicBackground(null);
    }

    /**
     * Encapsula la lógica de carga de logo de estación.
     */
    public void updateStationLogo(int freq, int band, String cachedUrl) {
        if (!isActivityUsable()) return;
        final int logoGen = mActivity.mLogoUiGeneration.get();
        ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
        String bandCacheKey = band + "_" + freq;

        if (cachedUrl != null) {
            if (!isLogoRequestStillValid(logoGen, freq, band)) return;
            // Misma URL que ya mostrábamos: igual hay que reaplicar Glide (un updateStationLogo(null) intermedio
            // pudo pintar car_logo encima; QS6 ~1–2s tras sintonizar).
            boolean sameUrlAsLast = cachedUrl.equals(mActivity.mLastLogoUrl);
            if (!sameUrlAsLast) {
                mActivity.mLastLogoUrl = cachedUrl;
                mActivity.mLogoCachePerBand.put(bandCacheKey, cachedUrl);
            }
            if (ivMainLogo != null) {
                if (mActivity.isV3LayoutActive()) {
                    try {
                        Glide.with(mActivity.getApplicationContext()).clear(ivMainLogo);
                    } catch (Exception ignored) {
                    }
                    ivMainLogo.setImageDrawable(null);
                    ivMainLogo.setVisibility(View.GONE);
                } else {
                    ivMainLogo.setVisibility(View.VISIBLE);
                    if (!sameUrlAsLast) {
                        ivMainLogo.setImageDrawable(null);
                    }
                }
                if (mActivity.mIsSimpleLayout) {
                    ensureSimpleLogoBoxVisible();
                }

                Glide.with(ivMainLogo)
                        .asBitmap()
                        .load(cachedUrl)
                        .apply(new RequestOptions()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                        .transform(new RoundedCorners(24))
                        .listener(stationLogoLoadFailureListener(logoGen, freq, band, ivMainLogo))
                        .transition(sameUrlAsLast
                                ? BitmapTransitionOptions.withCrossFade(0)
                                : BitmapTransitionOptions.withCrossFade(800))
                        .into(new com.bumptech.glide.request.target.BitmapImageViewTarget(ivMainLogo) {
                            @Override
                            public void onResourceReady(Bitmap resource, com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                                if (!isLogoRequestStillValid(logoGen, freq, band)) return;
                                super.onResourceReady(resource, transition);

                                if (mActivity.mIsSimpleLayout && mActivity.mSimpleLayoutManager != null) {
                                    mActivity.mSimpleLayoutManager.updateLogoPalette(resource);
                                }

                                if (mActivity.mMediaSessionManager != null) {
                                    String rdsName = (mActivity.mLastPs != null) ? mActivity.mLastPs : "";
                                    String freqStr = String.format("%.1f MHz", freq / 1000.0f);
                                    mActivity.mMediaSessionManager.updateMetadata(rdsName, freqStr, resource);
                                }
                            }
                        });
            }
            updateDynamicBackground(cachedUrl);
        } else {
            if (mActivity.mRepository == null) return;
            // Ya tenemos URL para band+freq: no pasar por lookup async ni pintar coche intermedio.
            String cachedForSlot = mActivity.mLogoCachePerBand.get(bandCacheKey);
            if (cachedForSlot != null && !cachedForSlot.trim().isEmpty()) {
                updateStationLogo(freq, band, cachedForSlot);
                return;
            }
            String livePs = null;
            if (mActivity.mRdsManager != null && mActivity.mEngine != null && freq == mActivity.mEngine.getCurrentFreq()) {
                String cn = mActivity.mRdsManager.getConfirmedName();
                if (cn != null && !cn.trim().isEmpty()) {
                    livePs = cn.trim();
                }
            }

            // Cache-first UX: fallback inmediato (ic_toast) mientras llega el lookup (local/cache/red).
            if (ivMainLogo != null && !mActivity.isV3LayoutActive()) {
                if (mActivity.mIsSimpleLayout) {
                    ensureSimpleLogoBoxVisible();
                }
                applyFallbackLogo(ivMainLogo);
            }

            mActivity.mRepository.getStationInfo(freq, url -> {
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    if (!isLogoRequestStillValid(logoGen, freq, band)) return;

                    if (url != null) {
                        boolean sameAsLast = url.equals(mActivity.mLastLogoUrl);
                        if (!sameAsLast) {
                            mActivity.mLastLogoUrl = url;
                            mActivity.mLogoCachePerBand.put(bandCacheKey, url);
                        }
                        if (ivMainLogo != null) {
                            if (mActivity.isV3LayoutActive()) {
                                try {
                                    Glide.with(mActivity.getApplicationContext()).clear(ivMainLogo);
                                } catch (Exception ignored) {
                                }
                                ivMainLogo.setImageDrawable(null);
                                ivMainLogo.setVisibility(View.GONE);
                            } else {
                                ivMainLogo.setVisibility(View.VISIBLE);
                                if (!sameAsLast) {
                                    ivMainLogo.setImageDrawable(null);
                                }
                            }
                            if (mActivity.mIsSimpleLayout) {
                                ensureSimpleLogoBoxVisible();
                            }

                            Glide.with(ivMainLogo)
                                    .asBitmap()
                                    .load(url)
                                    .apply(new RequestOptions()
                                            .format(DecodeFormat.PREFER_ARGB_8888)
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                                    .transform(new RoundedCorners(24))
                                    .listener(stationLogoLoadFailureListener(logoGen, freq, band, ivMainLogo))
                                    .transition(sameAsLast
                                            ? BitmapTransitionOptions.withCrossFade(0)
                                            : BitmapTransitionOptions.withCrossFade(800))
                                    .into(new com.bumptech.glide.request.target.BitmapImageViewTarget(ivMainLogo) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                            if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                                            if (!isLogoRequestStillValid(logoGen, freq, band)) return;
                                            super.onResourceReady(resource, transition);
                                            if (mActivity.mIsSimpleLayout && mActivity.mSimpleLayoutManager != null) {
                                                mActivity.mSimpleLayoutManager.updateLogoPalette(resource);
                                            }
                                            if (mActivity.mMediaSessionManager != null) {
                                                String rdsName = (mActivity.mLastPs != null) ? mActivity.mLastPs : "";
                                                String freqStr = String.format("%.1f MHz", freq / 1000.0f);
                                                mActivity.mMediaSessionManager.updateMetadata(rdsName, freqStr, resource);
                                            }
                                        }
                                    });
                        }
                        updateDynamicBackground(url);
                    } else {
                        mActivity.mLastLogoUrl = "";
                        mActivity.mLogoCachePerBand.remove(bandCacheKey);
                        if (ivMainLogo != null) {
                            if (mActivity.mIsSimpleLayout && mActivity.mSimpleLayoutManager != null) {
                                mActivity.mSimpleLayoutManager.setDefaultState();
                            }
                            applyFallbackLogo(ivMainLogo);
                            if (mActivity.isV3LayoutActive()) ivMainLogo.setVisibility(View.GONE);
                        }
                        updateDynamicBackground(null);
                    }
                });
            }, livePs);
        }
    }

    /**
     * Si falla la descarga/decodificación del logo de emisora, volver al fallback (ic_toast) en V2/Simple
     * (evita hueco o bitmap “pegado” de un intento anterior).
     */
    private RequestListener<android.graphics.Bitmap> stationLogoLoadFailureListener(
            final int logoGen, final int freq, final int band, final ImageView iv) {
        return new RequestListener<android.graphics.Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                    Target<android.graphics.Bitmap> target, boolean isFirstResource) {
                if (mActivity.isFinishing() || mActivity.isDestroyed()) return false;
                if (mActivity.isV3LayoutActive() || iv == null) return false;
                if (!isLogoRequestStillValid(logoGen, freq, band)) return true;
                mActivity.runOnUiThread(() -> {
                    if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
                    if (!isLogoRequestStillValid(logoGen, freq, band)) return;
                    applyFallbackLogo(iv);
                });
                return true;
            }

            @Override
            public boolean onResourceReady(android.graphics.Bitmap resource, Object model,
                    Target<android.graphics.Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        };
    }

    /**
     * Evita aplicar logos de una petición obsoleta (QS6: AIDL + shadow; zapping rápido).
     */
    private boolean isLogoRequestStillValid(int logoGen, int freq, int band) {
        if (logoGen != mActivity.mLogoUiGeneration.get()) {
            return false;
        }
        if (mActivity.mEngine != null) {
            try {
                if (mActivity.mEngine.getCurrentFreq() != freq) return false;
                if (mActivity.mEngine.getCurrentBand() != band) return false;
            } catch (Exception ignored) {
                return false;
            }
        }
        return true;
    }

    /**
     * V18.6.2: Cleanup to avoid memory leaks.
     */
    public void release() {
        mLastDynamicBgUrl = null;
        mLastStationLogoUrl = null;
        
        try {
            // Contexto de aplicación: en onDestroy / recreación por layout, Glide.with(Activity)
            // lanza "You cannot start a load for a destroyed activity".
            Context appCtx = mActivity.getApplicationContext();
            if (mBackgroundTarget != null) {
                Glide.with(appCtx).clear(mBackgroundTarget);
                mBackgroundTarget = null;
            }
            ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
            if (ivMainLogo != null) {
                Glide.with(appCtx).clear(ivMainLogo);
            }
            ImageView ivCarLogo = mActivity.findViewById(R.id.ivCarLogo);
            if (ivCarLogo != null) {
                Glide.with(appCtx).clear(ivCarLogo);
            }
            ImageView ivDynamicBackground = mActivity.findViewById(R.id.ivDynamicBackground);
            if (ivDynamicBackground != null) {
                Glide.with(appCtx).clear(ivDynamicBackground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during release", e);
        }
    }
}
