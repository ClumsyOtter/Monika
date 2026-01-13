package com.otto.monika.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.otto.monika.R

/**
 * 空状态视图
 * 用于显示空数据状态，支持自定义图标和文字
 */
class MonikaEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var emptyIcon: ImageView
    private var emptyText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.monika_layout_empty_view, this, true)
        emptyIcon = findViewById(R.id.iv_empty_icon)
        emptyText = findViewById(R.id.tv_empty_text)
    }

    /**
     * 设置空状态图标
     * @param iconResId 图标资源 ID
     */
    fun setEmptyIcon(iconResId: Int) {
        emptyIcon.setImageResource(iconResId)
    }

    /**
     * 设置空状态文字
     * @param text 文字内容
     */
    fun setEmptyText(text: String) {
        emptyText.text = text
    }

    /**
     * 设置空状态文字
     * @param textResId 文字资源 ID
     */
    fun setEmptyText(textResId: Int) {
        emptyText.setText(textResId)
    }
}

