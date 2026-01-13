package com.otto.monika.common.model

import androidx.lifecycle.Lifecycle
import com.otto.monika.common.base.MonikaBaseFragment

class MonikaFragmentRefreshState(
    val cyBaseFragment: MonikaBaseFragment, private val doRefresh: (Boolean) -> Unit
) {
    //是否需要重新选中的时候刷新当前页面
    private var shouldRefreshWhenSelected = false

    //是否需要进入onResume的时候触发
    private var shouldRefreshOnResume = false


    fun onFragmentSelected(isSelect: Boolean) {
        if (shouldRefreshWhenSelected) {
            doRefresh.invoke(true)
            shouldRefreshWhenSelected = false
        } else {
            doRefresh.invoke(false)
        }
    }

    fun onResume() {
        if (shouldRefreshOnResume) {
            doRefresh.invoke(true)
            shouldRefreshOnResume = false
        } else {
            doRefresh.invoke(false)
        }
    }

    private fun isFragmentStopped(): Boolean {
        return cyBaseFragment.lifecycle.currentState < Lifecycle.State.STARTED
    }

    private fun isFragmentSelected(): Boolean {
        return cyBaseFragment.isFragmentSelected
    }

    fun refresh() {
        if (isFragmentSelected()) {
            if (isFragmentStopped()) {
                shouldRefreshWhenSelected = false
                shouldRefreshOnResume = true
            } else {
                doRefresh.invoke(true)
            }
        } else {
            shouldRefreshWhenSelected = true
            shouldRefreshOnResume = false
        }
    }

}