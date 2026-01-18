package com.otto.network.network.interceptor

import android.text.TextUtils
import com.otto.common.token.TokenManager
import com.otto.common.utils.MD5Utils
import java.util.Locale
import kotlin.math.abs

object BaseApiParameter {
    /**
     * 需要签名的字段
     */
    private val HEADER_KEYS =
        mutableListOf<String>("os", "app", "appVersion", "openUDID", "time", "path", "hr")

    init {
        //签名字段排序
        HEADER_KEYS.sort<String>()
    }

    /**
     * APP接口签名密钥
     * jdk1.8写法
     */
    val APP_SIGN_KEY_MAP: MutableMap<String?, String?> = object : HashMap<String?, String?>() {
        init {
            //次芽
            put("1", "cy")
        }
    }

    fun nextInt(min: Int, max: Int): Int {
        val tmp = abs((Math.random() * 10).toInt())
        return tmp % (max - min + 1) + min
    }

    fun getFilterCommonHeader(
        params: MutableMap<String, String>,
        requestUrl: String
    ): MutableMap<String, String> {
        params["path"] =
            requestUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        params["sign"] = calcHeaderSign(params).uppercase(Locale.getDefault())
        return params
    }


    private fun calcHeaderSign(headers: MutableMap<String, String>): String {
        val builder = StringBuilder()
        for (key in HEADER_KEYS) {
            val value = headers[key]
            if (TextUtils.isEmpty(value)) {
                builder.append("null").append("_")
            } else {
                builder.append(value).append("_")
            }
        }
        val eProductidValue = headers["app"]
        if (APP_SIGN_KEY_MAP.containsKey(eProductidValue)) {
            builder.append(APP_SIGN_KEY_MAP[eProductidValue])
        }
        return MD5Utils.stringToMD5(builder.toString()) ?: ""
    }

    fun fillSystemParam(): MutableMap<String, String> {
        val params = mutableMapOf<String, String>()
        val _token: String? = TokenManager.token
        if (_token?.isNotEmpty() == true) {
            params["token"] = _token
        }
        return params
    }
}
