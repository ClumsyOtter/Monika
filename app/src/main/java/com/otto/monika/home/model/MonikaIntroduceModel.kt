package com.otto.monika.home.model

import com.otto.monika.api.model.user.response.Creator


class MonikaIntroduceModel {
    //        "x": 0,
    //        "y": 0,
    //        "value": 1
    var x: Int = 0
    var y: Int = 0
    var value: String? = null

    val isEmptyItem: Boolean
        get() = "0".equals(value, ignoreCase = true)

    //        "id": 1,
    //        "title": "动态标题",
    //        "content": "动态内容...",
    //        "images": "image1.jpg,image2.jpg",
    //        "uid": 1,
    //        "avatar": "https://example.com/avatar.jpg",
    //        "nickname": "张三"
    //{
    //        "id": 6718,
    //        "title": "今天和朋友一起玩耍",
    //        "content": "发现了一个新的兴趣爱好，很有意思。",
    //        "images": [
    //          "https://cos.chelun.com/static/20251124/0ef6d48d-e530-45e5-bbe2-56c5ec0632ec_720_450.jpeg",
    //          "https://cos.chelun.com/static/20251124/93ab64a0-3852-4cb6-be7b-dd7093f7f9bc_720_450.jpeg",
    //          "https://cos.chelun.com/static/20251124/ab40c2d8-31d3-457a-b8d4-4423425015cf_720_450.jpeg"
    //        ],
    //        "uid": 1336,
    //        "user": {
    //          "id": 1336,
    //          "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=981",
    //          "nickname": "小王子9425"
    //        }
    //      }
    var id: String? = null
    var uid: String? = null
    var title: String? = null
    var content: String? = null
    var images: MutableList<String?>? = null
    var user: Creator? = null
}
