package com.otto.monika.home.fragment.mine.listener

import com.otto.monika.api.model.user.response.MonikaUserInfoModel

abstract class AccountHeadListener {
    //点击订阅方案
    open fun onSubscribePlanClick(profileResponse: MonikaUserInfoModel) {}

    //点击作品上传
    open fun onUploadPostClick(profileResponse: MonikaUserInfoModel) {}

    //点击我的收入
    open fun onMyIncomeClick(profileResponse: MonikaUserInfoModel) {}

    //点击我的fans
    open fun onMyFansClick(profileResponse: MonikaUserInfoModel) {}

    //点击申请成为创作者
    open fun onApplyToCreatorClick(profileResponse: MonikaUserInfoModel) {}

    open fun onEditNameClick(profileResponse: MonikaUserInfoModel) {

    }
}