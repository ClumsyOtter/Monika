package com.otto.monika.common.decoration

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.common.utils.DipUtils

/**
 * 分割线装饰器
 * 在 RecyclerView 的 item 之间绘制分割线
 * @param heightDp 分割线高度（dp）
 * @param colorResId 分割线颜色资源 ID
 */
class DividerItemDecoration(
    private val heightDp: Int = 3,
    private val colorResId: Int = R.color.bg_f5f5f5
) : RecyclerView.ItemDecoration() {

    private val height = DipUtils.dpToPx(heightDp)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val context = parent.context
        paint.color = ContextCompat.getColor(context, colorResId)

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val itemCount = state.itemCount

            // 不在最后一个 item 下方绘制分割线
            if (position < itemCount - 1) {
                val left = child.left
                val right = child.right
                val top = child.bottom
                val bottom = top + height
                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // 每个 item 的底部添加间距（最后一个除外）
        if (position < itemCount - 1) {
            outRect.bottom = height
        }
    }
}

