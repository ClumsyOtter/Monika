package com.otto.monika.subscribe.support.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.common.decoration.GridSpacingItemDecoration
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.subscribe.support.model.PaymentMethod

/**
 * 支付方式适配器
 * 使用 BaseQuickAdapter 4.x 实现
 */
class PaymentMethodAdapter :
    BaseQuickAdapter<PaymentMethod, PaymentMethodAdapter.PaymentMethodViewHolder>() {

    var onItemClickListener: ((PaymentMethod) -> Unit)? = null

    /**
     * 创建 ViewHolder
     */
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): PaymentMethodViewHolder {
        return PaymentMethodViewHolder(parent)
    }

    /**
     * 绑定数据
     */
    override fun onBindViewHolder(
        holder: PaymentMethodViewHolder,
        position: Int,
        item: PaymentMethod?
    ) {
        item ?: return
        holder.bind(item, onItemClickListener)
    }

    /**
     * 设置数据
     */
    fun setData(newData: List<PaymentMethod>) {
        submitList(newData)
    }

    /**
     * 更新选中状态
     */
    fun updateSelection(selectedMethod: PaymentMethod) {
        items.forEachIndexed { index, method ->
            val wasSelected = method.isSelected
            method.isSelected = method.payChannel == selectedMethod.payChannel
            if (wasSelected != method.isSelected) {
                notifyItemChanged(index)
            }
        }
    }

    /**
     * ViewHolder 类
     */
    class PaymentMethodViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_payment_method, null)
    ) {
        private val iconImage: ImageView = itemView.findViewById(R.id.iv_payment_icon)
        private val nameText: TextView = itemView.findViewById(R.id.tv_payment_name)

        fun bind(item: PaymentMethod, clickListener: ((PaymentMethod) -> Unit)?) {
            // 设置图标
            item.icon?.let { iconImage.setImageResource(it) }

            // 设置名称
            nameText.text = item.name

            // 根据选中状态设置背景
            val backgroundRes = if (item.isSelected) {
                R.drawable.monika_corner_15_green_bg
            } else {
                R.drawable.monika_corner_15_white_bg
            }
            itemView.background = ContextCompat.getDrawable(itemView.context, backgroundRes)

            // 点击事件
            itemView.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    companion object {
        /**
         * 创建 Grid 布局的间距装饰器
         * @param spanCount 列数
         * @param spacingDp 间距（dp）
         * @param includeEdge 是否包含边缘间距
         */
        fun createSpacingDecoration(
            spanCount: Int = 2,
            spacingDp: Int = 10,
            includeEdge: Boolean = false
        ): GridSpacingItemDecoration {
            val spacing = DipUtils.dpToPx(spacingDp)
            return GridSpacingItemDecoration(spanCount, spacing, includeEdge)
        }
    }
}

