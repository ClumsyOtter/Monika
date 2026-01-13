package com.otto.monika.subscribe.plan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.subscribe.plan.model.SubscribePlanDiscountRules

/**
 * 订阅方案折扣规则 Adapter
 */
class SubscribePlanDiscountRuleAdapter :
    RecyclerView.Adapter<SubscribePlanDiscountRuleAdapter.DiscountRuleViewHolder>() {

    private var rules: List<SubscribePlanDiscountRules> = mutableListOf()
    private var basePrice: String? = null // 基础价格，用于计算折扣后的价格

    /**
     * 设置数据
     */
    fun setData(newRules: List<SubscribePlanDiscountRules>) {
        rules = newRules
        notifyDataSetChanged()
    }

    /**
     * 根据基础价格更新折扣规则中的价格字段
     */
    fun updateDiscountRulesPrices(basePriceText: String?) {
        basePrice = basePriceText
        if (basePrice == null) {
            // 如果基础价格为0或不存在，将所有规则的价格设为空
            for (i in rules.indices) {
                rules[i].price = null
            }
            notifyDataSetChanged()
        } else {
            for (i in rules.indices) {
                val rule = rules[i]
                val discount = rule.discount ?: 10
                val discountPrice = getDiscountPrice(basePrice, discount)
                rules[i].price = discountPrice
            }
            notifyDataSetChanged()
        }
    }

    /**
     * 更新指定位置的折扣值
     * 基于基础价格计算折扣后的价格
     */
    private fun updateDiscountAtPosition(position: Int, newDiscount: Int) {
        if (position < 0 || position >= rules.size) return
        val discountedPrice = getDiscountPrice(basePrice, newDiscount)
        rules[position].price = discountedPrice
        rules[position].discount = newDiscount
        notifyItemChanged(position)
    }

    fun getDiscountPrice(price: String?, newDiscount: Int): String? {
        price?.toDoubleOrNull()?.let {
            val discountPrice = it * newDiscount / 10.0
            return String.format("%.2f", discountPrice)
        }
        return null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountRuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscribe_plan_discount_rule, parent, false)
        return DiscountRuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscountRuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount(): Int = rules.size

    inner class DiscountRuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthsText: TextView = itemView.findViewById(R.id.tv_discount_rule_months)
        private val priceText: TextView = itemView.findViewById(R.id.tv_discount_rule_price)
        private val discountText: TextView = itemView.findViewById(R.id.tv_discount_rule_discount)
        private val minusBtn: ImageView = itemView.findViewById(R.id.tv_discount_rule_minus)
        private val plusBtn: ImageView = itemView.findViewById(R.id.tv_discount_rule_plus)

        fun bind(rule: SubscribePlanDiscountRules) {
            monthsText.text = "${rule.month ?: 0}个月"
            val currentDiscount = (rule.discount ?: 0).coerceIn(1, 10)
            discountText.text = currentDiscount.toString()

            // 直接显示数据模型中的 price 字段
            if (rule.price != null) {
                priceText.text = "¥${rule.price}"
                priceText.visibility = View.VISIBLE
            } else {
                priceText.visibility = View.GONE
            }

            minusBtn.setOnClickListener {
                val position = bindingAdapterPosition
                if (position < 0 || position >= rules.size) return@setOnClickListener
                val newDiscount = (currentDiscount - 1).coerceAtLeast(1)
                // 更新 adapter 数据和 view
                updateDiscountAtPosition(position, newDiscount)
                // 通知外部
            }

            plusBtn.setOnClickListener {
                val position = bindingAdapterPosition
                if (position < 0 || position >= rules.size) return@setOnClickListener
                val newDiscount = (currentDiscount + 1).coerceAtMost(10)
                // 更新 adapter 数据和 view
                updateDiscountAtPosition(position, newDiscount)
                // 通知外部
            }
        }
    }

}

