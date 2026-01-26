package com.example.openradiofm.data.repository;

import com.example.openradiofm.data.model.RadioStation;
import com.example.openradiofm.data.source.RootRDSSource;
import com.example.openradiofm.data.source.WebRadioSource;

public class RadioRepository {
    private final RootRDSSource rootSource;
    private final WebRadioSource webSource;

    public RadioRepository() {
        this.rootSource = new RootRDSSource();
        this.webSource = new WebRadioSource();
    }

    public interface LogoCallback {
        void onLogoFound(String logoUrl);
    }

    public RadioStation getStationInfo(int freqKHz, LogoCallback callback) {
        // 1. Nombre oficial del sistema (Root)
        String rootName = rootSource.getRdsName(freqKHz);
        String finalName = (rootName != null) ? rootName : "";

        RadioStation station = new RadioStation(freqKHz, finalName);

        // 2. Logo Local (/sdcard/RadioLogos/96900.png)
        String localPathFull = "/sdcard/RadioLogos/" + freqKHz + ".png";
        
        // Fix for files without last zero (e.g. 8750.png for 87500KHz)
        String localPathShort = "/sdcard/RadioLogos/" + (freqKHz / 10) + ".png";

        android.util.Log.d("RadioLogos", "Checking: " + localPathFull);
        if (new java.io.File(localPathFull).exists()) {
             android.util.Log.d("RadioLogos", "FOUND: " + localPathFull);
             station.setLogoUrl(localPathFull);
             if (callback != null) callback.onLogoFound(localPathFull);
        } else {
             android.util.Log.d("RadioLogos", "Checking Short: " + localPathShort);
             if (new java.io.File(localPathShort).exists()) { // Fallback to short name
                 android.util.Log.d("RadioLogos", "FOUND SHORT: " + localPathShort);
                 station.setLogoUrl(localPathShort);
                 if (callback != null) callback.onLogoFound(localPathShort);
             } else {
                 android.util.Log.d("RadioLogos", "NOT FOUND LOCAL");
                 // 3. Fallback Cloud + Download
                 new Thread(() -> {
                     String cloudUrl = webSource.fetchLogo(freqKHz, finalName, "ES");
                     if (cloudUrl != null) {
                         // Try to download and save
                         String savedPath = downloadAndSaveLogo(cloudUrl, freqKHz);
                         if (savedPath != null) {
                             station.setLogoUrl(savedPath);
                             if (callback != null) callback.onLogoFound(savedPath);
                         } else {
                             // Fallback to URL if download fails
                             station.setLogoUrl(cloudUrl);
                             if (callback != null) callback.onLogoFound(cloudUrl);
                         }
                     }
                 }).start();
             }
        }

        return station;
    }

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
            
            android.util.Log.d("RadioLogos", "DOWNLOAD SAVED: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
