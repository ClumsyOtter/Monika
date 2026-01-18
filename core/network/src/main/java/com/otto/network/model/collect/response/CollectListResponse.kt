package com.otto.network.model.collect.response

/**
 * 收藏列表响应
 */
data class CollectListResponse(
    val list: List<CollectItem> = mutableListOf(),
    val total: Int = 0
)

