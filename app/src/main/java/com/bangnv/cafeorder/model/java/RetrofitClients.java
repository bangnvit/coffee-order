package com.bangnv.cafeorder.model.java;

import androidx.annotation.NonNull;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClients {
    private static Retrofit retrofit;
//    private static final String URL_REQUEST = "http://103.226.249.210:8239/api/"; // đã sửa theo kiểu fake
    private static final String URL_REQUEST = "http://103.226.249.210:8239/api/";
    @NonNull
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(URL_REQUEST).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}