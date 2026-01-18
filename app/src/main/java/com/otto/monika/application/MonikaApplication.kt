package com.otto.monika.application

import android.app.Application
import com.otto.network.network.MonikaNetwork

class MonikaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MonikaNetwork.init(applicationContext)
    }
}