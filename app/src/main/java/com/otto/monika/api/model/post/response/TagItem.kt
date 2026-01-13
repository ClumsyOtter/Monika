package com.otto.monika.api.model.post.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 标签项
 */
@Parcelize
data class TagItem(
    val id: String? = null,
    val type: Int? = null,
    val name: String? = null,
    val desc: String? = null,
    val status: Int? = null,
    val num: Int? = null,
    val sort: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val icon: String? = null
) : Parcelable

