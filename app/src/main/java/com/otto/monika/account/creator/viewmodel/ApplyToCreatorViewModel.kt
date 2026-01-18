package com.otto.monika.account.creator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.network.client.MonikaClient
import com.otto.network.common.ApiResponse
import com.otto.network.common.asFlow
import com.otto.network.model.creator.request.ApplyCreatorRequest
import com.otto.network.model.creator.response.CreatorMetadataResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 申请成为创作者 ViewModel
 */
class ApplyToCreatorViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    // 创作者元数据状态（使用 StateFlow 包裹 Response）
    private val _creatorMetadataState = MutableStateFlow<ApiResponse<CreatorMetadataResponse>>(ApiResponse.Initial)
    val creatorMetadataState: StateFlow<ApiResponse<CreatorMetadataResponse>> = _creatorMetadataState.asStateFlow()

    /**
     * 加载数据（从后台获取）
     */
    fun loadData() {
        viewModelScope.launch {
            suspend { api.getCreatorMetadata() }.asFlow().collect { response ->
                _creatorMetadataState.value = response
            }
        }
    }

    /**
     * 提交创作者申请（使用 Flow 方式，自动处理 Loading 状态）
     * @param request 申请请求参数
     * @return 申请结果 Flow
     */
    fun applyCreatorFlow(request: ApplyCreatorRequest): Flow<ApiResponse<Boolean>> {
        return suspend { api.applyCreator(request) }.asFlow()
    }
}

