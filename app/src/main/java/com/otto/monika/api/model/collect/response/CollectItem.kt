package com.otto.monika.api.model.collect.response

import com.google.gson.annotations.SerializedName
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.api.model.post.response.UserInfo

/**
 * 收藏项
 */
data class CollectItem(
    val id: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("target_type")
    val targetType: Int? = null,
    @SerializedName("target_id")
    val targetId: String? = null,
    val status: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("post_info")
    val postInfo: PostItem? = null,
    @SerializedName("user_info")
    val userInfo: UserInfo? = null
)

