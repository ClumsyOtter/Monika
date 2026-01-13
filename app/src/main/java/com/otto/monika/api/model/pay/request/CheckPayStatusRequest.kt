package com.otto.monika.api.model.pay.request

/**
 * 检查支付状态请求
 * @param orderNo 订单号
 */
data class CheckPayStatusRequest(
    val orderNo: String?
)

