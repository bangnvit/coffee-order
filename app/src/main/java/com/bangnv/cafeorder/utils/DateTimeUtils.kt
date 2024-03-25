package com.bangnv.cafeorder.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    private const val DEFAULT_FORMAT_DATE = "dd-MM-yyyy,hh:mm a"
    private const val DEFAULT_FORMAT_DATE_2 = "dd/MM/yyyy"

    @JvmStatic
    fun convertTimeStampToDate(timeStamp: Long): String {
        var result = ""
        try {
            val sdf = SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.ENGLISH)
            val date = Date(timeStamp)
            result = sdf.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @JvmStatic
    fun convertTimeStampToDate_2(timeStamp: Long): String {
        var result = ""
        try {
            val sdf = SimpleDateFormat(DEFAULT_FORMAT_DATE_2, Locale.ENGLISH)
            val date = Date(timeStamp)
            result = sdf.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @JvmStatic
    fun convertDate2ToTimeStamp(strDate: String?): String {
        var result = ""
        if (strDate != null) {
            try {
                val format = SimpleDateFormat(DEFAULT_FORMAT_DATE_2, Locale.ENGLISH)
                val date = format.parse(strDate)
                if (date != null) {
                    val timestamp = date.time / 1000
                    result = timestamp.toString()
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return result
    }
}