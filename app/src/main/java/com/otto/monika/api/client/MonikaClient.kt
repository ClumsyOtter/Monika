package com.otto.monika.api.client

import com.otto.monika.api.MonikaApi
import com.otto.monika.api.network.MonikaNetwork


object MonikaClient {
    val monikaApi: MonikaApi
        get() = ApiHolder.monikaApi

    private object ApiHolder {
        val monikaApi: MonikaApi = MonikaNetwork.create(MonikaApi::class.java)
    }
}