package com.otto.monika.setting.viewmodel

import androidx.lifecycle.ViewModel
import com.otto.network.client.MonikaClient
import com.otto.network.common.ApiResponse
import com.otto.network.common.asFlow
import kotlinx.coroutines.flow.Flow

class MonikaSettingViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    /**
     * 退出登录
     * @return Flow<ApiResponse<Unit>>
     */
    fun logoutFlow(): Flow<ApiResponse<Unit>> {
        return suspend { api.logout() }.asFlow()
    }
}