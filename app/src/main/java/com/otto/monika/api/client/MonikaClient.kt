package com.otto.monika.api.client

import com.otto.monika.api.MonikaApi


object MonikaClient {
    val monikaApi: MonikaApi
        get() = ApiHolder.monikaApi

    private object ApiHolder {
        val monikaApi: MonikaApi = MonikaData.create(MonikaApi::class.java)
    }
}