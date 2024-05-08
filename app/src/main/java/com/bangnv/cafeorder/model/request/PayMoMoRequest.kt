package com.bangnv.cafeorder.model.request

import com.google.gson.annotations.SerializedName

data class PayMoMoRequest(
//    @SerializedName("userEmail")
//    val userEmail: String? = null,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("totalPrice")
    val totalPrice: String


)
