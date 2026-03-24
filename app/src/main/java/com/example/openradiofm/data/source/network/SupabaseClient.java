package com.example.openradiofm.data.source.network;

import com.example.openradiofm.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Cliente Retrofit para Supabase.
 */
public class SupabaseClient {
    private static final String BASE_URL = "https://hciqxvfvohcaiaqqrvdq.supabase.co/";
    private static SupabaseApi api;

    public static synchronized SupabaseApi getApi() {
        if (api == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(SupabaseApi.class);
        }
        return api;
    }
}
