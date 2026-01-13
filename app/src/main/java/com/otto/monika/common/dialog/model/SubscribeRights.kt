package com.otto.monika.common.dialog.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubscribeRights(
    val id: String? = null,
    @SerializedName("icon")
    val rightsAvatar: String? = null,
    @SerializedName("name")
    val rightsTitle: String? = null,
    @SerializedName("desc")
    val rightsContent: String? = null
) : Parcelable