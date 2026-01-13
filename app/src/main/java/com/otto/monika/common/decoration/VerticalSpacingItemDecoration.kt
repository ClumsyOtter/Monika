package com.otto.monika.common.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.common.utils.DipUtils

/**
 * 垂直间距装饰器，用于在垂直滚动的 RecyclerView 中为每个 item 添加底部间距
 */
class VerticalSpacingItemDecoration(spacingDp: Int = 5) :
    RecyclerView.ItemDecoration() {
    val spacing = DipUtils.dpToPx(spacingDp)
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
            outRect.bottom = spacing
        }
    }
}