package com.example.openradiofm.data.source;

import com.example.openradiofm.data.source.network.RadioBrowserApi;
import com.example.openradiofm.data.source.network.RadioBrowserClient;
import com.example.openradiofm.data.source.network.model.StationSearchResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class WebRadioSource {
    private final RadioBrowserApi api;

    public WebRadioSource() {
        this.api = RadioBrowserClient.getApi();
    }

    public String fetchLogo(int freqKHz, String rdsName, String countryCode) {
        // 1. Try by RDS Name (Best Match)
        if (rdsName != null && rdsName.length() > 2) {
             String logo = search(rdsName, countryCode);
             if (logo != null) return logo;
        }

        // 2. Fallback by Frequency (e.g. "91.3")
        String freqLabel = String.format(java.util.Locale.US, "%.1f", freqKHz / 1000.0);
        return search(freqLabel, countryCode);
    }
    
    private String search(String query, String countryCode) {
        try {
            Call<List<StationSearchResponse>> call = api.searchStations(query, countryCode, 5);
            Response<List<StationSearchResponse>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                for (StationSearchResponse station : response.body()) {
                    if (station.getFavicon() != null && !station.getFavicon().isEmpty()) {
                        return station.getFavicon();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
