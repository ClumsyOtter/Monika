package com.otto.monika.api.model.subscribe.response

/**
 * 批量创建订阅方案响应
 */
data class SubscribePlanCreateResponse(
    val added: Int? = null,
    val updated: Int? = null,
    val deleted: Int? = null
)

