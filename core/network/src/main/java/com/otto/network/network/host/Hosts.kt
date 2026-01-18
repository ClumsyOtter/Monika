package com.otto.network.network.host

import okhttp3.HttpUrl

internal object Hosts {
    //接口下发 需要替换的原域名 ==》接口下发的key
    val dynamicOriginalHostMap: MutableMap<String?, HostInfo?> = HashMap()

    //接口下发 已下发的数据
    val dynamicHostMap: MutableMap<String?, HttpUrl?> = HashMap()
}
