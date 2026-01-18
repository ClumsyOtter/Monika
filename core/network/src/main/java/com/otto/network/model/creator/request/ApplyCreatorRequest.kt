package com.otto.network.model.creator.request

/**
 * 申请成为创作者请求参数
 */
data class ApplyCreatorRequest(
    val uid: String? = null, // 用户ID
    val goodAtId: String? = null, // 擅长领域ID数组转字符串（逗号分隔）
    val selfTagId: String? = null, // 自我标签ID数组转字符串（逗号分隔）
    val socialMediaId: String? = null, // 社交媒体ID
    val socialMediaUrl: String? = null, // 社交媒体主页地址
    val realName: String? = null, // 真实姓名
    val contact: String? = null // 联系方式
)

