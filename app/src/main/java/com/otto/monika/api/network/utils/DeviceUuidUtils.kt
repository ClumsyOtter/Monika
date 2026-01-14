package com.otto.monika.api.network.utils

import android.content.Context
import android.provider.Settings.Secure
import android.text.TextUtils
import com.otto.monika.api.network.utils.AndroidUtils.uniqueID
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * 最大限度产生唯一设备号
 */
object DeviceUuidUtils {
    var deviceUuid: UUID? = null
    const val PREFS_FILE: String = "device_id"
    const val PREFS_DEVICE_ID: String = "device_id"
    fun getDeviceUuid(context: Context?): UUID? {
        if (deviceUuid == null) {
            synchronized(DeviceUuidUtils::class.java) {
                if (deviceUuid == null) {
                    val prefs = context?.getSharedPreferences(PREFS_FILE, 0)
                    val id = prefs?.getString(PREFS_DEVICE_ID, null)
                    if (id != null) {
                        // Use the ids previously computed and stored in the
                        // prefs file
                        deviceUuid = UUID.fromString(id)
                    } else {
                        val identity = getIdentityString(context)
                        try {
                            deviceUuid = if (!TextUtils.isEmpty(identity)) {
                                UUID.nameUUIDFromBytes(
                                    identity.toByteArray(
                                        StandardCharsets.UTF_8
                                    )
                                )
                            } else {
                                UUID.randomUUID()
                            }
                        } catch (_: Exception) { }

                        if (deviceUuid == null) {
                            deviceUuid = UUID.randomUUID()
                        }
                        // Write the value out to the prefs file
                        prefs?.edit()?.putString(PREFS_DEVICE_ID, deviceUuid.toString())?.commit()
                    }
                }
            }
        }
        return deviceUuid
    }


    /**
     * 获取用于生成uuid的身份标识字符串
     */
    private fun getIdentityString(context: Context?): String {
        // 优先获取android id
        val androidId = Secure.getString(context?.contentResolver, Secure.ANDROID_ID)
        if (!TextUtils.isEmpty(androidId) && ("9774d56d682e549c" != androidId) && ("0000000000000000" != androidId)) {
            return androidId
        }
        // 最后获取oaid
        return uniqueID
    }

}
