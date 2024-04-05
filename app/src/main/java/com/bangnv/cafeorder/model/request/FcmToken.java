package com.bangnv.cafeorder.model.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FcmToken {
    @SerializedName("fcm_token")
    @Expose
    private String fcmToken;

    public FcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }


}
