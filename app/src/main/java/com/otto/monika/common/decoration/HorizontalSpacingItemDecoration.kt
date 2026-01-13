package com.otto.monika.common.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.common.utils.DipUtils

class HorizontalSpacingItemDecoration(spacingDp: Int = 5) :
    RecyclerView.ItemDecoration() {
    val spacing = DipUtils.dpToPx(spacingDp)
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        // 第一个 item 的左边添加间距
        if (position == 0) {
            outRect.left = spacing
        }
        // 每个 item 的右边添加间距（包括最后一个）
        outRect.right = spacing
    }
}