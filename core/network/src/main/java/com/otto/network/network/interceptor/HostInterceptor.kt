package com.otto.network.network.interceptor

import android.text.TextUtils
import com.otto.network.network.host.HostInfo
import com.otto.network.network.host.Hosts
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 接口下发拦截替换HOST
 */
internal class HostInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val scheme = originalRequest.url.scheme
        var host = originalRequest.url.host //这里host添加了key,下面要去掉
        val hostInfo: HostInfo? = Hosts.dynamicOriginalHostMap[host]
        if (hostInfo != null) {
            var dynamicUrl: HttpUrl? = null
            if (hostInfo.dynamicHostKey.isNotEmpty()) {
                val key: String = hostInfo.dynamicHostKey
                dynamicUrl = Hosts.dynamicHostMap[key]
            }
            if (dynamicUrl != null) {
                val isSame = dynamicUrl.host == host && dynamicUrl.scheme == scheme
                if (!isSame) {
                    val url: HttpUrl = originalRequest.url.newBuilder()
                        .scheme(dynamicUrl.scheme)
                        .host(dynamicUrl.host)
                        .port(dynamicUrl.port)
                        .build()
                    val compressedRequest: Request = originalRequest.newBuilder().url(url).build()
                    return chain.proceed(compressedRequest)
                }
            }
            if (!TextUtils.isEmpty(hostInfo.srcHost)) {
                host = hostInfo.srcHost
                val url: HttpUrl = originalRequest.url.newBuilder().host(host).build()
                val compressedRequest: Request = originalRequest.newBuilder().url(url).build()
                return chain.proceed(compressedRequest)
            }
        }
        return chain.proceed(originalRequest)
    }
}
