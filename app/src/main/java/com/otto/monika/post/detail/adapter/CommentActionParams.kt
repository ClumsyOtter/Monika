package com.otto.monika.post.detail.adapter

import androidx.recyclerview.widget.RecyclerView
import com.otto.network.model.comment.response.CommentItem

/**
 * 评论操作参数 Model
 * 用于封装评论相关的回调参数，方便扩展
 * @param parentId 父评论ID（一级评论为 null，二级评论有值）
 *  * @param recyclerView 子评论的 RecyclerView
 * @param commentItem 评论项
 * @param position 当前评论在列表中的位置
 */
data class CommentActionParams(
    val commentItem: CommentItem,
    val position: Int,
    val parentId: String? = null,
    val subRecyclerView: RecyclerView? = null,
)

/**
 * 展开子评论参数 Model
 * 用于封装展开子评论相关的回调参数
 * @param parentId 父评论ID
 * @param subRecyclerView 子评论的 RecyclerView
 * @param position 父评论在列表中的位置
 * @param page 要加载的页码
 */
data class ExpandSubCommentsParams(
    val parentId: String?,
    val subRecyclerView: RecyclerView,
    val position: Int,
    val page: Int
)

