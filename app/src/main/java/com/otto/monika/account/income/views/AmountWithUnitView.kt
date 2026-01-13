package com.otto.monika.account.income.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.otto.monika.R
import com.otto.monika.common.utils.getView

/**
 * 金额带单位自定义View
 * 支持通过attr配置withdraw（单位），以及通过代码控制withdraw和amount
 */
class AmountWithUnitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val tvAmount: TextView by getView(R.id.tv_amount_with_unit_amount)
    private val tvWithdraw: TextView by getView(R.id.tv_amount_with_unit_withdraw)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_amount_with_unit_view, this)
        
        attrs?.let {
            context.withStyledAttributes(
                attrs,
                R.styleable.AmountWithUnitView
            ) {
                // 从attr中获取withdraw（单位）
                val withdrawText = getString(R.styleable.AmountWithUnitView_amountWithUnitWithdraw)
                if (!withdrawText.isNullOrEmpty()) {
                    setWithdraw(withdrawText)
                }
                
                // 从attr中获取amount（可选）
                val amountText = getString(R.styleable.AmountWithUnitView_amountWithUnitAmount)
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
     * 设置单位（withdraw）
     * @param withdraw 单位文字，如"元"
     */
    fun setWithdraw(withdraw: String) {
        tvWithdraw.text = withdraw
    }

    /**
     * 获取金额TextView，用于更细粒度的控制
     */
    fun getAmountTextView(): TextView = tvAmount

    /**
     * 获取单位TextView，用于更细粒度的控制
     */
    fun getWithdrawTextView(): TextView = tvWithdraw
}

