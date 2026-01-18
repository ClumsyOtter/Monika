package com.otto.monika.common.views

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.core.content.withStyledAttributes
import com.otto.common.utils.DipUtils
import com.otto.monika.R

class MonikaTabItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var subTitleView: TextView? = null
    var indicatorView: ImageView? = null
    var isTabSelected: Boolean = false
    private var subTitle: String? = null
    private var indicatorDrawable: Drawable? = null
    private var indicatorHeight: Float? = null
    private var indicatorWidth: Float? = null

    private var subTitleSelectedColor: Int = resources.getColor(R.color.theme_black, null)
    private var subTitleUnselectedColor: Int = resources.getColor(R.color.theme_gray, null)
    private var subTitleTextSize: Int = resources.getDimensionPixelSize(R.dimen.text_size_16)
    private var subTitleSelectedTextSize: Int =
        resources.getDimensionPixelSize(R.dimen.text_size_16)


    init {
        init()
        if (null != attrs) {
            context.withStyledAttributes(attrs, R.styleable.MonikaTabItem) {
                subTitle = getString(R.styleable.MonikaTabItem_mm_subTitle)
                indicatorDrawable = getDrawable(R.styleable.MonikaTabItem_mm_indicator_drawable)
                indicatorHeight =
                    getDimension(R.styleable.MonikaTabItem_mm_indicator_drawable_height, 0f)
                indicatorWidth =
                    getDimension(R.styleable.MonikaTabItem_mm_indicator_drawable_width, 0f)
                subTitleSelectedColor = getColor(
                    R.styleable.MonikaTabItem_mm_subTitle_text_selected_color,
                    resources.getColor(R.color.theme_black, null)
                )
                subTitleUnselectedColor = getColor(
                    R.styleable.MonikaTabItem_mm_subTitle_text_unselected_color,
                    resources.getColor(R.color.theme_gray, null)
                )
                // getDimensionPixelSize 返回的是 px 值，需要使用 COMPLEX_UNIT_PX 单位设置
                subTitleTextSize = getDimensionPixelSize(
                    R.styleable.MonikaTabItem_mm_subTitle_text_size,
                    resources.getDimensionPixelSize(R.dimen.text_size_16)
                )

                subTitleSelectedTextSize = getDimensionPixelSize(
                    R.styleable.MonikaTabItem_mm_subTitle_selected_text_size,
                    resources.getDimensionPixelSize(R.dimen.text_size_16)
                )

            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        subTitleView?.text = subTitle
        // 使用 COMPLEX_UNIT_PX 单位设置字体大小，因为 subTitleTextSize 是 px 值
        subTitleView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, subTitleTextSize.toFloat())
        indicatorDrawable?.let {
            indicatorView?.setImageDrawable(it)
        }
        indicatorHeight?.takeIf { it > 0 }?.let {
            indicatorView?.layoutParams?.height = it.toInt()
        }
        indicatorWidth?.takeIf { it > 0 }?.let {
            indicatorView?.layoutParams?.width = it.toInt()
        }
    }

    private fun init() {
        LayoutInflater.from(context).inflate(this.layoutId, this)
        subTitleView = findViewById(R.id.sub_title_view)
        indicatorView = findViewById(R.id.indicator_view)
    }

    val layoutId: Int
        get() = R.layout.monika_tab_item

    override fun setSelected(selected: Boolean) {
        this.isTabSelected = selected
        if (this.isTabSelected) {
            subTitleView?.setTextColor(subTitleSelectedColor)
            subTitleView?.setTypeface(null, Typeface.BOLD)
            subTitleView?.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                subTitleSelectedTextSize.toFloat()
            )
            indicatorView?.setVisibility(VISIBLE)
        } else {
            subTitleView?.setTextColor(subTitleUnselectedColor)
            subTitleView?.setTypeface(null, Typeface.NORMAL)
            subTitleView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, subTitleTextSize.toFloat())
            indicatorView?.setVisibility(INVISIBLE)
        }
    }

    /**
     * 设置标题文本
     */
    fun setSubTitle(title: String) {
        subTitle = title
        subTitleView?.text = title
    }

    fun getSubTitle(): String {
        return subTitleView?.text?.toString() ?: subTitle ?: ""
    }

    /**
     * 设置标题文字颜色
     * @param selectedColor 选中时的颜色
     * @param unselectedColor 未选中时的颜色
     */
    fun setSubTitleTextColor(selectedColor: Int, unselectedColor: Int) {
        subTitleSelectedColor = selectedColor
        subTitleUnselectedColor = unselectedColor
        // 根据当前选中状态更新颜色
        if (isTabSelected) {
            subTitleView?.setTextColor(selectedColor)
        } else {
            subTitleView?.setTextColor(unselectedColor)
        }
    }

    /**
     * 设置标题文字大小
     * @param normalSize 未选中时的文字大小（sp）
     * @param selectedSize 选中时的文字大小（sp）
     */
    fun setSubTitleTextSize(normalSize: Float, selectedSize: Float) {
        subTitleTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            normalSize,
            resources.displayMetrics
        ).toInt()
        subTitleSelectedTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            selectedSize,
            resources.displayMetrics
        ).toInt()
        // 根据当前选中状态更新文字大小
        if (isTabSelected) {
            subTitleView?.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                subTitleSelectedTextSize.toFloat()
            )
        } else {
            subTitleView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, subTitleTextSize.toFloat())
        }
    }

    /**
     * 设置指示器图标
     * @param drawableResId 指示器图标资源ID
     */
    fun setIndicatorDrawable(drawableResId: Int) {
        indicatorDrawable = resources.getDrawable(drawableResId, null)
        indicatorView?.setImageResource(drawableResId)
    }

    /**
     * 设置指示器大小
     * @param width 宽度（dp）
     * @param height 高度（dp）
     */
    fun setIndicatorSize(width: Int, height: Int) {
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            width.toFloat(),
            resources.displayMetrics
        ).toInt()
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            height.toFloat(),
            resources.displayMetrics
        ).toInt()
        indicatorWidth = widthPx.toFloat()
        indicatorHeight = heightPx.toFloat()
        indicatorView?.layoutParams?.width = widthPx
        indicatorView?.layoutParams?.height = heightPx
    }

    /**
     * 设置title和指示器的gap
     * @param gap dp
     */
    fun setTitleIndictorGap(gap: Int) {
        val dpToPx = DipUtils.dpToPx(gap)
        subTitleView?.updatePadding(bottom = dpToPx)
    }

}
