package com.bangnv.cafeorder.model.baseresponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MyResponse<T> {

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: T? = null

    @SerializedName("optional")
    @Expose
    var optional: Any? = null
}
