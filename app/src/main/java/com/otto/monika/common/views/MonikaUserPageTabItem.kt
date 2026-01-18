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
import com.otto.monika.R
import androidx.core.content.withStyledAttributes

class MonikaUserPageTabItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        subTitleView = findViewById(R.id.sub_title_view)
        indicatorView = findViewById(R.id.indicator_view)
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
    }

    val layoutId: Int
        get() = R.layout.monika_user_page_tab_item

    override fun setSelected(selected: Boolean) {
        this.isTabSelected = selected
        if (this.isTabSelected) {
            subTitleView?.setTextColor(subTitleSelectedColor)
            subTitleView?.setTypeface(null, Typeface.BOLD)
            indicatorView?.setVisibility(VISIBLE)
        } else {
            subTitleView?.setTextColor(subTitleUnselectedColor)
            subTitleView?.setTypeface(null, Typeface.NORMAL)
            indicatorView?.setVisibility(INVISIBLE)
        }
    }
}
