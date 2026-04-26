package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selector de packs de iconos cargados desde assets.
 * - Pack 0: drawables del APK.
 * - Pack 1: PNG {@code *_p2.png} ({@code icons_color}).
 * - Pack 2–6: SVG rasterizado + silueta blanca (modo noche / CLEAR / cloud como el resto).
 */
public class IconPackManager {
    public static final int PACK_DEFAULT = 0;
    public static final int PACK_COLOR_P2 = 1;
    public static final int PACK_GOOGLE_P3 = 2;
    public static final int PACK_LUCIDE_P4 = 3;
    public static final int PACK_REMIX_P5 = 4;
    public static final int PACK_AWESOME_P6 = 5;
    public static final int PACK_TABLER_P7 = 6;

    private static final String PREF_KEY = "pref_icon_pack";
    private static final String SUFFIX_P2 = "_p2.png";
    private static final String DIR_COLOR = "icons_color/";
    private static final String DIR_GOOGLE = "icons_google/";
    private static final String DIR_LUCIDE = "icons_lucide/";
    private static final String DIR_REMIX = "icons_remix/";
    private static final String DIR_AWESOME = "icons_awesome/";
    private static final String DIR_TABLER = "icons_tabler/";

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final AssetManager mAssets;
    private final ConcurrentHashMap<String, Drawable> mCache = new ConcurrentHashMap<>();

    public IconPackManager(Context context, SharedPreferences prefs) {
        this.mContext = context.getApplicationContext();
        this.mPrefs = prefs;
        this.mAssets = mContext.getAssets();
    }

    public int getSelectedPack() {
        return (mPrefs != null) ? mPrefs.getInt(PREF_KEY, PACK_DEFAULT) : PACK_DEFAULT;
    }

    public void setSelectedPack(int pack) {
        if (mPrefs == null) return;
        mPrefs.edit().putInt(PREF_KEY, pack).apply();
        mCache.clear();
    }

    /** Packs SVG (Material, Lucide, Remix, Awesome, Tabler): mismo tinte que PNG. */
    public static boolean isSvgTemplatePack(int packId) {
        return packId == PACK_GOOGLE_P3 || packId == PACK_LUCIDE_P4 || packId == PACK_REMIX_P5
                || packId == PACK_AWESOME_P6 || packId == PACK_TABLER_P7;
    }

    public boolean usesSvgTemplatePack() {
        return isSvgTemplatePack(getSelectedPack());
    }

    public void apply(ImageView iv, String baseName, int fallbackResId) {
        if (iv == null) return;
        int pack = getSelectedPack();
        if (pack == PACK_DEFAULT) {
            iv.setLayerType(View.LAYER_TYPE_NONE, null);
            iv.setImageResource(fallbackResId);
            return;
        }
        if (pack == PACK_COLOR_P2) {
            iv.setLayerType(View.LAYER_TYPE_NONE, null);
            Drawable d = loadP2Drawable(baseName);
            if (d != null) {
                iv.setImageDrawable(d);
            } else {
                iv.setImageResource(fallbackResId);
            }
            return;
        }
        if (isSvgTemplatePack(pack)) {
            String file = mapBaseNameToSvgFile(baseName, pack);
            Drawable d = file != null ? loadSvgAsBitmapDrawable(file) : null;
            if (d != null) {
                iv.setLayerType(View.LAYER_TYPE_NONE, null);
                iv.setImageDrawable(d);
            } else {
                iv.setLayerType(View.LAYER_TYPE_NONE, null);
                iv.setImageResource(fallbackResId);
            }
            return;
        }
        iv.setLayerType(View.LAYER_TYPE_NONE, null);
        iv.setImageResource(fallbackResId);
    }

    private Drawable loadP2Drawable(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) return null;

        // Compat:
        // - builds que empaquetan como subcarpeta (assets/icons_color/...)
        // - builds que empaquetan directo en raíz de assets (sourceSets con ../icons_color)
        String primary = DIR_COLOR + baseName + SUFFIX_P2;
        Drawable fromCache = mCache.get(primary);
        if (fromCache != null) return fromCache;

