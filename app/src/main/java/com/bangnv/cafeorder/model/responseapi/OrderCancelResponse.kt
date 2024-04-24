package com.bangnv.cafeorder.model.responseapi

import com.google.gson.annotations.SerializedName


data class OrderCancelResponse(
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("notification")
    val notification: ChildNotificationResponse? = null,
    @SerializedName("data")
    val data: ChildOrderCancelDataResponse? = null
)