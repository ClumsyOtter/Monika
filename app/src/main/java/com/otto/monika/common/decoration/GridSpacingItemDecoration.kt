package com.otto.monika.common.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class GridSpacingItemDecoration(// 列数
    private val spanCount: Int, // 间距
    private val spacing: Int, // 是否包含边缘
    private val includeEdge: Boolean
) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 获取项的位置
        val column = position % spanCount // 列数

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount // 左边距
            outRect.right = (column + 1) * spacing / spanCount // 右边距

            if (position < spanCount) { // 顶部边距
                outRect.top = spacing
            }
            outRect.bottom = spacing // 底部边距
        } else {
            outRect.left = column * spacing / spanCount // 左边距
            outRect.right = spacing - (column + 1) * spacing / spanCount // 右边距
            if (position >= spanCount) {
                outRect.top = spacing // 顶部边距
            }
        }
    }
}