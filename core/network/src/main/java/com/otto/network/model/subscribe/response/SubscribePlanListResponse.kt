package com.otto.network.model.subscribe.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 订阅方案列表响应
 */

@Parcelize
data class SubscribePlanListResponse(
    val list: List<SubscribePlan> = mutableListOf(),
    val total: Int = 0
) : Parcelable

