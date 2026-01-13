package com.otto.monika.home.fragment.artwork.model

/**
 * 用户艺术品数据模型
 */
data class UserArtworkModel(
    val id: String,
    val imageUrl: String? = null, // 图片URL
    val title: String, // 标题
    val content: String, // 内容
    val price: String, // 价格
    val discount: String? = null, // 折扣信息
    val isUnlocked: Boolean = false // 是否已解锁
)

