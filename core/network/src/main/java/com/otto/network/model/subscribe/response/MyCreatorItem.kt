package com.otto.network.model.subscribe.response

import com.google.gson.annotations.SerializedName

/**
 * 我的订阅用户项
 */
data class MyCreatorItem(
    val id: Long? = null,
    val creator: Creator? = null,
    val plan: SubscribePlan? = null,
    @SerializedName("expired_at")
    val expiredAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

