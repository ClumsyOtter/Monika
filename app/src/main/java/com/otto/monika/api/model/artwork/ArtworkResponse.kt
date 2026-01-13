package com.otto.monika.api.model.artwork

import com.otto.monika.home.fragment.artwork.model.UserArtworkModel

data class ArtworkResponse(
    val list: List<UserArtworkModel> = mutableListOf(),
    val total: Int = 0
) {
}