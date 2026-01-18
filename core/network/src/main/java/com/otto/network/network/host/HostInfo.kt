package com.otto.network.network.host

import android.text.TextUtils
import kotlin.concurrent.Volatile

/**
 *
 * 本类仅限CLData内部使用, 所有的字段仅创建的时候赋值一次, 之后不允许再次修改
 */
internal class HostInfo(//保存正确的host
    val srcHost: String,
    val dynamicHostKey: String,
    val isNeedSystemParam: Boolean,
    val signMethod: Int
) {
    /*用于唯一标识host数据的key*/
    @Volatile
    var hostKey: String
        private set

    init {
        hostKey = generateHostKey()
    }

    /**
     * 生成一个独一无二的key, 用于保存在[Hosts.dynamicOriginalHostMap]
     * 每个字段都需要参与到key的编码中
     */
    private fun generateHostKey(): String {
        val sb = StringBuilder()
        if (!TextUtils.isEmpty(dynamicHostKey)) {
            sb.append(dynamicHostKey)
            sb.append("_")
        }
        sb.append(if (this.isNeedSystemParam) "1" else "0")
            .append("_")
            .append(signMethod)
        return sb.toString()
    }
}
