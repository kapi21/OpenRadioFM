package com.example.openradiofm.data.repository;

import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.PredefinedStationSource;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.WebRadioSource;

public class RadioRepository {
    private final RootRDSSource rootSource;
    private final WebRadioSource webSource;
    private final PredefinedStationSource predefinedSource;
    private final boolean useRoot;

    private final android.content.SharedPreferences mPrefs;
    private final android.content.Context mContext; // V3.0: Needed for MediaScanner

    // ExecutorService para gestionar hilos de descarga de logos de forma eficiente.
    // Limita a 3 hilos concurrentes para evitar crear cientos de hilos.
    private final java.util.concurrent.ExecutorService logoExecutor = java.util.concurrent.Executors
            .newFixedThreadPool(3);

    // Caché en memoria para evitar recargas de logos al cambiar frecuencia o nombre.
    // V13.6: Key: freqKHz + "_" + stationName, Value: URL o path del logo
    private final java.util.HashMap<String, String> logoCache = new java.util.HashMap<>();

    // Repositorio central que combina:
    // - RootRDSSource: nombres RDS desde el fichero interno del servicio de radio
    // (requiere root).
    // - WebRadioSource: búsqueda de logos en internet (RadioBrowser) y caché local
    // en /sdcard/RadioLogos.
    // - SharedPreferences: nombres personalizados definidos por el usuario.
    //
    // El flag enableRoot permite desactivar por completo el acceso root cuando
    // estamos en MODO_FM_BASICO (dispositivos sin root o sin servicio especial).
    public RadioRepository(android.content.Context context, boolean enableRoot) {
        this.useRoot = enableRoot;
        this.mContext = context; // V3.0: Store for MediaScanner
        this.rootSource = enableRoot ? new RootRDSSource() : null;
        this.webSource = new WebRadioSource();
        this.predefinedSource = new PredefinedStationSource(context);
        // Usamos un archivo de preferencias específico para los nombres de emisoras
        this.mPrefs = context.getSharedPreferences("RadioStationNames", android.content.Context.MODE_PRIVATE);

        // V3.0: Asegurar que existe la carpeta RadioLogos
        ensureRadioLogosFolderExists();
    }

