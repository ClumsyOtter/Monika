package com.otto.monika.home.model

class SubscribeModel {
    //        "id": 1,
    //        "title": "动态标题",
    //        "content": "动态内容",
    //        "images": "image1.jpg,image2.jpg",
    //        "uid": 1,
    //        "visible_type": 0,
    //        "status": 1,
    //        "created_at": "2024-01-01 00:00:00",
    //        "updated_at": "2024-01-01 00:00:00",
    //        "user": {
    //          "id": 1,
    //          "avatar": "https://example.com/avatar.jpg",
    //          "nickname": "张三"
    //        },
    //        "is_subscribed": true,
    //        "subscribe_remainingtime": 10,
    //        "is_liked": true,
    //        "is_collected": true
    var id: String? = null
    var title: String? = null
    var content: String? = null
    var images: MutableList<String?>? = null
    var uid: String? = null
    var visible_type: String? = null
    var status: String? = null
    var user: User? = null
    var is_subscribed: Boolean = false
    var subscribe_remainingtime: Int = 0
    var is_liked: Boolean = false
    var is_collected: Boolean = false


    class User {
        var id: String? = null
        var avatar: String? = null
        var nickname: String? = null
    }
}
