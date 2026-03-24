package com.example.openradiofm.data.repository;

import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.PredefinedStationSource;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.SupabaseLogoSource;
import com.example.openradiofm.data.source.WebRadioSource;
import com.example.openradiofm.data.source.network.model.SupabaseLogoResponse;

public class RadioRepository {
    private static final long MIN_ACTIVITY_INDICATOR_MS = 700L;
    private final RootRDSSource rootSource;
    private final WebRadioSource webSource;
    private final SupabaseLogoSource supabaseSource; // V16.0: Servidor centralizado
    private final PredefinedStationSource predefinedSource;
    private final boolean useRoot;

    private final android.content.SharedPreferences mPrefs;
    private final android.content.Context mContext; // V3.0: Needed for MediaScanner

    // ExecutorService para gestionar hilos de descarga de logos de forma eficiente.
    // Limita a 3 hilos concurrentes para evitar crear cientos de hilos.
    private final java.util.concurrent.ExecutorService logoExecutor = java.util.concurrent.Executors
            .newFixedThreadPool(3);
    private final android.os.Handler mMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    // Caché en memoria para evitar recargas de logos al cambiar frecuencia o nombre.
    // V13.6: Key: freqKHz + "_" + stationName, Value: URL o path del logo
    private final java.util.concurrent.ConcurrentHashMap<String, String> logoCache = new java.util.concurrent.ConcurrentHashMap<>();

    // V16.2: Caché por nombre de emisora (Independiente de la frecuencia)
    // Evita búsquedas en red para diferentes frecuencias de la misma cadena.
    private final java.util.concurrent.ConcurrentHashMap<String, String> nameLogoCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    // V16.4: Evita inundar el executor con peticiones idénticas si ya hay una en curso.
    private final java.util.Set<String> pendingRequests = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    // Repositorio central que combina:
    // - RootRDSSource: nombres RDS desde el fichero interno del servicio de radio
    // (requiere root).
    // - WebRadioSource: búsqueda de logos en internet (RadioBrowser) y caché local
    // en /sdcard/RadioLogos.
    // - SupabaseLogoSource: Servidor centralizado con PI Code y logos HD.
    // - SharedPreferences: nombres personalizados definidos por el usuario.
    //
    // El flag enableRoot permite desactivar por completo el acceso root cuando
    // estamos en MODO_FM_BASICO (dispositivos sin root o sin servicio especial).
    public RadioRepository(android.content.Context context, boolean enableRoot) {
        this.useRoot = enableRoot;
        this.mContext = context; // V3.0: Store for MediaScanner
        this.rootSource = enableRoot ? new RootRDSSource() : null;
        this.webSource = new WebRadioSource();
        this.supabaseSource = new SupabaseLogoSource();
        this.predefinedSource = new PredefinedStationSource(context);
        // Usamos un archivo de preferencias específico para los nombres de emisoras
        this.mPrefs = context.getSharedPreferences("RadioStationNames", android.content.Context.MODE_PRIVATE);

        // V3.0: Asegurar que existe la carpeta RadioLogos
        ensureRadioLogosFolderExists();
    }

    // V21.1: Compatibilidad storage (preferir app-specific, fallback legacy /sdcard)
    private java.io.File getAppLogoDir() {
        java.io.File external = mContext.getExternalFilesDir(null);
        java.io.File base = (external != null) ? external : mContext.getFilesDir();
        return new java.io.File(base, "RadioLogos");
    }

    private java.io.File getLegacyLogoDir() {
        return new java.io.File("/sdcard/RadioLogos/");
    }

    private java.io.File getPreferredLogoDir() {
        java.io.File legacy = getLegacyLogoDir();
        try {
            if ((legacy.exists() || legacy.mkdirs()) && legacy.canWrite()) return legacy;
        } catch (Exception ignored) {}
        java.io.File app = getAppLogoDir();
        try { app.mkdirs(); } catch (Exception ignored) {}
        return app;
    }

    /**
     * V3.0: Asegura que la carpeta /sdcard/RadioLogos/ existe.
     */
    private void ensureRadioLogosFolderExists() {
        try {
            java.io.File dir = getPreferredLogoDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            android.util.Log.e("RadioRepository", "Error creando carpeta RadioLogos", e);
        }
    }

    public interface LogoCallback {
        void onLogoFound(String logoUrl);
    }

