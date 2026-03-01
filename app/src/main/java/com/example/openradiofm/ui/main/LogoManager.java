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
import com.example.openradiofm.R;

import java.io.File;

/**
 * V1: Gestor de Logos y Fondos para OpenRadioFM.
 * Extraído de MainActivity para modularizar la carga de imágenes.
 */
public class LogoManager {
    private static final String TAG = "LogoManager";
    private final MainActivity mActivity;
    private final String LOGO_DIR = "/sdcard/RadioLogos/";

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
            Glide.with(mActivity)
                    .load(logoFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv);
        } else {
            iv.setImageResource(R.mipmap.ic_launcher);
            iv.setVisibility(View.VISIBLE);
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
                Glide.with(mActivity)
                        .load(logoFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
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
        ImageView ivDynamicBackground = mActivity.findViewById(R.id.ivDynamicBackground);
        if (ivDynamicBackground == null) return;

        int bgMode = mActivity.mPrefs.getInt("pref_bg_mode", 1);
        if (bgMode == 2) {
            if (logoUrl != null && !logoUrl.isEmpty()) {
                ivDynamicBackground.setVisibility(View.VISIBLE);
                Glide.with(mActivity)
                        .load(logoUrl)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivDynamicBackground);
            } else {
                ivDynamicBackground.setVisibility(View.GONE);
                Glide.with(mActivity).clear(ivDynamicBackground);
                loadCustomBackground();
            }
        } else {
            ivDynamicBackground.setVisibility(View.GONE);
            loadCustomBackground();
        }
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
                        Glide.with(mActivity)
                                .load(cachedUrl)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(ivMainLogo);
                    }
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
                                    Glide.with(mActivity)
                                            .load(url)
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .into(ivMainLogo);
                                }
                            }
                            updateDynamicBackground(url);
                        }
                    } else {
                        mActivity.mLastLogoUrl = "";
                        mActivity.mLogoCachePerBand.remove(bandCacheKey);
                        if (ivMainLogo != null) {
                            applyFallbackLogo(ivMainLogo);
                            if (mActivity.mIsV3) ivMainLogo.setVisibility(View.GONE);
                        }
                        updateDynamicBackground(null);
                    }
                });
            });
        }

        // V3 ya permite la visualización del logo. Sin restricción aquí.
    }
}
