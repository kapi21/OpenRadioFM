package com.example.openradiofm.data.source.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RadioBrowserClient {
    // V16.2: Cambiado de de1 a at1 por mayor estabilidad reportada.
    // Tambien se puede usar 'all.api.radio-browser.info' que es un balanceador.
    private static final String BASE_URL = "https://at1.api.radio-browser.info/";
    private static RadioBrowserApi api;

    public static RadioBrowserApi getApi() {
        if (api == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(RadioBrowserApi.class);
        }
        return api;
    }
}
