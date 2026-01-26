package com.example.openradiofm.data.source.network;

import com.example.openradiofm.data.source.network.model.StationSearchResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RadioBrowserApi {
    @GET("json/stations/search")
    Call<List<StationSearchResponse>> searchStations(
            @Query("name") String name,
            @Query("countrycode") String countryCode,
            @Query("limit") int limit);
}
