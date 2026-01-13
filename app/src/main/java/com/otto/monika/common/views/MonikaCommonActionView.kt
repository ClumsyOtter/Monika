package com.otto.monika.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.otto.monika.R

/**
 * 通用 ActionBar View
 * 包含：返回按钮（左边）+ 标题（中间）
 * 背景透明
 */
class MonikaCommonActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val backButton: ImageView
    private val titleText: TextView

    // 点击回调
    var onBackClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_common_action, this, true)

        backButton = findViewById(R.id.iv_common_action_back)
        titleText = findViewById(R.id.tv_common_action_title)

        // 读取自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MonikaCommonActionView)
            try {
                // 读取标题文字
                val title = typedArray.getString(R.styleable.MonikaCommonActionView_actionTitle)
                title?.let {
                    titleText.text = it
                }

                // 读取标题文字颜色
                val titleTextColor = typedArray.getColor(
                    R.styleable.MonikaCommonActionView_actionTitleColor,
                    ContextCompat.getColor(context, R.color.text_000000)
                )
                titleText.setTextColor(titleTextColor)

                // 读取标题文字大小
                val titleTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaCommonActionView_actionTitleSize,
                    0
                )
                if (titleTextSize > 0) {
                    titleText.textSize = titleTextSize / context.resources.displayMetrics.scaledDensity
                }

                // 读取返回按钮图标
                val backIconRes = typedArray.getResourceId(
                    R.styleable.MonikaCommonActionView_actionBackIcon,
                    R.drawable.monika_custom_nav_bar_back_icon
                )
                backButton.setImageResource(backIconRes)
            } finally {
                typedArray.recycle()
            }
        }

        // 设置返回按钮点击事件
        backButton.setOnClickListener {
            onBackClickListener?.invoke()
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String?) {
        titleText.text = title ?: ""
    }

    /**
     * 设置标题文字颜色
     */
    fun setTitleColor(color: Int) {
        titleText.setTextColor(color)
    }

    /**
     * 设置标题文字大小（sp）
     */
    fun setTitleSize(sizeSp: Float) {
        titleText.textSize = sizeSp
    }

    /**
     * 设置返回按钮图标
     */
    fun setBackIcon(resId: Int) {
        backButton.setImageResource(resId)
    }

    /**
     * 获取标题
     */
    fun getTitle(): String {
        return titleText.text.toString()
    }
}

