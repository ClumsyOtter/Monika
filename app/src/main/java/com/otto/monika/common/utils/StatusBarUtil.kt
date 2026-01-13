package com.otto.monika.common.utils

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isNotEmpty

object StatusBarUtil {
    /**
     * 代码实现android:fitsSystemWindows
     */
    fun setRootViewFitsSystemWindows(activity: Activity, fitSystemWindows: Boolean) {
        val winContent = activity.findViewById<ViewGroup>(android.R.id.content)
        if (winContent.isNotEmpty()) {
            val rootView = winContent.getChildAt(0) as ViewGroup?
            rootView?.fitsSystemWindows = fitSystemWindows
        }
    }


    //获取状态栏高度
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}
