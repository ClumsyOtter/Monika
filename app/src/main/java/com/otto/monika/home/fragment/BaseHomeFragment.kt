package com.otto.monika.home.fragment

import android.content.Context
import com.otto.monika.common.base.MonikaBaseFragment

abstract class BaseHomeFragment : MonikaBaseFragment() {
    var homeActivityCallBack: IHomeActivityCallBack? = null

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        if (activity is IHomeActivityCallBack) {
            homeActivityCallBack = activity as IHomeActivityCallBack
        }
    }

    override fun onDetach() {
        super.onDetach()
        homeActivityCallBack = null
    }

    open fun onNavBarAlphaChange(value: Float) {
        if (homeActivityCallBack != null) {
            homeActivityCallBack!!.onNavBarAlphaChange(value)
        }
    }
}
