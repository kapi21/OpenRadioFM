package com.example.openradiofm.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
    private final String LOGO_DIR = "/sdcard/RadioLogos/";

    // V2.4: State guards to avoid redundant reloads/flicker
    private int mLastFallbackResId = -1;
    private String mLastDynamicBgUrl = null;
    private String mLastStationLogoUrl = null;

    public LogoManager(MainActivity activity) {
        this.mActivity = activity;
        createRadioLogosFolder();
    }

    /**
     * Crea la carpeta /sdcard/RadioLogos/ si no existe.
     */
    public void createRadioLogosFolder() {
        try {
            File radioLogosDir = new File(LOGO_DIR);
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
                File bgJpg = new File(LOGO_DIR + "background.jpg");
                File bgPng = new File(LOGO_DIR + "background.png");
                File backgroundFile = bgJpg.exists() ? bgJpg : (bgPng.exists() ? bgPng : null);

                if (backgroundFile != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(backgroundFile.getAbsolutePath());
                    if (bitmap != null) {
                        root.setBackground(new BitmapDrawable(mActivity.getResources(), bitmap));
                    }
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

        File logoFile = new File(LOGO_DIR + "car_logo.png");
        if (logoFile.exists()) {
            iv.setVisibility(View.VISIBLE);
            // V2.4: Only reload if needed
            String path = logoFile.getAbsolutePath();
            Object lastTag = iv.getTag(R.id.tag_logo_url);
            if (lastTag == null || !path.equals(lastTag)) {
                iv.setImageDrawable(null); // Clear to avoid overlapping during load
                Glide.with(mActivity)
                        .load(logoFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .transform(new RoundedCorners(24))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(iv);
                iv.setTag(R.id.tag_logo_url, path);
            }
        } else {
            MainActivity.setImageResourceIfChanged(iv, R.mipmap.ic_launcher);
            MainActivity.setVisibilityIfChanged(iv, View.VISIBLE);
        }
    }

    /**
     * Carga el logo de la marca del coche.
     */
    public void loadCarLogo() {
        ImageView ivCarLogo = mActivity.findViewById(R.id.ivCarLogo);
        ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);

        File logoFile = new File(LOGO_DIR + "car_logo.png");
        boolean logoExists = logoFile.exists();

        if (ivCarLogo != null) {
            if (logoExists) {
                ivCarLogo.setVisibility(View.VISIBLE);
                ivCarLogo.setImageDrawable(null); // Clear overlap
                Glide.with(mActivity)
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
                    Glide.with(mActivity)
                            .load(logoUrl)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(ivDynamicBackground);
                }
            } else {
                if (mLastDynamicBgUrl != null) {
                    mLastDynamicBgUrl = null;
                    MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.GONE);
                    Glide.with(mActivity).clear(ivDynamicBackground);
                    loadCustomBackground();
                }
            }
        } else {
            if (mLastDynamicBgUrl != null || ivDynamicBackground.getVisibility() == View.VISIBLE) {
                mLastDynamicBgUrl = null;
                MainActivity.setVisibilityIfChanged(ivDynamicBackground, View.GONE);
                loadCustomBackground();
            }
        }
    }

    /**
     * Limpia el logo actual y restablece el estado visual.
     * Útil al cambiar de frecuencia para evitar persistencia.
     */
    public void clearLogo() {
        mLastStationLogoUrl = null;
        ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
        if (ivMainLogo != null) {
            ivMainLogo.setTag(R.id.tag_logo_url, null);
            applyFallbackLogo(ivMainLogo);
        }
        updateDynamicBackground(null);
    }

    /**
     * Encapsula la lógica de carga de logo de estación.
     */
    public void updateStationLogo(int freq, int band, String cachedUrl) {
        ImageView ivMainLogo = mActivity.findViewById(R.id.ivMainLogo);
        String bandCacheKey = band + "_" + freq;

        if (cachedUrl != null) {
            if (!cachedUrl.equals(mActivity.mLastLogoUrl)) {
                mActivity.mLastLogoUrl = cachedUrl;
                if (ivMainLogo != null) {
                    if (mActivity.mIsV3) {
                        ivMainLogo.setVisibility(View.GONE);
                    } else {
                        ivMainLogo.setVisibility(View.VISIBLE);
                        ivMainLogo.setImageDrawable(null); // Clear overlap
                    }
                    
                    // V18.6.2: Gestión Unificada de Glide para evitar solapamientos y suavizar transiciones
                    Glide.with(mActivity)
                            .asBitmap()
                            .load(cachedUrl)
                            .transform(new RoundedCorners(24))
                            // Animación más larga para sensación premium (800ms)
                            .transition(BitmapTransitionOptions.withCrossFade(800))
                            // Usamos el drawable actual como placeholder para un fundido limpio
                            .placeholder(ivMainLogo.getDrawable())
                            .into(new com.bumptech.glide.request.target.BitmapImageViewTarget(ivMainLogo) {
                                @Override
                                public void onResourceReady(Bitmap resource, com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                    super.onResourceReady(resource, transition);
                                    
                                    // Notificar a otros componentes una vez el bitmap está listo
                                    if (mActivity.mIsSimpleLayout && mActivity.mSimpleLayoutManager != null) {
                                        // SimpleLayoutManager ya no necesita hacer su propio setImageBitmap
                                        // pero sí necesita el bitmap para la paleta y el fondo.
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
            }
        } else {
            if (mActivity.mRepository == null) return;
            mActivity.mRepository.getStationInfo(freq, url -> {
                mActivity.runOnUiThread(() -> {
                    if (url != null) {
                        if (!url.equals(mActivity.mLastLogoUrl)) {
                            mActivity.mLastLogoUrl = url;
                            mActivity.mLogoCachePerBand.put(bandCacheKey, url);
                            if (ivMainLogo != null) {
                                if (mActivity.mIsV3) {
                                    ivMainLogo.setVisibility(View.GONE);
                                } else {
                                    ivMainLogo.setVisibility(View.VISIBLE);
                                    ivMainLogo.setImageDrawable(null); // Clear overlap
                                }
                                
                                // V18.6.2: Aplicar la misma transición premium en la búsqueda asíncrona
                                Glide.with(mActivity)
                                        .asBitmap()
                                        .load(url)
                                        .transform(new RoundedCorners(24))
                                        .transition(BitmapTransitionOptions.withCrossFade(800))
                                        .placeholder(ivMainLogo.getDrawable())
                                        .into(new com.bumptech.glide.request.target.BitmapImageViewTarget(ivMainLogo) {
                                            @Override
                                            public void onResourceReady(Bitmap resource, com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
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
                        }
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
            });
        }
    }
}
