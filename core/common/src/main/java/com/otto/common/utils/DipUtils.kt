package com.otto.common.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

/**
 * px与dip转换公式
 * http://jinganglang777.blog.163.com/blog/static/790854972011911111537418/ pixs
 * =dips * (densityDpi/160). dips=(pixs*160)/densityDpi
 *
 */
object DipUtils {
    fun dip2px(context: Context?, dipValue: Float): Int {
        if (context == null) {
            return 0
        }
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }


    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    /**
     * 屏幕的宽度
     *
     * @param context
     * @return
     */
    fun getScreenWidth(context: Context?): Int {
        if (context == null) {
            return 1
        } else {
            if (context is Activity) {
                val metric = DisplayMetrics()
                context.windowManager.getDefaultDisplay().getMetrics(metric)
                return metric.widthPixels
            } else {
                return 1
            }
        }
    }

    fun getScreenHeight(context: Context?): Int {
        val wm = context?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return wm?.getDefaultDisplay()?.getHeight() ?: 0
    }
}
