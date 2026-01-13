package com.otto.monika.common.dialog.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.common.dialog.model.CommonBottomSheetItem

/**
 * 通用BottomSheet列表适配器
 * 支持单选和多选模式
 */
class CommonBottomSheetAdapter(
    private val context: Context,
    private val isMultiSelect: Boolean = false, // 是否多选模式，默认单选
    private val maxSelectCount: Int = -1
) : RecyclerView.Adapter<CommonBottomSheetAdapter.ItemViewHolder>() {
    val itemList: MutableList<CommonBottomSheetItem> = mutableListOf()

    var onItemSelectOverSize: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_common_sheet_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        val isSelected = item.isSelected
        holder.bind(item, isSelected)
    }

    override fun getItemCount(): Int = itemList.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.date_text)
        private val selectedIndicator: ImageView = itemView.findViewById(R.id.selected_indicator)

        fun bind(item: CommonBottomSheetItem, isSelected: Boolean) {
            contentText.text = item.content
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            // 根据选中状态设置字体加粗
            contentText.setTypeface(
                null,
                if (isSelected) Typeface.BOLD else Typeface.NORMAL
            )
            val textColor = if (isSelected) ResourcesCompat.getColor(
                itemView.resources,
                R.color.black, null
            ) else ResourcesCompat.getColor(
                itemView.resources,
                R.color.gray, null
            )
            contentText.setTextColor(textColor)

            itemView.setOnClickListener {
                if (isMultiSelect) {
                    if (maxSelectCount > 0 && itemList.filter { item -> item.isSelected }.size >= maxSelectCount && item.isSelected.not()) {
                        //多选超最大数量
                        onItemSelectOverSize?.invoke()
                    } else {
                        // 多选模式
                        item.isSelected = item.isSelected.not()
                        // 只更新当前项
                        notifyItemChanged(bindingAdapterPosition)
                    }
                } else {
                    itemList.forEachIndexed { index, item ->
                        if (item.isSelected && index != bindingAdapterPosition) {
                            item.isSelected = false
                            notifyItemChanged(index)
                        }
                    }
                    // 单选模式
                    item.isSelected = item.isSelected.not()
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    fun setData(items: List<CommonBottomSheetItem>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    /**
     * 获取多选模式下已选中的项列表
     */
    fun getSelectedItems(): List<CommonBottomSheetItem> {
        return itemList.filter { it.isSelected }
    }

    /**
     * 是否是多选模式
     */
    fun isMultiSelectMode(): Boolean = isMultiSelect
}

