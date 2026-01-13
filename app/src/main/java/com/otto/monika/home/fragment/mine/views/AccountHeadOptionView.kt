package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.otto.monika.R

/**
 * 账户头部选项 View
 * 从左到右：图标、名称（上方）、说明（下方）、右侧按钮
 */
class AccountHeadOptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val iconImage: ImageView
    private val nameText: TextView
    private val descText: TextView
    private val buttonText: TextView

    var onOptionClickListener: (() -> Unit)? = null

    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_account_head_option, this, true)

        // 获取子视图
        iconImage = findViewById(R.id.iv_option_icon)
        nameText = findViewById(R.id.tv_option_name)
        descText = findViewById(R.id.tv_option_desc)
        buttonText = findViewById(R.id.iv_option_button)

        // 设置点击监听
        buttonText.setOnClickListener { onOptionClickListener?.invoke() }
    }

    /**
     * 设置图标
     */
    fun updateContent(iconRes: Int, name: String?, desc: String?, endBtn: String?) {
        iconImage.setImageResource(iconRes)
        nameText.text = name ?: ""
        descText.text = desc ?: ""
        buttonText.text = endBtn ?: ""
    }


}

