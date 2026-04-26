package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.caverock.androidsvg.SVG;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carga números 1..18 desde assets (SVG) para el indicador de favorito.
 *
 * Ficheros esperados: icons_numbers/number-1-small.svg ... icons_numbers/number-18-small.svg
 */
public class PresetNumberIconManager {
    private final Context mContext;
    private final AssetManager mAssets;
    private final ConcurrentHashMap<String, Drawable> mCache = new ConcurrentHashMap<>();

    public PresetNumberIconManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mAssets = mContext.getAssets();
    }

    public void clearCache() {
        mCache.clear();
    }

    public Drawable loadNumberSmallSvg(int presetIdx) {
        if (presetIdx < 1 || presetIdx > 18) return null;
        String assetFileName = "icons_numbers/number-" + presetIdx + "-small.svg";
        Drawable fromCache = mCache.get(assetFileName);
        if (fromCache != null) return fromCache;

        try (InputStream is = mAssets.open(assetFileName)) {
            SVG svg = SVG.getFromInputStream(is);
            Picture picture = svg.renderToPicture();
            float density = mContext.getResources().getDisplayMetrics().density;
            int sizePx = Math.max(1, Math.round(56f * density)); // coincide aprox con ivFavoriteIndicator
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawPicture(picture, new RectF(0, 0, sizePx, sizePx));
            normalizeSvgRasterToWhiteTemplate(bmp);
            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bmp);
            bd.setTargetDensity(mContext.getResources().getDisplayMetrics());
            mCache.put(assetFileName, bd);
            return bd;
        } catch (Exception ignored) {
            // fallback compat: assets en raíz (sourceSets con ../icons_numbers)
            String rootName = "number-" + presetIdx + "-small.svg";
            Drawable fromCache2 = mCache.get(rootName);
            if (fromCache2 != null) return fromCache2;
            try (InputStream is2 = mAssets.open(rootName)) {
                SVG svg = SVG.getFromInputStream(is2);
                Picture picture = svg.renderToPicture();
                float density = mContext.getResources().getDisplayMetrics().density;
                int sizePx = Math.max(1, Math.round(56f * density));
                Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                canvas.drawPicture(picture, new RectF(0, 0, sizePx, sizePx));
                normalizeSvgRasterToWhiteTemplate(bmp);
                BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bmp);
                bd.setTargetDensity(mContext.getResources().getDisplayMetrics());
                mCache.put(rootName, bd);
                return bd;
            } catch (Exception ignored2) {
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

