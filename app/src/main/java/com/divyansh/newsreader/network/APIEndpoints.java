package com.divyansh.newsreader.network;

import com.divyansh.newsreader.pojo.News;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface APIEndpoints {

    @GET
    Call<News> fetchNews(@Url String url);

}
