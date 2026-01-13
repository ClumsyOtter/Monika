package com.otto.monika.subscribe.support.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.R
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.common.toApiResponse
import com.otto.monika.api.model.pay.request.CreateOrderRequest
import com.otto.monika.api.model.pay.request.TestCreateOrderRequest
import com.otto.monika.api.model.pay.response.CreateOrderResponse
import com.otto.monika.api.model.pay.response.TestCreateOrderResponse
import com.otto.monika.api.model.subscribe.request.SubscribePlanListRequest
import com.otto.monika.api.model.user.request.UserProfileRequest
import com.otto.monika.subscribe.plan.model.SubscribePlanDiscountRules
import com.otto.monika.subscribe.support.model.PaymentMethod
import com.otto.monika.subscribe.support.model.SubscriptionSupportPlan
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * 订阅支持 ViewModel
 * 负责处理订阅方案、支付方式、订阅期限等数据
 */
class SubscriptionSupportViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    // 订阅方案列表（使用 StateFlow）
    private val _subscriptionSupportPlansState =
        MutableStateFlow<ApiResponse<List<SubscriptionSupportPlan>>>(ApiResponse.Initial)
    val subscriptionSupportPlansState: StateFlow<ApiResponse<List<SubscriptionSupportPlan>>> =
        _subscriptionSupportPlansState.asStateFlow()

    // 当前选中的订阅方案（包含选中的支付方式和订阅期限）
    private val _selectedPlan = MutableStateFlow<SubscriptionSupportPlan?>(null)
    val selectedPlan: StateFlow<SubscriptionSupportPlan?> = _selectedPlan.asStateFlow()


    /**
     * 重置初始化状态（页面进入时调用）
     */
    fun resetInitializationState() {
        _selectedPlan.value = null
    }

    /**
     * 加载数据（同时获取订阅方案列表和用户信息）
     * @param uid 用户ID
     */
    fun loadData(uid: String) {
        viewModelScope.launch {
            loadDataFlow(uid).collect { response ->
                _subscriptionSupportPlansState.value = response
            }
        }
    }

    /**
     * 加载数据 Flow（同时获取订阅方案列表和用户信息）
     * @param uid 用户ID
     */
    private fun loadDataFlow(uid: String): Flow<ApiResponse<List<SubscriptionSupportPlan>>> = flow {
        emit(ApiResponse.Loading())
        try {
            // 使用 coroutineScope 创建作用域，以便使用 async
            val (planListResponse, userProfileResponse) = coroutineScope {
                // 并发调用两个 API
                val planListDeferred = async {
                    api.getSubscribePlanList(
                        SubscribePlanListRequest(
                            uid = uid,
                            page = 1,
                            pageSize = 100
                        )
                    )
                }
                val userProfileDeferred = async {
                    api.getUserProfile(UserProfileRequest(uid))
                }
                // 等待两个结果都返回
                val planList = planListDeferred.await().toApiResponse()
                val userProfile = userProfileDeferred.await().toApiResponse()
                Pair(planList, userProfile)
            }

            // 处理结果
            when {
                planListResponse is ApiResponse.Success && userProfileResponse is ApiResponse.Success -> {
                    val plans = planListResponse.data?.list ?: emptyList()
                    val userInfo = userProfileResponse.data
                    // 将每个 SubscribePlan 转换为 SubscriptionSupportPlan
                    val subscriptionPlans = plans.map { plan ->
                        SubscriptionSupportPlan(
                            subscribePlan = plan,
                            uid = userInfo?.uid,
                            nickname = userInfo?.nickname,
                            avatar = userInfo?.avatar,
                            paymentMethods = generatePaymentMethods()
                        )
                    }
                    emit(
                        ApiResponse.Success(
                            subscriptionPlans,
                            planListResponse.code,
                            planListResponse.message
                        )
                    )
                }

                planListResponse is ApiResponse.BusinessError -> {
                    emit(ApiResponse.BusinessError(planListResponse.code, planListResponse.message))
                }

                userProfileResponse is ApiResponse.BusinessError -> {
                    emit(
                        ApiResponse.BusinessError(
                            userProfileResponse.code,
                            userProfileResponse.message
                        )
                    )
                }

                planListResponse is ApiResponse.NetworkError -> {
                    emit(ApiResponse.NetworkError(planListResponse.throwable))
                }

                userProfileResponse is ApiResponse.NetworkError -> {
                    emit(ApiResponse.NetworkError(userProfileResponse.throwable))
                }

                else -> {
                    emit(ApiResponse.BusinessError(-1, "未知错误"))
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.NetworkError(e))
        }
    }


    /**
     * 生成支付方式列表（本地写死）
     */
    private fun generatePaymentMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod(
                "wxpay",
                "微信支付",
                R.drawable.monika_wx_pay_icon,
                true
            ), // 默认选中第一个
            PaymentMethod("alipay", "支付宝", R.drawable.monika_ali_pay_icon),
        )
    }

    /**
     * 更新支付方式数据（根据订阅方案）
     * @param prePlan 之前的订阅方案（用于获取当前选中的支付方式）
     * @param plan 当前订阅方案（需要更新支付方式选中状态）
     */
    private fun updatePaymentMethodsForPlan(
        prePlan: SubscriptionSupportPlan?,
        plan: SubscriptionSupportPlan
    ) {
        val currentSelectedMethod = prePlan?.paymentMethods?.find { it.isSelected }
        val shouldSelectFirst =
            currentSelectedMethod == null || plan.paymentMethods.none { it.payChannel == currentSelectedMethod.payChannel }

        if (shouldSelectFirst && plan.paymentMethods.isNotEmpty()) {
            // 重置所有选中状态，选中第一个
            plan.paymentMethods.forEach { it.isSelected = false }
            plan.paymentMethods.firstOrNull()?.isSelected = true
        } else {
            // 保持当前选中状态，但需要更新为新方案中对应的支付方式
            val matchedPaymentMethod =
                plan.paymentMethods.find { it.payChannel == currentSelectedMethod?.payChannel }
            if (matchedPaymentMethod != null) {
                plan.paymentMethods.forEach {
                    it.isSelected = it.payChannel == matchedPaymentMethod.payChannel
                }
            } else {
                // 如果找不到匹配的，选择第一个
                if (plan.paymentMethods.isNotEmpty()) {
                    plan.paymentMethods.forEach { it.isSelected = false }
                    plan.paymentMethods.firstOrNull()?.isSelected = true
                }
            }
        }
    }

    /**
     * 更新订阅期限数据（根据订阅方案）
     * @param prePlan 之前的订阅方案（用于获取当前选中的订阅期限）
     * @param plan 当前订阅方案（需要更新订阅期限选中状态）
     */
    private fun updateSubscriptionPeriodsForPlan(
        prePlan: SubscriptionSupportPlan?,
        plan: SubscriptionSupportPlan
    ) {
        val subscriptionPeriods = plan.subscribePlan?.discountRules?.toMutableList() ?: return
        val currentSelectedPeriod = prePlan?.subscribePlan?.discountRules?.find { it.isSelected }
        val shouldSelectFirstPeriod =
            currentSelectedPeriod == null || subscriptionPeriods.none { it.month == currentSelectedPeriod.month }

        if (shouldSelectFirstPeriod && subscriptionPeriods.isNotEmpty()) {
            // 重置所有选中状态，选中第一个
            subscriptionPeriods.forEach { it.isSelected = false }
            subscriptionPeriods.firstOrNull()?.isSelected = true
        } else {
            // 保持当前选中状态，但需要更新为新方案中对应的订阅期限对象（价格可能不同）
            val matchedPeriod =
                subscriptionPeriods.find { it.month == currentSelectedPeriod?.month }
            if (matchedPeriod != null) {
                subscriptionPeriods.forEach { it.isSelected = it.month == matchedPeriod.month }
            } else {
                // 如果找不到匹配的，选择第一个
                if (subscriptionPeriods.isNotEmpty()) {
                    subscriptionPeriods.forEach { it.isSelected = false }
                    subscriptionPeriods.firstOrNull()?.isSelected = true
                }
            }
        }
    }

    /**
     * 更新订阅方案（包括支付方式和订阅期限）
     * @param prePlan 之前的订阅方案
     * @param plan 当前订阅方案
     */
    fun updateSubscriptionSupportPlan(
        prePlan: SubscriptionSupportPlan?,
        plan: SubscriptionSupportPlan
    ) {
        // 更新支付方式数据
        updatePaymentMethodsForPlan(prePlan, plan)
        // 更新订阅期限数据
        updateSubscriptionPeriodsForPlan(prePlan, plan)
        // 更新选中的方案
        _selectedPlan.value = plan
    }

    /**
     * 获取当前选中的订阅方案
     * @return 当前选中的订阅方案，如果未选中则返回 null
     */
    fun getSelectedPlan(): SubscriptionSupportPlan? {
        return selectedPlan.value
    }

    /**
     * 获取当前选中的支付方式
     * @return 当前选中的支付方式，如果未选中则返回 null
     */
    fun getSelectedPaymentMethod(): PaymentMethod? {
        return selectedPlan.value?.paymentMethods?.find { it.isSelected }
    }

    /**
     * 获取当前选中的订阅期限
     * @return 当前选中的订阅期限，如果未选中则返回 null
     */
    fun getSelectedSubscriptionPeriod(): SubscribePlanDiscountRules? {
        return selectedPlan.value?.subscribePlan?.discountRules?.find { it.isSelected }
    }

    /**
     * 创建订单
     * @param subscribePlanId 订阅方案ID
     * @param duration 订阅期限（月数）
     * @return Flow<ApiResponse<CreateOrderResponse>>
     */
    fun createOrderFlow(
        subscribePlanId: String?,
        duration: Int
    ): Flow<ApiResponse<CreateOrderResponse>> {
        val request = CreateOrderRequest(
            subscribePlanId = subscribePlanId,
            duration = duration
        )
        return suspend { api.createOrder(request) }.asFlow()
    }

    /**
     * 模拟创建订单（测试用）
     * @param uid 用户ID
     * @param subscribePlanId 订阅方案ID
     * @param duration 订阅期限（月数）
     * @return Flow<ApiResponse<TestCreateOrderResponse>>
     */
    fun testCreateOrderFlow(
        uid: String?,
        subscribePlanId: String?,
        duration: Int
    ): Flow<ApiResponse<TestCreateOrderResponse>> {
        val request = TestCreateOrderRequest(
            uid = uid,
            subscribePlanId = subscribePlanId,
            duration = duration
        )
        return suspend { api.testCreateOrder(request) }.asFlow()
    }

}