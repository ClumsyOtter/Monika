package com.otto.monika.api.model.subscribe.response

/**
 * 订阅用户列表响应
 */
data class SubscribeUserListResponse(
    val list: List<SubscribeUserItem> = mutableListOf(),
    val total: Int = 0
)

