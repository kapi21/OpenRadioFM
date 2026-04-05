package com.example.openradiofm.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.example.openradiofm.R;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import java.io.File;

/**
 * V1: Gestor de Logos y Fondos para OpenRadioFM.
 * Extraído de MainActivity para modularizar la carga de imágenes.
 */
public class LogoManager {
    private static final String TAG = "LogoManager";
    private final MainActivity mActivity;
    
    // V21.1: Compatibilidad storage (preferir app-specific dir, fallback a legacy /sdcard)
    private final File mLegacyLogoDir = new File("/sdcard/RadioLogos/");
    private final File mAppLogoDir;
    private final File mActiveLogoDir;
    
    private CustomTarget<Drawable> mBackgroundTarget;

    // V2.4: State guards to avoid redundant reloads/flicker
    private int mLastFallbackResId = -1;
    private String mLastDynamicBgUrl = null;
    private String mLastStationLogoUrl = null;

    public LogoManager(MainActivity activity) {
        this.mActivity = activity;
        File external = activity.getExternalFilesDir(null);
        mAppLogoDir = new File(external != null ? external : activity.getFilesDir(), "RadioLogos");
        mActiveLogoDir = pickWritableDir(mLegacyLogoDir, mAppLogoDir);
        createRadioLogosFolder();
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
        int bgMode = mActivity.mPrefs.getInt("pref_bg_mode", 1);
        
        View root = mActivity.findViewById(R.id.rootLayout);
        if (root == null) return;

        if (bgMode == 0) {
            root.setBackgroundColor(Color.BLACK);
        } else if (bgMode == 1) {
            try {
                File bgJpg = resolveExistingFile("background.jpg");
                File bgPng = (bgJpg == null) ? resolveExistingFile("background.png") : null;
                File backgroundFile = (bgJpg != null) ? bgJpg : bgPng;

                if (backgroundFile != null) {
                    if (mBackgroundTarget != null) {
                        Glide.with(mActivity).clear(mBackgroundTarget);
                        mBackgroundTarget = null;
                    }
                    mBackgroundTarget = new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
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
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(mBackgroundTarget);
                } else {
                    root.setBackgroundResource(R.drawable.bg_grainy_dark);
                }
            } catch (Exception e) {
                root.setBackgroundResource(R.drawable.bg_grainy_dark);
            }
        } else {
            root.setBackgroundColor(Color.BLACK);
        }
    }

    /**
     * Aplica un logo de respaldo (Coche > Icono App).
     */
    public void applyFallbackLogo(ImageView iv) {
        if (iv == null) return;

        // V13.9.1: Ocultar logo principal en Layout 3 (V3)
        if (iv.getId() == R.id.ivMainLogo && mActivity.mIsV3) {
            iv.setVisibility(View.GONE);
            return;
        }

        File logoFile = resolveExistingFile("car_logo.png");
        if (logoFile != null && logoFile.exists()) {
            iv.setVisibility(View.VISIBLE);
            String path = logoFile.getAbsolutePath();
            // No confiar en tag_logo_url para omitir carga: updateStationLogo() pinta el logo de emisora
            // con Glide pero no actualiza el tag, que suele seguir siendo la ruta de car_logo. El “skip”
            // dejaba el bitmap de la emisora anterior visible (K706/V2) hasta el siguiente logo; step +/- sí
            // llamaba antes a clearLogo() (tag=null) y por eso ahí sí aparecía el coche.
            iv.setTag(R.id.tag_logo_url, null);
            try {
                Glide.with(iv).clear(iv);
            } catch (Exception ignored) {
            }
            iv.setImageDrawable(null);
            Glide.with(iv)
                    .load(logoFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transform(new RoundedCorners(24))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv);
            iv.setTag(R.id.tag_logo_url, path);
        } else {
            // UX: no usar el icono de la app como fallback en el logo principal.
            // Si no hay car_logo.png, ocultar el ivMainLogo para evitar confusión/solapamiento.
            if (iv.getId() == R.id.ivMainLogo) {
                MainActivity.setVisibilityIfChanged(iv, View.GONE);
            } else {
                MainActivity.setImageResourceIfChanged(iv, R.drawable.ic_app_logo);
                MainActivity.setVisibilityIfChanged(iv, View.VISIBLE);
            }
        }
    }

