package com.otto.monika.api.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings

/*
 * what:
 *   保存系统参数
 * why:
 *   应用市场合规检测 发现app频繁获取系统参数
 * how:
 *   系统参数一般都不会改变 所以获取一次后可以存起来 避免每次都向系统索取
 *
 * */
object PrivacyDataUtils {
    private var ANDROID_ID: String? = null
    private var SSID: String? = null
    private var BSSID: String? = null
    private var PHONE_MODEL: String? = null
    private var PHONE_BRAND: String? = null
    private var PHONE_SYS_VERSION: String? = null


    val phoneModel: String?
        get() {
            if (PHONE_MODEL == null) {
                PHONE_MODEL = Build.MODEL
            }
            return PHONE_MODEL
        }

    val phoneBrand: String?
        get() {
            if (PHONE_BRAND == null) {
                PHONE_BRAND = Build.BRAND
            }
            return PHONE_BRAND
        }

    val phoneSysVersion: String?
        get() {
            if (PHONE_SYS_VERSION == null) {
                PHONE_SYS_VERSION = Build.VERSION.RELEASE
            }
            return PHONE_SYS_VERSION
        }

    fun getAndroidId(context: Context?): String? {
        if (ANDROID_ID == null) {
            ANDROID_ID = getRealAndroidID(context)
        }
        return ANDROID_ID
    }


    private fun getRealAndroidID(context: Context?): String? {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getSSID(context: Context): String {
        if (SSID == null) {
            SSID = getRealSSID(context)
        }
        return SSID!!
    }

    private fun getRealSSID(context: Context): String {
        var ssid: String? = ""
        try {
            val mWifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager?
            if (mWifiManager != null) {
                val connectionInfo = mWifiManager.getConnectionInfo()
                if (connectionInfo != null) {
                    ssid = connectionInfo.getSSID()
                }
            }
        } catch (var14: Exception) {
            var14.printStackTrace()
        }
        if (ssid == null) {
            ssid = ""
        }
        return ssid
    }

    fun getBSSID(context: Context): String {
        if (BSSID == null) {
            BSSID = getRealBSSID(context)
        }
        return BSSID!!
    }

    private fun getRealBSSID(context: Context): String {
        var bssid: String? = ""

        try {
            val mWifiManager = context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE) as WifiManager?
            if (mWifiManager != null) {
                val connectionInfo = mWifiManager.getConnectionInfo()
                if (connectionInfo != null) {
                    bssid = connectionInfo.getBSSID()
                }
            }
        } catch (var14: Exception) {
            var14.printStackTrace()
        }
        if (bssid == null) {
            bssid = ""
        }
        return bssid
    }
}
