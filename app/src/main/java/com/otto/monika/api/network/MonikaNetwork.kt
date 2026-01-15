package com.otto.monika.api.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.otto.monika.api.network.host.HOST
import com.otto.monika.api.network.host.HostInfo
import com.otto.monika.api.network.host.Hosts
import com.otto.monika.api.network.interceptor.NetworkConfiguration
import com.otto.monika.api.network.interceptor.EncryptInterceptor
import com.otto.monika.api.network.interceptor.HostInterceptor
import com.otto.monika.api.network.interceptor.SignInterceptor
import com.otto.monika.api.network.interceptor.TimeoutInterceptor
import com.otto.monika.api.network.interceptor.TokenInterceptor
import com.otto.monika.api.network.utils.RSAHelper
import okhttp3.Dispatcher
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MonikaNetwork {
    var appContext: Context? = null
    private var retrofitBuilder: Retrofit.Builder? = null
    var isInit: Boolean = false //是否初始化了,需要先请求接口下发
        private set
    private var env = 2 //接口环境

    fun init(context: Context) {
        init(NetworkConfiguration.Builder().context(context).build())
    }


    fun init(configuration: NetworkConfiguration) {
        appContext = configuration.context
        env = configuration.env
        val dispatcher = Dispatcher()
        if (retrofitBuilder == null) {
            retrofitBuilder = createRetrofitBuild(dispatcher, configuration)
        }
        if (!isInit) {
            isInit = true
        }
    }

    @Synchronized
    fun <T> create(service: Class<T>): T {
        checkNotNull(appContext) { "没有调用init初始化" }
        val hostAnnotation: HOST? = service.getAnnotation(HOST::class.java)
        checkNotNull(hostAnnotation) { service.getName() + "需要定义@HOST" }
        val baseUrl = when (env) {
            1 -> {
                hostAnnotation.preUrl
            }

            2 -> {
                hostAnnotation.testUrl
            }

            else -> {
                hostAnnotation.releaseUrl
            }
        }

        var baseHttpUrl: HttpUrl? = baseUrl.toHttpUrlOrNull()
        checkNotNull(baseHttpUrl) { service.getName() + "没有找到对应环境Host baseUrl" }
        val hostInfo = HostInfo(
            baseHttpUrl.host,
            hostAnnotation.dynamicHostKey,
            hostAnnotation.needSystemParam,
            hostAnnotation.signMethod
        )
        val newHost: String = baseHttpUrl.host + "." + hostInfo.hostKey
        baseHttpUrl = baseHttpUrl.newBuilder().host(newHost).build()
        Hosts.dynamicOriginalHostMap[baseHttpUrl.host] = hostInfo
        return retrofitBuilder!!.baseUrl(baseHttpUrl).build().create(service)
    }

    private fun createRetrofitBuild(
        dispatcher: Dispatcher,
        configuration: NetworkConfiguration
    ): Retrofit.Builder {
        val clientBuilder = OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
        if (configuration.interceptors != null) {
            for (i in 0..<configuration.interceptors.size) {
                clientBuilder.addInterceptor(configuration.interceptors[i])
            }
        }
        clientBuilder.addInterceptor(TokenInterceptor())
        //统一加密处理
        var rsaHelper: RSAHelper? = null
        if (configuration.rsaCertificateAssetFileName != null) {
            rsaHelper =
                RSAHelper(appContext!!.assets, configuration.rsaCertificateAssetFileName)
        } else if (configuration.rsaCertificateFilePath != null) {
            rsaHelper = RSAHelper(configuration.rsaCertificateFilePath)
        }
        if (rsaHelper != null) {
            clientBuilder.addInterceptor(EncryptInterceptor(rsaHelper))
        }
        clientBuilder
            .addInterceptor(SignInterceptor())
            .addInterceptor(HostInterceptor())
            .addInterceptor(TimeoutInterceptor())
        //Add network interceptors
        if (configuration.networkInterceptors != null && !configuration.networkInterceptors.isEmpty()) {
            for (i in 0..<configuration.networkInterceptors.size) {
                clientBuilder.addNetworkInterceptor(configuration.networkInterceptors.get(i))
            }
        }
        val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(clientBuilder.build())
        return retrofitBuilder
    }
}
