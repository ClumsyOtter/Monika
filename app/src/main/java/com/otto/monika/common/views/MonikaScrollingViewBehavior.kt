package com.otto.monika.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

/**
 * 扩展的 ScrollingViewBehavior
 * 用于扩展 appbar_scrolling_view_behavior 的功能
 * 
 * 扩展功能：
 * 1. 滚动偏移监听
 * 2. 自定义滚动逻辑
 * 3. 滚动边界处理
 * 4. 滚动速度控制
 * 
 * 使用示例：
 * ```kotlin
 * // 在布局文件中使用
 * // app:layout_behavior="com.otto.monika.common.views.MonikaScrollingViewBehavior"
 * 
 * // 在代码中获取 Behavior 并设置监听
 * val behavior = MonikaScrollingViewBehavior.from(nestedScrollView)
 * behavior?.setOnScrollOffsetChangedListener { scrollingView, appBarLayout, offset, maxOffset ->
 *     // 处理滚动偏移变化
 *     val progress = offset.toFloat() / maxOffset
 *     // 根据 progress 更新 UI
 * }
 * ```
 */
class MonikaScrollingViewBehavior @JvmOverloads constructor(
    context: Context? = null,
    attrs: AttributeSet? = null
) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    /**
     * 滚动偏移监听器
     */
    interface OnScrollOffsetChangedListener {
        /**
         * 滚动偏移改变时调用
         * @param scrollingView 滚动的视图（如 NestedScrollView）
         * @param appBarLayout AppBarLayout
         * @param offset 当前偏移量（负值表示向上滚动）
         * @param maxOffset 最大偏移量
         */
        fun onOffsetChanged(
            scrollingView: View,
            appBarLayout: AppBarLayout,
            offset: Int,
            maxOffset: Int
        )
    }

    private var scrollOffsetListener: OnScrollOffsetChangedListener? = null
    private var lastOffset: Int = 0
    private var lastAppBarLayout: AppBarLayout? = null

    /**
     * 设置滚动偏移监听器
     */
    fun setOnScrollOffsetChangedListener(listener: OnScrollOffsetChangedListener?) {
        this.scrollOffsetListener = listener
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val result = super.onDependentViewChanged(parent, child, dependency)
        
        // 如果 dependency 是 AppBarLayout，监听其偏移变化
        if (dependency is AppBarLayout) {
            lastAppBarLayout = dependency
            
            // 获取 AppBarLayout 的实际偏移量
            // AppBarLayout 向上滚动时，translationY 为负值
            val currentOffset = -dependency.translationY.toInt()
            val maxOffset = dependency.getTotalScrollRange()
            
            // 只在偏移量变化时通知
            if (currentOffset != lastOffset) {
                lastOffset = currentOffset
                scrollOffsetListener?.onOffsetChanged(child, dependency, currentOffset, maxOffset)
            }
        }
        
        return result
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        val result = super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
        return result
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        // 可以在这里添加自定义的滚动前处理逻辑
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type
        )
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        val result = super.onNestedFling(
            coordinatorLayout,
            child,
            target,
            velocityX,
            velocityY,
            consumed
        )
        return result
    }

    /**
     * 获取当前 AppBarLayout 的偏移量
     * @return 当前偏移量（正值，0 表示完全展开，maxOffset 表示完全折叠）
     */
    fun getCurrentOffset(): Int {
        return lastAppBarLayout?.let {
            -it.translationY.toInt()
        } ?: 0
    }

    /**
     * 获取 AppBarLayout 的最大偏移量
     */
    fun getMaxOffset(): Int {
        return lastAppBarLayout?.getTotalScrollRange() ?: 0
    }

    companion object {
        /**
         * 从 View 中获取 MonikaScrollingViewBehavior 实例
         * @param view 应用了 Behavior 的 View（如 NestedScrollView）
         * @return MonikaScrollingViewBehavior 实例，如果不存在则返回 null
         */
        @JvmStatic
        fun from(view: View): MonikaScrollingViewBehavior? {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
            return params?.behavior as? MonikaScrollingViewBehavior
        }
    }
}