    public void setDataActivityListener(SupabaseLogoSource.DataActivityListener listener) {
        if (supabaseSource != null) {
            supabaseSource.setDataActivityListener(listener);
        }
    }

    public SupabaseLogoSource getSupabaseSource() {
        return supabaseSource;
    }

    /**
     * Guarda un nombre personalizado para una frecuencia específica.
     */
    public void setCustomName(int freqKHz, String name) {
        if (name == null || name.trim().isEmpty()) {
            mPrefs.edit().remove("CUSTOM_" + freqKHz).apply();
        } else {
            mPrefs.edit().putString("CUSTOM_" + freqKHz, name.trim()).apply();
        }
        
        // V16.5: Limpiar caché en memoria para forzar una nueva consulta a Supabase
        String prefix = freqKHz + "_";
        java.util.ArrayList<String> keysToRemove = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : logoCache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) keysToRemove.add(entry.getKey());
        }
        for (String key : keysToRemove) {
            logoCache.remove(key);
        }
        synchronized (pendingRequests) {
            java.util.Iterator<String> pendIt = pendingRequests.iterator();
            while (pendIt.hasNext()) {
                if (pendIt.next().startsWith(prefix)) {
                    pendIt.remove();
                }
            }
        }
        // Eliminar también posibles URL de streaming cacheadas (incluso las vacías) para forzar reintento
        mPrefs.edit().remove("STREAM_" + freqKHz).apply();
    }

    /**
     * V17.1: Limpia la caché local (memoria y disco/SharedPreferences) de una frecuencia
     * y fuerza la recarga desde Supabase (como si fuese la primera vez).
     */
    public void clearCacheForFrequency(int freqKHz) {
        String prefix = freqKHz + "_";
        
        // 1. Limpiar Caché en Memoria
        java.util.ArrayList<String> keysToRemove = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, String> entry : logoCache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) keysToRemove.add(entry.getKey());
        }
        for (String key : keysToRemove) {
            logoCache.remove(key);
        }
        synchronized (pendingRequests) {
            java.util.Iterator<String> pendIt = pendingRequests.iterator();
            while (pendIt.hasNext()) {
                if (pendIt.next().startsWith(prefix)) {
                    pendIt.remove();
                }
            }
        }

        // 2. Limpiar SharedPreferences
        mPrefs.edit()
            .remove("CUSTOM_" + freqKHz)
            .remove("RDS_" + freqKHz)
            .remove("PTY_" + freqKHz)
            .remove("PI_" + freqKHz)
            .remove("STREAM_" + freqKHz)
            .apply();

        // 3. Borrar logo local de la carpeta para forzar descarga
        try {
            java.io.File[] dirs = new java.io.File[] { getPreferredLogoDir(), getLegacyLogoDir() };
            for (java.io.File dir : dirs) {
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    java.io.File[] files = dir.listFiles((d, name) -> name.startsWith(prefix));
                    if (files != null) {
                        for (java.io.File file : files) {
                            if (file.delete()) {
                                android.util.Log.d("RadioRepository", "Logo cache borrado: " + file.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("RadioRepository", "Error borrando logo de disco", e);
        }
    }

    /**
     * V9: Guarda el nombre RDS PS recibido dinámicamente.
     */
    public void saveRdsName(int freqKHz, String name) {
        if (name == null || name.trim().isEmpty() || name.length() < 2) return;
        if (SupabaseLogoSource.isGarbageZeroPs(name)) {
            android.util.Log.d("RadioRepository", "saveRdsName: ignorando PS solo-ceros (buffer vacío)");
            return;
        }
        String existing = mPrefs.getString("RDS_" + freqKHz, "");
        if (!name.equals(existing)) {
            mPrefs.edit().putString("RDS_" + freqKHz, name.trim()).apply();
        }
    }

    /**
     * V9.9: Guarda el PTY (índice o nombre) recibido para una frecuencia.
     */
    public void saveRdsPty(int freqKHz, String pty) {
        if (pty != null && !pty.trim().isEmpty()) {
            String existing = mPrefs.getString("PTY_" + freqKHz, "");
            if (!pty.equals(existing)) {
                mPrefs.edit().putString("PTY_" + freqKHz, pty.trim()).apply();
            }
        }
    }

    /**
     * V16.0: Guarda el PI Code recibido para una frecuencia.
     */
    public void saveRdsPi(int freqKHz, String pi) {
        if (pi != null && !pi.trim().isEmpty()) {
            String existing = mPrefs.getString("PI_" + freqKHz, "");
            if (!pi.equals(existing)) {
                mPrefs.edit().putString("PI_" + freqKHz, pi.trim()).apply();
            }
        }
    }

    /**
     * Devuelve la información de la emisora para una frecuencia dada.
     * Prioridad de nombre:
     * 1. Nombre Personalizado (Usuario)
     * 2. Nombre RDS (Root)
     * 3. Vacío (UI mostrará frecuencia)
     *
     * IMPORTANTE - RECOMENDACIÓN DE THREAD:
     * - Este método accede a disco (SharedPreferences, archivos) y puede ser lento.
     * - Se RECOMIENDA llamarlo desde un hilo de fondo para no bloquear la UI.
     * - Si se detecta que se llama desde el UI thread, se loguea un warning pero NO
     * se lanza excepción.
     * - El callback de logo se ejecuta SIEMPRE en un hilo de fondo; la Activity
     * debe hacer runOnUiThread() al actualizar vistas.
     */
    public RadioStation getStationInfo(int freqKHz, LogoCallback callback) {
        return getStationInfo(freqKHz, callback, null);
    }

    /**
     * @param liveRdsPsOverride PS actual de la sesión de sintonía (motor RDS), si coincide con {@code freqKHz}.
     *                         Tiene prioridad sobre {@code RDS_*} persistido (evita logo/nombre de otra emisora
     *                         que compartía la misma frecuencia).
     */
    public RadioStation getStationInfo(int freqKHz, LogoCallback callback, String liveRdsPsOverride) {
        // Warning si se llama desde UI thread (pero no matamos la app)
        if (android.os.Looper.getMainLooper().getThread() == Thread.currentThread()) {
            android.util.Log.w("RadioRepository",
                    "WARNING: getStationInfo() llamado desde UI thread. " +
                            "Esto puede causar lag en la interfaz. " +
                            "Considera ejecutarlo en un hilo de fondo.");
        }
        // V9: Prioridad de nombre
        // 1. Custom (Usuario)
        // 2. RDS PS (Capturado en vivo)
        // 3. RDS Root (Sistema)
        
        String customName = mPrefs.getString("CUSTOM_" + freqKHz, null);
        String rdsPsName = mPrefs.getString("RDS_" + freqKHz, null);
        if (rdsPsName != null && SupabaseLogoSource.isGarbageZeroPs(rdsPsName)) {
            rdsPsName = null;
        }
        // V21.2: PS confirmado en esta sesión (memoria) antes de que .apply() persista en prefs
        if (liveRdsPsOverride != null && !liveRdsPsOverride.trim().isEmpty()) {
            String live = liveRdsPsOverride.trim();
            if (!SupabaseLogoSource.isGarbageZeroPs(live)) {
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

        // 2.5. Si no hay nombre aún, intentar desde el catálogo predefinido
        // (España/Gala API)
        // V6.1: Desactivado por petición de usuario (confuso con RDS)
        /*
        RadioStation predefined = predefinedSource.findStation(freqKHz);
        if ((finalName == null || finalName.isEmpty()) && predefined != null) {
            finalName = predefined.getName();
            station.setName(finalName);
            if (station.getPty() == null)
                station.setPty(predefined.getPty());
        }
        */
        RadioStation predefined = null; // Force null

        // 0. Revisar Caché en Memoria (Por Frecuencia + Metadata)
        // V16.3: Incluimos PI Code en la clave para reinvavlidar si cambia la metadata.
        String cacheKey = freqKHz + "_" + (piCode != null ? piCode : "") + "_" + (finalName != null ? finalName.trim().toUpperCase() : "");
        if (logoCache.containsKey(cacheKey)) {
            String cachedPath = logoCache.get(cacheKey);
            if (!"NO_LOGO".equals(cachedPath)) {
                station.setLogoUrl(cachedPath);
                if (callback != null)
                    callback.onLogoFound(cachedPath);
            }
            
            // V16.3: Si tenemos el logo en caché de memoria pero nos falta el streaming, pedirlo en background
            boolean onlineLogosEnabled = mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_logos_online", true);
            if (onlineLogosEnabled && streamUrlStored == null && !"NO_LOGO".equals(cachedPath)) {
                fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
            }
            
            return station;
        }

        // 0.1 Revisar Caché en Memoria (Por Nombre Sanitizado)
        String sanitizedNameKey = (finalName != null && !finalName.trim().isEmpty()) 
                ? finalName.trim().toUpperCase() : null;
        if (sanitizedNameKey != null && nameLogoCache.containsKey(sanitizedNameKey)) {
            String cachedPath = nameLogoCache.get(sanitizedNameKey);
            station.setLogoUrl(cachedPath);
            logoCache.put(cacheKey, cachedPath); // Sincronizar caché de freq
            if (callback != null)
                callback.onLogoFound(cachedPath);
                
            boolean onlineLogosEnabled = mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_logos_online", true);
            if (onlineLogosEnabled && streamUrlStored == null) {
                fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
            }
            return station;
        }

        // V3.0: Búsqueda de logos con prioridad frecuencia+RDS
        // 1. Logo con frecuencia + nombre RDS: /sdcard/RadioLogos/96900_LOS40.png
        String logoPath = getLogoPath(freqKHz, finalName);

        if (logoPath != null) {
            android.util.Log.d("RadioLogos", "FOUND: " + logoPath);
            station.setLogoUrl(logoPath);
            logoCache.put(cacheKey, logoPath);
            if (sanitizedNameKey != null) nameLogoCache.put(sanitizedNameKey, logoPath);
            
            if (callback != null)
                callback.onLogoFound(logoPath);

            // V16.2: Contribuir logo local a la nube si tenemos PI o RDS
            boolean onlineLogosEnabled = mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_logos_online", true);

            if (onlineLogosEnabled && streamUrlStored == null) {
                // Si tenemos el logo local pero nos falta la URL de streaming, la pedimos a Supabase
                fetchStreamUrlAsync(cacheKey, freqKHz, finalName, piCode, station);
            }
            
            // V17.5: Contribuir logo local/RDS a la nube si el ajuste de contribución está activado
            boolean contribCloud = mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_cloud_contrib", true);

            if (contribCloud && (piCode != null || (finalName != null && finalName.length() >= 4))) {
                final String fPi = piCode;
                final String fName = finalName;
                final String fPath = logoPath;
                // Intentamos subir si es un logo de archivo (no nulo) o al menos tenemos la info RDS
                logoExecutor.submit(() -> supabaseSource.upsertLogoData(mContext, fPi, fName, freqKHz, "file://" + fPath, null));
            }
        } else {
            android.util.Log.d("RadioLogos", "NOT FOUND LOCAL");
            // 2. Fallback Cloud + Download
            final String stationNameForLambda = finalName;

            // V18.2: NO buscar información en internet (Supabase/Web) para bandas AM/SW.
            // Estas bandas no tienen metainformación centralizada fiable por PI/RDS.
            if (freqKHz < 30000) {
                android.util.Log.d("RadioRepository", "Download skipped: Freq < 30MHz (AM/SW). Cloud disabled for these bands.");
                return station;
            }

            if (com.example.openradiofm.BuildConfig.DEBUG) {
                android.util.Log.d("DEBUG_FETCH", "Fetching freq=" + freqKHz + ", Name=" + finalName + ", PI=" + piCode + ", Provider=" + mPrefs.getInt("pref_logo_provider", 0));
            }

            if (!tryMarkPending(cacheKey)) {
                if (com.example.openradiofm.BuildConfig.DEBUG) {
                    android.util.Log.d("DEBUG_FETCH", "Skipped due to pendingRequests: " + cacheKey);
                }
                // Ya hay una búsqueda en curso para esta combinación de freq+meta
                return station;
            }

            // V18.6: Guarda de seguridad para evitar RejectedExecutionException si el executor ya se cerró
            if (logoExecutor == null || logoExecutor.isShutdown()) {
                removePending(cacheKey);
                return station;
            }

            logoExecutor.submit(() -> {
                try {
                    String country = getCountryCode();
                    int provider = mPrefs.getInt("pref_logo_provider", 0); // 0=Supabase, 1=Web, 2=Both
                    String logoUrlToDownload = null;

                    // 1. Intentar Supabase si está habilitado (0 o 2)
                    SupabaseLogoResponse supabaseData = null;
                    if (provider == 0 || provider == 2) {
                        final long supabaseActivityStartMs = android.os.SystemClock.uptimeMillis();
                        // Notificar inicio de actividad de red
                        supabaseSource.notifyActivity(true);

                        try {
                            String pi = piCode != null ? piCode : "";
                            android.util.Log.d("RadioRepository", "SUPABASE FETCH START: PI=" + piCode + ", Name=" + stationNameForLambda);

                            // V18.8: Prioridad absoluta al nombre personalizado si existe y no es genérico
                            String cName = mPrefs.getString("CUSTOM_" + freqKHz, null);
                            retrofit2.Call<java.util.List<SupabaseLogoResponse>> call = null;
                            
                            if (cName != null && !cName.isEmpty() && !SupabaseLogoSource.isNameGeneric(cName)) {
                                android.util.Log.d("RadioRepository", "SUPABASE FETCH: Using Custom Name -> " + cName);
                                call = supabaseSource.getSupabaseApi().getLogosByName(supabaseSource.getApiKey(), "Bearer " + supabaseSource.getApiKey(), "ilike." + cName.trim(), "eq." + country, "*");
                            } else if (piCode != null && !piCode.isEmpty()) {
                                call = supabaseSource.getSupabaseApi().getLogosByPi(supabaseSource.getApiKey(), "Bearer " + supabaseSource.getApiKey(), "ilike." + piCode, "eq." + country, "*");
                            } else if (stationNameForLambda != null && !SupabaseLogoSource.isNameGeneric(stationNameForLambda)) {
                                call = supabaseSource.getSupabaseApi().getLogosByName(supabaseSource.getApiKey(), "Bearer " + supabaseSource.getApiKey(), "ilike." + stationNameForLambda.trim(), "eq." + country, "*");
                            }

                            if (call != null) {
                                retrofit2.Response<java.util.List<SupabaseLogoResponse>> res = call.execute();
                                if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                                    supabaseData = res.body().get(0);
                                    logoUrlToDownload = supabaseData.getLogoUrl();
                                    android.util.Log.d("RadioRepository", "SUPABASE SUCCESS: Logo=" + logoUrlToDownload + ", Stream=" + supabaseData.getStreamUrl());
                                    // Guardar streamUrl en SharedPreferences (incluso si es null para evitar reconsultas) y en el objeto station
                                    String streamUrlToSave = supabaseData.getStreamUrl() != null ? supabaseData.getStreamUrl() : "";
                                    mPrefs.edit().putString("STREAM_" + freqKHz, streamUrlToSave).apply();
                                    if (!streamUrlToSave.isEmpty()) {
                                        station.setStreamUrl(streamUrlToSave);
                                    }
                                } else {
                                    android.util.Log.d("RadioRepository", "SUPABASE EMPTY OR ERROR: " + (res != null ? res.code() : "null"));
                                    mPrefs.edit().putString("STREAM_" + freqKHz, "").apply();
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("RadioRepository", "Error fetching Supabase data", e);
                        } finally {
                            // Evitar bloquear el hilo de red y mantener visibilidad mínima del indicador.
                            finishSupabaseActivityWithMinDuration(supabaseActivityStartMs);
                        }
                    }

                    // 2. Intentar RadioBrowser si Supabase falló o si se eligió solo Web (1 o 2)
                    if (logoUrlToDownload == null && (provider == 1 || provider == 2)) {
                        logoUrlToDownload = webSource.fetchLogo(freqKHz, stationNameForLambda, country);
                    }

                    if (logoUrlToDownload != null) {
                        // Try to download and save with RDS name
                        String savedPath = downloadAndSaveLogo(logoUrlToDownload, freqKHz, stationNameForLambda);
                        if (savedPath != null) {
                            station.setLogoUrl(savedPath);
                            logoCache.put(cacheKey, savedPath);
                            if (sanitizedNameKey != null) nameLogoCache.put(sanitizedNameKey, savedPath);
                            if (callback != null)
                                callback.onLogoFound(savedPath);
                        } else {
                            // Fallback to URL if download fails
                            station.setLogoUrl(logoUrlToDownload);
                            logoCache.put(cacheKey, logoUrlToDownload);
                            if (sanitizedNameKey != null) nameLogoCache.put(sanitizedNameKey, logoUrlToDownload);
                            if (callback != null)
                                callback.onLogoFound(logoUrlToDownload);
                        }
                    } else {
                        // V16.3: CACHÉ NEGATIVA. Guardar que no hay logo para evitar reintentos inmediatos.
                        logoCache.put(cacheKey, "NO_LOGO");
                        // No ponemos en nameLogoCache para permitir reintentar con otra frecuencia.
                    }
                } catch (Exception e) {
                    android.util.Log.e("RadioRepository", "Fatal loop error", e);
                } finally {
                    // SIEMPRE liberar la petición pendiente al terminar (éxito o fallo)
                    removePending(cacheKey);
                }
            });
        }

        return station;
    }

    /**
     * V3.0: Busca el logo en el orden de prioridad:
     * 1. /sdcard/RadioLogos/96900_LOS40.png (frecuencia + RDS)
     * 2. /sdcard/RadioLogos/96900.png (solo frecuencia, compatibilidad)
     * 3. /sdcard/RadioLogos/9690.png (formato corto)
     */
    private String getLogoPath(int freqKHz, String rdsName) {
        // Sanitizar nombre RDS para nombre de archivo (quitar espacios y caracteres
        // especiales)
        String sanitizedName = (rdsName != null && !rdsName.isEmpty())
                ? rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase()
                : null;

        // 1. Prioridad: Frecuencia + RDS
        if (sanitizedName != null && !sanitizedName.isEmpty()) {
            String fileName = freqKHz + "_" + sanitizedName + ".png";
            java.io.File f1 = new java.io.File(getPreferredLogoDir(), fileName);
            if (f1.exists()) return f1.getAbsolutePath();
            java.io.File f2 = new java.io.File(getLegacyLogoDir(), fileName);
            if (f2.exists()) return f2.getAbsolutePath();
        }

        // 2. Compatibilidad: Solo frecuencia completa
        String fullName = freqKHz + ".png";
        java.io.File full1 = new java.io.File(getPreferredLogoDir(), fullName);
        if (full1.exists()) return full1.getAbsolutePath();
        java.io.File full2 = new java.io.File(getLegacyLogoDir(), fullName);
        if (full2.exists()) return full2.getAbsolutePath();

        // 3. Compatibilidad: Frecuencia corta (sin último cero)
        String shortName = (freqKHz / 10) + ".png";
        java.io.File short1 = new java.io.File(getPreferredLogoDir(), shortName);
        if (short1.exists()) return short1.getAbsolutePath();
        java.io.File short2 = new java.io.File(getLegacyLogoDir(), shortName);
        if (short2.exists()) return short2.getAbsolutePath();

        return null;
    }

    /**
     * V3.0: Descarga un logo y lo guarda con formato frecuencia_RDS.png
     * Se debe llamar SIEMPRE desde un hilo de fondo.
     */
    private String downloadAndSaveLogo(String urlString, int freqKHz, String rdsName) {
        try {
            ensureRadioLogosFolderExists();
            // V21.1: Usar Glide para downsample + decode seguro (evita OOM)
            android.graphics.Bitmap bitmap = com.bumptech.glide.Glide.with(mContext)
                    .asBitmap()
                    .load(urlString)
                    .submit(512, 512)
                    .get();

            // V3.0: Guardar con nombre RDS si está disponible
            String fileName;
            if (rdsName != null && !rdsName.isEmpty()) {
                String sanitizedName = rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                fileName = freqKHz + "_" + sanitizedName + ".png";
            } else {
                fileName = freqKHz + ".png";
            }

            java.io.File destFile = new java.io.File(getPreferredLogoDir(), fileName);
            java.io.FileOutputStream out = new java.io.FileOutputStream(destFile);

            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            android.util.Log.d("RadioLogos", "DOWNLOAD SAVED: " + destFile.getAbsolutePath());

            // V3.0 FIX: Persistencia forzada mediante MediaScanner
            // Avisar al sistema que hay un nuevo archivo para que no lo borre ni lo ignore
            android.media.MediaScannerConnection.scanFile(
                    mContext,
                    new String[] { destFile.getAbsolutePath() },
                    null,
                    (path, uri) -> android.util.Log.i("RadioLogos", "Scanned " + path + ":-> uri=" + uri));

            // V16.2: Alimentar servidor central tras descarga exitosa
            boolean onlineAfterDownload = mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_logos_online", false);
            if (onlineAfterDownload) {
                String pi = mPrefs.getString("PI_" + freqKHz, null);
                supabaseSource.upsertLogoData(mContext, pi, rdsName, freqKHz, urlString, null);
            }

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            android.util.Log.e("RadioRepository", "downloadAndSaveLogo", e);
            return null;
        }
    }

    /**
     * Libera recursos de las fuentes subyacentes.
     * Debe llamarse cuando la Activity principal se destruye para:
     * - Cerrar el proceso root abierto por RootRDSSource.
     * - Cerrar el ExecutorService de logos para evitar fugas de hilos.
     */
    public void shutdown() {
        if (rootSource != null) {
            rootSource.shutdown();
        }

        // V18.4: Cerrar el ExecutorService de logos inmediatamente
        if (logoExecutor != null) {
            logoExecutor.shutdownNow();
            android.util.Log.d("RadioRepository", "ExecutorService de logos cerrado (shutdownNow).");
        }
    }

    private String getCountryCode() {
        try {
            String country = java.util.Locale.getDefault().getCountry();
            return (country != null && !country.isEmpty()) ? country.toUpperCase() : "ES";
        } catch (Exception e) {
            return "ES";
        }
    }

    /**
     * Resuelve la URL de streaming para FM (≥ 30 MHz): usa caché {@code STREAM_} y, si falta o está
     * vacía, consulta Supabase en el <b>hilo actual</b> (solo invocar desde hilo de fondo).
     * <p>
     * Necesario al pulsar la nube: {@link #getStationInfo} a menudo devuelve la emisora sin URL porque
     * {@link #fetchStreamUrlAsync} aún no ha terminado o nunca se disparó.
     */
    public String resolveStreamUrlForFrequency(int freqKHz) {
        if (freqKHz < 30000) return null;
        String streamUrlStored = mPrefs.getString("STREAM_" + freqKHz, null);
        if (streamUrlStored != null && !streamUrlStored.trim().isEmpty()) {
            return streamUrlStored;
        }
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

    /**
     * Misma lógica que el fetch asíncrono de URL: Supabase por nombre custom, PI o RDS.
     * Persiste {@code STREAM_{freq}}. Solo hilo de fondo.
     */
    private String querySupabaseForStreamUrl(int freqKHz, String finalName, String piCode) {
        int provider = mPrefs.getInt("pref_logo_provider", 0);
        if (!(provider == 0 || provider == 2)) {
            return null;
        }
        final long supabaseActivityStartMs = android.os.SystemClock.uptimeMillis();
        try {
            supabaseSource.notifyActivity(true);
            String cName = mPrefs.getString("CUSTOM_" + freqKHz, null);
            retrofit2.Call<java.util.List<SupabaseLogoResponse>> call = null;
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
            if (call == null) {
                return null;
            }
            retrofit2.Response<java.util.List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                SupabaseLogoResponse supabaseData = res.body().get(0);
                String streamUrlToSave = supabaseData.getStreamUrl() != null ? supabaseData.getStreamUrl() : "";
                mPrefs.edit().putString("STREAM_" + freqKHz, streamUrlToSave).apply();
                return streamUrlToSave.isEmpty() ? null : streamUrlToSave;
            }
            mPrefs.edit().putString("STREAM_" + freqKHz, "").apply();
            return null;
        } catch (Exception e) {
            android.util.Log.e("RadioRepository", "querySupabaseForStreamUrl", e);
            return null;
        } finally {
            finishSupabaseActivityWithMinDuration(supabaseActivityStartMs);
        }
    }

    private void finishSupabaseActivityWithMinDuration(long startUptimeMs) {
        long elapsed = android.os.SystemClock.uptimeMillis() - startUptimeMs;
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

    // Método auxiliar para buscar la URL de streaming en background
    private void fetchStreamUrlAsync(String cacheKey, int freqKHz, String finalName, String piCode, RadioStation station) {
        // V18.2: Bloqueo global AM/SW
        if (freqKHz < 30000) return;
        
        String streamCacheKey = cacheKey + "_STREAM";
        if (!tryMarkPending(streamCacheKey)) return;
        
        // V18.6: Guarda de seguridad para evitar crash si el executor se cierra en mitad de la petición
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
                android.util.Log.e("RadioRepository", "Error fetching stream URL", e);
            } finally {
                removePending(streamCacheKey);
            }
        });
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
}
