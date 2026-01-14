package com.otto.monika.common.utils

import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

object MD5Utils {
    private val hextable =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    fun byteArrayToHex(array: ByteArray): String {
        var s = ""
        for (i in array.indices) {
            val di = (array[i] + 256) and 0xFF // Make it unsigned
            s = s + hextable[(di shr 4) and 0xF] + hextable[di and 0xF]
        }
        return s
    }

    fun md5(s: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray(charset("UTF-8")))
            val messageDigest = digest.digest()
            // 将加密后的字节以十六进制形式字符串返回
            return MD5Utils.byteArrayToHex(messageDigest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun md5(inputStream: InputStream): String {
        try {
            val bytes = ByteArray(4096)
            var read = 0
            val digest = MessageDigest.getInstance("MD5")
            while ((inputStream.read(bytes).also { read = it }) != -1) {
                digest.update(bytes, 0, read)
            }
            inputStream.close()
            val messageDigest = digest.digest()
            return byteArrayToHex(messageDigest)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
            }
        }
        return ""
    }
}
