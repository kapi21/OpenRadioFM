package com.example.openradiofm.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.CloudContributionGuard;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.SupabaseLogoSource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio central de datos de radio en Modo 100% OFFLINE (OpenRadioFM 5.5).
 * - Cero dependencias de red / Supabase / RadioBrowser.
 * - Logos locales en almacenamiento (/sdcard/RadioLogos o almacenamiento de la app).
 * - Formatos admitidos: PNG, JPG, JPEG (normalizados a máx 300x300 px).
 * - Nombres personalizados y decodificación RDS en memoria local.
 */
public class RadioRepository {
    private static final String TAG = "RadioRepository";
    private final RootRDSSource rootSource;
    private final boolean useRoot;
    private final SharedPreferences mPrefs;
    private final Context mContext;

    private final ExecutorService logoExecutor = Executors.newFixedThreadPool(2);
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final ConcurrentHashMap<String, String> logoCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> nameLogoCache = new ConcurrentHashMap<>();
    private final Set<String> pendingRequests = Collections.synchronizedSet(new HashSet<>());

    public RadioRepository(Context context, boolean enableRoot) {
        this.useRoot = enableRoot;
        this.mContext = context;
        this.rootSource = enableRoot ? new RootRDSSource() : null;
        this.mPrefs = context.getSharedPreferences("RadioStationNames", Context.MODE_PRIVATE);

        ensureRadioLogosFolderExists();
    }

    public void setCloudContributionGuard(CloudContributionGuard guard) {
        // No-op en modo Offline
    }

    public void setDataActivityListener(SupabaseLogoSource.DataActivityListener listener) {
        // No-op en modo Offline
    }

    public SupabaseLogoSource getSupabaseSource() {
        return null;
    }

    private File getAppLogoDir() {
        File external = mContext.getExternalFilesDir(null);
        File base = (external != null) ? external : mContext.getFilesDir();
        return new File(base, "RadioLogos");
    }

    private File getLegacyLogoDir() {
        return new File("/sdcard/RadioLogos/");
    }

    public File getPreferredLogoDir() {
        File legacy = getLegacyLogoDir();
        try {
            if ((legacy.exists() || legacy.mkdirs()) && legacy.canWrite()) return legacy;
        } catch (Exception ignored) {}
        File app = getAppLogoDir();
        try { app.mkdirs(); } catch (Exception ignored) {}
        return app;
    }

    private void ensureRadioLogosFolderExists() {
        try {
            File dir = getPreferredLogoDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creando carpeta RadioLogos", e);
        }
    }

    public interface LogoCallback {
        void onLogoFound(String logoUrl);
    }

    public void setCustomName(int freqKHz, String name) {
        if (name == null || name.trim().isEmpty()) {
            mPrefs.edit().remove("CUSTOM_" + freqKHz).apply();
        } else {
            mPrefs.edit().putString("CUSTOM_" + freqKHz, name.trim()).apply();
        }

        clearMemoryCacheForFrequency(freqKHz);
    }

