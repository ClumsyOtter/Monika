package com.otto.monika.api.model.pay.request

/**
 * 模拟创建订单请求（测试用）
 * @param uid 用户ID
 * @param subscribePlanId 订阅方案ID
 * @param duration 订阅期限（月数）
 */
data class TestCreateOrderRequest(
    val uid: String?,
    val subscribePlanId: String?,
    val duration: Int?
)

