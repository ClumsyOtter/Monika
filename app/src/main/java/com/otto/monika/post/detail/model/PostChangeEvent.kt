package com.otto.monika.post.detail.model

data class PostChangeEvent(
    val postId: String?,
    val collectChanged: ItemChanged? = null,
    val likeChanged: ItemChanged? = null,
    val replayChanged: ItemChanged? = null,
    val postDelete: ItemChanged? = null
)

data class ItemChanged(val newState: Boolean?, val count: Int? = null)