package com.otto.monika.api.network.interceptor

/**
 * 表示接口Body是否需要加密的
 * 接口interface上添加了该注解表示需要加密 否则不需要
 * 只对post且不分段的body进行加密
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestBodyEncrypt 
