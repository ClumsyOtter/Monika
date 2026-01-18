package com.otto.network.model.subscribe.response

import com.google.gson.annotations.SerializedName

/**
 * 订阅用户项
 */
data class SubscribeUserItem(
    val id: String? = null,
    val subscriber: Subscriber? = null,
    val plan: SubscribePlan? = null,
    @SerializedName("totalPrice")
    val totalPrice: Double? = null,
    @SerializedName("expired_at")
    val expiredAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

