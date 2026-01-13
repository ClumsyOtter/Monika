package com.otto.monika.api.model.comment.request

/**
 * 删除评论请求
 */
data class CommentRemoveRequest(
    val commentId: String?,
    val postId: String?
)

