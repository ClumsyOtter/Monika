package com.otto.monika.api.model.user.request

/**
 * 编辑用户信息请求参数
 */
data class EditUserRequest(
    val nickname: String? = null, // 昵称
    val avatar: String? = null, // 头像URL
    val intro: String? = null //用户个人简介
)