    public void clearMemoryCacheForFrequency(int freqKHz) {
        String prefix = freqKHz + "_";
        ArrayList<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : logoCache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) keysToRemove.add(entry.getKey());
        }
        for (String key : keysToRemove) {
            logoCache.remove(key);
        }
        synchronized (pendingRequests) {
            Iterator<String> pendIt = pendingRequests.iterator();
            while (pendIt.hasNext()) {
                if (pendIt.next().startsWith(prefix)) {
                    pendIt.remove();
                }
            }
        }
    }

    public void clearCacheForFrequency(int freqKHz) {
        clearMemoryCacheForFrequency(freqKHz);

        mPrefs.edit()
            .remove("CUSTOM_" + freqKHz)
            .remove("RDS_" + freqKHz)
            .remove("PTY_" + freqKHz)
            .remove("PI_" + freqKHz)
            .remove("STREAM_" + freqKHz)
            .apply();

        try {
            String prefix = freqKHz + "_";
            File[] dirs = new File[] { getPreferredLogoDir(), getLegacyLogoDir() };
            for (File dir : dirs) {
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((d, name) -> name.startsWith(prefix) || name.equals(freqKHz + ".png") || name.equals(freqKHz + ".jpg"));
                    if (files != null) {
                        for (File file : files) {
                            if (file.delete()) {
                                Log.d(TAG, "Logo local borrado: " + file.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error borrando logo de disco", e);
        }
    }

    public void saveRdsName(int freqKHz, String name) {
        if (name == null || name.trim().isEmpty() || name.length() < 2) return;
        if (isGarbageZeroPs(name)) {
            Log.d(TAG, "saveRdsName: ignorando PS solo ceros");
            return;
        }
        String trimmed = name.trim();
        String existing = mPrefs.getString("RDS_" + freqKHz, "");
        if (!trimmed.equals(existing)) {
            mPrefs.edit().putString("RDS_" + freqKHz, trimmed).apply();
        }
    }

    public void saveRdsPty(int freqKHz, String pty) {
        if (pty != null && !pty.trim().isEmpty()) {
            String existing = mPrefs.getString("PTY_" + freqKHz, "");
            if (!pty.equals(existing)) {
                mPrefs.edit().putString("PTY_" + freqKHz, pty.trim()).apply();
            }
        }
    }

    public void saveRdsPi(int freqKHz, String pi) {
        if (pi == null || pi.trim().isEmpty()) return;
        String trimmed = pi.trim();
        String existing = mPrefs.getString("PI_" + freqKHz, "");
        if (!trimmed.equals(existing)) {
            mPrefs.edit().putString("PI_" + freqKHz, trimmed).apply();
        }
    }

    public RadioStation getStationInfo(int freqKHz, LogoCallback callback) {
        return getStationInfo(freqKHz, callback, null);
    }

    public RadioStation getStationInfo(int freqKHz, LogoCallback callback, String liveRdsPsOverride) {
        final boolean onUi = Looper.getMainLooper().getThread() == Thread.currentThread();
        if (callback != null && onUi) {
            if (logoExecutor != null && !logoExecutor.isShutdown()) {
                logoExecutor.execute(() -> getStationInfoImpl(freqKHz, callback, liveRdsPsOverride));
            }
            return new RadioStation(freqKHz, "");
        }
        return getStationInfoImpl(freqKHz, callback, liveRdsPsOverride);
    }

    private RadioStation getStationInfoImpl(int freqKHz, LogoCallback callback, String liveRdsPsOverride) {
        String customName = mPrefs.getString("CUSTOM_" + freqKHz, null);
        String rdsPsName = mPrefs.getString("RDS_" + freqKHz, null);
        if (rdsPsName != null && isGarbageZeroPs(rdsPsName)) {
            rdsPsName = null;
        }
        if (liveRdsPsOverride != null && !liveRdsPsOverride.trim().isEmpty()) {
            String live = liveRdsPsOverride.trim();
            if (!isGarbageZeroPs(live)) {
                rdsPsName = live;
            }
        }
        String ptyStored = mPrefs.getString("PTY_" + freqKHz, null);
        String piCode = mPrefs.getString("PI_" + freqKHz, null);

        String rootName = null;
        if (useRoot && rootSource != null) {
            rootName = rootSource.getRdsName(freqKHz);
        }

        String finalName = "";
        if (customName != null && !customName.isEmpty()) {
            finalName = customName;
        } else if (rdsPsName != null && !rdsPsName.isEmpty()) {
            finalName = rdsPsName;
        } else if (rootName != null && !rootName.isEmpty()) {
            finalName = rootName;
        }

        RadioStation station = new RadioStation(freqKHz, finalName);
        if (ptyStored != null) {
            station.setPty(ptyStored);
        }

        // Revisar Caché en Memoria (Por Frecuencia + Metadata)
        String cacheKey = freqKHz + "_" + (piCode != null ? piCode : "") + "_" + (finalName != null ? finalName.trim().toUpperCase() : "");
        if (logoCache.containsKey(cacheKey)) {
            String cachedPath = logoCache.get(cacheKey);
            if (!"NO_LOGO".equals(cachedPath)) {
                station.setLogoUrl(cachedPath);
                if (callback != null)
                    callback.onLogoFound(cachedPath);
            }
            return station;
        }

        // Revisar Caché en Memoria (Por Nombre Sanitizado)
        String sanitizedNameKey = (finalName != null && !finalName.trim().isEmpty())
                ? finalName.trim().toUpperCase() : null;
        if (sanitizedNameKey != null && nameLogoCache.containsKey(sanitizedNameKey)) {
            String cachedPath = nameLogoCache.get(sanitizedNameKey);
            station.setLogoUrl(cachedPath);
            logoCache.put(cacheKey, cachedPath);
            if (callback != null)
                callback.onLogoFound(cachedPath);
            return station;
        }

        // Búsqueda de logo local (PNG, JPG, JPEG)
        String logoPath = getLogoPath(freqKHz, finalName);
        if (logoPath != null) {
            Log.d(TAG, "FOUND LOCAL LOGO: " + logoPath);
            station.setLogoUrl(logoPath);
            logoCache.put(cacheKey, logoPath);
            if (sanitizedNameKey != null) nameLogoCache.put(sanitizedNameKey, logoPath);
            if (callback != null)
                callback.onLogoFound(logoPath);
        } else {
            Log.d(TAG, "NOT FOUND LOCAL LOGO (OFFLINE)");
            logoCache.put(cacheKey, "NO_LOGO");
        }

        return station;
    }

    /**
     * Busca el logo en almacenamiento local (/sdcard/RadioLogos y app-dir).
     * Extensiones admitidas: .png, .jpg, .jpeg
     * Prioridades:
     * 1. {freq}_{sanitizedName}.{ext}
     * 2. {freq}.{ext}
     * 3. {freq/10}.{ext}
     */
    public String getLogoPath(int freqKHz, String rdsName) {
        String sanitizedName = (rdsName != null && !rdsName.isEmpty())
                ? rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase()
                : null;

        String[] extensions = new String[] { ".png", ".jpg", ".jpeg", ".PNG", ".JPG", ".JPEG" };
        File[] dirs = new File[] { getPreferredLogoDir(), getLegacyLogoDir() };

        for (File dir : dirs) {
            if (dir == null || !dir.exists()) continue;

            // 1. Prioridad: Frecuencia + RDS
            if (sanitizedName != null && !sanitizedName.isEmpty()) {
                for (String ext : extensions) {
                    File f = new File(dir, freqKHz + "_" + sanitizedName + ext);
                    if (f.exists()) return f.getAbsolutePath();
                }
            }

            // 2. Frecuencia completa
            for (String ext : extensions) {
                File f = new File(dir, freqKHz + ext);
                if (f.exists()) return f.getAbsolutePath();
            }

            // 3. Frecuencia corta
            for (String ext : extensions) {
                File f = new File(dir, (freqKHz / 10) + ext);
                if (f.exists()) return f.getAbsolutePath();
            }
        }

        return null;
    }

    /**
     * Guarda un logo seleccionado por el usuario:
     * - Lo redimensiona a un máximo de 300x300 px manteniendo relación de aspecto.
     * - Lo guarda en formato PNG en el directorio preferido.
     * - Notifica a MediaScanner y actualiza la caché local.
     */
    public String saveCustomStationLogo(int freqKHz, String rdsName, Bitmap sourceBitmap) {
        if (sourceBitmap == null) return null;
        try {
            ensureRadioLogosFolderExists();

            int srcW = sourceBitmap.getWidth();
            int srcH = sourceBitmap.getHeight();
            int maxDim = 300;
            Bitmap scaledBitmap;
            if (srcW > maxDim || srcH > maxDim) {
                float ratio = Math.min((float) maxDim / srcW, (float) maxDim / srcH);
                int targetW = Math.max(1, Math.round(srcW * ratio));
                int targetH = Math.max(1, Math.round(srcH * ratio));
                scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, targetW, targetH, true);
            } else {
                scaledBitmap = sourceBitmap;
            }

            String sanitizedName = (rdsName != null && !rdsName.trim().isEmpty())
                    ? rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase()
                    : null;
            String fileName = (sanitizedName != null && !sanitizedName.isEmpty())
                    ? freqKHz + "_" + sanitizedName + ".png"
                    : freqKHz + ".png";

            File destFile = new File(getPreferredLogoDir(), fileName);
            FileOutputStream out = new FileOutputStream(destFile);
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            MediaScannerConnection.scanFile(
                    mContext,
                    new String[] { destFile.getAbsolutePath() },
                    null,
                    null);

            clearMemoryCacheForFrequency(freqKHz);
            String savedPath = destFile.getAbsolutePath();
            logoCache.put(freqKHz + "_" + (sanitizedName != null ? sanitizedName : ""), savedPath);
            if (sanitizedName != null) nameLogoCache.put(sanitizedName, savedPath);

            Log.i(TAG, "Logo guardado exitosamente (max 300x300): " + savedPath);
            return savedPath;
        } catch (Exception e) {
            Log.e(TAG, "Error guardando logo local", e);
            return null;
        }
    }

    public String resolveStreamUrlForFrequency(int freqKHz) {
        return null; // Modo Offline: sin streams de internet
    }

    public void shutdown() {
        if (rootSource != null) {
            rootSource.shutdown();
        }
        if (logoExecutor != null) {
            logoExecutor.shutdownNow();
        }
    }

    private static boolean isGarbageZeroPs(String ps) {
        if (ps == null) return true;
        String t = ps.trim();
        return t.isEmpty() || t.replace("0", "").replace(" ", "").isEmpty();
    }
}
