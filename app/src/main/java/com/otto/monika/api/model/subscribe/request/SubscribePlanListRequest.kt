package com.otto.monika.api.model.subscribe.request

/**
 * 订阅方案列表请求
 * @param uid 用户ID，0表示查询所有
 * @param page 页码
 * @param pageSize 每页数量
 */
data class SubscribePlanListRequest(
    val uid: String? = null,
    val page: Int = 1,
    val pageSize: Int = 10
)

