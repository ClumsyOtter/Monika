package com.otto.network.model.comment.request

/**
 * 二级评论列表请求
 * @param parentId 父评论ID
 * @param postId 帖子ID
 * @param uid 用户ID
 * @param page 页码
 * @param pageSize 每页数量
 */
data class CommentSubListRequest(
    val parentId: String?,
    val postId: String?,
    val uid: String?,
    val page: Int?,
    val pageSize: Int?
)

