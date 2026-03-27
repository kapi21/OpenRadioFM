package com.example.openradiofm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selector simple de packs de iconos (PNG) cargados desde assets.
 * Si el PNG no existe, se mantiene el drawable resource actual.
 */
public class IconPackManager {
    private static final int PACK_DEFAULT = 0;
    private static final int PACK_COLOR_P2 = 1;

    private static final String PREF_KEY = "pref_icon_pack";
    private static final String SUFFIX_P2 = "_p2.png";

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

    public void apply(ImageView iv, String baseName, int fallbackResId) {
        if (iv == null) return;
        int pack = getSelectedPack();
        if (pack != PACK_COLOR_P2) {
            iv.setImageResource(fallbackResId);
            return;
        }
        Drawable d = loadP2Drawable(baseName);
        if (d != null) {
            iv.setImageDrawable(d);
        } else {
            iv.setImageResource(fallbackResId);
        }
    }

    private Drawable loadP2Drawable(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) return null;

        // Caso especial: el pack actual trae este fichero con doble underscore.
        // Intentamos ambos para no romper el selector.
        String primary = baseName + SUFFIX_P2;
        Drawable fromCache = mCache.get(primary);
        if (fromCache != null) return fromCache;

        Drawable d = tryLoadFromAssets(primary);
        if (d == null && "btn_next_n".equals(baseName)) {
            d = tryLoadFromAssets("btn_next__n" + SUFFIX_P2);
        }
        if (d != null) {
            mCache.put(primary, d);
        }
        return d;
    }

    private Drawable tryLoadFromAssets(String assetFileName) {
        try (InputStream is = mAssets.open(assetFileName)) {
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp == null) return null;
            return new BitmapDrawable(mContext.getResources(), bmp);
        } catch (Exception ignored) {
            return null;
        }
    }
}

