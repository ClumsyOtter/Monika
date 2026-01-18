package com.otto.network.model.home



data class MonikaIntroduceData(var list: MutableList<MonikaIntroduceModel>?)

data class MonikaPostData(var list: MutableList<MonikaIntroduceModel>?)

data class MonikaBannerData(var list: MutableList<MonikaBannerItem>? = null)

data class MonikaSubscribeData(var list: MutableList<SubscribeModel>? = null, val total: Int = 0)

data class MonikaRankData(var list: MutableList<RankModel>? = null, val total: Int = 0)

data class MonikaBannerItem(
    val id: Long? = null,
    val title: String? = null,
    val detail: String? = null,
    var image_url: String? = null,
    var link_url: String? = null
)