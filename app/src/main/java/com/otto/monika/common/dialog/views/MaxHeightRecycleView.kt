package com.otto.monika.common.dialog.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.withStyledAttributes
import com.otto.monika.R

/**
 * 支持最大高度的 RecyclerView
 */
class MaxHeightRecycleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var maxHeight: Int = -1

    init {
        attrs?.let { attrSet ->
            context.withStyledAttributes(attrSet, R.styleable.MaxHeightRecycleView) {
                maxHeight = getDimensionPixelSize(R.styleable.MaxHeightRecycleView_maxHeight, -1)
            }
        }
    }

    /**
     * 设置最大高度（单位：px）
     * @param maxHeightPx 最大高度（像素）
     */
    fun setMaxHeight(maxHeightPx: Int) {
        if (this.maxHeight != maxHeightPx) {
            this.maxHeight = maxHeightPx
            requestLayout()
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val hSize = MeasureSpec.getSize(heightSpec)
        val heightMeasureSpec = if (maxHeight < 0) {
            heightSpec
        } else {
            when (MeasureSpec.getMode(heightSpec)) {
                MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(
                    hSize.coerceAtMost(maxHeight),
                    MeasureSpec.AT_MOST
                )
                MeasureSpec.UNSPECIFIED -> MeasureSpec.makeMeasureSpec(
                    maxHeight,
                    MeasureSpec.AT_MOST
                )
                MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(
                    hSize.coerceAtMost(maxHeight),
                    MeasureSpec.EXACTLY
                )
                else -> MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            }
        }
        super.onMeasure(widthSpec, heightMeasureSpec)
    }
}