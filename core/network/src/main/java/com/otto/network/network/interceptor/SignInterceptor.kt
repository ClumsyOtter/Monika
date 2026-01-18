package com.otto.network.network.interceptor

import android.os.Build
import com.otto.common.utils.AndroidUtils
import com.otto.common.utils.CryptoUtils
import com.otto.common.utils.DeviceUuidUtils
import com.otto.common.utils.NetworkUtils
import com.otto.common.utils.PrivacyDataUtils
import com.otto.network.network.MonikaNetwork
import com.otto.network.network.constant.ApiConstant
import com.otto.network.network.host.HostInfo
import com.otto.network.network.host.Hosts
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * 对已知域名增加sign
 */
internal class SignInterceptor() : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val host = originalRequest.url.host
        val hostInfo: HostInfo? = Hosts.dynamicOriginalHostMap[host]
        var needSystemParam: Boolean
        var signMethod = -1
        if (hostInfo != null) {
            needSystemParam = hostInfo.isNeedSystemParam
            signMethod = hostInfo.signMethod
        } else {
            //read from header
            needSystemParam = "1" == originalRequest.header("needSystemParam")
            try {
                val signMethodFromHeader = originalRequest.header("signMethod")
                signMethod = (signMethodFromHeader ?: "-1").toInt()
            } catch (_: Exception) {
            }
        }
        //有些域名不需要系统参数和sign
        if (!needSystemParam && signMethod == -1) {
            return chain.proceed(originalRequest)
        }
        val builder = originalRequest.url.newBuilder()
        //system param
        var newUrl: HttpUrl?
        var newFormBody: FormBody? = null
        if (needSystemParam) {
            newUrl = builder.addQueryParameter(ApiConstant.API_APP, app)
                .addQueryParameter(ApiConstant.API_OS, OS)
                .addQueryParameter(ApiConstant.API_OS_VERSION, systemVersion)
                .addQueryParameter(ApiConstant.API_DEVICE_ID, openUDID)
                .addQueryParameter(ApiConstant.API_DEVICE_MODEL, model)
                .addQueryParameter(ApiConstant.API_APP_VERSION, appVersion)
                .addQueryParameter(ApiConstant.API_DEVICE_BRAND, brand)
                .addQueryParameter(ApiConstant.API_ANDROID_ID, androidId)
                .addQueryParameter(ApiConstant.API_PACKAGE, packageName).build()
            //如果是post 而且是FormUrlEncoded表单则在body里也放一份系统参数
            if ("POST" == originalRequest.method) {
                val body: RequestBody? = originalRequest.body
                if (body is FormBody) {
                    val formBody: FormBody = body
                    val formBodyBuilder = FormBody.Builder()
                    for (i in 0..<formBody.size) {
                        formBodyBuilder.add(formBody.name(i), formBody.value(i))
                    }
                    app.let { formBodyBuilder.add("app", it) }
                    formBodyBuilder.add("os", OS)
                    formBodyBuilder.add("osVersion", systemVersion)
                    openUDID?.let { formBodyBuilder.add("deviceId", it) }
                    model?.let { formBodyBuilder.add("deviceModel", it) }
                    appVersion?.let { formBodyBuilder.add("appVersion", it) }
                    brand?.let { formBodyBuilder.add("deviceBrand", it) }
                    androidId.let { formBodyBuilder.add("androidId", it) }
                    packageName?.let { formBodyBuilder.add("package", it) }
                    newFormBody = formBodyBuilder.build()
                }
            }
        } else {
            newUrl = builder.build()
        }
        //sign
        if (signMethod > 0) {
            val sign: String?
            if ("POST" == originalRequest.method) {
                val sink = Buffer()
                if (newFormBody != null) {
                    newFormBody.writeTo(sink)
                } else if (originalRequest.body != null) {
                    originalRequest.body!!.writeTo(sink)
                }
                sign = genericPostSign(sink)
            } else {
                sign = genericGetSign()
            }
            if (sign.isNotEmpty()) {
                newUrl = builder.addQueryParameter("sign", sign).build()
            }
        }
        val compressedRequestBuilder = originalRequest.newBuilder()
        compressedRequestBuilder.url(newUrl)
        if (newFormBody != null) {
            compressedRequestBuilder.post(newFormBody)
        }
        return chain.proceed(compressedRequestBuilder.build())
    }

    /**
     * 生成post sign
     */
    private fun genericPostSign( sink: Buffer): String {
        try {
            val sign1 = genericGetSign()
            val sign2 = nativeSign()
            return CryptoUtils.HASH.md5(sign1 + sign2)
        } catch (_: Exception) {
        } finally {
            sink.close()
        }
        return ""
    }

    //生成get sign
    private fun genericGetSign(): String {
        try {
            return nativeSign()
        } catch (_: Exception) {
        }
        return ""
    }

    private fun nativeSign(): String {
        return AndroidUtils.uniqueID
    }

    companion object {
        private const val OS = "Android"
        private val appVersion: String?
            /**
             * 获取应用版本号
             */
            get() = AndroidUtils.getAppVersionName(MonikaNetwork.appContext)

        private val openUDID: String?
            /**
             * 获取设备唯一标识
             */
            get() = DeviceUuidUtils.getDeviceUuid(MonikaNetwork.appContext)?.toString()

        private val app: String
            /**
             * 获取应用标识
             */
            get() = AndroidUtils.getApp(MonikaNetwork.appContext)

        private val systemVersion: String
            /**
             * 获取系统版本
             */
            get() = Build.VERSION.RELEASE

        private val model: String?
            /**
             * 获取设备型号
             */
            get() = PrivacyDataUtils.phoneModel

        private val brand: String?
            /**
             * 获取设备品牌
             */
            get() = PrivacyDataUtils.phoneBrand

        private val packageName: String?
            /**
             * 获取包名
             */
            get() = MonikaNetwork.appContext?.packageName

        private val androidId: String
            /**
             * 获取Android ID（Base64编码）
             */
            get() = NetworkUtils.base64Encode2String(PrivacyDataUtils.getAndroidId(MonikaNetwork.appContext))

        val systemParams: MutableMap<String, String?>
            /**
             * 获取所有系统参数Map，供外部使用
             * @return 包含所有系统参数的Map，key为参数名，value为参数值
             */
            get() {
                val params: MutableMap<String, String?> = mutableMapOf()
                params[ApiConstant.API_APP] = app
                params[ApiConstant.API_OS] = OS
                params[ApiConstant.API_OS_VERSION] = systemVersion
                params[ApiConstant.API_DEVICE_ID] = openUDID
                params[ApiConstant.API_DEVICE_MODEL] = model
                params[ApiConstant.API_APP_VERSION] = appVersion
                params[ApiConstant.API_DEVICE_BRAND] = brand
                params[ApiConstant.API_ANDROID_ID] = androidId
                params[ApiConstant.API_PACKAGE] = packageName
                return params
            }

        val systemParamsArray: Array<String?>
            /**
             * 获取所有系统参数数组，供外部使用
             * @return 包含所有系统参数的数组，格式为 [key1, value1, key2, value2, ...]
             */
            get() {
                val params: MutableMap<String, String?> =
                    systemParams
                val array =
                    arrayOfNulls<String>(params.size * 2)
                var index = 0
                for (entry in params.entries) {
                    array[index++] = entry.key
                    array[index++] = entry.value
                }
                return array
            }
    }
}
