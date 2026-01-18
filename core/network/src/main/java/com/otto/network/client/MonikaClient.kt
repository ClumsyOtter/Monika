package com.otto.network.client

import com.otto.network.MonikaApi
import com.otto.network.network.MonikaNetwork


object MonikaClient {
    val monikaApi: MonikaApi
        get() = ApiHolder.monikaApi

    private object ApiHolder {
        val monikaApi: MonikaApi = MonikaNetwork.create(MonikaApi::class.java)
    }
}