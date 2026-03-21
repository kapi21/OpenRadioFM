package com.example.openradiofm.data.source;

import com.example.openradiofm.data.source.network.SupabaseApi;
import com.example.openradiofm.data.source.network.SupabaseClient;
import com.example.openradiofm.data.source.network.model.SupabaseLogoResponse;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Fuente de logos centralizada en Supabase.
 */
public class SupabaseLogoSource {
    private final SupabaseApi api;
    private final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhjaXF4dmZ2b2hjYWlhcXFydmRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2MjExNDcsImV4cCI6MjA4ODE5NzE0N30.kE5W3_qHMWMc1nKQQQn_lMb9NXOu6kFjEL5glpIhswM";
    private final String storageUrl = "https://hciqxvfvohcaiaqqrvdq.supabase.co/storage/v1/object/public/station-logos/espana/";

    public SupabaseLogoSource() {
        this.api = SupabaseClient.getApi();
    }

    /**
     * V18.8: Lista negra de nombres genéricos que ensucian la base de datos o causan búsquedas erróneas.
     */
    private static final String[] BLACKLIST_NAMES = {
            "FM", "RADIO", "STEREO", "RDS", "BUSCANDO", "SCAN", "TUNING", "SINR", "NO RDS", "EMPTY", "WAITING", "SIGNAL"
    };

    public static boolean isNameGeneric(String name) {
        if (name == null || name.trim().length() < 2) return true;
        String clean = name.trim().toUpperCase();
        for (String black : BLACKLIST_NAMES) {
            if (clean.equals(black)) return true;
        }
        return false;
    }

    public SupabaseApi getSupabaseApi() {
        return api;
    }

    public String getApiKey() {
        return apiKey;
    }

    public interface DataActivityListener {
        void onDataActivity(boolean active);
    }

    private DataActivityListener mActivityListener;

    public void setDataActivityListener(DataActivityListener mActivityListener) {
        this.mActivityListener = mActivityListener;
    }

    public void notifyActivity(boolean active) {
        // android.util.Log.d("SupabaseLogoSource", "notifyActivity: active=" + active);
        if (mActivityListener != null) {
            try {
                mActivityListener.onDataActivity(active);
            } catch (Exception e) {
                android.util.Log.e("SupabaseLogoSource", "Error notifying activity: " + e.getMessage());
            }
        }
    }
    /**
     * Busca el logo en Supabase siguiendo el orden de prioridad.
     * V16.2: Ahora filtra también por país si está disponible.
     */
    public String fetchLogo(String piCode, String rdsName, int freqKHz) {
        notifyActivity(true);
        String country = java.util.Locale.getDefault().getCountry();
        if (country == null || country.isEmpty()) country = "ES";
        
        android.util.Log.d("SupabaseLogoSource", "FETCH START: PI=" + piCode + ", Name=" + rdsName + ", Freq=" + freqKHz + ", Country=" + country);
        try {
            // 1. prioridad: PI Code (exacto)
            if (piCode != null && !piCode.isEmpty()) {
                String logo = queryByPi(piCode, country);
                if (logo != null) {
                    android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (PI): " + logo);
                    return logo;
                }
            }

            // 2. prioridad: RDS Name (exacto)
            if (rdsName != null && !rdsName.isEmpty()) {
                String trimmedName = rdsName.trim();
                if (!isNameGeneric(trimmedName)) {
                    String logo = queryByName(trimmedName, country);
                    if (logo != null) {
                        android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (Name): " + logo);
                        return logo;
                    }
                } else {
                    android.util.Log.d("SupabaseLogoSource", "FETCH SKIP: Name '" + trimmedName + "' is generic.");
                }
            }

            // 3. prioridad: Frecuencia (kHz)
            String logoFreq = queryByFreq(freqKHz, country);
            if (logoFreq != null) {
                android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (Freq): " + logoFreq);
            } else {
                android.util.Log.d("SupabaseLogoSource", "FETCH EMPTY: No logo found for this station.");
            }
            return logoFreq;
        } catch (Throwable e) {
            android.util.Log.e("SupabaseLogoSource", "FETCH ERROR: " + e.getMessage());
            return null;
        } finally {
            notifyActivity(false);
        }
    }

