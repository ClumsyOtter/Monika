package com.otto.monika.api.network.utils

import android.util.Base64
import java.nio.charset.StandardCharsets

object NetworkUtils {

    fun base64Encode2String(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val bytes: ByteArray = input.toByteArray(StandardCharsets.UTF_8)
        if (bytes.isEmpty()) return ""
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun base64Decode(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val decode = Base64.decode(input, Base64.NO_WRAP)
        if (decode == null || decode.isEmpty()) return ""
        return String(decode, StandardCharsets.UTF_8)
    }
}
