package com.otto.network.model.post.response

/**
 * 帖子列表响应
 */
data class PostListResponse(
    val list: List<PostItem> = mutableListOf(),
    val total: Int = 0
)

