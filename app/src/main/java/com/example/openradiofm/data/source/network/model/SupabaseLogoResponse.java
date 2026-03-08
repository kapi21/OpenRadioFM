package com.example.openradiofm.data.source.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de respuesta para la tabla de logos en Supabase.
 */
public class SupabaseLogoResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("pi_code")
    private String piCode;

    @SerializedName("ps_name")
    private String psName;

    @SerializedName("frequency")
    private Integer frequency;

    @SerializedName("logo_url")
    private String logoUrl;

    @SerializedName("stream_url")
    private String streamUrl;

    @SerializedName("user_id")
    private String userId;

    public SupabaseLogoResponse() {}

    public SupabaseLogoResponse(String piCode, String psName, Integer frequency, String logoUrl) {
        this.piCode = piCode;
        this.psName = psName;
        this.frequency = frequency;
        this.logoUrl = logoUrl;
    }

    public SupabaseLogoResponse(String piCode, String psName, Integer frequency, String logoUrl, String streamUrl) {
        this(piCode, psName, frequency, logoUrl);
        this.streamUrl = streamUrl;
    }

    public SupabaseLogoResponse(String piCode, String psName, Integer frequency, String logoUrl, String streamUrl, String userId) {
        this(piCode, psName, frequency, logoUrl, streamUrl);
        this.userId = userId;
    }

    // Getters
    public String getPiCode() { return piCode; }
    public String getPsName() { return psName; }
    public Integer getFrequency() { return frequency; }
    public String getLogoUrl() { return logoUrl; }
    public String getStreamUrl() { return streamUrl; }
    public String getUserId() { return userId; }
}
