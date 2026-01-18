package com.otto.network.model.comment.request

/**
 * 创建评论请求
 */
data class CommentCreateRequest(
    val content: String?,
    val postId: String?
)

