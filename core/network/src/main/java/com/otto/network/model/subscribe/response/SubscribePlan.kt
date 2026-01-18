package com.otto.network.model.subscribe.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * 订阅方案
 */
@Parcelize
data class SubscribePlan(
    val id: String? = null,
    var title: String? = null,
    var price: String? = null,
    val createAt: String? = null,
    val isSubscribed: Boolean? = null,
    var rightsDesc: String? = null,
    var openDiscount: Int? = null,
    var discountRules: List<SubscribePlanDiscountRules> = emptyList(),
    var isSelected: Boolean = false
) : Parcelable

@Parcelize
data class SubscribePlanDiscountRules(
    val month: Int? = null,
    var discount: Int? = null,
    var price: String? = null,
    var isSelected: Boolean = false
) : Parcelable

