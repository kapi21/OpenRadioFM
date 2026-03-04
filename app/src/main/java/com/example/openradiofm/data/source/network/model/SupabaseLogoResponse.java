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

    @SerializedName("rds_name")
    private String rdsName;

    @SerializedName("frequency")
    private Integer frequency;

    @SerializedName("logo_url")
    private String logoUrl;

    @SerializedName("country")
    private String country;

    public SupabaseLogoResponse() {}

    public SupabaseLogoResponse(String piCode, String rdsName, Integer frequency, String logoUrl, String country) {
        this.piCode = piCode;
        this.rdsName = rdsName;
        this.frequency = frequency;
        this.logoUrl = logoUrl;
        this.country = country;
    }

    // Getters
    public String getPiCode() { return piCode; }
    public String getRdsName() { return rdsName; }
    public Integer getFrequency() { return frequency; }
    public String getLogoUrl() { return logoUrl; }
    public String getCountry() { return country; }
}
