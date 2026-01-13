package com.otto.monika.home.model

import com.otto.monika.subscribe.rank.model.RankModel


data class MonikaIntroduceData(var list: MutableList<MonikaIntroduceModel>?)

data class MonikaPostData(var list: MutableList<MonikaIntroduceModel>?)

data class MonikaBannerData(var list: MutableList<MonikaBannerItem>? = null)

data class MonikaSubscribeData(var list: MutableList<SubscribeModel>? = null, val total: Int = 0)

data class MonikaRankData(var list: MutableList<RankModel>? = null, val total: Int = 0)

data class MonikaBannerItem(
    //        "id": 1,
    //        "title": "活动Banner",
    //        "image_url": "https://example.com/banner.jpg",
    //        "link_url": "https://example.com/link",
    //        "position": 0,
    //        "status": 1,
    //        "sort": 0,
    //        "start_time": 1640995200,
    //        "end_time": 1672531199,
    //        "created_at": "2024-01-01 00:00:00",
    //        "updated_at": "2024-01-01 00:00:00"
    val id: Long? = null,
    val title: String? = null,
    val detail: String? = null,
    var image_url: String? = null,
    var link_url: String? = null
)