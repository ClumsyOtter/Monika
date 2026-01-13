package com.otto.monika.api.model.pay.request

/**
 * 创建订单请求
 * @param subscribePlanId 订阅方案ID
 * @param duration 订阅期限（月数）
 */
data class CreateOrderRequest(
    val subscribePlanId: String?,
    val duration: Int?
)

