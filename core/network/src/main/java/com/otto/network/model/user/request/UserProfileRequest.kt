package com.otto.network.model.user.request

/**
 * 获取用户信息请求参数
 * @param uid 可选的用户ID，如果传了则查询指定用户，不传则查询当前登录用户
 */
data class UserProfileRequest(
    val uid: String? = null
)

