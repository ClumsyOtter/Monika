package com.otto.monika.home.fragment

import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.R

/**
 * 空白内容Fragment
 * 用于显示空状态
 */
class EmptyContentFragment : MonikaBaseFragment() {

    override fun getContentViewId(): Int {
        return R.layout.fragment_empty_content
    }

    override fun onFinishCreateView() {}
}