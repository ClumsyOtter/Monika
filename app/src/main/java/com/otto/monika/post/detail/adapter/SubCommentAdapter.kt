package com.otto.monika.post.detail.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.post.detail.model.SubCommentFoot
import com.otto.monika.post.detail.views.CommentItemView
import com.otto.network.model.comment.response.CommentItem
import com.otto.network.model.comment.response.SubCommentItem

/**
 * 子评论适配器
 * 负责管理子评论的显示和状态
 * 使用 BaseQuickAdapter 4.x 实现
 */
class SubCommentAdapter(val parentId: String?) :
    BaseQuickAdapter<CommentItem, SubCommentAdapter.SubCommentViewHolder>() {

    private var isExpanded: Boolean = false
    private var currentPage: Int = 0
    private var totalCommentCount: Int = 0

    var onExpandListener: ((String?, Int, Int) -> Unit)? = null
    var onCollapseListener: (() -> Unit)? = null
    var onReplyClickListener: ((CommentActionParams) -> Unit)? = null
    var onLikeClickListener: ((CommentActionParams) -> Unit)? = null
    var onLongClickListener: ((CommentActionParams) -> Unit)? = null
    var onItemClickListener: ((CommentActionParams) -> Unit)? = null

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): SubCommentViewHolder {
        return SubCommentViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: SubCommentViewHolder,
        position: Int,
        item: CommentItem?
    ) {
        item ?: return
        holder.bind(
            item = item,
            position = position,
            onReplyClickListener = onReplyClickListener,
            onLikeClickListener = onLikeClickListener,
            onLongClickListener = onLongClickListener,
            onItemClickListener = onItemClickListener
        )
    }

    fun isExpanded(): Boolean = isExpanded
    fun hasMore(): Boolean = items.size < totalCommentCount

    fun updateSubComments(subComments: List<CommentItem>, page: Int, total: Int) {
        if (items.isNotEmpty()) {
            addAll(subComments)
        } else {
            submitList(subComments)
        }
        this.currentPage = page
        this.totalCommentCount = total
        this.isExpanded = true
    }

    fun recoverSubComments(
        subCommentItem: SubCommentItem,
        replyCount: Int
    ) {
        submitList(subCommentItem.subCommentList.toMutableList())
        this.currentPage = subCommentItem.currentPage
        this.totalCommentCount = replyCount
        this.isExpanded = subCommentItem.subCommentList.isNotEmpty()
    }

    fun addComments(commentItem: CommentItem) {
        if (items.isEmpty()) {
            submitList(listOf(commentItem))
        } else {
            addAll(listOf(commentItem))
        }
        totalCommentCount++
    }

    fun resetExpanded() {
        submitList(emptyList())
        isExpanded = false
        currentPage = 0
    }


    fun getSubCommentFoot(): SubCommentFoot {
        return SubCommentFoot(
            isExpanded = isExpanded,
            hasMore = hasMore(),
            emptyComment = items.isEmpty()
        )
    }

    fun handleFooterClick() {
        if (!isExpanded) {
            onExpandListener?.invoke(parentId, 0, 1)
        } else if (hasMore()) {
            onExpandListener?.invoke(parentId, 0, currentPage + 1)
        } else {
            onCollapseListener?.invoke()
        }
    }

    fun removeComment(position: Int) {
        if (position >= 0 && position < items.size) {
            removeAt(position)
            totalCommentCount = maxOf(0, totalCommentCount - 1)
        }
    }

    class SubCommentViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_comment_child, null)
    ) {
        private val commentItemView: CommentItemView = itemView.findViewById(R.id.view_comment_item)

        fun bind(
            item: CommentItem,
            position: Int,
            onReplyClickListener: ((CommentActionParams) -> Unit)?,
            onLikeClickListener: ((CommentActionParams) -> Unit)?,
            onLongClickListener: ((CommentActionParams) -> Unit)?,
            onItemClickListener: ((CommentActionParams) -> Unit)?
        ) {
            commentItemView.bindCommentItem(item)
            val currentPosition = bindingAdapterPosition
            commentItemView.onReplyClickListener = { commentItem ->
                onReplyClickListener?.invoke(CommentActionParams(commentItem, currentPosition))
            }
            commentItemView.onLikeClickListener = { commentItem ->
                onLikeClickListener?.invoke(CommentActionParams(commentItem, currentPosition))
            }
            commentItemView.onLongClickListener = { commentItem ->
                onLongClickListener?.invoke(CommentActionParams(commentItem, currentPosition))
            }
            commentItemView.onItemClickListener = { commentItem ->
                onItemClickListener?.invoke(CommentActionParams(commentItem, currentPosition))
            }
        }
    }
}
