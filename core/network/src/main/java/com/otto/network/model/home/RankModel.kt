package com.otto.network.model.home

import com.otto.network.model.user.response.MonikaUserInfoModel

data class RankModel(
    var rank: String? = null, var creator: MonikaUserInfoModel? = null
) {
    var isMeasured: Boolean = false
    var isNeedScroll: Boolean = false
}