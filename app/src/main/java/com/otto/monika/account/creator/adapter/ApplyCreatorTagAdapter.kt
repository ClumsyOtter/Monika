package com.otto.monika.account.creator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.api.model.creator.response.CreatorMetadataItem

/**
 * 申请创作者标签列表 Adapter
 * 支持单选模式和多选模式
 * 选中项背景变为黑色，文字颜色变为 #C4FF05
 */
class ApplyCreatorTagAdapter(
    private val isMultiSelect: Boolean = false, // true: 多选模式，false: 单选模式（不可取消）
    private val allowDeselect: Boolean = false // true: 允许取消选中（仅多选模式有效），false: 不允许取消选中
) : RecyclerView.Adapter<ApplyCreatorTagAdapter.TagViewHolder>() {

    private var tags: List<CreatorMetadataItem> = emptyList()

    var onTagSelectedListener: ((CreatorMetadataItem, Boolean) -> Unit)? = null // 参数：tag 和是否选中
    var onSelectionChangedListener: (() -> Unit)? = null // 选中状态变化回调（用于验证）

    /**
     * 设置数据
     */
    fun setData(newTags: List<CreatorMetadataItem>) {
        val diffCallback = TagDiffCallback(tags, newTags)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tags = newTags
        diffResult.dispatchUpdatesTo(this)
    }


    /**
     * 获取选中的标签列表（多选模式）
     */
    fun getSelectedTags(): List<CreatorMetadataItem> {
        return tags.filter { it.isSelected == true }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_apply_creator_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount(): Int = tags.size

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagText: TextView = itemView.findViewById(R.id.tv_tag_item_text)

        fun bind(tag: CreatorMetadataItem) {
            tagText.text = tag.name

            // 根据选中状态更新样式
            if (tag.isSelected == true) {
                // 选中样式：黑色圆角背景，文字颜色 #C4FF05
                tagText.background = ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.monika_corner_10_black_border_selected
                )
                tagText.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.color_C4FF05)
                )
            } else {
                // 未选中样式：灰色圆角背景，灰色文字
                tagText.background = ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.monika_corner_10_gray_bg
                )
                tagText.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.text_666666)
                )
            }

            itemView.setOnClickListener {
                val wasSelected = tag.isSelected
                if (isMultiSelect) {
                    // 多选模式
                    if (wasSelected == true && allowDeselect) {
                        // 已选中且允许取消：取消选中
                        tag.isSelected = false
                        notifyItemChanged(bindingAdapterPosition)
                        onTagSelectedListener?.invoke(tag, false)
                        onSelectionChangedListener?.invoke()
                    } else if (wasSelected != true) {
                        // 未选中：选中
                        tag.isSelected = true
                        notifyItemChanged(bindingAdapterPosition)
                        onTagSelectedListener?.invoke(tag, true)
                        onSelectionChangedListener?.invoke()
                    }
                } else {
                    // 单选模式（不可取消）
                    if (wasSelected != true) {
                        // 未选中：选中（替换之前的选中项）
                        tags.forEachIndexed { index, item ->
                            if (item == tag) {
                                tag.isSelected = true
                                notifyItemChanged(index)
                            } else {
                                if (item.isSelected == true) {
                                    item.isSelected = false
                                    notifyItemChanged(index)
                                }
                            }
                        }
                        onTagSelectedListener?.invoke(tag, true)
                        onSelectionChangedListener?.invoke()
                    }
                    // 已选中：不做任何操作（不可取消）
                }
            }
        }
    }

    /**
     * DiffUtil Callback
     */
    private class TagDiffCallback(
        private val oldList: List<CreatorMetadataItem>,
        private val newList: List<CreatorMetadataItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

