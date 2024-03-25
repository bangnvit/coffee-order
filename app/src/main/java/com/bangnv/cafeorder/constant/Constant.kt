package com.bangnv.cafeorder.constant

interface Constant {
    companion object {
        const val GENERIC_ERROR = "General error, please try again later"
        const val PAGE_FACEBOOK = ""
        const val LINK_FACEBOOK = "https://www.facebook.com/bangnv26"
        const val LINK_YOUTUBE = "https://www.youtube.com/@bangnguyenvan7151"
        const val PHONE_NUMBER = "+84 356 265 666"
        const val GMAIL = "bangnv.it@gmail.com"
        const val SKYPE_ID = "bangofficial.no1_1" // "live:bangofficial.no1_1"
        const val ZALO_LINK = "https://zalo.me/0356265666"
        const val FIREBASE_URL = "https://cafeorder-f666-default-rtdb.firebaseio.com"
        const val CURRENCY = " 000 VNĐ"
        const val TYPE_PAYMENT_CASH = 1
        const val PAYMENT_METHOD_CASH = "Tiền mặt"
        const val ADMIN_EMAIL_FORMAT = "@admin.com"

        const val NEXT = "IME_ACTION_NEXT"
        const val DONE = "IME_ACTION_DONE"
        const val SEARCH = "IME_ACTION_SEARCH"

        // Key Intent
        const val KEY_INTENT_FOOD_OBJECT = "food_object"

        const val MAX_LINES = 8
    }
}