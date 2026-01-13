package com.otto.monika.api.model.pay.response

import com.google.gson.annotations.SerializedName

/**
 * 创建订单响应
 * @param serialNumber 订单序列号
 * @param orderNo 订单号
 * @param appid 应用ID
 * @param channels 支付渠道列表
 */
data class CreateOrderResponse(
    @SerializedName("serial_number")
    val serialNumber: String? = null,
    val orderNo: String? = null,
    val appid: String? = null,
    val channels: List<String> = mutableListOf()
)

