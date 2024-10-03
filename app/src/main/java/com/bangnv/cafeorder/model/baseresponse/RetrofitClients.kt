package com.bangnv.cafeorder.model.baseresponse

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClients {

    // Mạng wifi ở nhà: URL_REQUEST
//    private const val URL_REQUEST = "http://192.168.0.103:3006/"
//    private const val URL_REQUEST = "http://192.168.0.102:3006/"

    // Deployed URL_REQUEST
    private const val URL_REQUEST = "https://coffeenbnode.onrender.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_REQUEST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @JvmStatic
    fun getInstance(): Retrofit = retrofit

    @JvmStatic
    fun getMoMoInstance(): Retrofit {
        val moMoUrl = "https://test-payment.momo.vn/"
        return Retrofit.Builder()
            .baseUrl(moMoUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
