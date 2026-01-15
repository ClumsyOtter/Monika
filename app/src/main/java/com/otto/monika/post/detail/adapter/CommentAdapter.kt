package com.otto.monika.post.detail.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.otto.monika.R
import com.otto.monika.api.model.comment.response.CommentItem
import com.otto.monika.api.model.comment.response.addSubComment
import com.otto.monika.api.model.comment.response.clearSubComments
import com.otto.monika.api.model.comment.response.getSubComments
import com.otto.monika.api.model.comment.response.removeSubComment
import com.otto.monika.api.model.comment.response.updateLikeState
import com.otto.monika.api.model.comment.response.updateSubComments
import com.otto.monika.post.detail.views.CommentItemView

/**
 * 评论列表适配器（统一处理父评论和子评论）
 * 使用 BaseQuickAdapter 4.x 实现
 */
class CommentAdapter : BaseQuickAdapter<CommentItem, CommentAdapter.CommentViewHolder>() {

    private var currentPage = 0
    private var commentTotal = 0

    var onExpandSubCommentsListener: ((ExpandSubCommentsParams) -> Unit)? = null
    var onReplyClickListener: ((CommentActionParams) -> Unit)? = null
    var onLikeClickListener: ((CommentActionParams) -> Unit)? = null
    var onLongClickListener: ((CommentActionParams) -> Unit)? = null
    var onItemClickListener: ((CommentActionParams) -> Unit)? = null

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        return CommentViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
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
            onItemClickListener = onItemClickListener,
            onExpandSubCommentsListener = onExpandSubCommentsListener
        )
    }

    fun updateSubComments(
        expandSubCommentsParams: ExpandSubCommentsParams,
        subComments: List<CommentItem>,
        total: Int
    ) {
        val parentComment = items.find { it.id == expandSubCommentsParams.parentId }
        parentComment?.updateSubComments(expandSubCommentsParams.page, subComments)
        val parentPosition = items.indexOfFirst { it.id == expandSubCommentsParams.parentId }
        if (parentPosition >= 0) {
            val recyclerViewAdapters =
                expandSubCommentsParams.subRecyclerView.adapter as? ConcatAdapter
            val subCommentAdapter =
                recyclerViewAdapters?.adapters?.find { it is SubCommentAdapter } as? SubCommentAdapter
            if (subCommentAdapter != null) {
                subCommentAdapter.updateSubComments(
                    subComments,
                    expandSubCommentsParams.page,
                    total
                )
                val footViewAdapter = recyclerViewAdapters.adapters.find { it is FootViewAdapter } as? FootViewAdapter
                footViewAdapter?.setItem(subCommentAdapter.getSubCommentFoot(), null)
            } else {
                notifyItemChanged(parentPosition)
            }
        }
    }

    fun rollbackCommentLikeState(commentActionParams: CommentActionParams) {
        commentActionParams.commentItem.updateLikeState()
        if (commentActionParams.parentId == null) {
            notifyItemChanged(commentActionParams.position)
        } else {
            val recyclerViewAdapters =
                commentActionParams.subRecyclerView?.adapter as? ConcatAdapter
            val subCommentAdapter =
                recyclerViewAdapters?.adapters?.find { it is SubCommentAdapter } as? SubCommentAdapter
            if (subCommentAdapter != null) {
                subCommentAdapter.notifyItemChanged(commentActionParams.position)
            } else {
                val parentPosition = items.indexOfFirst { it.id == commentActionParams.parentId }
                notifyItemChanged(parentPosition)
            }
        }
    }

    fun replyNewComment(newComment: CommentItem, commentActionParams: CommentActionParams?) {
        if (commentActionParams == null) {
            add(0, newComment)
            commentTotal++
        } else {
            replySubComment(newComment, commentActionParams)
        }
    }

    fun deleteComment(commentActionParams: CommentActionParams) {
        if (commentActionParams.parentId == null) {
            if (commentActionParams.position >= 0 && commentActionParams.position < items.size) {
                removeAt(commentActionParams.position)
                commentTotal = maxOf(0, commentTotal - 1)
            }
        } else {
            val parentComment = items.find { it.id == commentActionParams.parentId }
            parentComment?.removeSubComment(commentActionParams.commentItem)
            val recyclerViewAdapters =
                commentActionParams.subRecyclerView?.adapter as? ConcatAdapter
            val subCommentAdapter =
                recyclerViewAdapters?.adapters?.find { it is SubCommentAdapter } as? SubCommentAdapter
            if (subCommentAdapter != null) {
                subCommentAdapter.removeComment(commentActionParams.position)
                val footViewAdapter =
                    recyclerViewAdapters.adapters.find { it is FootViewAdapter } as? FootViewAdapter
                footViewAdapter?.setItem(subCommentAdapter.getSubCommentFoot(), null)
            } else {
                val parentPosition = items.indexOfFirst { it.id == commentActionParams.parentId }
                notifyItemChanged(parentPosition)
            }
        }
    }

    fun replySubComment(commentItem: CommentItem, commentActionParams: CommentActionParams) {
        val parentId = commentActionParams.parentId ?: commentActionParams.commentItem.id
        val parentComment = items.find { it.id == parentId }
        parentComment?.addSubComment(commentItem)
        val recyclerViewAdapters = commentActionParams.subRecyclerView?.adapter as? ConcatAdapter
        val subCommentAdapter =
            recyclerViewAdapters?.adapters?.find { it is SubCommentAdapter } as? SubCommentAdapter
        if (subCommentAdapter != null) {
            subCommentAdapter.addComments(commentItem)
            val footViewAdapter =
                recyclerViewAdapters.adapters.find { it is FootViewAdapter } as? FootViewAdapter
            footViewAdapter?.setItem(subCommentAdapter.getSubCommentFoot(), null)
        } else {
            val parentPosition = items.indexOfFirst { it.id == parentId }
            notifyItemChanged(parentPosition)
        }
    }

    fun addComments(newComments: List<CommentItem>, total: Int, page: Int) {
        commentTotal = total
        if (page == 1) {
            currentPage = 1
            submitList(newComments)
        } else {
            currentPage = page
            addAll(newComments)
        }
    }

    fun getCurrentPage(): Int = currentPage
    fun getCommentTotal(): Int = commentTotal

    class CommentViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_comment_parent, null)
    ) {
        private val commentItemView: CommentItemView = itemView.findViewById(R.id.view_comment_item)
        private val repliesRecycler: RecyclerView = itemView.findViewById(R.id.rv_comment_replies)
        private var subCommentAdapter: SubCommentAdapter? = null
        private var footViewAdapter: FootViewAdapter? = null
        fun bind(
            item: CommentItem,
            position: Int,
            onReplyClickListener: ((CommentActionParams) -> Unit)?,
            onLikeClickListener: ((CommentActionParams) -> Unit)?,
            onLongClickListener: ((CommentActionParams) -> Unit)?,
            onItemClickListener: ((CommentActionParams) -> Unit)?,
            onExpandSubCommentsListener: ((ExpandSubCommentsParams) -> Unit)?
        ) {
            commentItemView.bindCommentItem(item)
            val currentPosition = bindingAdapterPosition
            commentItemView.onReplyClickListener = { commentItem ->
                onReplyClickListener?.invoke(
                    CommentActionParams(
                        commentItem,
                        currentPosition,
                        null,
                        repliesRecycler
                    )
                )
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
            val replyCount = item.replyCount ?: 0
            if (replyCount > 0) {
                showSubComments()
                initSubCommentAdapter(
                    item,
                    replyCount,
                    onReplyClickListener,
                    onLikeClickListener,
                    onLongClickListener,
                    onItemClickListener,
                    onExpandSubCommentsListener
                )
            } else {
                hideSubComments()
            }
        }

        fun initSubCommentAdapter(
            item: CommentItem,
            replyCount: Int,
            onReplyClickListener: ((CommentActionParams) -> Unit)?,
            onLikeClickListener: ((CommentActionParams) -> Unit)?,
            onLongClickListener: ((CommentActionParams) -> Unit)?,
            onItemClickListener: ((CommentActionParams) -> Unit)?,
            onExpandSubCommentsListener: ((ExpandSubCommentsParams) -> Unit)?
        ): SubCommentAdapter {
            repliesRecycler.itemAnimator = null
            if (subCommentAdapter == null) {
                subCommentAdapter = SubCommentAdapter(item.id)
                val quickAdapterHelper = QuickAdapterHelper.Builder(subCommentAdapter!!).build()
                footViewAdapter = FootViewAdapter()
                footViewAdapter?.onClickListener = {
                    subCommentAdapter?.handleFooterClick()
                }
                quickAdapterHelper.addAfterAdapter(footViewAdapter!!)
                repliesRecycler.adapter = quickAdapterHelper.adapter
                if (repliesRecycler.layoutManager == null) {
                    repliesRecycler.layoutManager = LinearLayoutManager(repliesRecycler.context)
                }
                subCommentAdapter?.onReplyClickListener = {
                    onReplyClickListener?.invoke(
                        it.copy(
                            parentId = item.id,
                            subRecyclerView = repliesRecycler
                        )
                    )
                }
                subCommentAdapter?.onLikeClickListener = {
                    onLikeClickListener?.invoke(
                        it.copy(
                            parentId = item.id,
                            subRecyclerView = repliesRecycler
                        )
                    )
                }
                subCommentAdapter?.onLongClickListener = {
                    onLongClickListener?.invoke(
                        it.copy(
                            parentId = item.id,
                            subRecyclerView = repliesRecycler
                        )
                    )
                }
                subCommentAdapter?.onItemClickListener = {
                    onItemClickListener?.invoke(
                        it.copy(
                            parentId = item.id,
                            subRecyclerView = repliesRecycler
                        )
                    )
                }
                subCommentAdapter?.onExpandListener = { parentId, _, page ->
                    onExpandSubCommentsListener?.invoke(
                        ExpandSubCommentsParams(
                            parentId,
                            repliesRecycler,
                            bindingAdapterPosition,
                            page
                        )
                    )
                }
                subCommentAdapter?.onCollapseListener = {
                    item.clearSubComments()
                    subCommentAdapter?.resetExpanded()
                    footViewAdapter?.setItem(subCommentAdapter?.getSubCommentFoot(), null)
                }
            }
            subCommentAdapter!!.recoverSubComments(item.getSubComments(), replyCount)
            footViewAdapter?.setItem(subCommentAdapter?.getSubCommentFoot(), null)
            return subCommentAdapter!!
        }


        fun getSubRecyclerView(): RecyclerView = repliesRecycler
        fun showSubComments() {
            repliesRecycler.visibility = View.VISIBLE
        }

        fun hideSubComments() {
            repliesRecycler.visibility = View.GONE
        }
    }
}
