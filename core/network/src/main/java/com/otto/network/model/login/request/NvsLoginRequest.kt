package com.otto.network.model.login.request

/**
 * 一键登录登录请求参数
 */
data class NvsLoginRequest(
    val phone: String?,
    val salt: String?
)

