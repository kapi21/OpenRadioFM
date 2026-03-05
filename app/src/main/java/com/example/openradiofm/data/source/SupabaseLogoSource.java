package com.example.openradiofm.data.source;

import com.example.openradiofm.data.source.network.SupabaseApi;
import com.example.openradiofm.data.source.network.SupabaseClient;
import com.example.openradiofm.data.source.network.model.SupabaseLogoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Fuente de logos centralizada en Supabase.
 */
public class SupabaseLogoSource {
    private final SupabaseApi api;
    private final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhjaXF4dmZ2b2hjYWlhcXFydmRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2MjExNDcsImV4cCI6MjA4ODE5NzE0N30.kE5W3_qHMWMc1nKQQQn_lMb9NXOu6kFjEL5glpIhswM";

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
     */
    public void upsertLogoData(String piCode, String rdsName, int freqKHz, String logoUrl, String streamUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) return;

        new Thread(() -> {
            notifyActivity(true);
            try {
                SupabaseLogoResponse data = new SupabaseLogoResponse(piCode, rdsName, freqKHz, logoUrl, streamUrl);
                // V16.2: Añadido on_conflict para evitar duplicados. Preferimos ps_name si PI es nulo.
                String conflictColumns = (piCode != null && !piCode.isEmpty()) ? "pi_code" : "ps_name";
                
                Call<Void> call = api.upsertLogo(apiKey, "Bearer " + apiKey, "resolution=merge-duplicates", conflictColumns, data);
                retrofit2.Response<Void> response = call.execute();
                
                if (response.isSuccessful()) {
                    android.util.Log.d("SupabaseLogoSource", "UPSERT SUCCESS: " + rdsName + " (" + freqKHz + ")");
                } else {
                    android.util.Log.e("SupabaseLogoSource", "UPSERT FAILED: Code=" + response.code() + " Message=" + response.message());
                    if (response.errorBody() != null) {
                        android.util.Log.e("SupabaseLogoSource", "Error Body: " + response.errorBody().string());
                    }
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
