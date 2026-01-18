package com.otto.network.model.login.request

/**
 * 手机号验证码登录请求参数
 */
data class PhoneLoginRequest(
    val phone: String?,
    val code: String?
)

