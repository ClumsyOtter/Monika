package com.otto.monika.home.adapter.listener

import com.otto.monika.home.model.MonikaBannerItem
import com.otto.monika.home.model.SubscribeModel
import com.otto.monika.subscribe.rank.model.RankModel

abstract class MonikaPageListener {
    abstract fun onRankMoreClick()
    abstract fun onRankItemClick(model: RankModel)
    abstract fun onSubscribeItemClick(subscribeModel: SubscribeModel)
    abstract fun oSubscribeMoreClick()
    abstract fun onBannerItemClick(bannerItem: MonikaBannerItem)
}