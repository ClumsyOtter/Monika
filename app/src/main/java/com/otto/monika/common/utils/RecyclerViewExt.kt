package com.otto.monika.common.utils

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 设置嵌套水平滑动处理，解决内层水平 RecyclerView 与外层水平 RecyclerView 的滑动冲突
 * 只有当内层 RecyclerView 能够滑动时，才阻止父 RecyclerView 拦截触摸事件
 *
 * 使用示例：
 * ```
 * recyclerView.setupNestedHorizontalScroll()
 * ```
 */
fun RecyclerView.setupNestedHorizontalScroll() {
    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var initialX = 0f
    var initialY = 0f

    addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = e.x
                    initialY = e.y
                    // 只有当内层 RecyclerView 可以滑动时，才阻止父 RecyclerView 拦截触摸事件
                    if (rv.canScrollHorizontally(1) || rv.canScrollHorizontally(-1)) {
                        rv.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(e.x - initialX)
                    val deltaY = abs(e.y - initialY)
                    // 只有当内层 RecyclerView 可以滑动时，才处理滑动冲突
                    if (rv.canScrollHorizontally(1) || rv.canScrollHorizontally(-1)) {
                        // 如果主要是水平滑动，继续阻止父 RecyclerView 拦截
                        // 如果主要是垂直滑动，允许父 RecyclerView 拦截（虽然外层是水平滑动，但为了兼容性保留此逻辑）
                        if (deltaX > touchSlop && deltaX > deltaY) {
                            rv.parent?.requestDisallowInterceptTouchEvent(true)
                        } else if (deltaY > touchSlop && deltaY > deltaX) {
                            rv.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    } else {
                        // 如果内层 RecyclerView 不能滑动，允许父 RecyclerView 拦截
                        rv.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    rv.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            return false
        }
    })
}

fun RecyclerView.setEmptyAreaClickListener(onClickListener: View.OnClickListener) {
    val gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent) {}

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClickListener.onClick(this@setEmptyAreaClickListener)
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }
    })
    setOnTouchListener { v, event ->
        if (v is RecyclerView) {
            return@setOnTouchListener gestureDetector.onTouchEvent(event)
        }
        return@setOnTouchListener false
    }
}

