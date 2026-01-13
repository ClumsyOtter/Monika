package com.otto.monika.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.lang.ref.WeakReference
import java.lang.reflect.Field

/**
 * 自定义 BottomSheetBehavior
 * 优化处理多个滚动视图的情况（NestedScrollView + RecyclerView + ViewPager）
 * 
 * 关键改进：
 * 1. 优先识别 NestedScrollView 作为滚动子视图
 * 2. 正确处理 ViewPager 内的 RecyclerView
 * 3. 支持多个滚动视图的协同工作
 * 
 * 工作原理：
 * - 由于 findScrollingChild 是 package-private，无法直接重写
 * - 重写 onLayoutChild 方法，使用自定义逻辑查找滚动子视图
 * - 通过反射设置 nestedScrollingChildRef 字段
 */
class MonikaBottomSheetBehavior<V : View> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BottomSheetBehavior<V>(context, attrs) {

    companion object {
        /**
         * 从 View 获取 Behavior 实例
         */
        @JvmStatic
        fun <V : View> from(view: V): MonikaBottomSheetBehavior<V>? {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
            return params?.behavior as? MonikaBottomSheetBehavior<V>
        }

        // 使用反射获取 nestedScrollingChildRef 字段
        private var nestedScrollingChildRefField: Field? = null

        init {
            try {
                nestedScrollingChildRefField = BottomSheetBehavior::class.java.getDeclaredField("nestedScrollingChildRef")
                nestedScrollingChildRefField?.isAccessible = true
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 重写 onLayoutChild 方法
     * 在布局完成后，使用自定义逻辑查找滚动子视图并设置到 nestedScrollingChildRef
     */
    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: V,
        layoutDirection: Int
    ): Boolean {
        // 先调用父类方法完成布局
        val result = super.onLayoutChild(parent, child, layoutDirection)

        try {
            // 使用自定义逻辑查找滚动子视图
            val scrollingChild = findCustomScrollingChild(child)
            
            // 通过反射设置 nestedScrollingChildRef
            nestedScrollingChildRefField?.set(this, WeakReference(scrollingChild))
        } catch (e: Exception) {
            // 如果设置失败，记录错误但不影响布局
        }

        return result
    }

    /**
     * 自定义查找滚动子视图的逻辑
     * 优先识别 NestedScrollView，如果没有则查找其他滚动视图
     */
    private fun findCustomScrollingChild(view: View): View? {
        if (view !is ViewGroup) {
            return null
        }

        // 优先查找 NestedScrollView（最外层滚动容器）
        val nestedScrollView = findNestedScrollView(view)
        if (nestedScrollView != null) {
            return nestedScrollView
        }

        // 如果没有 NestedScrollView，查找 ViewPager 及其内部的 RecyclerView
        if (view is androidx.viewpager.widget.ViewPager) {
            // ViewPager 内部通常包含可滚动的子视图（如 RecyclerView）
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val scrollingChild = findScrollingChildInViewPager(child)
                if (scrollingChild != null) {
                    return scrollingChild
                }
            }
            // 如果 ViewPager 内部没有可滚动子视图，返回 ViewPager 本身
            return view
        }

        // 如果启用了嵌套滚动，直接返回（RecyclerView 等）
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view
        }

        // 递归查找子视图
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val scrollingChild = findCustomScrollingChild(view.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
            }
        }

        return null
    }

    /**
     * 递归查找 NestedScrollView（优先识别）
     */
    private fun findNestedScrollView(view: View): androidx.core.widget.NestedScrollView? {
        if (view is androidx.core.widget.NestedScrollView) {
            return view
        }
        if (view is ViewGroup) {
            try {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    if (child != null) {
                        findNestedScrollView(child)?.let {
                            return it
                        }
                    }
                }
            } catch (e: Exception) {
                // 如果访问子视图时出错，记录但不抛出异常
            }
        }
        return null
    }

    /**
     * 在 ViewPager 的子视图（Fragment）中查找滚动视图
     */
    private fun findScrollingChildInViewPager(view: View): View? {
        // 查找 RecyclerView
        if (view is androidx.recyclerview.widget.RecyclerView) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is androidx.recyclerview.widget.RecyclerView) {
                    return child
                }
                // 递归查找
                if (child is ViewGroup) {
                    findScrollingChildInViewPager(child)?.let {
                        return it
                    }
                }
            }
        }
        return null
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // 调用父类方法处理基本的滚动逻辑
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        // 添加调试日志
        if (target is androidx.core.widget.NestedScrollView || 
            target is androidx.recyclerview.widget.RecyclerView) {
        }
    }
}

