package com.otto.monika.subscribe.plan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.creator.response.CreatorMetadataResponse
import com.otto.monika.api.model.subscribe.request.SubscribePlanListRequest
import com.otto.monika.api.model.subscribe.response.SubscribePlanCreateResponse
import com.otto.monika.api.model.subscribe.response.SubscribePlanListResponse
import com.otto.monika.common.dialog.model.SubscribeRights
import com.otto.monika.subscribe.plan.model.SubscribePlan
import com.otto.monika.subscribe.plan.model.SubscribePlanDiscountRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

/**
 * 订阅方案 ViewModel
 * 负责处理订阅方案数据
 */
class SubscribePlanViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    // 订阅方案列表状态（使用 StateFlow 实现状态共享）
    private val _subscribePlanListState =
        MutableStateFlow<ApiResponse<SubscribePlanListResponse>>(ApiResponse.Initial)
    val subscribePlanListState: StateFlow<ApiResponse<SubscribePlanListResponse>> =
        _subscribePlanListState.asStateFlow()

    val subscribePlanRights: MutableList<SubscribeRights> = mutableListOf()

    /**
     * 加载数据（从后台获取，使用 Flow 方式，自动处理 Loading 状态）
     * 同时调用 getSubscribePlanList 和 getCreatorMetadata，使用 zip 组合两个 Flow
     * 等两个结果都获取到了再更新状态
     * @param uid 用户ID，如果为 null 或空字符串，则传 0
     * @param page 页码，默认为 1
     * @param pageSize 每页数量，默认为 100
     */
    fun initData(uid: String? = null, page: Int = 1, pageSize: Int = 100) {
        val uidLong = uid ?: ""
        val request = SubscribePlanListRequest(uid = uidLong, page = page, pageSize = pageSize)

        viewModelScope.launch {
            // 将两个 API 调用转换为 Flow
            val planListFlow = suspend { api.getSubscribePlanList(request) }.asFlow()
            val metadataFlow = suspend { api.getCreatorMetadata() }.asFlow()
            // 使用 zip 组合两个 Flow，等待两个结果都返回
            planListFlow.zip(metadataFlow) { planListResponse, metadataResponse ->
                // 两个结果都获取到了，返回组合结果
                Pair(planListResponse, metadataResponse)
            }.collect { (planListResponse, metadataResponse) ->
                // 更新两个状态
                _subscribePlanListState.value = planListResponse
                handleMetadataResponse(metadataResponse)
            }
        }
    }

    private fun handleMetadataResponse(metadataResponse: ApiResponse<CreatorMetadataResponse>) {
        when (metadataResponse) {
            is ApiResponse.Success -> {
                metadataResponse.data?.subRight?.let {
                    subscribePlanRights.clear()
                    subscribePlanRights.addAll(it)
                }
            }

            else -> {}
        }
    }

    /**
     * 批量创建订阅方案（使用 Flow 方式，自动处理 Loading 状态）
     * @param plans 订阅方案列表
     * @return 创建结果 Flow，包含 added、updated、deleted 统计信息
     */
    fun createPlansFlow(plans: List<SubscribePlan>): Flow<ApiResponse<SubscribePlanCreateResponse>> {
        // 将 plans 列表转换为 JSON 字符串
        val plansJson = Gson().toJson(plans)
        return suspend { api.createSubscribePlans(plansJson) }.asFlow()
    }

    /**
     * 删除订阅方案
     */
    fun deleteSubscribePlanFlow(id: String): Flow<ApiResponse<Boolean>> {
        return suspend { api.deleteSubscribePlan(id) }.asFlow()
    }

    /**
     * 添加新的空白方案
     */
    fun generateNewPlan(): SubscribePlan {
        return SubscribePlan(
            openDiscount = 0,
            discountRules = generateEmptySubscribePlanDiscountRules()
        )
    }

    private fun generateEmptySubscribePlanDiscountRules(): MutableList<SubscribePlanDiscountRules> {
        val discountRules = mutableListOf<SubscribePlanDiscountRules>()
        discountRules.add(SubscribePlanDiscountRules(month = 1, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 2, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 3, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 6, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 9, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 12, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 15, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 18, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 24, discount = 10))
        discountRules.add(SubscribePlanDiscountRules(month = 36, discount = 10))
        return discountRules
    }

}

