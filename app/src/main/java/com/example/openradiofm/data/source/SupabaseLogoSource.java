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
    private final String storageUrl = "https://hciqxvfvohcaiaqqrvdq.supabase.co/storage/v1/object/public/logos/";

    public SupabaseLogoSource() {
        this.api = SupabaseClient.getApi();
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
        android.util.Log.d("SupabaseLogoSource", "notifyActivity: active=" + active);
        if (mActivityListener != null) {
            mActivityListener.onDataActivity(active);
        }
    }
    /**
     * Busca el logo en Supabase siguiendo el orden de prioridad.
     * V16.2: Ahora filtra también por país si está disponible.
     */
    public String fetchLogo(String piCode, String rdsName, int freqKHz) {
        notifyActivity(true);
        android.util.Log.d("SupabaseLogoSource", "FETCH START: PI=" + piCode + ", Name=" + rdsName + ", Freq=" + freqKHz);
        try {
            // 1. prioridad: PI Code (exacto)
            if (piCode != null && !piCode.isEmpty()) {
                String logo = queryByPi(piCode);
                if (logo != null) {
                    android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (PI): " + logo);
                    return logo;
                }
            }

            // 2. prioridad: RDS Name (exacto)
            if (rdsName != null && !rdsName.isEmpty()) {
                String logo = queryByName(rdsName.trim());
                if (logo != null) {
                    android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (Name): " + logo);
                    return logo;
                }
            }

            // 3. prioridad: Frecuencia (kHz)
            String logoFreq = queryByFreq(freqKHz);
            if (logoFreq != null) {
                android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (Freq): " + logoFreq);
            } else {
                android.util.Log.d("SupabaseLogoSource", "FETCH EMPTY: No logo found for this station.");
            }
            return logoFreq;
        } catch (Exception e) {
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
        if (logoUrl == null || logoUrl.isEmpty()) return;

        new Thread(() -> {
            notifyActivity(true);
            try {
                String finalLogoUrl = logoUrl;
                String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                // Si el logo es local, primero lo subimos al Storage de Supabase
                if (logoUrl.startsWith("file://")) {
                    String localPath = logoUrl.substring(7);
                    File file = new File(localPath);
                    if (file.exists()) {
                        String fileName = (piCode != null ? piCode : (rdsName != null ? rdsName.replaceAll("[^a-zA-Z0-9]", "") : "station")) 
                                + "_" + freqKHz + ".png";
                        
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
                        Call<Void> uploadCall = api.uploadLogoFile(apiKey, "Bearer " + apiKey, fileName, requestBody);
                        Response<Void> uploadRes = uploadCall.execute();
                        
                        if (uploadRes.isSuccessful() || uploadRes.code() == 409) { // 409 duplicated is okay for us
                            finalLogoUrl = storageUrl + fileName;
                            android.util.Log.d("SupabaseLogoSource", "UPLOAD SUCCESS: " + finalLogoUrl);
                        } else {
                            android.util.Log.e("SupabaseLogoSource", "UPLOAD FAILED: " + uploadRes.code());
                            return; // No seguimos si falla la subida de la imagen
                        }
                    }
                }

                SupabaseLogoResponse data = new SupabaseLogoResponse(piCode, rdsName, freqKHz, finalLogoUrl, streamUrl, deviceId);
                // V16.2: Añadido on_conflict para evitar duplicados. Preferimos ps_name si PI es nulo.
                // V17.5: Evitar subir nombres genéricos o demasiado cortos que ensucian la base
                if (piCode == null && (rdsName == null || rdsName.length() < 3 || rdsName.equals("BUSCANDO") || rdsName.contains("..."))) {
                    return;
                }

                String conflictColumns = (piCode != null && !piCode.isEmpty()) ? "pi_code" : "ps_name";
                
                Call<Void> call = api.upsertLogo(apiKey, "Bearer " + apiKey, "resolution=merge-duplicates", conflictColumns, data);
                retrofit2.Response<Void> response = call.execute();
                
                if (response.isSuccessful()) {
                    android.util.Log.d("SupabaseLogoSource", "UPSERT SUCCESS: " + rdsName + " (" + freqKHz + ")");
                } else {
                    android.util.Log.e("SupabaseLogoSource", "UPSERT FAILED: Code=" + response.code() + " Message=" + response.message());
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseLogoSource", "Error upserting logo: " + e.getMessage());
            } finally {
                notifyActivity(false);
            }
        }).start();
    }

    private String queryByPi(String pi) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByPi(apiKey, "Bearer " + apiKey, "ilike." + pi, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String queryByName(String name) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByName(apiKey, "Bearer " + apiKey, "ilike." + name, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String queryByFreq(int freq) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByFreq(apiKey, "Bearer " + apiKey, "eq." + freq, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
