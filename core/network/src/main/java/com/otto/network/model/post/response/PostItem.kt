package com.otto.network.model.post.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


/**
 * 帖子项
 */
@Parcelize
data class PostItem(
    val id: String? = null,
    val uid: String? = null,
    val title: String? = null,
    val images: List<String> = emptyList(), // 图片列表（数组）
    val content: String? = null,
    @SerializedName("visible_type")
    val visibleType: Int? = null,
    val status: Int? = null,
    val ip: String? = null,
    @SerializedName("ip_addr")
    val ipAddr: String? = null,
    @SerializedName("comment_num")
    var commentNum: Int? = null,
    @SerializedName("favor_num")
    val favorNum: Int? = null,
    @SerializedName("like_num")
    var likeNum: Int? = null,
    @SerializedName("audit_at")
    val auditAt: Long? = null,
    @SerializedName("audit_reason")
    val auditReason: String? = null,
    @SerializedName("audit_admin")
    val auditAdmin: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("is_recommend")
    val isRecommend: Int? = null,
    @SerializedName("hot_num")
    val hotNum: Int? = null,
    @SerializedName("collect_num")
    var collectNum: Int? = null,
    @SerializedName("view_num")
    val viewNum: Int? = null,
    val tags: List<TagItem>? = null,
    @SerializedName("is_liked")
    var isLiked: Boolean? = false,
    val user: UserInfo? = null,
    @SerializedName("create_time")
    val createTime: String? = null,
    @SerializedName("is_subscribed")
    val isSubscribed: Boolean? = false,
    @SerializedName("subscribe_remaining_time")
    val subscribeRemainingTime: Int? = null,
    @SerializedName("is_collected")
    var isCollected: Boolean? = false
) : Parcelable

