package com.otto.ui.views.recycleview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 子RecyclerView
 */
class ChildRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    private var mParentRecyclerView: ParentRecyclerView? = null

    /**
     * fling时的加速度
     */
    private var mVelocity = 0

    private var mLastInterceptX = 0

    private var mLastInterceptY = 0

    init {
        init()
    }

    private fun init() {
        setOverScrollMode(OVER_SCROLL_NEVER)

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    dispatchParentFling()
                }
            }
        })
    }

    private fun dispatchParentFling() {
        ensureParentRecyclerView()
        // 子容器滚动到顶部，如果还有剩余加速度，就交给父容器处理
        if (mParentRecyclerView != null && this.isScrollToTop && mVelocity != 0) {
            // 尽量让速度传递更加平滑
            var velocityY = NestedOverScroller.invokeCurrentVelocity(this)
            if (abs(velocityY) <= 2.0E-5f) {
                velocityY = this.mVelocity.toFloat() * 0.5f
            } else {
                velocityY *= 0.65f
            }
            mParentRecyclerView!!.fling(0, velocityY.toInt())
            mVelocity = 0
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mVelocity = 0
        }
        val x = ev.rawX.toInt()
        val y = ev.rawY.toInt()
        if (ev.action != MotionEvent.ACTION_MOVE) {
            mLastInterceptX = x
            mLastInterceptY = y
        }
        val deltaX = x - mLastInterceptX
        val deltaY = y - mLastInterceptY
        if (this.isScrollToTop && abs(deltaX) <= abs(deltaY) && parent != null) {
            // 子容器滚动到顶部，继续向上滑动，此时父容器需要继续拦截事件。与父容器 onInterceptTouchEvent 对应
            parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        if (!isAttachedToWindow) return false
        val fling = super.fling(velocityX, velocityY)
        mVelocity = if (!fling || velocityY >= 0) {
            0
        } else {
            velocityY
        }
        return fling
    }

    val isScrollToTop: Boolean
        get() = !canScrollVertically(-1)

    val isScrollToBottom: Boolean
        get() = !canScrollVertically(1)

    private fun ensureParentRecyclerView() {
        if (mParentRecyclerView == null) {
            var parentView = parent
            while (parentView !is ParentRecyclerView) {
                parentView = parentView.parent
            }
            mParentRecyclerView = parentView
        }
    }
}
