package com.otto.monika.api.model.login.request

/**
 * 手机号绑定请求
 * @param oldPhone 旧手机号
 * @param newPhone 新手机号
 * @param code 验证码
 * @param acToken 访问令牌
 */
data class PhoneBindRequest(
    val oldPhone: String?,
    val newPhone: String?,
    val code: String?,
    val acToken: String?
)

