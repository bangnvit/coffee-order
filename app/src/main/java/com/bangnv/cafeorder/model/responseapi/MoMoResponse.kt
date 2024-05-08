package com.bangnv.cafeorder.model.responseapi

import com.google.gson.annotations.SerializedName

data class MoMoResponse(
    @SerializedName("partnerCode")
    val partnerCode: String? = null,

    @SerializedName("orderId")
    val orderId: String? = null,

    @SerializedName("requestId")
    val requestId: String? = null,

    @SerializedName("amount")
    val amount: Int? = null,

    @SerializedName("responseTime")
    val responseTime: Long? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("resultCode")
    val resultCode: Int? = null,

    @SerializedName("payUrl")
    val payUrl: String? = null,

    @SerializedName("deeplink")
    val deeplink: String? = null,

    @SerializedName("qrCodeUrl")
    val qrCodeUrl: String? = null
)
