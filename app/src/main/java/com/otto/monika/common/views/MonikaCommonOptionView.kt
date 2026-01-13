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
 * 通用选项视图（图标 + 数量）
 * 可复用的状态切换组件，支持通过 attr 自定义选中/未选中状态的图标
 */
class MonikaCommonOptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val optionIcon: ImageView
    private val optionContent: TextView

    // 选中和未选中状态的图标资源
    private var iconSelectedRes: Int = 0
    private var iconUnselectedRes: Int = 0

    // 状态
    var isOptionSelected: Boolean = false
        set(value) {
            field = value
            updateIcon()
        }

    // 数量
    private var countValue: Int = -1

    /**
     * 设置数量
     * @param value 数量值
     * @param format 是否格式化显示，默认为 false
     */
    fun setCountValue(value: Int, format: Boolean = true) {
        countValue = value
        optionContent.text = if (format) {
            formatCountValue(value)
        } else {
            value.toString()
        }
    }

    // 点击回调
    var onOptionClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_common_option, this, true)

        optionIcon = findViewById(R.id.iv_common_icon)
        optionContent = findViewById(R.id.tv_common_count)

        // 读取自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MonikaCommonOptionView)
            try {
                // 读取选中和未选中状态的图标
                iconSelectedRes = typedArray.getResourceId(
                    R.styleable.MonikaCommonOptionView_iconSelected,
                    R.drawable.monika_post_detail_like_icon_selected
                )
                iconUnselectedRes = typedArray.getResourceId(
                    R.styleable.MonikaCommonOptionView_iconUnselected,
                    iconSelectedRes
                )

                // 读取图标尺寸
                val iconSize = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaCommonOptionView_iconSize,
                    0
                )
                if (iconSize > 0) {
                    optionIcon.layoutParams.width = iconSize
                    optionIcon.layoutParams.height = iconSize
                }

                // 读取数量文字颜色
                val optionText = typedArray.getString(
                    R.styleable.MonikaCommonOptionView_optionText
                )
                optionContent.text = optionText

                // 读取数量文字颜色
                val textColor = typedArray.getColor(
                    R.styleable.MonikaCommonOptionView_optionTextColor,
                    ContextCompat.getColor(context, R.color.text_808080)
                )
                optionContent.setTextColor(textColor)

                // 读取数量文字大小
                val countTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaCommonOptionView_optionTextSize,
                    0
                )
                if (countTextSize > 0) {
                    optionContent.textSize =
                        countTextSize / context.resources.displayMetrics.scaledDensity
                }

                // 读取图标和数量之间的间距
                val iconCountSpacing = typedArray.getDimensionPixelSize(
                    R.styleable.MonikaCommonOptionView_iconCountSpacing,
                    0
                )
                if (iconCountSpacing > 0) {
                    (optionContent.layoutParams as? LayoutParams)?.let { params ->
                        params.marginStart = iconCountSpacing
                        optionContent.layoutParams = params
                    }
                }
            } finally {
                typedArray.recycle()
            }
        }

        // 初始化图标
        updateIcon()

        // 设置点击事件
        setOnClickListener {
            // 触发回调
            onOptionClickListener?.invoke()
        }
    }

    /**
     * 更新图标
     */
    private fun updateIcon() {
        val iconRes = if (isOptionSelected) iconSelectedRes else iconUnselectedRes
        if (iconRes != 0) {
            optionIcon.setImageResource(iconRes)
        }
    }

    /**
     * 格式化数量
     * 规则：
     * - 超过 9999，显示 "1.0万" 格式（带小数点）
     * - 超过 999.9万（即 9999000），不带小数点显示，如 "1123万"
     * - 最大上限 9999万
     * @param count 数量
     * @return 格式化后的字符串
     */
    private fun formatCountValue(count: Int): String {
        if (count < 0) {
            return "0"
        }
        if (count <= 9999) {
            return count.toString()
        }
        // 超过 9999，转换为万
        val wanValue = count / 10000.0
        // 超过 999.9万（即 9999000），不带小数点显示
        if (count >= 9999000) {
            // 最大上限 9999万
            val wanInt = (wanValue + 0.5).toInt().coerceAtMost(9999)
            return "${wanInt}万"
        }
        // 超过 9999 但小于 999.9万，显示带小数点的格式
        return String.format("%.1f万", wanValue)
    }
}