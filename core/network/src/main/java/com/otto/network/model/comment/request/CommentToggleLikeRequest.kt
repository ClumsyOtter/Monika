package com.otto.network.model.comment.request

/**
 * 评论点赞/取消点赞请求
 */
data class CommentToggleLikeRequest(
    val commentId: String?
)

