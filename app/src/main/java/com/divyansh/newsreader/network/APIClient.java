package com.divyansh.newsreader.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://newsapi.org/v2/";

    public static Retrofit getInstance() {

        if (retrofit == null) {

            // if we do not have the client ... make new client
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
