package com.otto.monika.subscribe.rank.fragment.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.common.views.TagItemView

/**
 * 标签适配器
 */
class TagAdapter(val showHashTag: Boolean = false, val backgroundBgDrawable: Drawable? = null) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private var tags: List<String> = emptyList()

    var onItemClickListener: ((String) -> Unit)? = null

    /**
     * 更新标签数据
     * @param newTags 新的标签列表
     */
    fun updateTags(newTags: List<String>) {
        tags = newTags
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TagViewHolder {
        val view = TagItemView(parent.context)
        view.setShowTagIcon(showHashTag)
        backgroundBgDrawable?.let {
            view.setTagBackground(backgroundBgDrawable)
        }
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        if (position < tags.size) {
            holder.bind(tags[position])
        }
    }

    override fun getItemCount(): Int = tags.size

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(tag: String) {
            (itemView as? TagItemView)?.setTagText(tag)
            itemView.setOnClickListener {
                onItemClickListener?.invoke(tag)
            }
        }
    }

}