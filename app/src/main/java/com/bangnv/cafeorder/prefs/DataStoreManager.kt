package com.bangnv.cafeorder.prefs

import android.content.Context
import com.google.gson.Gson
import com.bangnv.cafeorder.model.User
import com.bangnv.cafeorder.utils.StringUtil.isEmpty

class DataStoreManager {

    private var sharedPreferences: MySharedPreferences? = null

    companion object {
        private const val PREF_USER_INFOR = "PREF_USER_INFOR"
        private const val PREF_TOKEN = "PREF_TOKEN"
        private const val PREF_TOKEN_ID = "PREF_TOKEN_ID"
        private lateinit var instance: DataStoreManager
        fun init(context: Context?) {
            instance = DataStoreManager()
            instance.sharedPreferences = MySharedPreferences(context)
        }

        fun getInstance(): DataStoreManager {
            return instance
        }

        @JvmStatic
        var user: User?
            get() {
                val jsonUser = getInstance().sharedPreferences!!.getStringValue(PREF_USER_INFOR)
                return if (!isEmpty(jsonUser)) {
                    Gson().fromJson(jsonUser, User::class.java)
                } else User()
            }
            set(user) {
                var jsonUser: String? = ""
                if (user != null) {
                    jsonUser = user.toJSon()
                }
                getInstance().sharedPreferences!!.putStringValue(PREF_USER_INFOR, jsonUser)
            }

        @JvmStatic
        var tokenId: Long?
            get() {
                return getInstance().sharedPreferences!!.getLongValue(PREF_TOKEN_ID)
            }
            set(tokenId) {
                if (tokenId != null) {
                    getInstance().sharedPreferences!!.putLongValue(PREF_TOKEN_ID, tokenId)
                }
            }

        @JvmStatic
        var token: String?
            get() {
                return getInstance().sharedPreferences!!.getStringValue(PREF_TOKEN)
            }
            set(token) {
                if (token != null) {
                    getInstance().sharedPreferences!!.putStringValue(PREF_TOKEN, token)
                }
            }

    }
}