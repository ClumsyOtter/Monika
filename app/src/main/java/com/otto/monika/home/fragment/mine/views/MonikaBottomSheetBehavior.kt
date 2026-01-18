package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.otto.monika.R
import java.lang.reflect.Field
import kotlin.math.abs

class MonikaBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    BottomSheetBehavior<V>(context, attrs) {
    private var initialX = 0f
    private var initialY = 0f
    private var verticalOffset = 0f
    private var isInitialStateSet = false

    init {
        // 通过反射设置默认状态为展开，这样在 onLayoutChild 中会直接设置位置，不会有动画
        try {
            if (stateField != null) {
                stateField!!.set(this, STATE_EXPANDED)
                isInitialStateSet = true
            }
        } catch (e: Exception) {
        }
    }

    fun setVerticalOffset(verticalOffset: Float) {
        this.verticalOffset = verticalOffset
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        val recyclerView = child.findViewById<RecyclerView?>(R.id.base_list)
        val appBarLayout = child.findViewById<AppBarLayout?>(R.id.appBarLayout)
        // 如果recyclerView能滑动的话，那么就不拦截事件
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Record the initial touch position
                initialX = event.x
                initialY = event.getY()
            }

            MotionEvent.ACTION_MOVE -> {
                // Calculate movement distances
                val deltaX = event.getX() - initialX
                val deltaY = event.getY() - initialY

                // Check if the vertical movement is greater than horizontal movement and is downward
                val isVerScroll = abs(deltaY) > abs(deltaX)
                if (getState() == STATE_EXPANDED && isVerScroll && deltaY > 0) {
                    val isRecyclerViewIsCanScroll =
                        recyclerView != null && recyclerView.canScrollVertically(-1)
                    if (isRecyclerViewIsCanScroll) {
                        return super.onInterceptTouchEvent(parent, child, event)
                    }
                    // 说明子View有滚动
                    if (abs(verticalOffset) > 0) {
                        return super.onInterceptTouchEvent(parent, child, event)
                    }
                    return true
                }
                //
                if (getState() == STATE_HALF_EXPANDED && isVerScroll) {
                    return true
                }

                if (getState() == STATE_COLLAPSED && isVerScroll) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(parent, child, event)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        // 如果是首次布局且还未设置初始状态，通过反射设置
        if (!isInitialStateSet && getState() != STATE_EXPANDED) {
            isInitialStateSet = true
            try {
                if (stateField != null) {
                    stateField!!.set(this, STATE_EXPANDED)
                }
            } catch (e: Exception) {
            }
        }
        return super.onLayoutChild(parent, child!!, layoutDirection)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return super.onTouchEvent(parent, child!!, event)
    }

    companion object {
        private var stateField: Field? = null

        init {
            try {
                stateField = BottomSheetBehavior::class.java.getDeclaredField("state")
                stateField!!.setAccessible(true)
            } catch (e: Exception) {
            }
        }
    }
}