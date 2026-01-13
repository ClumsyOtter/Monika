package com.otto.monika.api.model.subscribe.response

import android.os.Parcelable
import com.otto.monika.subscribe.plan.model.SubscribePlan
import kotlinx.parcelize.Parcelize

/**
 * 订阅方案列表响应
 */

@Parcelize
data class SubscribePlanListResponse(
    val list: List<SubscribePlan> = mutableListOf(),
    val total: Int = 0
) : Parcelable

