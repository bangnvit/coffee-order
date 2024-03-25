package com.bangnv.cafeorder.model

class Contact(var id: Int, var image: Int) {

    companion object {
        const val FACEBOOK = 0
        const val HOTLINE = 1
        const val GMAIL = 2
        const val SKYPE = 3
        const val YOUTUBE = 4
        const val ZALO = 5
    }
}