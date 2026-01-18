package com.otto.network.model.home

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CommunityCategoryModel(
    val channel: String?,
    val id: String,
    val name: String,
    val is_def: String? //是否为默认选中，1是 0否 ，如果全是0，默认就是第一个
) : Parcelable
