package com.otto.monika.api.model.pay.response

import com.google.gson.annotations.SerializedName

/**
 * 模拟创建订单响应（测试用）
 * @param orderId 订单ID
 * @param callbackResult 回调结果
 */
data class TestCreateOrderResponse(
    @SerializedName("order_id")
    val orderId: String? = null,
    @SerializedName("callback_result")
    val callbackResult: String? = null
)

