package com.otto.monika.api.model.collect.request

import com.google.gson.annotations.SerializedName

/**
 * 收藏/取消收藏请求
 * @param targetType 目标类型：1-用户，2-动态/帖子，3-其他
 * @param targetId 目标ID（动态ID、话题ID、用户ID等）
 */
data class CollectRequest(
    @SerializedName("target_type")
    val targetType: Int?,
    @SerializedName("target_id")
    val targetId: String?
)

