package com.otto.monika.subscribe.support.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.common.decoration.GridSpacingItemDecoration
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.subscribe.plan.model.SubscribePlanDiscountRules

/**
 * 订阅期限适配器
 * 使用 BaseQuickAdapter 4.x 实现
 */
class SubscriptionPeriodAdapter :
    BaseQuickAdapter<SubscribePlanDiscountRules, SubscriptionPeriodAdapter.SubscriptionPeriodViewHolder>() {

    var onItemClickListener: ((SubscribePlanDiscountRules) -> Unit)? = null

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionPeriodViewHolder {
        return SubscriptionPeriodViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: SubscriptionPeriodViewHolder,
        position: Int,
        item: SubscribePlanDiscountRules?
    ) {
        item ?: return
        holder.bind(item, onItemClickListener)
    }

    fun setData(newData: List<SubscribePlanDiscountRules>) {
        submitList(newData)
    }

    fun updateSelection(selectedPeriod: SubscribePlanDiscountRules) {
        items.forEachIndexed { index, period ->
            val wasSelected = period.isSelected
            period.isSelected = period.month == selectedPeriod.month
            if (wasSelected != period.isSelected) {
                notifyItemChanged(index)
            }
        }
    }

    class SubscriptionPeriodViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_subscription_period, null)
    ) {
        private val nameText: TextView = itemView.findViewById(R.id.tv_period_name)
        private val price: TextView = itemView.findViewById(R.id.tv_period_price)
        private val discount: TextView = itemView.findViewById(R.id.tv_period_discount)

        fun bind(
            item: SubscribePlanDiscountRules,
            clickListener: ((SubscribePlanDiscountRules) -> Unit)?
        ) {
            // 设置名称
            nameText.text = "${item.month ?: 0}个月"
            price.text = "¥" + item.price

            item.discount?.let {
                discount.text = it.toString() + "折"
                discount.isVisible = item.discount != null && it < 10
            }

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
