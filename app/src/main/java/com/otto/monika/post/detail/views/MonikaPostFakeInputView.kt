package com.otto.monika.post.detail.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.otto.monika.R

/**
 * 帖子假输入框 View
 * 显示图标和提示文字，点击后弹出输入框
 */
class MonikaPostFakeInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val inputIcon: ImageView
    private val inputText: TextView

    // 点击回调
    var onClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post_fake_input, this, true)
        
        inputIcon = findViewById(R.id.iv_fake_input_icon)
        inputText = findViewById(R.id.tv_fake_input_text)
        
        // 读取自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MonikaPostFakeInputView)
            try {
                // 读取图标
                val iconRes = typedArray.getResourceId(
                    R.styleable.MonikaPostFakeInputView_inputIcon,
                    R.drawable.monika_post_detail_textinput_icon
                )
                inputIcon.setImageResource(iconRes)
                
                // 读取图标尺寸
                val iconSize = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaPostFakeInputView_inputIconSize,
                    0
                )
                if (iconSize > 0) {
                    inputIcon.layoutParams.width = iconSize
                    inputIcon.layoutParams.height = iconSize
                }
                
                // 读取提示文字
                val hint = typedArray.getString(R.styleable.MonikaPostFakeInputView_inputHint)
                if (!hint.isNullOrEmpty()) {
                    inputText.hint = hint
                }
                
                // 读取文字内容
                val text = typedArray.getString(R.styleable.MonikaPostFakeInputView_inputText)
                if (!text.isNullOrEmpty()) {
                    inputText.text = text
                }
                
                // 读取文字颜色
                val textColor = typedArray.getColor(
                    R.styleable.MonikaPostFakeInputView_inputTextColor,
                    ContextCompat.getColor(context, R.color.text_000000)
                )
                inputText.setTextColor(textColor)
                
                // 读取提示文字颜色
                val hintColor = typedArray.getColor(
                    R.styleable.MonikaPostFakeInputView_inputHintColor,
                    ContextCompat.getColor(context, R.color.text_808080)
                )
                inputText.setHintTextColor(hintColor)
                
                // 读取文字大小
                val textSize = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaPostFakeInputView_inputTextSize,
                    0
                )
                if (textSize > 0) {
                    inputText.textSize = textSize / context.resources.displayMetrics.scaledDensity
                }
                
                // 读取图标和文字之间的间距
                val iconTextSpacing = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaPostFakeInputView_iconTextSpacing,
                    0
                )
                if (iconTextSpacing > 0) {
                    (inputText.layoutParams as? LayoutParams)?.let { params ->
                        params.marginStart = iconTextSpacing
                        inputText.layoutParams = params
                    }
                }
            } finally {
                typedArray.recycle()
            }
        }
        
        // 设置点击事件
        setOnClickListener {
            onClickListener?.invoke()
        }
    }

    /**
     * 设置提示文字
     */
    fun setHint(hint: String) {
        inputText.hint = hint
    }

    /**
     * 设置文字内容
     */
    fun setText(text: String) {
        inputText.text = text
    }

    /**
     * 获取文字内容
     */
    fun getText(): String {
        return inputText.text.toString()
    }
}

