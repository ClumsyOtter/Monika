package com.otto.monika.home.adapter.listener

import com.otto.network.model.home.MonikaBannerItem
import com.otto.network.model.home.RankModel
import com.otto.network.model.home.SubscribeModel

abstract class MonikaPageListener {
    abstract fun onRankMoreClick()
    abstract fun onRankItemClick(model: RankModel)
    abstract fun onSubscribeItemClick(subscribeModel: SubscribeModel)
    abstract fun oSubscribeMoreClick()
    abstract fun onBannerItemClick(bannerItem: MonikaBannerItem)
}