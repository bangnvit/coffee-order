package com.bangnv.cafeorder.model.request

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("userEmail")
    val userEmail: String? = null,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("reason")
    val reason: String? = null
)