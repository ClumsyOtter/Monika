package com.otto.monika.api.model.subscribe.request

/**
 * 订阅用户列表请求
 * @param uid 用户ID
 * @param page 页码（从1开始）
 * @param pageSize 每页数量
 */
data class SubscribeUserListRequest(
    val uid: String?,
    val page: Int = 1,
    val pageSize: Int = 10
)

