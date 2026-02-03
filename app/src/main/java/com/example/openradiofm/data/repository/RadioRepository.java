package com.example.openradiofm.data.repository;

import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.WebRadioSource;

public class RadioRepository {
    private final RootRDSSource rootSource;
    private final WebRadioSource webSource;
    private final boolean useRoot;

    private final android.content.SharedPreferences mPrefs;
    private final android.content.Context mContext; // V3.0: Needed for MediaScanner
    
    // ExecutorService para gestionar hilos de descarga de logos de forma eficiente.
    // Limita a 3 hilos concurrentes para evitar crear cientos de hilos.
    private final java.util.concurrent.ExecutorService logoExecutor = 
        java.util.concurrent.Executors.newFixedThreadPool(3);
    
    // Caché en memoria para evitar recargas de logos al cambiar frecuencia.
    // Key: frecuencia en kHz, Value: URL o path del logo
    private final java.util.HashMap<Integer, String> logoCache = new java.util.HashMap<>();

    // Repositorio central que combina:
    // - RootRDSSource: nombres RDS desde el fichero interno del servicio de radio (requiere root).
    // - WebRadioSource: búsqueda de logos en internet (RadioBrowser) y caché local en /sdcard/RadioLogos.
    // - SharedPreferences: nombres personalizados definidos por el usuario.
    //
    // El flag enableRoot permite desactivar por completo el acceso root cuando
    // estamos en MODO_FM_BASICO (dispositivos sin root o sin servicio especial).
    public RadioRepository(android.content.Context context, boolean enableRoot) {
        this.useRoot = enableRoot;
        this.mContext = context; // V3.0: Store for MediaScanner
        this.rootSource = enableRoot ? new RootRDSSource() : null;
        this.webSource = new WebRadioSource();
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
            mPrefs.edit().remove("NAME_" + freqKHz).apply();
        } else {
            mPrefs.edit().putString("NAME_" + freqKHz, name.trim()).apply();
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
     * - Si se detecta que se llama desde el UI thread, se loguea un warning pero NO se lanza excepción.
     * - El callback de logo se ejecuta SIEMPRE en un hilo de fondo; la Activity
     *   debe hacer runOnUiThread() al actualizar vistas.
     */
    public RadioStation getStationInfo(int freqKHz, LogoCallback callback) {
        // Warning si se llama desde UI thread (pero no matamos la app)
        if (android.os.Looper.getMainLooper().getThread() == Thread.currentThread()) {
            android.util.Log.w("RadioRepository", 
                "WARNING: getStationInfo() llamado desde UI thread. " +
                "Esto puede causar lag en la interfaz. " +
                "Considera ejecutarlo en un hilo de fondo."
            );
        }
        // 1. Verificar nombre personalizado (Prioridad Máxima)
        String customName = mPrefs.getString("NAME_" + freqKHz, null);
        
        String finalName = "";
        
        if (customName != null && !customName.isEmpty()) {
            finalName = customName;
        } else {
            // 2. Nombre oficial del sistema (Root). Si no hay root, devolverá null sin excepciones.
            String rootName = null;
            if (useRoot && rootSource != null) {
                rootName = rootSource.getRdsName(freqKHz);
            }
            finalName = (rootName != null) ? rootName : "";
        }

        RadioStation station = new RadioStation(freqKHz, finalName);
        
        // 0. Revisar Caché en Memoria
        String cacheKey = freqKHz + "_" + finalName; // V3.0: Caché con nombre RDS
        if (logoCache.containsKey(freqKHz)) {
            String cachedPath = logoCache.get(freqKHz);
            station.setLogoUrl(cachedPath);
            if (callback != null) callback.onLogoFound(cachedPath);
            return station;
        }

        // V3.0: Búsqueda de logos con prioridad frecuencia+RDS
        // 1. Logo con frecuencia + nombre RDS: /sdcard/RadioLogos/96900_LOS40.png
        String logoPath = getLogoPath(freqKHz, finalName);
        
        if (logoPath != null) {
            android.util.Log.d("RadioLogos", "FOUND: " + logoPath);
            station.setLogoUrl(logoPath);
            logoCache.put(freqKHz, logoPath);
            if (callback != null) callback.onLogoFound(logoPath);
        } else {
            android.util.Log.d("RadioLogos", "NOT FOUND LOCAL");
            // 2. Fallback Cloud + Download
            final String stationNameForLambda = finalName;
            
            logoExecutor.submit(() -> {
                String cloudUrl = webSource.fetchLogo(freqKHz, stationNameForLambda, "ES");
                if (cloudUrl != null) {
                    // Try to download and save with RDS name
                    String savedPath = downloadAndSaveLogo(cloudUrl, freqKHz, stationNameForLambda);
                    if (savedPath != null) {
                        station.setLogoUrl(savedPath);
                        logoCache.put(freqKHz, savedPath);
                        if (callback != null) callback.onLogoFound(savedPath);
                    } else {
                        // Fallback to URL if download fails
                        station.setLogoUrl(cloudUrl);
                        logoCache.put(freqKHz, cloudUrl);
                        if (callback != null) callback.onLogoFound(cloudUrl);
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
        // Sanitizar nombre RDS para nombre de archivo (quitar espacios y caracteres especiales)
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
                new String[]{destFile.getAbsolutePath()}, 
                null, 
                (path, uri) -> android.util.Log.i("RadioLogos", "Scanned " + path + ":-> uri=" + uri)
            );

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
