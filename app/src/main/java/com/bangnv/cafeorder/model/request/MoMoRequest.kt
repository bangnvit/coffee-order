package com.bangnv.cafeorder.model.request

import com.google.gson.annotations.SerializedName

class MoMoRequest(
    @SerializedName("partnerCode")
    val partnerCode: String,
    @SerializedName("partnerName")
    val partnerName: String,
    @SerializedName("storeId")
    val storeId: String,
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("orderInfo")
    val orderInfo: String,
    @SerializedName("redirectUrl")
    val redirectUrl: String,
    @SerializedName("ipnUrl")
    val ipnUrl: String,
    @SerializedName("lang")
    val lang: String,
    @SerializedName("requestType")
    val requestType: String,
    @SerializedName("autoCapture")
    val autoCapture: Boolean,
    @SerializedName("extraData")
    val extraData: String,
    @SerializedName("orderGroupId")
    val orderGroupId: String,
    @SerializedName("signature")
    val signature: String
)