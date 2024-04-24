package com.bangnv.cafeorder.model

import com.bangnv.cafeorder.constant.Constant
import com.google.gson.Gson

class User {

    var email: String? = null
    var password: String? = null
//    var isAdmin = false // sẽ sửa thành type //dùng ở signin, signup, global function (go to MainActivity)
    var type = Constant.TYPE_USER_USER
    var active = true

    constructor() {}
    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }

    fun toJSon(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}