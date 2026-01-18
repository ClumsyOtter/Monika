package com.otto.monika.account.income.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.otto.common.utils.getView
import com.otto.monika.R

/**
 * 收入项自定义View
 * 支持通过attr设置icon和label，以及通过方法设置icon、label、amount
 */
@SuppressLint("Recycle")
class IncomeItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tvAmount: TextView by getView(R.id.tv_income_item_amount)
    private val ivIcon: ImageView by getView(R.id.iv_income_item_icon)
    private val tvLabel: TextView by getView(R.id.tv_income_item_label)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_income_item_view, this)
        
        attrs?.let {
            context.withStyledAttributes(
                attrs,
                R.styleable.IncomeItemView
            ) {

                // 从attr中获取icon
                val iconRes = getResourceId(
                    R.styleable.IncomeItemView_incomeItemIcon,
                    0
                )
                if (iconRes != 0) {
                    setIcon(iconRes)
                }

                // 从attr中获取label
                val labelText = getString(R.styleable.IncomeItemView_incomeItemLabel)
                if (!labelText.isNullOrEmpty()) {
                    setLabel(labelText)
                }

                // 从attr中获取amount（可选）
                val amountText = getString(R.styleable.IncomeItemView_incomeItemAmount)
                if (!amountText.isNullOrEmpty()) {
                    setAmount(amountText)
                }

            }
        }
    }

    /**
     * 设置金额
     * @param amount 金额文字
     */
    fun setAmount(amount: String) {
        tvAmount.text = amount
    }

    /**
     * 设置图标
     * @param iconRes 图标资源ID
     */
    fun setIcon(iconRes: Int) {
        ivIcon.setImageResource(iconRes)
    }

    /**
     * 设置标签文字
     * @param label 标签文字
     */
    fun setLabel(label: String) {
        tvLabel.text = label
    }

    /**
     * 获取金额TextView，用于更细粒度的控制
     */
    fun getAmountTextView(): TextView = tvAmount

    /**
     * 获取图标ImageView，用于更细粒度的控制
     */
    fun getIconImageView(): ImageView = ivIcon

    /**
     * 获取标签TextView，用于更细粒度的控制
     */
    fun getLabelTextView(): TextView = tvLabel
}

