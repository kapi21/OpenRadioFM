package com.example.openradiofm.data.repository;

import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.WebRadioSource;

public class RadioRepository {
    private final RootRDSSource rootSource;
    private final WebRadioSource webSource;
    private final boolean useRoot;

    private final android.content.SharedPreferences mPrefs;
    
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
        this.rootSource = enableRoot ? new RootRDSSource() : null;
        this.webSource = new WebRadioSource();
        // Usamos un archivo de preferencias específico para los nombres de emisoras
        this.mPrefs = context.getSharedPreferences("RadioStationNames", android.content.Context.MODE_PRIVATE);
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
        if (logoCache.containsKey(freqKHz)) {
            String cachedPath = logoCache.get(freqKHz);
            station.setLogoUrl(cachedPath);
            if (callback != null) callback.onLogoFound(cachedPath);
            return station;
        }

        // 2. Logo Local (/sdcard/RadioLogos/96900.png)
        String localPathFull = "/sdcard/RadioLogos/" + freqKHz + ".png";
        
        // Fix for files without last zero (e.g. 8750.png for 87500KHz)
        String localPathShort = "/sdcard/RadioLogos/" + (freqKHz / 10) + ".png";

        android.util.Log.d("RadioLogos", "Checking: " + localPathFull);
        if (new java.io.File(localPathFull).exists()) {
             android.util.Log.d("RadioLogos", "FOUND: " + localPathFull);
             station.setLogoUrl(localPathFull);
             logoCache.put(freqKHz, localPathFull); // Guardo en caché
             if (callback != null) callback.onLogoFound(localPathFull);
        } else {
             android.util.Log.d("RadioLogos", "Checking Short: " + localPathShort);
             if (new java.io.File(localPathShort).exists()) { // Fallback to short name
                 android.util.Log.d("RadioLogos", "FOUND SHORT: " + localPathShort);
                 station.setLogoUrl(localPathShort);
                 logoCache.put(freqKHz, localPathShort); // Guardo en caché
                 if (callback != null) callback.onLogoFound(localPathShort);
             } else {
                 android.util.Log.d("RadioLogos", "NOT FOUND LOCAL");
                 // 3. Fallback Cloud + Download
                 //    Usa ExecutorService en lugar de new Thread() para gestión eficiente.
                 
                 // Crear variable final para uso en lambda
                 final String stationNameForLambda = finalName;
                 
                 logoExecutor.submit(() -> {
                     String cloudUrl = webSource.fetchLogo(freqKHz, stationNameForLambda, "ES");
                     if (cloudUrl != null) {
                         // Try to download and save
                         String savedPath = downloadAndSaveLogo(cloudUrl, freqKHz);
                         if (savedPath != null) {
                             station.setLogoUrl(savedPath);
                             logoCache.put(freqKHz, savedPath); // Guardo en caché
                             if (callback != null) callback.onLogoFound(savedPath);
                         } else {
                             // Fallback to URL if download fails (cache URL para no reintentar descarga inmediatamente)
                             station.setLogoUrl(cloudUrl);
                             logoCache.put(freqKHz, cloudUrl);
                             if (callback != null) callback.onLogoFound(cloudUrl);
                         }
                     }
                 });
             }
        }

        return station;
    }

    /**
     * Descarga un logo desde una URL y lo guarda como PNG en /sdcard/RadioLogos.
     * Se debe llamar SIEMPRE desde un hilo de fondo.
     */
    private String downloadAndSaveLogo(String urlString, int freqKHz) {
        try {
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            
            java.io.InputStream input = connection.getInputStream();
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
            
            java.io.File destFolder = new java.io.File("/sdcard/RadioLogos/");
            if (!destFolder.exists()) destFolder.mkdirs();
            
            java.io.File destFile = new java.io.File(destFolder, freqKHz + ".png");
            java.io.FileOutputStream out = new java.io.FileOutputStream(destFile);
            
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            
            // CRÍTICO: Liberar el Bitmap de memoria para evitar OutOfMemoryError
            bitmap.recycle();
            
            android.util.Log.d("RadioLogos", "DOWNLOAD SAVED: " + destFile.getAbsolutePath());
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
