package com.otto.monika.subscribe.rank.model

import com.otto.monika.api.model.user.response.MonikaUserInfoModel


data class RankModel(
    var rank: String? = null, var creator: MonikaUserInfoModel? = null
) {
    var isMeasured: Boolean = false
    var isNeedScroll: Boolean = false
}