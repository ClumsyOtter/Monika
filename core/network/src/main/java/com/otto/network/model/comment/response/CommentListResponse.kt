package com.otto.network.model.comment.response

/**
 * 评论列表响应
 * @param list 评论列表
 * @param total 总数
 */
data class CommentListResponse(
    val list: List<CommentItem> = emptyList(),
    val total: Int = 0
)

