package com.otto.monika.subscribe.support.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.otto.monika.R
import androidx.core.content.withStyledAttributes

/**
 * 价格显示自定义 View
 * 支持显示价格符号和价格信息，可通过 attr 自定义样式
 */
class MonikaPriceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val symbolTextView: TextView
    private val priceTextView: TextView
    private val suffixTextView: TextView

    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_price, this, true)

        // 获取子视图
        symbolTextView = findViewById(R.id.tv_price_symbol)
        priceTextView = findViewById(R.id.tv_price_text)
        suffixTextView = findViewById(R.id.tv_price_suffix)

        // 解析自定义属性
        attrs?.let {
            context.withStyledAttributes(
                it,
                R.styleable.MonikaPriceView,
                0,
                0
            ) {

                // 价格符号
                val symbol = getString(R.styleable.MonikaPriceView_priceSymbol) ?: "¥"
                symbolTextView.text = symbol

                // 价格符号大小
                val symbolSize = getDimensionPixelSize(
                    R.styleable.MonikaPriceView_priceSymbolSize,
                    (16 * resources.displayMetrics.scaledDensity).toInt()
                )
                symbolTextView.textSize = symbolSize / resources.displayMetrics.scaledDensity

                // 价格信息
                val price = getString(R.styleable.MonikaPriceView_priceText)
                priceTextView.text = price ?: ""

                // 价格信息字体大小
                val priceSize = getDimensionPixelSize(
                    R.styleable.MonikaPriceView_priceTextSize,
                    (20 * resources.displayMetrics.scaledDensity).toInt()
                )
                priceTextView.textSize = priceSize / resources.displayMetrics.scaledDensity

                // 价格整体颜色
                val textColor = getColor(
                    R.styleable.MonikaPriceView_priceColor,
                    ContextCompat.getColor(context, R.color.text_c4ff05)
                )
                symbolTextView.setTextColor(textColor)
                priceTextView.setTextColor(textColor)

                // 价格后缀
                val suffix = getString(R.styleable.MonikaPriceView_priceSuffix)
                if (!suffix.isNullOrEmpty()) {
                    suffixTextView.text = suffix
                }

                // 价格后缀是否显示
                val suffixVisible = getBoolean(
                    R.styleable.MonikaPriceView_priceSuffixVisible,
                    false
                )
                suffixTextView.visibility = if (suffixVisible) VISIBLE else GONE

                // 价格后缀字体大小
                val suffixSize = getDimensionPixelSize(
                    R.styleable.MonikaPriceView_priceSuffixSize,
                    (14 * resources.displayMetrics.scaledDensity).toInt()
                )
                suffixTextView.textSize = suffixSize / resources.displayMetrics.scaledDensity

                // 后缀颜色与整体颜色一致
                suffixTextView.setTextColor(textColor)

            }
        }
    }

    /**
     * 设置价格符号
     */
    fun setPriceSymbol(symbol: String) {
        symbolTextView.text = symbol
    }

    /**
     * 设置价格符号大小（sp）
     */
    fun setPriceSymbolSize(sizeSp: Float) {
        symbolTextView.textSize = sizeSp
    }

    /**
     * 设置价格信息
     */
    fun setPriceText(price: String) {
        priceTextView.text = price
    }

    /**
     * 设置价格信息字体大小（sp）
     */
    fun setPriceTextSize(sizeSp: Float) {
        priceTextView.textSize = sizeSp
    }

    /**
     * 设置价格整体颜色
     */
    fun setPriceColor(color: Int) {
        symbolTextView.setTextColor(color)
        priceTextView.setTextColor(color)
        suffixTextView.setTextColor(color)
    }

    /**
     * 设置价格后缀
     */
    fun setPriceSuffix(suffix: String) {
        suffixTextView.text = suffix
    }

    /**
     * 设置价格后缀是否显示
     */
    fun setPriceSuffixVisible(visible: Boolean) {
        suffixTextView.visibility = if (visible) VISIBLE else GONE
    }

    /**
     * 设置价格后缀字体大小（sp）
     */
    fun setPriceSuffixSize(sizeSp: Float) {
        suffixTextView.textSize = sizeSp
    }

    /**
     * 获取价格符号
     */
    fun getPriceSymbol(): String {
        return symbolTextView.text.toString()
    }

    /**
     * 获取价格信息
     */
    fun getPriceText(): String {
        return priceTextView.text.toString()
    }

    /**
     * 获取价格后缀
     */
    fun getPriceSuffix(): String {
        return suffixTextView.text.toString()
    }
}