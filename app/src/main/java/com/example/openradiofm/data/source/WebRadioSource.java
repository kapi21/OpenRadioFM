package com.example.openradiofm.data.source;

import com.example.openradiofm.data.source.network.RadioBrowserApi;
import com.example.openradiofm.data.source.network.RadioBrowserClient;
import com.example.openradiofm.data.source.network.model.StationSearchResponse;
import android.util.Log;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class WebRadioSource {
    private static final String TAG = "WebRadioSource";
    private final RadioBrowserApi api;

    public WebRadioSource() {
        this.api = RadioBrowserClient.getApi();
    }

    public String fetchLogo(int freqKHz, String rdsName, String countryCode) {
        StationSearchResponse station = fetchStation(freqKHz, rdsName, countryCode);
        return station != null ? station.getFavicon() : null;
    }

    public StationSearchResponse fetchStation(int freqKHz, String rdsName, String countryCode) {
        // 1. Try by RDS Name (Best Match)
        if (rdsName != null && rdsName.length() > 2) {
             StationSearchResponse station = search(rdsName, countryCode);
             if (station != null) return station;
        }

        // 2. Fallback by Frequency (e.g. "91.3")
        String freqLabel = String.format(java.util.Locale.US, "%.1f", freqKHz / 1000.0);
        return search(freqLabel, countryCode);
    }
    
    private StationSearchResponse search(String query, String countryCode) {
        try {
            Call<List<StationSearchResponse>> call = api.searchStations(query, countryCode, 10);
            Response<List<StationSearchResponse>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                for (StationSearchResponse station : response.body()) {
                    // Si buscamos por nombre, intentamos que coincida lo mejor posible
                    return station; 
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "search query=" + query + ", country=" + countryCode, e);
        }
        return null;
    }
}
