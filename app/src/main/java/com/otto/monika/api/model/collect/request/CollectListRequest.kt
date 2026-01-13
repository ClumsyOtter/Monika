package com.otto.monika.api.model.collect.request

import com.google.gson.annotations.SerializedName

/**
 * 收藏列表请求
 * @param targetType 目标类型，2 代表获取 post
 * @param createdAtStart 创建时间开始
 * @param createdAtEnd 创建时间结束
 * @param page 页码（从1开始）
 * @param pageSize 每页数量
 */
data class CollectListRequest(
    val uid: String? = null, // 用户ID
    @SerializedName("target_type")
    val targetType: Int = 2,
    @SerializedName("createdAtStart")
    val createdAtStart: String? = null,
    @SerializedName("createdAtEnd")
    val createdAtEnd: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)

