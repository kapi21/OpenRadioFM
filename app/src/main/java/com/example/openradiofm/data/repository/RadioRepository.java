package com.example.openradiofm.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.example.openradiofm.BuildConfig;
import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.CloudContributionGuard;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.SupabaseLogoSource;
import com.example.openradiofm.data.source.WebRadioSource;
import com.example.openradiofm.data.source.network.model.SupabaseLogoResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Repositorio central de datos de radio unificado (OpenRadioFM 5.5 Universal).
 * - Soporta Modo 100% OFFLINE por defecto (cero red, logos en /sdcard/RadioLogos).
 * - Soporta Modo ONLINE configurable (Supabase, RadioBrowser, streaming, contribución comunitaria).
 * - Gobernado centralmente por 'pref_offline_mode'.
 */
public class RadioRepository {
    private static final String TAG = "RadioRepository";
    private static final long MIN_ACTIVITY_INDICATOR_MS = 350L;
    private static final long CLOUD_PS_STABLE_MS = 4000L;
    private static final long CLOUD_PS_ONLY_UPSERT_COOLDOWN_MS = 6L * 60L * 60L * 1000L; // 6h

    private final RootRDSSource rootSource;
    private final WebRadioSource webSource;
    private final SupabaseLogoSource supabaseSource;
    private final boolean useRoot;
    private final SharedPreferences mPrefs;
    private final SharedPreferences mGlobalPrefs;
    private final Context mContext;

    private CloudContributionGuard cloudGuard = null;

    private final ExecutorService logoExecutor = Executors.newFixedThreadPool(2);
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final ConcurrentHashMap<String, String> logoCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> nameLogoCache = new ConcurrentHashMap<>();
    private final Set<String> pendingRequests = Collections.synchronizedSet(new HashSet<>());

    private final Object cloudPsStabilityLock = new Object();
    private int cloudPsStableFreqKHz = -1;
    private String cloudPsStablePiNorm = "";
    private String cloudPsStablePsNorm = "";
    private long cloudPsStableSinceMs = 0L;
    private final ConcurrentHashMap<String, Long> cloudPsOnlyUpsertCache = new ConcurrentHashMap<>();

    public RadioRepository(Context context, boolean enableRoot) {
        this.useRoot = enableRoot;
        this.mContext = context;
        this.rootSource = enableRoot ? new RootRDSSource() : null;
        this.webSource = new WebRadioSource();
        this.supabaseSource = new SupabaseLogoSource();
        this.mPrefs = context.getSharedPreferences("RadioStationNames", Context.MODE_PRIVATE);
        this.mGlobalPrefs = context.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE);

