package com.otto.network.model.subscribe.response

/**
 * 我的订阅用户列表响应
 */
data class MyCreatorListResponse(
    val list: List<MyCreatorItem> = emptyList(),
    val total: Int? = null
)

