package com.otto.monika.api.network.interceptor

import com.otto.monika.api.network.utils.RSAHelper
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import retrofit2.Invocation
import java.io.IOException
import java.nio.charset.StandardCharsets

class EncryptInterceptor(val rSAHelper: RSAHelper) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val invocation = request.tag<Invocation?>(Invocation::class.java)
        val needEncrypt = if (invocation != null) {
            invocation.method().getAnnotation(RequestBodyEncrypt::class.java) != null
        } else {
            false
        }
        if (needEncrypt) {
            val body = request.body
            if (body == null || body is MultipartBody) { //分段的body不加密
                return chain.proceed(request)
            }
            val builder = request.newBuilder()
            //加密,重新组装body
            val sink = Buffer()
            try {
                body.writeTo(sink)
                val bodyStr = sink.readString(StandardCharsets.UTF_8)
                val encryptContent: String = rSAHelper.encrypt(bodyStr)
                builder.post(encryptContent.toRequestBody())
                val newUrl = request.url.newBuilder().addQueryParameter("encrypt", "1").build()
                builder.url(newUrl)
                return chain.proceed(builder.build())
            } catch (_: Exception) {
            } finally {
                sink.close()
            }
        }
        return chain.proceed(request)
    }
}
