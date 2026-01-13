package com.otto.monika.api.model.subscribe.response

/**
 * 我的订阅用户列表响应
 */
data class MyCreatorListResponse(
    val list: List<MyCreatorItem> = emptyList(),
    val total: Int? = null
)

