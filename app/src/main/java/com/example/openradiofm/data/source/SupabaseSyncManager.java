package com.example.openradiofm.data.source;

import android.content.Context;
import android.util.Log;

import com.example.openradiofm.data.source.network.SupabaseApi;
import com.example.openradiofm.data.source.network.SupabaseClient;
import com.example.openradiofm.util.AppIoExecutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gestiona la sincronización de datos desde archivos M3U8 hacia Supabase.
 */
public class SupabaseSyncManager {
    private static final String TAG = "SupabaseSyncManager";
    private final Context mContext;
    private final SupabaseLogoSource mSupabaseSource;

    public SupabaseSyncManager(Context context, SupabaseLogoSource supabaseSource) {
        this.mContext = context;
        this.mSupabaseSource = supabaseSource;
    }

    /**
     * Procesa un archivo M3U8 local y sube los datos a Supabase.
     * @param filePath Ruta absoluta al archivo .m3u8
     */
    public void syncFromM3u(String filePath) {
        AppIoExecutor.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                processReader(reader);
            } catch (Exception e) {
                Log.e(TAG, "Error leyendo archivo M3U", e);
            }
        });
    }

    /**
     * Procesa un archivo M3U8 desde el directorio raíz del proyecto (para depuración).
     */
    public void syncFromProjectRoot() {
        AppIoExecutor.execute(() -> {
            String path = "/sdcard/OpenRadioFM/radio.m3u8"; // Ajustar según donde el usuario ponga el archivo
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                processReader(reader);
            } catch (Exception e) {
                Log.e(TAG, "Error en sync directo", e);
            }
        });
    }

    private void processReader(BufferedReader reader) throws Exception {
        String line;
        String currentName = null;
        String currentLogo = null;
        String currentGroup = null;
        String currentStream = null;

        // Pattern para extraer atributos de #EXTINF
        Pattern namePattern = Pattern.compile("tvg-name=\"([^\"]+)\"");
        Pattern logoPattern = Pattern.compile("tvg-logo=\"([^\"]+)\"");
        Pattern groupPattern = Pattern.compile("group-title=\"([^\"]+)\"");

        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#EXTINF")) {
                Matcher mName = namePattern.matcher(line);
                Matcher mLogo = logoPattern.matcher(line);
                Matcher mGroup = groupPattern.matcher(line);

                if (mName.find()) currentName = mName.group(1);
                if (mLogo.find()) currentLogo = mLogo.group(1);
                if (mGroup.find()) currentGroup = mGroup.group(1);

                // Si no hay tvg-name, a veces está al final de la línea tras la coma
                if (currentName == null || currentName.isEmpty()) {
                    int lastComma = line.lastIndexOf(",");
                    if (lastComma != -1) {
                        currentName = line.substring(lastComma + 1).trim();
                    }
                }
            } else if (line.startsWith("http")) {
                currentStream = line.trim();
                
                // Tenemos un bloque completo (V19.0: Logo ya no es obligatorio para sincronizar)
                if (currentName != null && !currentName.isEmpty()) {
                    uploadToSupabase(currentName, currentLogo, currentGroup, currentStream);
                    count++;
                }
                
                // Reset para el siguiente
                currentName = null;
                currentLogo = null;
                currentGroup = null;
                currentStream = null;
            }
        }
        Log.d(TAG, "Sincronización finalizada. Procesadas " + count + " emisoras.");
    }

    private void uploadToSupabase(String name, String logo, String group, String stream) {
        // Sanitizar nombre para que coincida con RDS (quitar " (Radio)", etc)
        String rdsName = name.replace(" (Radio)", "").trim();
        
        // El M3U no tiene PI ni Frecuencia, pero Supabase hace upsert por rds_name
        // Pasamos frecuencia 0 para indicar que es un registro maestro sin frecuencia fija
        mSupabaseSource.upsertLogoData(mContext, null, rdsName, 0, logo, stream);
    }
}
