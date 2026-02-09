package com.example.openradiofm.data.source;

import android.content.Context;
import android.util.Log;
import com.example.openradiofm.data.model.RadioStation;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Fuente de datos para emisoras predefinidas (Catálogo offline).
 * Carga un archivo JSON desde assets y permite buscar coincidencias por frecuencia.
 */
public class PredefinedStationSource {
    private static final String TAG = "PredefinedStationSource";
    private static final String ASSET_FILE = "stations_es.json";
    
    private final List<RadioStation> mPredefinedStations = new ArrayList<>();

    public PredefinedStationSource(Context context) {
        loadFromAssets(context);
    }

    /**
     * Lee el JSON de assets y lo convierte en una lista de objetos RadioStation.
     */
    private void loadFromAssets(Context context) {
        try {
            InputStream is = context.getAssets().open(ASSET_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                
                String freqStr = obj.getString("frequency");
                // Convertir frecuencia (ej: "93.9") a kHz (93900)
                float freqFloat = Float.parseFloat(freqStr);
                int freqKHz = (int) (freqFloat * 1000);
                
                if (freqKHz < 60000 && freqKHz > 500) {
                     // Caso especial para radios AM o frecuencias raras en el JSON original
                     // (aunque el filtro de abajo es más robusto para FM)
                }

                RadioStation station = new RadioStation(freqKHz, obj.getString("name"));
                station.setLogoUrl(obj.getString("logo_url"));
                station.setPty(obj.getString("genre"));
                // El slogan lo guardamos como PTY si el género es genérico o concatenado
                if (obj.has("slogan") && !obj.getString("slogan").isEmpty()) {
                    station.setPty(station.getPty() + " - " + obj.getString("slogan"));
                }
                
                mPredefinedStations.add(station);
            }
            Log.d(TAG, "Cargadas " + mPredefinedStations.size() + " emisoras predefinidas.");
            
        } catch (Exception e) {
            Log.e(TAG, "Error cargando catálogo de assets: " + e.getMessage());
        }
    }

    /**
     * Busca una emisora en el catálogo que coincida con la frecuencia dada.
     * Soporta un pequeño margen de error si fuera necesario, pero aquí buscamos exacta.
     */
    public RadioStation findStation(int freqKHz) {
        for (RadioStation s : mPredefinedStations) {
            if (s.getFreqKHz() == freqKHz) {
                return s;
            }
        }
        
        // Búsqueda aproximada (margen de 100kHz para sintonías finas)
        for (RadioStation s : mPredefinedStations) {
            if (Math.abs(s.getFreqKHz() - freqKHz) <= 100) {
                return s;
            }
        }
        
        return null;
    }
}
