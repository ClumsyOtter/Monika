package com.otto.monika.home.fragment.favorite.creator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.network.client.MonikaClient
import com.otto.network.common.ApiResponse
import com.otto.network.common.asFlow
import com.otto.network.model.collect.request.CollectListRequest
import com.otto.network.model.collect.response.CollectListResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户喜欢的创作者列表 ViewModel
 * 支持分页加载
 */
class UserFavoriteCreatorViewModel : ViewModel() {

    companion object {
        const val PAGE_SIZE = 20 // 每页数据量
    }

    private val api = MonikaClient.monikaApi

    // 列表数据（使用 StateFlow）
    private val _creatorListState =
        MutableStateFlow<ApiResponse<CollectListResponse>>(ApiResponse.Initial)
    val creatorListState: StateFlow<ApiResponse<CollectListResponse>> =
        _creatorListState.asStateFlow()

    /**
     * 加载数据（分页）
     * @param page 页码（从0开始，内部使用）
     * @param uid 用户ID，如果为 null，则使用上次设置的 uid 或 0
     */
    fun loadData(page: Int, uid: String? = null) {
        val requestPage = page + 1 // 将内部页码（从0开始）转换为 API 页码（从1开始）
        viewModelScope.launch {
            val request = CollectListRequest(
                uid = uid,
                targetType = 1, // 1 代表获取用户
                createdAtStart = null, // 可以根据需要设置
                createdAtEnd = null, // 可以根据需要设置
                page = requestPage,
                pageSize = PAGE_SIZE
            )

            suspend { api.getCollectList(request) }.asFlow().collect { response ->
                _creatorListState.value = response
            }
        }
    }

    /**
     * 重置数据（用于刷新）
     */
    fun resetData() {
        _creatorListState.value = ApiResponse.Initial
    }
}

