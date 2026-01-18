package com.otto.ui.views.recycleview

import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Field


/**
 * 平滑滚动效果的辅助类
 */
object NestedOverScroller {
    @JvmStatic
    fun invokeCurrentVelocity(rv: RecyclerView): Float {
        try {
            var viewFlinger: Field? = null
            var superClass: Class<*>? = rv.javaClass.getSuperclass()
            while (superClass != null) {
                try {
                    viewFlinger = superClass.getDeclaredField("mViewFlinger")
                    break
                } catch (ignored: Throwable) {
                }
                superClass = superClass.getSuperclass()
            }

            if (viewFlinger == null) {
                return 0.0f
            } else {
                viewFlinger.isAccessible = true
                val viewFlingerValue = viewFlinger.get(rv)
                val scroller = viewFlingerValue!!.javaClass.getDeclaredField("mScroller")
                scroller.isAccessible = true
                val scrollerValue = scroller.get(viewFlingerValue)
                val scrollerY = scrollerValue!!.javaClass.getDeclaredField("mScrollerY")
                scrollerY.isAccessible = true
                val scrollerYValue = scrollerY.get(scrollerValue)
                val currVelocity = scrollerYValue!!.javaClass.getDeclaredField("mCurrVelocity")
                currVelocity.isAccessible = true
                return (currVelocity.get(scrollerYValue) as kotlin.Float?)!!
            }
        } catch (ignored: Throwable) {
            return 0.0f
        }
    }
}
