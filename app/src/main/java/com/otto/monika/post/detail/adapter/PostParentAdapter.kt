package com.otto.monika.post.detail.adapter

import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.otto.monika.R
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.common.views.recycleview.ChildRecyclerView
import com.otto.monika.common.views.recycleview.INestedParentAdapter
import com.otto.monika.post.detail.views.MonikaPostContentView

class PostParentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), INestedParentAdapter {
    private val dataList: MutableList<PostItem> = ArrayList()

    private var childRecyclerView: ChildRecyclerView? = null
    private var childRecyclerViewAdapter: CommentAdapter = CommentAdapter()

    var onLoadMoreComment: (() -> Unit)? = null
    val quickAdapterHelper: QuickAdapterHelper =
        QuickAdapterHelper.Builder(childRecyclerViewAdapter)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    onLoadMoreComment?.invoke()
                }

                override fun onFailRetry() {
                    onLoadMoreComment?.invoke()
                }

            }).build()

    var onImagesClick: ((imageList: List<String>, index: Int) -> Unit)? = null

    fun init() {
        dataList.add(PostItem())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            val cyPostContentView = MonikaPostContentView(viewGroup.context)
            cyPostContentView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            return object : RecyclerView.ViewHolder(cyPostContentView) {}
        }

        if (childRecyclerView == null) {
            childRecyclerView = ChildRecyclerView(viewGroup.context)
            childRecyclerView?.itemAnimator = null
            childRecyclerView?.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            childRecyclerView?.setLayoutManager(LinearLayoutManager(viewGroup.context))
            childRecyclerView?.addItemDecoration(VerticalSpacingItemDecoration(10))
            childRecyclerView?.setPadding(viewGroup.resources.getDimensionPixelSize(R.dimen.padding_15))
            childRecyclerView?.setAdapter(quickAdapterHelper.adapter)
        } else {
            (childRecyclerView?.parent as? ViewGroup)?.removeView(childRecyclerView)
        }
        childRecyclerViewAdapter.stateView = MonikaEmptyView(viewGroup.context).apply {
            setEmptyText("暂无评论(｡･ω･｡)")
        }
        return object : RecyclerView.ViewHolder(childRecyclerView!!) {
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_ITEM) {
            val cyPostContentView = viewHolder.itemView as MonikaPostContentView
            val post = dataList[position]
            cyPostContentView.onImagesClick = onImagesClick
            // 设置帖子内容数据
            cyPostContentView.setPostData(post)
        }
    }


    override fun getItemCount(): Int {
        return dataList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < dataList.size) TYPE_ITEM else TYPE_INNER
    }

    override fun getCurrentChildRecyclerView(): ChildRecyclerView? {
        return childRecyclerView
    }

    fun getCommentAdapter(): CommentAdapter {
        return childRecyclerViewAdapter
    }

    fun setData(postItem: PostItem) {
        this.dataList.clear()
        this.dataList.add(postItem)
        notifyItemChanged(0)
    }


    companion object {
        private const val TYPE_ITEM = 0

        private const val TYPE_INNER = 1
    }
}
