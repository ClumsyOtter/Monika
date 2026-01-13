package com.otto.monika.api.model.subscribe.response

import com.google.gson.annotations.SerializedName

/**
 * 订阅者信息
 */
data class Subscriber(
    val uid: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val intro: String? = null,
    @SerializedName("isCreator")
    val isCreator: Boolean? = false
)

