package com.otto.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String? = null
)