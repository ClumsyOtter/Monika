package com.otto.monika.common.views.recycleview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.common.views.recycleview.NestedOverScroller.invokeCurrentVelocity
import kotlin.math.abs

/**
 * 父RecyclerView
 *
 */
class ParentRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    private val mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop()

    /**
     * fling时的加速度
     */
    private var mVelocity = 0

    private var mLastTouchY = 0f

    private var mLastInterceptX = 0
    private var mLastInterceptY = 0

    /**
     * 用于向子容器传递 fling 速度
     */
    private val mVelocityTracker: VelocityTracker = VelocityTracker.obtain()
    private var mMaximumFlingVelocity = 0
    private var mMinimumFlingVelocity = 0

    /**
     * 子容器是否消耗了滑动事件
     */
    private var childConsumeTouch = false

    /**
     * 子容器消耗的滑动距离
     */
    private var childConsumeDistance = 0

    init {
        init()
    }

    private fun init() {
        val configuration = ViewConfiguration.get(context)
        mMaximumFlingVelocity = configuration.scaledMaximumFlingVelocity
        mMinimumFlingVelocity = configuration.scaledMinimumFlingVelocity

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    dispatchChildFling()
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mVelocity = 0
                mLastTouchY = ev.getRawY()
                childConsumeTouch = false
                childConsumeDistance = 0

                val childRecyclerView = findNestedScrollingChildRecyclerView()
                if (this.isScrollToBottom && (childRecyclerView != null && !childRecyclerView.isScrollToTop)) {
                    stopScroll()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                childConsumeTouch = false
                childConsumeDistance = 0
            }

            else -> {}
        }

        try {
            return super.dispatchTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (isChildConsumeTouch(event)) {
            // 子容器如果消费了触摸事件，后续父容器就无法再拦截事件
            // 在必要的时候，子容器需调用 requestDisallowInterceptTouchEvent(false) 来允许父容器继续拦截事件
            return false
        }
        // 子容器不消费触摸事件，父容器按正常流程处理
        return super.onInterceptTouchEvent(event)
    }

    /**
     * 子容器是否消费触摸事件
     */
    private fun isChildConsumeTouch(event: MotionEvent): Boolean {
        val x = event.getRawX().toInt()
        val y = event.getRawY().toInt()
        if (event.getAction() != MotionEvent.ACTION_MOVE) {
            mLastInterceptX = x
            mLastInterceptY = y
            return false
        }
        val deltaX = x - mLastInterceptX
        val deltaY = y - mLastInterceptY
        if (abs(deltaX) > abs(deltaY) || abs(deltaY) <= mTouchSlop) {
            return false
        }

        return shouldChildScroll(deltaY)
    }

    /**
     * 子容器是否需要消费滚动事件
     */
    private fun shouldChildScroll(deltaY: Int): Boolean {
        val childRecyclerView = findNestedScrollingChildRecyclerView()
        if (childRecyclerView == null) {
            return false
        }
        if (this.isScrollToBottom) {
            // 父容器已经滚动到底部 且 向下滑动 且 子容器还没滚动到底部
            return deltaY < 0 && !childRecyclerView.isScrollToBottom
        } else {
            // 父容器还没滚动到底部 且 向上滑动 且 子容器已经滚动到顶部
            return deltaY > 0 && !childRecyclerView.isScrollToTop
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (this.isScrollToBottom) {
            // 如果父容器已经滚动到底部，且向上滑动，且子容器还没滚动到顶部，事件传递给子容器
            val childRecyclerView = findNestedScrollingChildRecyclerView()
            if (childRecyclerView != null) {
                val deltaY = (mLastTouchY - e.rawY).toInt()
                if (deltaY >= 0 || !childRecyclerView.isScrollToTop) {
                    mVelocityTracker.addMovement(e)
                    if (e.action == MotionEvent.ACTION_UP) {
                        // 传递剩余 fling 速度
                        mVelocityTracker.computeCurrentVelocity(
                            1000,
                            mMaximumFlingVelocity.toFloat()
                        )
                        val velocityY = mVelocityTracker.yVelocity
                        if (abs(velocityY) > mMinimumFlingVelocity) {
                            childRecyclerView.fling(0, -velocityY.toInt())
                        }
                        mVelocityTracker.clear()
                    } else {
                        // 传递滑动事件
                        childRecyclerView.scrollBy(0, deltaY)
                    }

                    childConsumeDistance += deltaY
                    mLastTouchY = e.rawY
                    childConsumeTouch = true
                    return true
                }
            }
        }
        mLastTouchY = e.rawY
        if (childConsumeTouch) {
            // 在同一个事件序列中，子容器消耗了部分滑动距离，需要扣除掉
            val adjustedEvent = MotionEvent.obtain(
                e.downTime,
                e.eventTime,
                e.action,
                e.x,
                e.y + childConsumeDistance,  // 更新Y坐标
                e.metaState
            )

            val handled = super.onTouchEvent(adjustedEvent)
            adjustedEvent.recycle()
            return handled
        }

        if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) {
            mVelocityTracker.clear()
        }

        try {
            return super.onTouchEvent(e)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }

    override fun fling(velX: Int, velY: Int): Boolean {
        val fling = super.fling(velX, velY)
        if (!fling || velY <= 0) {
            mVelocity = 0
        } else {
            mVelocity = velY
        }
        return fling
    }

    private fun dispatchChildFling() {
        // 父容器滚动到底部后，如果还有剩余加速度，传递给子容器
        if (this.isScrollToBottom && mVelocity != 0) {
            // 尽量让速度传递更加平滑
            var mVelocity = invokeCurrentVelocity(this)
            if (abs(mVelocity) <= 2.0E-5f) {
                mVelocity = this.mVelocity.toFloat() * 0.5f
            } else {
                mVelocity *= 0.46f
            }
            val childRecyclerView = findNestedScrollingChildRecyclerView()
            childRecyclerView?.fling(0, mVelocity.toInt())
        }
        mVelocity = 0
    }

    fun findNestedScrollingChildRecyclerView(): ChildRecyclerView? {
        if (adapter is INestedParentAdapter) {
            return (adapter as INestedParentAdapter).getCurrentChildRecyclerView()
        }
        return null
    }

    val isScrollToBottom: Boolean
        get() = !canScrollVertically(1)

    val isScrollToTop: Boolean
        get() = !canScrollVertically(-1)

    override fun scrollToPosition(position: Int) {
        checkChildNeedScrollToTop(position)

        super.scrollToPosition(position)
    }

    override fun smoothScrollToPosition(position: Int) {
        checkChildNeedScrollToTop(position)

        super.smoothScrollToPosition(position)
    }

    private fun checkChildNeedScrollToTop(position: Int) {
        if (position == 0) {
            // 父容器滚动到顶部，从交互上来说子容器也需要滚动到顶部
            val childRecyclerView = findNestedScrollingChildRecyclerView()
            childRecyclerView?.scrollToPosition(0)
        }
    }
}
