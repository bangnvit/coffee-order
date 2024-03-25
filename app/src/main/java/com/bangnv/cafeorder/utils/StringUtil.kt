package com.bangnv.cafeorder.utils

import android.util.Patterns
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

object StringUtil {
    @JvmStatic
    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) false else Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    @JvmStatic
    fun isEmpty(input: String?): Boolean {
        return input == null || input.isEmpty() || "" == input.trim { it <= ' ' }
    }

    @JvmStatic
    fun getDoubleNumber(number: Int): String {
        return if (number < 10) {
            "0$number"
        } else "" + number
    }

    // Replace Vietnamese to English (đ -> d,..), lowercase
    @JvmStatic
    fun normalizeEnglishString(str: String): String {
        val normalizedStr = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        val withoutDiacritics = pattern.matcher(normalizedStr).replaceAll("")
        return withoutDiacritics.replace("đ", "d").toLowerCase(Locale.getDefault())
    }
}