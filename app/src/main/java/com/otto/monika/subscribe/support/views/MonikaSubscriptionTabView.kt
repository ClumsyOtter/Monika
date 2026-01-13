package com.otto.monika.subscribe.support.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.otto.monika.R

/**
 * 订阅方案 Tab 自定义 View
 * 封装了 tab item 的布局和背景更新逻辑
 */
class MonikaSubscriptionTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val textView: TextView
    private var isTabSelected: Boolean = false

    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.item_tab_subscription, this, true)
        textView = findViewById(R.id.tv_tab_text)
        // 初始状态为未选中
        setTabSelected(false)
    }

    /**
     * 设置 tab 的选中状态
     * @param selected true 表示选中，false 表示未选中
     */
    fun setTabSelected(selected: Boolean) {
        if (isTabSelected == selected) {
            return
        }
        isTabSelected = selected
        updateBackground()
        updateTextColor()
    }

    /**
     * 更新背景
     */
    private fun updateBackground() {
        val backgroundDrawable = if (isTabSelected) {
            ContextCompat.getDrawable(context, R.drawable.bg_tab_item_selected)
        } else {
            null
        }
        this.background = backgroundDrawable
    }

    /**
     * 更新文字颜色
     */
    private fun updateTextColor() {
        val textColor = if (isTabSelected) {
            ContextCompat.getColor(context, R.color.text_000000)
        } else {
            ContextCompat.getColor(context, R.color.text_808080)
        }
        textView.setTextColor(textColor)
    }

    /**
     * 设置文本内容
     */
    fun setText(text: CharSequence?) {
        textView.text = text
    }

    /**
     * 获取文本内容
     */
    fun getText(): CharSequence? {
        return textView.text
    }

    /**
     * 获取当前选中状态
     */
    fun isTabSelected(): Boolean = isTabSelected
}