package com.example.openradiofm.data.source.network;

import com.example.openradiofm.data.source.network.model.SupabaseLogoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {
    @GET("rest/v1/")
    Call<Void> ping(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth
    );

    @GET("rest/v1/stations")
    Call<List<SupabaseLogoResponse>> getLogosByPi(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Query("pi_code") String piFilter,
            @Query("select") String select
    );

    @GET("rest/v1/stations")
    Call<List<SupabaseLogoResponse>> getLogosByName(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Query("ps_name") String nameFilter,
            @Query("select") String select
    );

    @GET("rest/v1/stations")
    Call<List<SupabaseLogoResponse>> getLogosByFreq(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Query("frequency") String freqFilter,
            @Query("select") String select
    );

    @Headers({"Content-Type: application/json"})
    @POST("rest/v1/stations")
    Call<Void> upsertLogo(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Header("Prefer") String prefer, // return=minimal,resolution=merge-duplicates
            @Query("on_conflict") String onConflict,
            @retrofit2.http.Body SupabaseLogoResponse data
    );

    @Headers({"x-upsert: true"})
    @POST("storage/v1/object/station-logos/{path}")
    Call<Void> uploadLogoFile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @retrofit2.http.Path(value = "path", encoded = true) String fileName,
            @retrofit2.http.Body okhttp3.RequestBody file
    );
}
