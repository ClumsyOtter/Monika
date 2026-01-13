package com.otto.monika.api.model.comment.request

/**
 * 评论点赞/取消点赞请求
 */
data class CommentToggleLikeRequest(
    val commentId: String?
)

