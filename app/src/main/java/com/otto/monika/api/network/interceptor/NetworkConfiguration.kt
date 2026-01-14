package com.otto.monika.api.network.interceptor

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Interceptor

class NetworkConfiguration private constructor(builder: Builder) {
    val context: Context?
    val env: Int
    val interceptors: MutableList<Interceptor>?
    val networkInterceptors: MutableList<Interceptor>?
    val gsonBuilder: GsonBuilder?
    val rsaCertificateAssetFileName: String?
    val rsaCertificateFilePath: String?
    val noCacheParams: MutableList<String?>?

    init {
        this.context = builder.context
        this.env = builder.env
        this.interceptors = builder.interceptors
        this.networkInterceptors = builder.networkInterceptors
        this.gsonBuilder = builder.gsonBuilder
        this.rsaCertificateAssetFileName = builder.rsaCertificateAssetFileName
        this.rsaCertificateFilePath = builder.rsaCertificateFilePath
        this.noCacheParams = builder.noCacheParams
    }

    class Builder {
        var context: Context? = null
        var env = 0
        var interceptors: MutableList<Interceptor>? = null
        var networkInterceptors: MutableList<Interceptor>? = null
        var gsonBuilder: GsonBuilder? = null
        var rsaCertificateAssetFileName: String? = null
        var rsaCertificateFilePath: String? = null
        val noCacheParams: MutableList<String?> = ArrayList()

        fun context(context: Context): Builder {
            this.context = context
            return this
        }

        fun env(env: Int): Builder {
            if (!(env == 0 || env == 1 || env == 2)) {
                throw NullPointerException("env wrong")
            }
            this.env = env
            return this
        }

        fun interceptors(interceptors: MutableList<Interceptor>?): Builder {
            this.interceptors = interceptors
            return this
        }

        fun networkInterceptors(networkInterceptors: MutableList<Interceptor>?): Builder {
            this.networkInterceptors = networkInterceptors
            return this
        }

        fun gsonBuilder(gsonBuilder: GsonBuilder?): Builder {
            this.gsonBuilder = gsonBuilder
            return this
        }

        //RSAHelper
        fun rsaCertificateAssetFileName(assetFileName: String): Builder {
            this.rsaCertificateAssetFileName = assetFileName
            return this
        }

        fun rsaCertificateFilePath(filePath: String): Builder {
            this.rsaCertificateFilePath = filePath
            return this
        }

        /**
         * 如果请求参数中带有该`param`, 则不对该接口进行缓存
         * @param param 不进行缓存的参数
         */
        fun noCacheFor(param: String?): Builder {
            noCacheParams.add(param)
            return this
        }

        /**
         * 如果请求的参数中有在`params`中的, 则不对该接口进行缓存
         * @param params 不进行缓存的参数列表
         */
        fun noCacheFor(params: MutableList<String?>): Builder {
            noCacheParams.addAll(params)
            return this
        }

        fun build(): NetworkConfiguration {
            checkNotNull(context) { "context == null" }
            return NetworkConfiguration(this)
        }
    }
}
