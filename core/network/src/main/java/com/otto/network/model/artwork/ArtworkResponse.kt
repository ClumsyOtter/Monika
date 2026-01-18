package com.otto.network.model.artwork


data class ArtworkResponse(
    val list: List<UserArtworkModel> = mutableListOf(),
    val total: Int = 0
) {
}