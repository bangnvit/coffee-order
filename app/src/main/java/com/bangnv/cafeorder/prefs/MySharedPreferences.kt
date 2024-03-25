package com.bangnv.cafeorder.prefs

import android.content.Context

class MySharedPreferences {

    private var mContext: Context? = null

    private constructor() {}

    constructor(mContext: Context?) {
        this.mContext = mContext
    }

    fun putLongValue(key: String?, n: Long) {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        val editor = pref.edit()
        editor.putLong(key, n)
        editor.apply()
    }

    fun getLongValue(key: String?): Long {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        return pref.getLong(key, 0)
    }

    fun putIntValue(key: String?, n: Int) {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        val editor = pref.edit()
        editor.putInt(key, n)
        editor.apply()
    }

    fun getIntValue(key: String?): Int {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        return pref.getInt(key, 0)
    }

    fun putStringValue(key: String?, s: String?) {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        val editor = pref.edit()
        editor.putString(key, s)
        editor.apply()
    }

    fun getStringValue(key: String?): String? {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        return pref.getString(key, "")
    }

    fun getStringValue(key: String?, defaultValue: String?): String? {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        return pref.getString(key, defaultValue)
    }

    fun putBooleanValue(key: String?, b: Boolean?) {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        val editor = pref.edit()
        editor.putBoolean(key, b!!)
        editor.apply()
    }

    fun getBooleanValue(key: String?): Boolean {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        return pref.getBoolean(key, false)
    }

    fun putFloatValue(key: String?, f: Float) {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        val editor = pref.edit()
        editor.putFloat(key, f)
        editor.apply()
    }

    fun getFloatValue(key: String?): Float {
        val pref = mContext!!.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0)
        return pref.getFloat(key, 0.0f)
    }

    companion object {
        private const val FRUITY_DROID_PREFERENCES = "MY_PREFERENCES"
    }
}