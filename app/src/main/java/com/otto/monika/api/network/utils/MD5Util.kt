package com.otto.monika.api.network.utils

import android.text.TextUtils
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

/**
 * 功能：MD5加密工具类
 */
object MD5Util {
    fun stringToMD5(string: String): String? {
        val hash: ByteArray?
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if ((b.toInt() and 0xFF) < 0x10) hex.append("0")
            hex.append(Integer.toHexString(b.toInt() and 0xFF))
        }
        return hex.toString()
    }

    /**
     * 1.对文本进行32位小写MD5加密
     *
     * @param plainText
     * 要进行加密的文本
     * @return 加密后的内容
     */
    fun textToMD5L32(plainText: String?): String? {
        var result: String? = null
        // 首先判断是否为空
        if (TextUtils.isEmpty(plainText)) {
            return null
        }

        try {
            // 首先进行实例化和初始化
            val md = MessageDigest.getInstance("MD5")
            // 得到一个操作系统默认的字节编码格式的字节数组
            val btInput = plainText!!.toByteArray()
            // 对得到的字节数组进行处理
            md.update(btInput)
            // 进行哈希计算并返回结果
            val btResult = md.digest()
            // 进行哈希计算后得到的数据的长度
            val sb = StringBuffer()

            for (b in btResult) {
                val bt = b.toInt() and 0xff
                if (bt < 16) {
                    sb.append(0)
                }
                sb.append(Integer.toHexString(bt))
            }

            result = sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return result
    }

    /**
     * 2.对文本进行32位MD5大写加密
     *
     * @param plainText
     * 要进行加密的文本
     * @return 加密后的内容
     */
    fun textToMD5U32(plainText: String?): String? {
        if (TextUtils.isEmpty(plainText)) {
            return null
        }

        val result = textToMD5L32(plainText)

        return result!!.uppercase(Locale.getDefault())
    }

    /**
     * 3.对文本进行16位MD5小写加密
     *
     * @param plainText
     * 需要进行加密的文本
     */
    fun textToMD5L16(plainText: String?): String? {
        if (TextUtils.isEmpty(plainText)) {
            return null
        }

        val result = textToMD5L32(plainText)

        return result!!.substring(8, 24)
    }

    /**
     * 4.对文本进行16位MD5大写加密
     *
     * @param plainText
     * 需要进行加密的文本
     * @return
     */
    fun textToMD5U16(plainText: String?): String? {
        if (TextUtils.isEmpty(plainText)) {
            return null
        }

        val result = textToMD5L32(plainText)

        return result!!.uppercase(Locale.getDefault()).substring(8, 24)
    }
}
