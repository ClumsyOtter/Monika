package com.otto.monika.api.model.login.responese

/**
 * 手机号验证响应
 * @param acToken 访问令牌
 */
data class PhoneVerifyResponse(
    val acToken: String? = null
)

