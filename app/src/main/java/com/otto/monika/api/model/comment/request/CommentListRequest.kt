package com.otto.monika.api.model.comment.request

/**
 * 评论列表请求
 * @param postId 帖子ID
 * @param uid 用户ID
 * @param page 页码
 * @param pageSize 每页数量
 */
data class CommentListRequest(
    val postId: String?,
    val uid: String?,
    val page: Int?,
    val pageSize: Int?
)

