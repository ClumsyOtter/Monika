package com.otto.monika.home.fragment.subscribe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.subscribe.request.MyCreatorListRequest
import com.otto.monika.api.model.subscribe.response.MyCreatorListResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户订阅列表 ViewModel
 * 支持分页加载
 */
class UserSubscribeViewModel : ViewModel() {

    companion object {
        const val PAGE_SIZE = 20 // 每页数据量
    }

    private val api = MonikaClient.monikaApi

    // 列表数据（使用 StateFlow）
    private val _subscribeListState =
        MutableStateFlow<ApiResponse<MyCreatorListResponse>>(ApiResponse.Initial)
    val subscribeListState: StateFlow<ApiResponse<MyCreatorListResponse>> =
        _subscribeListState.asStateFlow()

    private var currentUid: String? = null

    /**
     * 加载数据（分页）
     * @param page 页码（从0开始，内部使用）
     * @param uid 用户ID，如果为 null，则使用上次设置的 uid 或 0
     */
    fun loadData(page: Int, uid: String? = null) {
        // 如果传入了新的 uid，更新 currentUid
        if (uid != null) {
            currentUid = uid
        }
        val requestUid = currentUid ?: ""
        val requestPage = page + 1 // 将内部页码（从0开始）转换为 API 页码（从1开始）
        viewModelScope.launch {
            val request = MyCreatorListRequest(
                uid = requestUid,
                page = requestPage,
                pageSize = PAGE_SIZE
            )

            suspend { api.getMyCreatorList(request) }.asFlow().collect { response ->
                _subscribeListState.value = response
            }
        }
    }

    /**
     * 重置数据（用于刷新）
     */
    fun resetData() {
        _subscribeListState.value = ApiResponse.Initial
    }
}

