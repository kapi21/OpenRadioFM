package com.example.openradiofm.data.source.network.model;

import com.google.gson.annotations.SerializedName;

public class StationSearchResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("favicon")
    private String favicon;

    @SerializedName("url")
    private String streamUrl;

    @SerializedName("tags")
    private String tags;

    public String getName() {
        return name;
    }

    public String getFavicon() {
        return favicon;
    }

    public String getTags() {
        return tags;
    }
}
