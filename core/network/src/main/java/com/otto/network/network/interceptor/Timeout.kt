package com.otto.network.network.interceptor

/**
 * 对单独接口设置超时时间
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Timeout(
    /**
     * 连接超时时间 单位: 毫秒
     * 只接受大于0的值
     */
    val connectTimeout: Int = 0,
    /**
     * 读超时时间 单位: 毫秒
     * 只接收大于0的值
     */
    val readTimeout: Int = 0,
    /**
     * 写超时时间 单位: 毫秒
     * 只接受大于0的值
     */
    val writeTimeout: Int = 0
)
