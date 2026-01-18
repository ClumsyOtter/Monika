package com.otto.network.model.comment.response

import com.google.gson.annotations.SerializedName

/**
 * 评论项（一级评论和二级评论共用）
 */
data class CommentItem(
    val id: String? = null,
    val content: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    @SerializedName("reply_to_user_name")
    val replyToUserName: String? = null,
    val location: String? = null,
    var likes: Int? = null,
    @SerializedName("canDel")
    val canDel: Int? = null, // 1-可以删除，0-不可以删除
    @SerializedName("isAuthor")
    val isAuthor: Int? = null, // 1-是作者，0-不是作者
    @SerializedName("admired")
    var admired: Int? = null, // 1-已点赞，0-未点赞
    val time: String? = null,
    @SerializedName("replyCount")
    var replyCount: Int? = null, // 回复数量（只有一级评论有）
    var subComments: SubCommentItem? = null // 二级评论列表（只有一级评论有，可动态增加删除）
)

data class SubCommentItem(
    var currentPage: Int = 0,
    var subCommentList: MutableList<CommentItem> = mutableListOf()
)

fun CommentItem.getSubComments(): SubCommentItem {
    if (subComments == null) {
        subComments = SubCommentItem()
    }
    return subComments!!
}

fun CommentItem.updateSubComments(
    page: Int,
    subCommentList: List<CommentItem>,
): SubCommentItem {
    val subComments = getSubComments()
    subComments.currentPage = page
    if (page == 1) {
        // 第一页，替换数据
        subComments.subCommentList.clear()
        subComments.subCommentList.addAll(subCommentList)
    } else {
        //追加数据
        subComments.subCommentList.addAll(subCommentList)
    }
    return subComments
}

fun CommentItem.updateLikeState() {
    // 切换点赞状态
    val isLiked = admired == 1
    admired = if (isLiked) 0 else 1

    // 更新点赞数
    val currentLikes = likes ?: 0
    likes = if (isLiked) {
        // 取消点赞，点赞数减1
        maxOf(0, currentLikes - 1)
    } else {
        // 点赞，点赞数加1
        currentLikes + 1
    }
}

fun CommentItem.removeSubComment(commentItem: CommentItem) {
    val subComments = getSubComments()
    subComments.subCommentList.removeAll { it.id == commentItem.id }
    // 更新父评论的 replyCount
    val currentReplyCount = replyCount ?: 0
    replyCount = maxOf(0, currentReplyCount - 1)
}

fun CommentItem.addSubComment(commentItem: CommentItem) {
    val subComments = getSubComments()
    subComments.subCommentList.add(commentItem)
    // 更新父评论的 replyCount
    val currentReplyCount = replyCount ?: 0
    replyCount = currentReplyCount + 1
}

fun CommentItem.clearSubComments() {
    val subComments = getSubComments()
    subComments.currentPage = 0
    subComments.subCommentList.clear()
}


