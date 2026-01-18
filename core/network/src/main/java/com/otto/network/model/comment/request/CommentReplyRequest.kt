package com.otto.network.model.comment.request

/**
 * 回复评论请求
 */
data class CommentReplyRequest(
    val content: String?,
    val postId: String?,
    val commentId: String?
)

