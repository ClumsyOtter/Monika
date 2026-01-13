package com.otto.monika.api.model.pay.response

import com.google.gson.annotations.SerializedName

/**
 * 检查支付状态响应
 * @param isPay 是否支付成功
 */
data class CheckPayStatusResponse(
    @SerializedName("is_pay")
    val isPay: Boolean? = null
)

