package com.bangnv.cafeorder.database;

import androidx.annotation.NonNull;

import com.bangnv.cafeorder.model.java.Response;
import com.bangnv.cafeorder.model.request.FcmToken;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AppApi {
    @NonNull
    @POST("firebase/save-token")
    Call<Response<FcmToken>> postFcmToken(@Header("Authorization") String authHeader, @Body FcmToken fcmToken);
}
