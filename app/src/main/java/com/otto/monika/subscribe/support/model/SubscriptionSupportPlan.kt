package com.otto.monika.subscribe.support.model

import com.otto.monika.subscribe.plan.model.SubscribePlan

/**
 * 订阅方案（合并 SubscribePlan 和 MonikaUserInfoModel 的数据）
 */
data class SubscriptionSupportPlan(
    //订阅方案
    val subscribePlan: SubscribePlan? = null,
    //用户信息
    val uid: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),  // 支付方式列表（本地写死）
)

/**
 * 支付方式
 */
data class PaymentMethod(
    val payChannel: String,
    val name: String? = null,
    val icon: Int? = null,
    var isSelected: Boolean = false
)

