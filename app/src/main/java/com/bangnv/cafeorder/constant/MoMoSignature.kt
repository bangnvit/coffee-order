package com.bangnv.cafeorder.constant

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

object MoMoSignature {
    private const val SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz"

    fun generateSignature(data: String): String {
        try {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "HmacSHA256")
            sha256Hmac.init(secretKeySpec)
            val encodedData = sha256Hmac.doFinal(data.toByteArray())
            return bytesToHex(encodedData)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            result.append(String.format("%02x", b))
        }
        return result.toString()
    }
}