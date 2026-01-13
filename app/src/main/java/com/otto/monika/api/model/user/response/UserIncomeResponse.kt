package com.otto.monika.api.model.user.response

import com.google.gson.annotations.SerializedName

/**
 * 用户收入信息响应
 */
data class UserIncomeResponse(
    @SerializedName("total")
    val totalIncome: String? = null,
    @SerializedName("unsettled")
    val unsettledIncome: String? = null,
    @SerializedName("settled")
    val settledIncome: String? = null,
    @SerializedName("withdrawn")
    val withdrawnIncome: String? = null,
    @SerializedName("withdrawnable")
    val withdrawnableIncome: String? = null
)

