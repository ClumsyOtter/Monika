package com.otto.monika.post.detail.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.otto.monika.R

/**
 * 子评论 Footer View
 * 用于显示展开/收起按钮
 */
class SubCommentFooterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var textView: TextView

    init {
        initView()
    }

    private fun initView() {
        // 创建 TextView
        textView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            text = "展开"
            setTextColor(0xFF8EB21C.toInt())
            textSize = 14f
        }
        addView(textView)
    }

    /**
     * 设置文本
     */
    fun setText(text: String) {
        textView.text = text
    }

    /**
     * 获取文本
     */
    fun getText(): String = textView.text.toString()

    /**
     * 设置点击监听器
     */
    fun setOnFooterClickListener(listener: OnClickListener?) {
        textView.setOnClickListener(listener)
    }
}

