package com.otto.monika.post.detail.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.otto.monika.post.detail.model.SubCommentFoot
import com.otto.monika.post.detail.views.SubCommentFooterView

class FootViewAdapter : BaseSingleItemAdapter<SubCommentFoot, FootViewAdapter.FootViewHolder>() {
    var onClickListener: View.OnClickListener? = null
    override fun onBindViewHolder(
        holder: FootViewHolder,
        item: SubCommentFoot?
    ) {
        item?.let { holder.bind(it, onClickListener) }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): FootViewHolder {
        return FootViewHolder(SubCommentFooterView(context))
    }

    class FootViewHolder(val footerView: SubCommentFooterView) : RecyclerView.ViewHolder(footerView) {
        fun bind(
            item: SubCommentFoot,
            onClickListener: View.OnClickListener?
        ) {
            footerView.setOnFooterClickListener(onClickListener)
            footerView.setText(if (item.isExpanded && item.hasMore.not()) "收起" else "展开")
        }
    }
}