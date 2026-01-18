package com.otto.monika.account.edit.viewmodel

import androidx.lifecycle.ViewModel
import com.otto.network.client.MonikaClient
import com.otto.network.common.ApiResponse
import com.otto.network.common.asFlow
import com.otto.network.model.user.request.EditUserRequest
import kotlinx.coroutines.flow.Flow

/**
 * 账号编辑 ViewModel
 */
class AccountEditViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    /**
     * 编辑用户信息（使用 Flow 方式，自动处理 Loading 状态）
     * @param nickname 新昵称
     * @param avatar 新头像URL
     * @return 编辑结果 Flow
     */
    fun editUserFlow(
        nickname: String? = null,
        avatar: String? = null,
        userDesc: String? = null
    ): Flow<ApiResponse<Boolean>> {
        val request = EditUserRequest(
            nickname = nickname,
            avatar = avatar,
            intro = userDesc
        )
        return suspend { api.editUser(request) }.asFlow()
    }
}