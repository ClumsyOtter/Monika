package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.otto.monika.R

/**
 * 账户头部下拉View
 * 显示收藏数和天数信息
 */
class AccountHeadDropDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvTopText: TextView
    private val tvTopCount: TextView
    private val tvBottomText: TextView
    private val ivRightIcon: ImageView

    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_account_head_drop_down, this, true)
        // 获取子视图
        tvTopText = findViewById(R.id.tv_top_text)
        tvTopCount = findViewById(R.id.tv_top_count)
        tvBottomText = findViewById(R.id.tv_bottom_text)
        ivRightIcon = findViewById(R.id.iv_right_icon)
    }

    /**
     * 设置收藏数
     * @param count 收藏数，如 236
     */
    fun setFavoriteCount(count: Int) {
        tvTopCount.text = count.toString()
    }

    /**
     * 设置天数
     * @param days 天数，如 236
     */
    fun setDays(days: String?) {
        val text = "这是你来到次芽的第${days}"
        tvBottomText.text = text
    }

    /**
     * 设置右边图标
     * @param resId 图标资源ID
     */
    fun setRightIcon(resId: Int) {
        ivRightIcon.setImageResource(resId)
    }
}