    /**
     * V16.2: Envía o actualiza los datos de una emisora en el servidor centralizado.
     * V18.5: Añadido soporte para Storage y UserId.
     */
    public void upsertLogoData(android.content.Context context, String piCode, String rdsName, int freqKHz, String logoUrl, String streamUrl) {
        // V19.0: Logo ya no es estrictamente obligatorio para el upsert (permite base de datos comunitaria)
        // Pero necesitamos al menos PI o Nombre
        if ((piCode == null || piCode.isEmpty()) && (rdsName == null || rdsName.isEmpty())) return;

        new Thread(() -> {
            notifyActivity(true);
            try {
                String finalLogoUrl = logoUrl;
                // V19.4: Formatear frecuencia a String MHz (ej: 87.50) para coincidir con SQL
                String freqStr = String.format(java.util.Locale.US, "%.2f", freqKHz / 1000.0);

                // Si el logo es local y existe, primero lo subimos al Storage de Supabase
                if (logoUrl != null && logoUrl.startsWith("file://")) {
                    String localPath = logoUrl.substring(7);
                    File file = new File(localPath);
                    if (file.exists()) {
                        String fileName = (piCode != null ? piCode : (rdsName != null ? rdsName.replaceAll("[^a-zA-Z0-9]", "") : "station")) 
                                + "_" + freqKHz + ".png";
                        
                        // V19.4: Añadimos prefijo de carpeta 'espana/' según la estructura del bucket
                        String storagePath = "espana/" + fileName;
                        
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
                        Call<Void> uploadCall = api.uploadLogoFile(apiKey, "Bearer " + apiKey, storagePath, requestBody);
                        Response<Void> uploadRes = uploadCall.execute();
                        
                        if (uploadRes.isSuccessful() || uploadRes.code() == 409) { // 409 duplicated is okay for us
                            finalLogoUrl = storageUrl + fileName;
                            android.util.Log.d("SupabaseLogoSource", "UPLOAD SUCCESS: " + finalLogoUrl);
                        } else {
                            String errorBody = "";
                            try {
                                if (uploadRes.errorBody() != null) {
                                    errorBody = uploadRes.errorBody().string();
                                }
                            } catch (Exception ignored) {}
                            android.util.Log.e("SupabaseLogoSource", "UPLOAD FAILED: " + uploadRes.code() + " Error=" + errorBody);
                        }
                    }
                }

                String hwModel = android.os.Build.MODEL; // Identificador del hardware (ej: K706, etc)
                // V18.6.3: Use ApplicationContext for safety in background threads.
                android.content.Context appContext = (context != null) ? context.getApplicationContext() : null;
                String deviceId = com.example.openradiofm.utils.DeviceMetadataUtils.getUniqueDeviceId(appContext != null ? appContext : context);

                // Enriquecer con Stream URL si no viene definido (auto-populación de la base comunitaria)
                String finalStreamUrl = streamUrl;
                String country = java.util.Locale.getDefault().getCountry();
                if (country == null || country.isEmpty()) country = "ES";

                if ((finalStreamUrl == null || finalStreamUrl.isEmpty()) && !isNameGeneric(rdsName)) {
                    android.util.Log.d("SupabaseLogoSource", "SUPABASE ENRICH: Searching stream for " + rdsName);
                    WebRadioSource webSource = new WebRadioSource();
                    
                    com.example.openradiofm.data.source.network.model.StationSearchResponse webStation = webSource.fetchStation(freqKHz, rdsName, country);
                    if (webStation != null && webStation.getStreamUrl() != null) {
                        finalStreamUrl = webStation.getStreamUrl();
                        android.util.Log.d("SupabaseLogoSource", "SUPABASE ENRICH: Found stream -> " + finalStreamUrl);
                        
                        // También aprovechamos si no hay logoUrl para usar el favicon encontrado
                        if (finalLogoUrl == null || finalLogoUrl.isEmpty()) {
                            finalLogoUrl = webStation.getFavicon();
                        }
                    }
                }

                SupabaseLogoResponse data = new SupabaseLogoResponse(piCode, rdsName, freqStr, finalLogoUrl, finalStreamUrl, hwModel, deviceId, country);
                
                // V18.8: Filtro mejorado con isNameGeneric para evitar basura en la DB
                if (piCode == null && (rdsName == null || isNameGeneric(rdsName))) {
                    android.util.Log.d("SupabaseLogoSource", "UPSERT ABORT: Name '" + rdsName + "' is generic or null and no PI Code.");
                    return;
                }

                String conflictColumns = (piCode != null && !piCode.isEmpty()) ? "pi_code,country_code" : "ps_name,country_code";
                
                // V19.4: Cabecera 'return=minimal,resolution=merge-duplicates' para asegurar compatibilidad con PostgREST
                Call<Void> call = api.upsertLogo(apiKey, "Bearer " + apiKey, "return=minimal,resolution=merge-duplicates", conflictColumns, data);
                retrofit2.Response<Void> response = call.execute();
                
                if (response.isSuccessful()) {
                    android.util.Log.d("SupabaseLogoSource", "UPSERT SUCCESS: " + rdsName + " (" + freqStr + ")");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    android.util.Log.e("SupabaseLogoSource", "UPSERT FAILED: Code=" + response.code() + " Error=" + errorBody);
                }
            } catch (Throwable e) {
                android.util.Log.e("SupabaseLogoSource", "Error upserting logo: " + e.getMessage());
            } finally {
                notifyActivity(false);
            }
        }).start();
    }

    private String queryByPi(String pi, String country) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByPi(apiKey, "Bearer " + apiKey, "ilike." + pi, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String queryByName(String name, String country) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByName(apiKey, "Bearer " + apiKey, "ilike." + name, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String queryByFreq(int freq, String country) {
        try {
            // V19.4: Formatear frecuencia a String MHz (ej: 87.50) para coincidir con SQL
            String freqStr = String.format(java.util.Locale.US, "%.2f", freq / 1000.0);
            Call<List<SupabaseLogoResponse>> call = api.getLogosByFreq(apiKey, "Bearer " + apiKey, "eq." + freqStr, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void checkConnection(java.util.function.Consumer<Boolean> callback) {
        new Thread(() -> {
            try {
                Call<Void> call = api.ping(apiKey, "Bearer " + apiKey);
                retrofit2.Response<Void> res = call.execute();
                callback.accept(res.isSuccessful());
            } catch (Throwable e) {
                callback.accept(false);
            }
        }).start();
    }
}
