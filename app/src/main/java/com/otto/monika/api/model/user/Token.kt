package com.otto.monika.api.model.user

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String? = null
)