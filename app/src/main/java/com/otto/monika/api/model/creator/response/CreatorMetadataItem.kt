package com.otto.monika.api.model.creator.response

/**
 * 创作者元数据项（擅长领域、自我标签、社交媒体）
 */
data class CreatorMetadataItem(
    val id: String? = null,
    val name: String? = null,
    var isSelected: Boolean? = null
)