        ensureRadioLogosFolderExists();
    }

    public boolean isOfflineMode() {
        if (mGlobalPrefs == null) return true;
        return mGlobalPrefs.getBoolean("pref_offline_mode", true);
    }

    public void setCloudContributionGuard(CloudContributionGuard guard) {
        this.cloudGuard = guard;
    }

    private boolean mayContributeCloud() {
        return cloudGuard == null || cloudGuard.allowCloudContributionNow();
    }

    public void setDataActivityListener(SupabaseLogoSource.DataActivityListener listener) {
        if (supabaseSource != null) {
            supabaseSource.setDataActivityListener(listener);
        }
    }

    public SupabaseLogoSource getSupabaseSource() {
        return supabaseSource;
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
        void onLogoFound(String logoPath);
    }

    public void setCustomName(int freqKHz, String name) {
        if (name == null || name.trim().isEmpty()) {
            mPrefs.edit().remove("CUSTOM_" + freqKHz).apply();
        } else {
            mPrefs.edit().putString("CUSTOM_" + freqKHz, name.trim()).apply();
        }
        clearCacheForFrequency(freqKHz);
    }

    public void clearMemoryCacheForFrequency(int freqKHz) {
        String prefix = freqKHz + "_";
        logoCache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public void clearCacheForFrequency(int freqKHz) {
        clearMemoryCacheForFrequency(freqKHz);

        String customName = mPrefs.getString("CUSTOM_" + freqKHz, null);
        String rdsPsName = mPrefs.getString("RDS_" + freqKHz, null);
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

        if (!finalName.trim().isEmpty()) {
            nameLogoCache.remove(finalName.trim().toUpperCase(Locale.ROOT));
        }
    }

    public void saveRdsName(int freqKHz, String name) {
        if (name == null || name.trim().isEmpty() || isGarbageZeroPs(name)) {
            return;
        }
        String currentRds = mPrefs.getString("RDS_" + freqKHz, null);
        if (!name.equals(currentRds)) {
            mPrefs.edit().putString("RDS_" + freqKHz, name).apply();
            clearMemoryCacheForFrequency(freqKHz);
            String pi = mPrefs.getString("PI_" + freqKHz, "");
            notifyPsSampleForCloudStability(freqKHz, pi, name);
        }
    }

    public void saveRdsPty(int freqKHz, String pty) {
        if (pty == null || pty.trim().isEmpty()) {
            return;
        }
        String currentPty = mPrefs.getString("PTY_" + freqKHz, null);
        if (!pty.equals(currentPty)) {
            mPrefs.edit().putString("PTY_" + freqKHz, pty).apply();
        }
    }

    public void saveRdsPi(int freqKHz, String pi) {
        if (pi == null || pi.trim().isEmpty()) {
            return;
        }
        String currentPi = mPrefs.getString("PI_" + freqKHz, null);
        if (!pi.equals(currentPi)) {
            mPrefs.edit().putString("PI_" + freqKHz, pi).apply();
            clearMemoryCacheForFrequency(freqKHz);
            String ps = mPrefs.getString("RDS_" + freqKHz, "");
            notifyPsSampleForCloudStability(freqKHz, pi, ps);
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
        String streamUrlStored = mPrefs.getString("STREAM_" + freqKHz, null);

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
        if (streamUrlStored != null) {
            station.setStreamUrl(streamUrlStored);
        }

        // Revisar si el usuario quitó explícitamente el logo para esta frecuencia
        if (mPrefs.getBoolean("NO_LOGO_" + freqKHz, false)) {
            String cacheKey = freqKHz + "_" + (piCode != null ? piCode : "") + "_" + (finalName != null ? finalName.trim().toUpperCase(Locale.ROOT) : "");
            logoCache.put(cacheKey, "NO_LOGO");
            return station;
        }

        // Revisar Caché en Memoria (Por Frecuencia + Metadata)
        String cacheKey = freqKHz + "_" + (piCode != null ? piCode : "") + "_" + (finalName != null ? finalName.trim().toUpperCase(Locale.ROOT) : "");
        if (logoCache.containsKey(cacheKey)) {
            String cachedPath = logoCache.get(cacheKey);
            if (!"NO_LOGO".equals(cachedPath)) {
                station.setLogoUrl(cachedPath);
                if (callback != null)
                    callback.onLogoFound(cachedPath);
            }

            if (!isOfflineMode() && !"NO_LOGO".equals(cachedPath)) {
                if (streamUrlStored == null) {
                    fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
                }
            } else if (!isOfflineMode() && "NO_LOGO".equals(cachedPath)) {
                maybeContributePsOnlyToCloud(freqKHz, piCode, finalName);
            }
            return station;
        }

        // Revisar Caché en Memoria (Por Nombre Sanitizado)
        String sanitizedNameKey = (finalName != null && !finalName.trim().isEmpty())
                ? finalName.trim().toUpperCase(Locale.ROOT) : null;
        if (sanitizedNameKey != null && nameLogoCache.containsKey(sanitizedNameKey)) {
            String cachedPath = nameLogoCache.get(sanitizedNameKey);
            station.setLogoUrl(cachedPath);
            logoCache.put(cacheKey, cachedPath);
            if (callback != null)
                callback.onLogoFound(cachedPath);

            if (!isOfflineMode()) {
                if (streamUrlStored == null) {
                    fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
                }
            }
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

            if (!isOfflineMode()) {
                if (streamUrlStored == null) {
                    fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
                }
                boolean contribCloud = mGlobalPrefs.getBoolean("pref_cloud_contrib", true);
                if (contribCloud && mayContributeCloud()
                        && isPsStableForCloudContribution(freqKHz, piCode, finalName)
                        && SupabaseLogoSource.isAcceptableForCloudUpsert(piCode != null ? piCode : "", finalName != null ? finalName : "")) {
                    final String fPi = piCode;
                    final String fName = finalName;
                    final String fPath = logoPath;
                    logoExecutor.submit(() -> supabaseSource.upsertLogoData(mContext, fPi, fName, freqKHz, "file://" + fPath, null));
                }
            }
        } else {
            if (isOfflineMode()) {
                Log.d(TAG, "NOT FOUND LOCAL LOGO (OFFLINE)");
                logoCache.put(cacheKey, "NO_LOGO");
            } else {
                fetchLogoFromNetworkAsync(cacheKey, sanitizedNameKey, freqKHz, finalName, piCode, station, callback);
            }
        }

        return station;
    }

    private void fetchLogoFromNetworkAsync(String cacheKey, String sanitizedNameKey, int freqKHz,
                                           String finalName, String piCode, RadioStation station, LogoCallback callback) {
        if (freqKHz < 30000) {
            return;
        }
        if (!isOfflineMode() && station.getStreamUrl() == null) {
            fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
        }
        boolean onlineLogosEnabled = mGlobalPrefs.getBoolean("pref_logos_online", true);
        if (!onlineLogosEnabled) {
            logoCache.put(cacheKey, "NO_LOGO");
            return;
        }

        if (!tryMarkPending(cacheKey)) {
            return;
        }
        if (logoExecutor == null || logoExecutor.isShutdown()) {
            removePending(cacheKey);
            return;
        }

        final String stationNameForLambda = finalName;
        logoExecutor.submit(() -> {
            try {
                String country = getCountryCode();
                int provider = mGlobalPrefs.getInt("pref_logo_provider", 0); // 0=Supabase, 1=Web, 2=Both
                String logoUrlToDownload = null;

                SupabaseLogoResponse supabaseData = null;
                if (provider == 0 || provider == 2) {
                    final long supabaseActivityStartMs = SystemClock.uptimeMillis();
                    supabaseSource.notifyActivity(true);
                    try {
                        String pi = piCode != null ? piCode : "";
                        String cName = mPrefs.getString("CUSTOM_" + freqKHz, null);
                        Call<List<SupabaseLogoResponse>> call = null;

                        if (cName != null && !cName.isEmpty() && !SupabaseLogoSource.isNameGeneric(cName)) {
                            call = supabaseSource.getSupabaseApi().getLogosByName(supabaseSource.getApiKey(), "Bearer " + supabaseSource.getApiKey(), "ilike." + cName.trim(), "eq." + country, "*");
                        } else if (piCode != null && !piCode.isEmpty()) {
                            call = supabaseSource.getSupabaseApi().getLogosByPi(supabaseSource.getApiKey(), "Bearer " + supabaseSource.getApiKey(), "ilike." + piCode, "eq." + country, "*");
                        } else if (stationNameForLambda != null && !SupabaseLogoSource.isNameGeneric(stationNameForLambda)) {
                            call = supabaseSource.getSupabaseApi().getLogosByName(supabaseSource.getApiKey(), "Bearer " + supabaseSource.getApiKey(), "ilike." + stationNameForLambda.trim(), "eq." + country, "*");
                        }

                        if (call != null) {
                            Response<List<SupabaseLogoResponse>> res = call.execute();
                            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                                supabaseData = pickBestSupabaseRow(res.body(), freqKHz, stationNameForLambda);
                            }
                        }
                        if (supabaseData != null) {
                            logoUrlToDownload = supabaseData.getLogoUrl();
                            String streamUrlToSave = supabaseData.getStreamUrl() != null ? supabaseData.getStreamUrl() : "";
                            mPrefs.edit().putString("STREAM_" + freqKHz, streamUrlToSave).apply();
                            if (!streamUrlToSave.isEmpty()) {
                                station.setStreamUrl(streamUrlToSave);
                            }
                        } else {
                            mPrefs.edit().putString("STREAM_" + freqKHz, "").apply();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error fetching Supabase data", e);
                    } finally {
                        finishSupabaseActivityWithMinDuration(supabaseActivityStartMs);
                    }
                }

                if (logoUrlToDownload == null && (provider == 1 || provider == 2)) {
                    logoUrlToDownload = webSource.fetchLogo(freqKHz, stationNameForLambda, country);
                }

                if (logoUrlToDownload != null) {
                    String savedPath = downloadAndSaveLogo(logoUrlToDownload, freqKHz, stationNameForLambda);
                    if (savedPath != null) {
                        station.setLogoUrl(savedPath);
                        logoCache.put(cacheKey, savedPath);
                        if (sanitizedNameKey != null) nameLogoCache.put(sanitizedNameKey, savedPath);
                        if (callback != null) callback.onLogoFound(savedPath);
                    } else {
                        station.setLogoUrl(logoUrlToDownload);
                        logoCache.put(cacheKey, logoUrlToDownload);
                        if (sanitizedNameKey != null) nameLogoCache.put(sanitizedNameKey, logoUrlToDownload);
                        if (callback != null) callback.onLogoFound(logoUrlToDownload);
                    }
                } else {
                    logoCache.put(cacheKey, "NO_LOGO");
                    maybeContributePsOnlyToCloud(freqKHz, piCode, stationNameForLambda);
                }
            } catch (Exception e) {
                Log.e(TAG, "Fatal network fetch error", e);
            } finally {
                removePending(cacheKey);
            }
        });
    }

    private String downloadAndSaveLogo(String urlString, int freqKHz, String rdsName) {
        try {
            ensureRadioLogosFolderExists();
            Bitmap bitmap = com.bumptech.glide.Glide.with(mContext)
                    .asBitmap()
                    .load(urlString)
                    .apply(new com.bumptech.glide.request.RequestOptions()
                            .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565))
                    .submit(300, 300)
                    .get();

            String fileName;
            if (rdsName != null && !rdsName.isEmpty()) {
                String sanitizedName = rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
                fileName = freqKHz + "_" + sanitizedName + ".png";
            } else {
                fileName = freqKHz + ".png";
            }

            File destFile = new File(getPreferredLogoDir(), fileName);
            FileOutputStream out = new FileOutputStream(destFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            MediaScannerConnection.scanFile(mContext, new String[]{destFile.getAbsolutePath()}, null, null);
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "downloadAndSaveLogo", e);
            return null;
        }
    }

    public String getLogoPath(int freqKHz, String rdsName) {
        if (mPrefs.getBoolean("NO_LOGO_" + freqKHz, false)) {
            return null;
        }

        String sanitizedName = (rdsName != null && !rdsName.isEmpty())
                ? rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT)
                : null;

        String[] extensions = new String[] { ".png", ".jpg", ".jpeg", ".PNG", ".JPG", ".JPEG" };
        File[] dirs = new File[] { getPreferredLogoDir(), getLegacyLogoDir() };

        for (File dir : dirs) {
            if (dir == null || !dir.exists()) continue;

            if (sanitizedName != null && !sanitizedName.isEmpty()) {
                for (String ext : extensions) {
                    File f = new File(dir, freqKHz + "_" + sanitizedName + ext);
                    if (f.exists()) return f.getAbsolutePath();
                }
            }

            for (String ext : extensions) {
                File f = new File(dir, freqKHz + ext);
                if (f.exists()) return f.getAbsolutePath();
            }

            for (String ext : extensions) {
                File f = new File(dir, (freqKHz / 10) + ext);
                if (f.exists()) return f.getAbsolutePath();
            }
        }

        return null;
    }

    public void deleteExistingLogoFilesForFrequency(int freqKHz) {
        // Preservación estricta de archivos: no se borran ficheros al desasignar logos.
    }

    public void removeCustomStationLogo(int freqKHz) {
        mPrefs.edit().putBoolean("NO_LOGO_" + freqKHz, true).apply();
        clearMemoryCacheForFrequency(freqKHz);
    }

    public String saveCustomStationLogo(int freqKHz, String rdsName, Bitmap sourceBitmap) {
        if (sourceBitmap == null) return null;
        try {
            ensureRadioLogosFolderExists();
            mPrefs.edit().remove("NO_LOGO_" + freqKHz).apply();

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
                    ? rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT)
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
            String piCode = mPrefs.getString("PI_" + freqKHz, "");
            String cacheKey = freqKHz + "_" + (piCode != null ? piCode : "") + "_" + (sanitizedName != null ? sanitizedName : "");
            logoCache.put(cacheKey, savedPath);
            if (sanitizedName != null) nameLogoCache.put(sanitizedName, savedPath);

            Log.i(TAG, "Logo guardado exitosamente (max 300x300): " + savedPath);
            return savedPath;
        } catch (Exception e) {
            Log.e(TAG, "Error guardando logo local", e);
            return null;
        }
    }

    public String resolveStreamUrlForFrequency(int freqKHz) {
        if (freqKHz < 30000) return null;
        String streamUrlStored = mPrefs.getString("STREAM_" + freqKHz, null);
        if (streamUrlStored != null && !streamUrlStored.trim().isEmpty()) {
            return streamUrlStored;
        }
        if (isOfflineMode()) return null;

        String customName = mPrefs.getString("CUSTOM_" + freqKHz, null);
        String rdsPsName = mPrefs.getString("RDS_" + freqKHz, null);
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
        return querySupabaseForStreamUrl(freqKHz, finalName, piCode);
    }

    private String querySupabaseForStreamUrl(int freqKHz, String finalName, String piCode) {
        int provider = mGlobalPrefs.getInt("pref_logo_provider", 0);
        if (!(provider == 0 || provider == 2)) {
            return null;
        }
        final long supabaseActivityStartMs = SystemClock.uptimeMillis();
        try {
            supabaseSource.notifyActivity(true);
            String cName = mPrefs.getString("CUSTOM_" + freqKHz, null);
            Call<List<SupabaseLogoResponse>> call = null;
            String country = getCountryCode();
            if (cName != null && !cName.isEmpty() && !SupabaseLogoSource.isNameGeneric(cName)) {
                call = supabaseSource.getSupabaseApi().getLogosByName(supabaseSource.getApiKey(),
                        "Bearer " + supabaseSource.getApiKey(), "ilike." + cName.trim(), "eq." + country, "*");
            } else if (piCode != null && !piCode.isEmpty()) {
                call = supabaseSource.getSupabaseApi().getLogosByPi(supabaseSource.getApiKey(),
                        "Bearer " + supabaseSource.getApiKey(), "ilike." + piCode, "eq." + country, "*");
            } else if (finalName != null && !SupabaseLogoSource.isNameGeneric(finalName)) {
                call = supabaseSource.getSupabaseApi().getLogosByName(supabaseSource.getApiKey(),
                        "Bearer " + supabaseSource.getApiKey(), "ilike." + finalName.trim(), "eq." + country, "*");
            }
            SupabaseLogoResponse supabaseData = null;
            if (call != null) {
                Response<List<SupabaseLogoResponse>> res = call.execute();
                if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                    supabaseData = pickBestSupabaseRow(res.body(), freqKHz, finalName);
                }
            }
            if (supabaseData != null) {
                String streamUrlToSave = supabaseData.getStreamUrl() != null ? supabaseData.getStreamUrl() : "";
                mPrefs.edit().putString("STREAM_" + freqKHz, streamUrlToSave).apply();
                return streamUrlToSave.isEmpty() ? null : streamUrlToSave;
            }
            mPrefs.edit().putString("STREAM_" + freqKHz, "").apply();
            return null;
        } catch (Exception e) {
            Log.e(TAG, "querySupabaseForStreamUrl", e);
            return null;
        } finally {
            finishSupabaseActivityWithMinDuration(supabaseActivityStartMs);
        }
    }

    private void finishSupabaseActivityWithMinDuration(long startUptimeMs) {
        long elapsed = SystemClock.uptimeMillis() - startUptimeMs;
        long delay = Math.max(0L, MIN_ACTIVITY_INDICATOR_MS - elapsed);
        if (delay == 0L) {
            supabaseSource.notifyActivity(false);
            return;
        }
        mMainHandler.postDelayed(() -> {
            try {
                supabaseSource.notifyActivity(false);
            } catch (Exception ignored) {}
        }, delay);
    }

    private void fetchStreamUrlAsync(String cacheKey, int freqKHz, String finalName, String piCode, RadioStation station) {
        if (freqKHz < 30000) return;
        String streamCacheKey = cacheKey + "_STREAM";
        if (!tryMarkPending(streamCacheKey)) return;

        if (logoExecutor == null || logoExecutor.isShutdown()) {
            removePending(streamCacheKey);
            return;
        }

        final String stationNameForLambda = finalName;
        logoExecutor.submit(() -> {
            try {
                String url = querySupabaseForStreamUrl(freqKHz, stationNameForLambda, piCode);
                if (url != null && !url.isEmpty()) {
                    station.setStreamUrl(url);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching stream URL", e);
            } finally {
                removePending(streamCacheKey);
            }
        });
    }

    private SupabaseLogoResponse pickBestSupabaseRow(List<SupabaseLogoResponse> rows, int freqKHz, String psHint) {
        if (rows == null || rows.isEmpty()) return null;
        if (rows.size() == 1) return rows.get(0);
        String hint = psHint != null ? psHint.trim() : "";
        String hintNorm = hint.isEmpty() ? "" : hint.toUpperCase(Locale.ROOT);
        SupabaseLogoResponse best = null;
        long bestScore = Long.MAX_VALUE;
        for (SupabaseLogoResponse row : rows) {
            int fk = parseSupabaseFrequencyToKhz(row.getFrequency());
            long dist = fk < 0 ? 50_000_000L : (long) Math.abs(fk - freqKHz);
            if (!hintNorm.isEmpty()) {
                String ps = row.getPsName() != null ? row.getPsName().trim() : "";
                if (ps.toUpperCase(Locale.ROOT).equals(hintNorm)) {
                    dist -= 10_000_000L;
                }
            }
            if (best == null || dist < bestScore) {
                bestScore = dist;
                best = row;
            }
        }
        return best != null ? best : rows.get(0);
    }

    private static int parseSupabaseFrequencyToKhz(String freqField) {
        if (freqField == null) return -1;
        String t = freqField.trim().replace(',', '.');
        if (t.isEmpty()) return -1;
        try {
            double v = Double.parseDouble(t);
            if (v >= 200.0) return (int) Math.round(v);
            return (int) Math.round(v * 1000.0);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void notifyPsSampleForCloudStability(int freqKHz, String piCode, String rdsName) {
        synchronized (cloudPsStabilityLock) {
            long now = SystemClock.elapsedRealtime();
            String pi = piCode != null ? piCode.trim() : "";
            String ps = rdsName != null ? rdsName.trim() : "";
            if (freqKHz != cloudPsStableFreqKHz
                    || !pi.equals(cloudPsStablePiNorm)
                    || !ps.equals(cloudPsStablePsNorm)) {
                cloudPsStableFreqKHz = freqKHz;
                cloudPsStablePiNorm = pi;
                cloudPsStablePsNorm = ps;
                cloudPsStableSinceMs = now;
            }
        }
    }

    private boolean isPsStableForCloudContribution(int freqKHz, String piCode, String finalNameForUpsert) {
        String fn = finalNameForUpsert != null ? finalNameForUpsert.trim() : "";
        try {
            String custom = mPrefs.getString("CUSTOM_" + freqKHz, null);
            if (custom != null && !custom.trim().isEmpty() && fn.equals(custom.trim())) {
                return true;
            }
        } catch (Exception ignored) {}
        String pi = piCode != null ? piCode.trim() : "";
        synchronized (cloudPsStabilityLock) {
            long now = SystemClock.elapsedRealtime();
            if (freqKHz != cloudPsStableFreqKHz
                    || !pi.equals(cloudPsStablePiNorm)
                    || !fn.equals(cloudPsStablePsNorm)) {
                return false;
            }
            return now - cloudPsStableSinceMs >= CLOUD_PS_STABLE_MS;
        }
    }

    private void maybeContributePsOnlyToCloud(int freqKHz, String piCode, String finalNameForUpsert) {
        if (freqKHz < 30000) return;
        boolean contribCloud = mGlobalPrefs.getBoolean("pref_cloud_contrib", true);
        if (!contribCloud) return;
        if (!mayContributeCloud()) return;

        if (!isPsStableForCloudContribution(freqKHz, piCode, finalNameForUpsert)) return;
        if (!SupabaseLogoSource.isAcceptableForCloudUpsert(piCode != null ? piCode : "",
                finalNameForUpsert != null ? finalNameForUpsert : "")) {
            return;
        }

        final String piNorm = (piCode != null) ? piCode.trim() : "";
        final String psNorm = (finalNameForUpsert != null) ? finalNameForUpsert.trim() : "";
        final String key = freqKHz + "|" + piNorm + "|" + psNorm.toUpperCase(Locale.ROOT);
        final long now = SystemClock.elapsedRealtime();
        Long last = cloudPsOnlyUpsertCache.get(key);
        if (last != null && (now - last) < CLOUD_PS_ONLY_UPSERT_COOLDOWN_MS) return;
        cloudPsOnlyUpsertCache.put(key, now);

        if (logoExecutor == null || logoExecutor.isShutdown()) return;

        logoExecutor.submit(() -> {
            try {
                supabaseSource.upsertLogoData(mContext, piCode, finalNameForUpsert, freqKHz, null, null);
            } catch (Exception ignored) {}
        });
    }

    private String getCountryCode() {
        try {
            return com.example.openradiofm.utils.CountryPrefs.getCountry(mContext);
        } catch (Exception e) {
            return "ES";
        }
    }

    private boolean tryMarkPending(String key) {
        synchronized (pendingRequests) {
            if (pendingRequests.contains(key)) return false;
            pendingRequests.add(key);
            return true;
        }
    }

    private void removePending(String key) {
        synchronized (pendingRequests) {
            pendingRequests.remove(key);
        }
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
