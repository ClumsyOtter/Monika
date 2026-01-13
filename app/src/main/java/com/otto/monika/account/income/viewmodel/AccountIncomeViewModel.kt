package com.otto.monika.account.income.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.user.response.UserIncomeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountIncomeViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    // 收入信息状态（使用 StateFlow 实现状态共享）
    private val _incomeState = MutableStateFlow<ApiResponse<UserIncomeResponse>>(ApiResponse.Initial)
    val incomeState: StateFlow<ApiResponse<UserIncomeResponse>> = _incomeState.asStateFlow()

    /**
     * 加载收入信息（使用 StateFlow 方式，自动处理 Loading 状态）
     */
    fun loadIncome() {
        viewModelScope.launch {
            suspend { api.getUserIncome() }.asFlow().collect { response ->
                _incomeState.value = response
            }
        }
    }
}