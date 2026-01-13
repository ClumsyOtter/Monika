package com.otto.monika.api.model.login.request

/**
 * 手机号验证请求
 * @param phone 手机号
 * @param code 验证码
 */
data class PhoneVerifyRequest(
    val phone: String?,
    val code: String?
)

