package com.bangnv.cafeorder.prefs

import android.content.Context
import com.google.gson.Gson
import com.bangnv.cafeorder.model.User
import com.bangnv.cafeorder.utils.StringUtil.isEmpty

class DataStoreManager {

    private var sharedPreferences: MySharedPreferences? = null

    companion object {
        private const val PREF_USER_INFOR = "PREF_USER_INFOR"
        private var instance: DataStoreManager? = null
        fun init(context: Context?) {
            instance = DataStoreManager()
            instance!!.sharedPreferences = MySharedPreferences(context)
        }

        fun getInstance(): DataStoreManager? {
            return if (instance != null) {
                instance
            } else {
                throw IllegalStateException("Not initialized")
            }
        }

        @JvmStatic
        var user: User?
            get() {
                val jsonUser = getInstance()!!.sharedPreferences!!.getStringValue(PREF_USER_INFOR)
                return if (!isEmpty(jsonUser)) {
                    Gson().fromJson(jsonUser, User::class.java)
                } else User()
            }
            set(user) {
                var jsonUser: String? = ""
                if (user != null) {
                    jsonUser = user.toJSon()
                }
                getInstance()!!.sharedPreferences!!.putStringValue(PREF_USER_INFOR, jsonUser)
            }
    }
}