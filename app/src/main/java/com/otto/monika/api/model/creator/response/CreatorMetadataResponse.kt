package com.otto.monika.api.model.creator.response

import com.google.gson.annotations.SerializedName
import com.otto.monika.common.dialog.model.SubscribeRights

/**
 * 创作者申请页面元数据响应
 */
data class CreatorMetadataResponse(
    @SerializedName("goodAt")
    val goodAt: List<CreatorMetadataItem> = emptyList(), // 擅长领域

    @SerializedName("selfTag")
    val selfTag: List<CreatorMetadataItem> = emptyList(), // 自我标签

    @SerializedName("socialMedia")
    val socialMedia: List<CreatorMetadataItem> = emptyList(), // 社交媒体

    //订阅者权益
    val subRight: List<SubscribeRights> = emptyList()
)