        Drawable d = tryLoadPngFromAssets(primary);
        if (d == null && "btn_next_n".equals(baseName)) {
            d = tryLoadPngFromAssets(DIR_COLOR + "btn_next__n" + SUFFIX_P2);
        }
        if (d == null) {
            // fallback root assets
            d = tryLoadPngFromAssets(baseName + SUFFIX_P2);
            if (d == null && "btn_next_n".equals(baseName)) {
                d = tryLoadPngFromAssets("btn_next__n" + SUFFIX_P2);
            }
        }
        if (d != null) {
            mCache.put(primary, d);
        }
        return d;
    }

    private Drawable tryLoadPngFromAssets(String assetFileName) {
        try (InputStream is = mAssets.open(assetFileName)) {
            Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
            if (bmp == null) return null;
            return new BitmapDrawable(mContext.getResources(), bmp);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String mapBaseNameToSvgFile(String baseName, int pack) {
        if (baseName == null || baseName.trim().isEmpty()) return null;

        if (pack == PACK_GOOGLE_P3) {
            if ("power_off".equals(baseName)) {
                return DIR_GOOGLE + "power_settings_new_24dp_E3E3E3.svg";
            }
            if ("btn_next_n".equals(baseName)) {
                return DIR_GOOGLE + "btn_next__n_p3.svg";
            }
            return DIR_GOOGLE + baseName + "_p3.svg";
        }
        if (pack == PACK_LUCIDE_P4) {
            if ("btn_next_n".equals(baseName)) {
                return DIR_LUCIDE + "btn_next__n_p4.svg";
            }
            return DIR_LUCIDE + baseName + "_p4.svg";
        }
        if (pack == PACK_REMIX_P5) {
            if ("btn_next_n".equals(baseName)) {
                return DIR_REMIX + "btn_next__n_p5.svg";
            }
            return DIR_REMIX + baseName + "_p5.svg";
        }
        if (pack == PACK_AWESOME_P6) {
            if ("btn_next_n".equals(baseName)) {
                return DIR_AWESOME + "btn_next__n_p6.svg";
            }
            return DIR_AWESOME + baseName + "_p6.svg";
        }
        if (pack == PACK_TABLER_P7) {
            if ("btn_next_n".equals(baseName)) {
                return DIR_TABLER + "btn_next__n_p7.svg";
            }
            // En la carpeta hay dos ficheros con sufijo _p2 en lugar de _p7 (ajuste manual).
            if ("ic_android_settings".equals(baseName)) {
                return DIR_TABLER + "ic_android_settings_p2.svg";
            }
            if ("power_off".equals(baseName)) {
                return DIR_TABLER + "power_off_p2.svg";
            }
            return DIR_TABLER + baseName + "_p7.svg";
        }
        return null;
    }

    private static String stripKnownIconDirPrefix(String assetPath) {
        if (assetPath == null) return null;
        String p = assetPath;
        if (p.startsWith(DIR_GOOGLE)) return p.substring(DIR_GOOGLE.length());
        if (p.startsWith(DIR_LUCIDE)) return p.substring(DIR_LUCIDE.length());
        if (p.startsWith(DIR_REMIX)) return p.substring(DIR_REMIX.length());
        if (p.startsWith(DIR_AWESOME)) return p.substring(DIR_AWESOME.length());
        if (p.startsWith(DIR_TABLER)) return p.substring(DIR_TABLER.length());
        return p;
    }

    private Drawable loadSvgAsBitmapDrawable(String assetFileName) {
        Drawable fromCache = mCache.get(assetFileName);
        if (fromCache != null) return fromCache;
        try (InputStream is = mAssets.open(assetFileName)) {
            SVG svg = SVG.getFromInputStream(is);
            Picture picture = svg.renderToPicture();
            float density = mContext.getResources().getDisplayMetrics().density;
            int sizePx = Math.max(1, Math.round(48f * density));
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawPicture(picture, new RectF(0, 0, sizePx, sizePx));
            normalizeSvgRasterToWhiteTemplate(bmp);
            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bmp);
            bd.setTargetDensity(mContext.getResources().getDisplayMetrics());
            mCache.put(assetFileName, bd);
            return bd;
        } catch (Exception e) {
            // fallback root assets (sin subcarpeta)
            try {
                String root = stripKnownIconDirPrefix(assetFileName);
                if (root == null || root.equals(assetFileName)) return null;
                Drawable fromCache2 = mCache.get(root);
                if (fromCache2 != null) return fromCache2;
                try (InputStream is2 = mAssets.open(root)) {
                    SVG svg = SVG.getFromInputStream(is2);
                    Picture picture = svg.renderToPicture();
                    float density = mContext.getResources().getDisplayMetrics().density;
                    int sizePx = Math.max(1, Math.round(48f * density));
                    Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmp);
                    canvas.drawPicture(picture, new RectF(0, 0, sizePx, sizePx));
                    normalizeSvgRasterToWhiteTemplate(bmp);
                    BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bmp);
                    bd.setTargetDensity(mContext.getResources().getDisplayMetrics());
                    mCache.put(root, bd);
                    return bd;
                }
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private static void normalizeSvgRasterToWhiteTemplate(Bitmap bmp) {
        if (bmp == null || bmp.isRecycled()) return;
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] px = new int[w * h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        for (int i = 0; i < px.length; i++) {
            int a = Color.alpha(px[i]);
            if (a == 0) continue;
            px[i] = Color.argb(a, 255, 255, 255);
        }
        bmp.setPixels(px, 0, w, 0, 0, w, h);
    }
}
