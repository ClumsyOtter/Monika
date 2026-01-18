package com.otto.network.network.host

/**
 * 标记那个域名
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HOST(
    val releaseUrl: String,
    val preUrl: String = "",
    val testUrl: String = "",
    val dynamicHostKey: String = "",  //域名下发
    val signMethod: Int = -1,  //签名
    val needSystemParam: Boolean = true //是否需要填充系统参数
) {
    companion object {
        const val SIGN_METHOD_PP: Int = 1
        const val SIGN_METHOD_QUERYVIOLATIONS: Int = 2
        const val SIGN_METHOD_COMMON: Int = 3
        const val SIGN_METHOD_TOKEN: Int = 4
        const val SIGN_METHOD_BAOJIA: Int = 5
        const val SIGN_METHOD_DRIVING_TEST: Int = 6
    }
}