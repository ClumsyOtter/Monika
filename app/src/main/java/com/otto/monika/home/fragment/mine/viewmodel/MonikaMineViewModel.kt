package com.otto.monika.home.fragment.mine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.collect.request.CollectRequest
import com.otto.monika.api.model.user.request.UserProfileRequest
import com.otto.monika.api.model.user.response.MonikaUserInfoModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MonikaMineViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi


    // 用户信息状态（使用 StateFlow 实现状态共享）
    private val _userProfileState = MutableStateFlow<ApiResponse<MonikaUserInfoModel>>(ApiResponse.Initial)
    val userProfileState: StateFlow<ApiResponse<MonikaUserInfoModel>> = _userProfileState.asStateFlow()

    /**
     * 加载用户信息（使用 StateFlow 方式，自动处理 Loading 状态）
     * @param uid 可选的用户ID，如果传了则查询指定用户，不传则查询当前登录用户
     */
    fun loadUserProfile(uid: String? = null) {
        viewModelScope.launch {
            suspend { api.getUserProfile(UserProfileRequest(uid)) }.asFlow().collect { response ->
                _userProfileState.value = response
            }
        }
    }


    /**
     * 收藏用户
     * @param targetId 目标用户ID
     * @return Flow<ApiResponse<Boolean>> 收藏结果
     */
    fun addCollectFlow(targetId: String?): Flow<ApiResponse<Boolean>> {
        val request = CollectRequest(
            targetType = 1, // 1-用户
            targetId = targetId
        )
        return suspend { api.addCollect(request) }.asFlow()
    }

    /**
     * 取消收藏用户
     * @param targetId 目标用户ID
     * @return Flow<ApiResponse<Boolean>> 取消收藏结果
     */
    fun removeCollectFlow(targetId: String?): Flow<ApiResponse<Boolean>> {
        val request = CollectRequest(
            targetType = 1, // 1-用户
            targetId = targetId
        )
        return suspend { api.removeCollect(request) }.asFlow()
    }

}