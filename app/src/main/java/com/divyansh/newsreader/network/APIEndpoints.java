package com.divyansh.newsreader.network;

import com.divyansh.newsreader.pojo.News;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface APIEndpoints {

    public final static String APIKEY = "7eb04dd3f8df465e93fcf6cb5e3eef93";

    @GET("/v2/top-headlines")
    Call<News> fetchNews(@QueryMap() Map<String, String> options);

}
