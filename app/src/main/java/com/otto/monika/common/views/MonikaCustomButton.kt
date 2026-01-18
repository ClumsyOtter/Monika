package com.otto.monika.common.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import com.otto.common.utils.getView
import com.otto.monika.R


class MonikaCustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val btnImageView: ImageView by getView(R.id.btn_bg_image_view)
    val btnTitleView: TextView by getView(R.id.btn_title_view)

    init {
        LayoutInflater.from(this.context).inflate(R.layout.monika_custom_button, this)
        attrs?.let {
            context.withStyledAttributes(attrs, R.styleable.MonikaCustomButton) {

                // 解析宽高比
                val mWHRatio = getFloat(R.styleable.MonikaCustomButton_whRatio, 1.0f)

                // 解析图片
                val image = getDrawable(R.styleable.MonikaCustomButton_monikaImage)
                image?.let {
                    // 解析 tint 颜色
                    val tintColor = getColor(R.styleable.MonikaCustomButton_monikaImageTint, 0)
                    if (tintColor != 0) {
                        // 对 drawable 进行 tint 操作
                        val tintedDrawable = DrawableCompat.wrap(it.mutate())
                        DrawableCompat.setTint(tintedDrawable, tintColor)
                        btnImageView.setBackgroundDrawable(tintedDrawable)
                    } else {
                        btnImageView.setBackgroundDrawable(it)
                    }
                }

                // 解析Text左边图片
                val textLeftDrawable =
                    getDrawable(R.styleable.MonikaCustomButton_monikaTitleLeftDrawable)
                textLeftDrawable?.let {
                    // 设置TextView左侧drawable，其他方向为null
                    btnTitleView.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
                }

                // 解析标题文字
                val title = getString(R.styleable.MonikaCustomButton_monikaTitle)
                btnTitleView.text = title ?: ""

                // 解析标题字体大小
                val titleFontSize = getDimension(
                    R.styleable.MonikaCustomButton_monikaTitleFontSize,
                    0f
                )
                if (titleFontSize > 0f) {
                    btnTitleView.textSize = titleFontSize / resources.displayMetrics.scaledDensity
                }

                // 解析标题文字颜色
                val titleTextColor =
                    getColorStateList(R.styleable.MonikaCustomButton_monikaTitleTextColor)
                titleTextColor?.let {
                    btnTitleView.setTextColor(it)
                }

            }
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(text: String) {
        btnTitleView.text = text
    }

    /**
     * 设置标题字体大小（sp）
     */
    fun setTitleSize(sizeSp: Float) {
        btnTitleView.textSize = sizeSp
    }

    /**
     * 设置标题文字颜色
     */
    fun setTextColor(titleTextColor: Int) {
        btnTitleView.setTextColor(titleTextColor)
    }

    /**
     * 设置背景图片（Drawable）
     */
    fun setBackgroundImage(drawable: Drawable?) {
        drawable?.let {
            btnImageView.setBackgroundDrawable(it)
        } ?: run {
            btnImageView.background = null
        }
    }

    /**
     * 设置背景图片（资源ID）
     */
    fun setBackgroundImageRes(resId: Int) {
        if (resId != 0) {
            val drawable = ContextCompat.getDrawable(context, resId)
            setBackgroundImage(drawable)
        } else {
            btnImageView.background = null
        }
    }

    /**
     * 设置背景图片 tint 颜色
     */
    fun setBackgroundImageTint(color: Int) {
        val background = btnImageView.background
        background?.let {
            val tintedDrawable = DrawableCompat.wrap(it.mutate())
            DrawableCompat.setTint(tintedDrawable, color)
            btnImageView.setBackgroundDrawable(tintedDrawable)
        }
    }

    /**
     * 设置标题左边图片（Drawable）
     */
    fun setTitleLeftDrawable(drawable: Drawable?) {
        btnTitleView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    /**
     * 设置标题左边图片（资源ID）
     */
    fun setTitleLeftDrawableRes(resId: Int) {
        if (resId != 0) {
            val drawable = ContextCompat.getDrawable(context, resId)
            setTitleLeftDrawable(drawable)
        } else {
            setTitleLeftDrawable(null)
        }
    }

    /**
     * 设置标题图片间距（px）
     */
    fun setTitleDrawablePadding(paddingPx: Int) {
        btnTitleView.compoundDrawablePadding = paddingPx
    }

    /**
     * 设置标题图片间距（dp）
     */
    fun setTitleDrawablePaddingDp(paddingDp: Int) {
        val paddingPx = (paddingDp * context.resources.displayMetrics.density).toInt()
        setTitleDrawablePadding(paddingPx)
    }

}