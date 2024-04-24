package com.bangnv.cafeorder.model.responseapi

import com.google.gson.annotations.SerializedName


data class ChildNotificationResponse(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("body")
    val body: String? = null
)