    /**
     * V3.0: Asegura que la carpeta /sdcard/RadioLogos/ existe.
     */
    private void ensureRadioLogosFolderExists() {
        try {
            java.io.File dir = new java.io.File("/sdcard/RadioLogos/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            android.util.Log.e("RadioRepository", "Error creando carpeta RadioLogos: " + e.getMessage());
        }
    }

    public interface LogoCallback {
        void onLogoFound(String logoUrl);
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
    }

    /**
     * V9: Guarda el nombre RDS PS recibido dinámicamente.
     */
    public void saveRdsName(int freqKHz, String name) {
        if (name != null && !name.trim().isEmpty() && name.length() >= 2) {
            String existing = mPrefs.getString("RDS_" + freqKHz, "");
            if (!name.equals(existing)) {
                mPrefs.edit().putString("RDS_" + freqKHz, name.trim()).apply();
            }
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
        String ptyStored = mPrefs.getString("PTY_" + freqKHz, null);
        
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

        // 0. Revisar Caché en Memoria
        String cacheKey = freqKHz + "_" + finalName; // V3.0: Caché con nombre RDS
        if (logoCache.containsKey(cacheKey)) {
            String cachedPath = logoCache.get(cacheKey);
            station.setLogoUrl(cachedPath);
            if (callback != null)
                callback.onLogoFound(cachedPath);
            return station;
        }

        // V3.0: Búsqueda de logos con prioridad frecuencia+RDS
        // 1. Logo con frecuencia + nombre RDS: /sdcard/RadioLogos/96900_LOS40.png
        String logoPath = getLogoPath(freqKHz, finalName);

        if (logoPath != null) {
            android.util.Log.d("RadioLogos", "FOUND: " + logoPath);
            station.setLogoUrl(logoPath);
            logoCache.put(cacheKey, logoPath);
            if (callback != null)
                callback.onLogoFound(logoPath);
        } else {
            android.util.Log.d("RadioLogos", "NOT FOUND LOCAL");
            // 2. Fallback Cloud + Download
            final String stationNameForLambda = finalName;

            // Check if online logos are enabled
            boolean onlineLogosEnabled = mContext.getSharedPreferences("RadioPresets", android.content.Context.MODE_PRIVATE)
                    .getBoolean("pref_logos_online", false);

            if (!onlineLogosEnabled) {
                android.util.Log.d("RadioLogos", "Download skipped: pref_logos_online is disabled.");
                return station; // Skip download entirely
            }

            logoExecutor.submit(() -> {
                // Prioridad 1: Usar logo del catálogo predefinido si existe
                String catalogLogoUrl = (predefined != null) ? predefined.getLogoUrl() : null;
                String logoUrlToDownload = (catalogLogoUrl != null) ? catalogLogoUrl
                        : webSource.fetchLogo(freqKHz, stationNameForLambda, "ES");

                if (logoUrlToDownload != null) {
                    // Try to download and save with RDS name
                    String savedPath = downloadAndSaveLogo(logoUrlToDownload, freqKHz, stationNameForLambda);
                    if (savedPath != null) {
                        station.setLogoUrl(savedPath);
                        logoCache.put(cacheKey, savedPath);
                        if (callback != null)
                            callback.onLogoFound(savedPath);
                    } else {
                        // Fallback to URL if download fails
                        station.setLogoUrl(logoUrlToDownload);
                        logoCache.put(cacheKey, logoUrlToDownload);
                        if (callback != null)
                            callback.onLogoFound(logoUrlToDownload);
                    }
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
            String pathWithRds = "/sdcard/RadioLogos/" + freqKHz + "_" + sanitizedName + ".png";
            if (new java.io.File(pathWithRds).exists()) {
                return pathWithRds;
            }
        }

        // 2. Compatibilidad: Solo frecuencia completa
        String pathFull = "/sdcard/RadioLogos/" + freqKHz + ".png";
        if (new java.io.File(pathFull).exists()) {
            return pathFull;
        }

        // 3. Compatibilidad: Frecuencia corta (sin último cero)
        String pathShort = "/sdcard/RadioLogos/" + (freqKHz / 10) + ".png";
        if (new java.io.File(pathShort).exists()) {
            return pathShort;
        }

        return null;
    }

    /**
     * V3.0: Descarga un logo y lo guarda con formato frecuencia_RDS.png
     * Se debe llamar SIEMPRE desde un hilo de fondo.
     */
    private String downloadAndSaveLogo(String urlString, int freqKHz, String rdsName) {
        try {
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            java.io.InputStream input = connection.getInputStream();
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);

            ensureRadioLogosFolderExists();

            // V3.0: Guardar con nombre RDS si está disponible
            String fileName;
            if (rdsName != null && !rdsName.isEmpty()) {
                String sanitizedName = rdsName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                fileName = freqKHz + "_" + sanitizedName + ".png";
            } else {
                fileName = freqKHz + ".png";
            }

            java.io.File destFile = new java.io.File("/sdcard/RadioLogos/", fileName);
            java.io.FileOutputStream out = new java.io.FileOutputStream(destFile);

            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // CRÍTICO: Liberar el Bitmap de memoria para evitar OutOfMemoryError
            bitmap.recycle();

            android.util.Log.d("RadioLogos", "DOWNLOAD SAVED: " + destFile.getAbsolutePath());

            // V3.0 FIX: Persistencia forzada mediante MediaScanner
            // Avisar al sistema que hay un nuevo archivo para que no lo borre ni lo ignore
            android.media.MediaScannerConnection.scanFile(
                    mContext,
                    new String[] { destFile.getAbsolutePath() },
                    null,
                    (path, uri) -> android.util.Log.i("RadioLogos", "Scanned " + path + ":-> uri=" + uri));

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
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

        // Cerrar el ExecutorService de logos
        logoExecutor.shutdownNow();
        android.util.Log.d("RadioRepository", "ExecutorService de logos cerrado.");
    }
}
