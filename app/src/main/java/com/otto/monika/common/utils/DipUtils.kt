package com.otto.monika.common.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

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
}
