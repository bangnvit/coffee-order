package com.bangnv.cafeorder.model.baseresponse

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClients {

    private var retrofit: Retrofit? = null

    private const val URL_REQUEST = "https://coffeenbnode.onrender.com/"    // Deployed URL_REQUEST

//    private const val URL_REQUEST = "http://192.168.0.103:3006/"        // Mạng wifi ở nhà: URL_REQUEST

    @JvmStatic
    fun getInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(URL_REQUEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
