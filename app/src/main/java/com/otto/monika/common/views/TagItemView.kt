package com.otto.monika.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.otto.monika.R
import androidx.core.content.withStyledAttributes

/**
 * 标签按钮 View
 * 包含：图标 + 文本
 * 用于显示标签（如：地区标签、分类标签等）
 */
@SuppressLint("CustomViewStyleable")
class TagItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconImage: ImageView
    private val tagText: TextView
    private val tagContainer: ConstraintLayout

    var onClickListener: (() -> Unit)? = null

    init {
        // 设置水平方向
        orientation = HORIZONTAL

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.tag_item_view, this, true)

        // 获取子视图
        iconImage = findViewById(R.id.iv_tag_icon)
        tagText = findViewById(R.id.tv_tag_text)
        tagContainer = findViewById<ConstraintLayout>(R.id.cl_tag_container)

        // 解析自定义属性
        attrs?.let {
            context.withStyledAttributes(
                it,
                R.styleable.TagButtonView,
                0,
                0
            ) {

                // 标签文本
                val text = getString(R.styleable.TagButtonView_tagText)
                if (!text.isNullOrEmpty()) {
                    setTagText(text)
                }

                // 标签图标
                val iconRes = getResourceId(R.styleable.TagButtonView_tagIcon, 0)
                if (iconRes != 0) {
                    setIcon(iconRes)
                }

                // 是否显示"#"号（默认 false）
                val showTagIcon = getBoolean(R.styleable.TagButtonView_showHashTag, false)
                if (showTagIcon) {
                    setShowTagIcon(true)
                }

            }
        }

        // 设置点击监听
        setOnClickListener { onClickListener?.invoke() }
    }

    /**
     * 设置标签文本
     * 根据 showHashTag 属性决定是否添加"#"号
     */
    fun setTagText(text: String?) {
        tagText.text = text ?: ""
    }

    /**
     * 设置图标
     */
    fun setIcon(iconRes: Int) {
        iconImage.setImageResource(iconRes)
    }

    /**
     * 设置是否显示"#"号
     */
    fun setShowTagIcon(show: Boolean) {
        iconImage.isVisible = show
    }

    fun setTagBackground(drawable: Drawable) {
        tagContainer.background = drawable
    }

}

