package com.bangnv.cafeorder.model.responseapi

import com.google.gson.annotations.SerializedName

data class ChildOrderCancelDataResponse (
    @SerializedName("typeFor")
    val typeFor: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("reason")
    val reason: String? = null
)