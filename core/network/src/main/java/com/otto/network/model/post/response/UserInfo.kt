package com.otto.network.model.post.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 用户信息
 */
@Parcelize
data class UserInfo(
    val id: String? = null,
    val openid: String? = null,
    @SerializedName("device_id")
    val deviceId: String? = null,
    val username: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val intro: String? = null,
    val gender: Int? = null,
    val address: String? = null,
    @SerializedName("creator_verified")
    val creatorVerified: Int? = null,
    val status: Int? = null,
    @SerializedName("is_visitor")
    val isVisitor: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("showId")
    val showId: String? = null,
    @SerializedName("collect_num")
    val collectNum: Int? = null,
    @SerializedName("subscribe_count")
    val subscribeCount: Int? = null,
    @SerializedName("is_subscribed")
    val isSubscribed: Boolean? = null,
    @SerializedName("subscribe_remaining_time")
    val subscribeRemainingTime: Int? = null,
    val subscribedtime: Int? = null
) : Parcelable

