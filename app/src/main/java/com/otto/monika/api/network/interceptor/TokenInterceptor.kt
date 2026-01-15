package com.otto.monika.api.network.interceptor

import androidx.datastore.core.IOException
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.RequestBody


class TokenInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val urlBuilder = request.url.newBuilder()
        //系统参数
        val params: MutableMap<String, String> = BaseApiParameter.fillSystemParam()
        for (entry in params.entries) {
            urlBuilder.addEncodedQueryParameter(entry.key, entry.value)
        }
        //如果是post 而且是FormUrlEncoded表单则在body里也放一份
        if ("POST" == request.method) {
            val body: RequestBody? = request.body
            if (body is FormBody) {
                val formBody: FormBody = body
                val formBodyBuilder = FormBody.Builder()
                for (i in 0..<formBody.size) {
                    formBodyBuilder.add(formBody.name(i), formBody.value(i))
                }
                for (entry in params.entries) {
                    formBodyBuilder.add(entry.key, entry.value)
                }
                requestBuilder.post(formBodyBuilder.build())
            }
        }
        val newUrl: HttpUrl = urlBuilder.build()
        requestBuilder.url(newUrl)
        //系统Header参数
        try {
            val requestUrl = newUrl.toString()
            val header: MutableMap<String, String> =
                BaseApiParameter.getFilterCommonHeader(params, requestUrl)
            for (entry in header.entries) {
                requestBuilder.header(entry.key, entry.value)
            }
        } catch (e: Exception) {
            // 如果获取header失败，继续执行请求，避免拦截器导致请求失败
            e.printStackTrace()
        }
        return chain.proceed(requestBuilder.build())
    }
}