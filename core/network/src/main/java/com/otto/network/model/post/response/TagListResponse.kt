package com.otto.network.model.post.response

/**
 * 标签列表响应
 */
data class TagListResponse(
    val list: List<TagItem> = mutableListOf()
)

