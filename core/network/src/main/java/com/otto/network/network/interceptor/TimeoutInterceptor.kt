package com.otto.network.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.io.IOException
import java.util.concurrent.TimeUnit


class TimeoutInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var connectTimeout = chain.connectTimeoutMillis()
        var readTimeout = chain.readTimeoutMillis()
        var writeTimeout = chain.writeTimeoutMillis()
        var timeout: Timeout? = null
        val invocation = request.tag<Invocation?>(Invocation::class.java)
        if (invocation != null) {
            timeout = invocation.method().getAnnotation(Timeout::class.java)
        }
        if (timeout != null && timeout.connectTimeout > 0) {
            connectTimeout = timeout.connectTimeout
        }
        if (timeout != null && timeout.readTimeout > 0) {
            readTimeout = timeout.readTimeout
        }
        if (timeout != null && timeout.writeTimeout > 0) {
            writeTimeout = timeout.writeTimeout
        }
        return chain
            .withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .proceed(request)
    }
}