    /**
     * Carga el logo de la marca del coche.
     */
    public void loadCarLogo() {
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
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
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
        mLastDynamicBgUrl = null;
        MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.GONE);
        loadCustomBackground();
    }

    /**
     * Actualiza el fondo dinámico (difuminado).
     */
    public void updateDynamicBackground(String logoUrl) {
        // V18.6: El Simple Layout gestiona su propio fondo dinámico basado en paletas.
        // Evitamos que LogoManager interfiera para no mezclar diseños.
        if (mActivity.mIsSimpleLayout) return;

        ImageView ivDynamicBackground = mActivity.findViewById(R.id.ivDynamicBackground);
        if (ivDynamicBackground == null) return;

        int bgMode = mActivity.mPrefs.getInt("pref_bg_mode", 1);
        if (bgMode == 2) {
            if (logoUrl != null && !logoUrl.isEmpty()) {
                if (!logoUrl.equals(mLastDynamicBgUrl)) {
                    mLastDynamicBgUrl = logoUrl;
                    MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.VISIBLE);
                    Glide.with(ivDynamicBackground)
                            .load(logoUrl)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(ivDynamicBackground);
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
        mLastStationLogoUrl = null;

        // Layout V3: el logo de emisora no se muestra en ivMainLogo (GONE), pero Glide/ivDynamicBackground
        // podían dejar arte detrás del TextView de frecuencia (QS6, zapping). Reset duro al limpiar.
        if (mActivity.mIsV3) {
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
            File car = resolveExistingFile("car_logo.png");
            if (car != null && car.exists()) {
                applyFallbackLogo(ivMainLogo);
            } else {
                // Sin car_logo: igual hay que quitar el arte de la emisora anterior (K706/V2, zapping).
                // Mantener el bitmap viejo como "placeholder" dejaba el logo pegado hasta el siguiente fetch.
                try {
                    Glide.with(mActivity.getApplicationContext()).clear(ivMainLogo);
                } catch (Exception ignored) {
                }
                ivMainLogo.setImageDrawable(null);
                MainActivity.setVisibilityIfChanged(ivMainLogo, View.GONE);
            }
        }
        updateDynamicBackground(null);
    }

    /**
     * Encapsula la lógica de carga de logo de estación.
     */
    public void updateStationLogo(int freq, int band, String cachedUrl) {
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
                if (mActivity.mIsV3) {
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

                Glide.with(ivMainLogo)
                        .asBitmap()
                        .load(cachedUrl)
                        .transform(new RoundedCorners(24))
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

            // Cache-first UX: si aún no tenemos URL para esta emisora, no dejar el logo anterior "pegado".
            // Mostrar fallback inmediato mientras llega el lookup (local/cache/red).
            if (ivMainLogo != null && !mActivity.mIsV3) {
                File car = resolveExistingFile("car_logo.png");
                if (car != null && car.exists()) {
                    applyFallbackLogo(ivMainLogo);
                } else {
                    // Sin car_logo.png: mantener el logo actual como placeholder (no vaciar/ocultar).
                    MainActivity.setVisibilityIfChanged(ivMainLogo, View.VISIBLE);
                }
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
                            if (mActivity.mIsV3) {
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

                            Glide.with(ivMainLogo)
                                    .asBitmap()
                                    .load(url)
                                    .transform(new RoundedCorners(24))
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
                            applyFallbackLogo(ivMainLogo);
                            if (mActivity.mIsV3) ivMainLogo.setVisibility(View.GONE);
                            if (mActivity.mIsSimpleLayout && mActivity.mSimpleLayoutManager != null) {
                                mActivity.mSimpleLayoutManager.setDefaultState();
                            }
                        }
                        updateDynamicBackground(null);
                    }
                });
            }, livePs);
        }
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
