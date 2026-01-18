package com.otto.monika.home.fragment.artwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.network.common.ApiResponse
import com.otto.network.model.artwork.ArtworkResponse
import com.otto.network.model.artwork.UserArtworkModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户艺术品列表 ViewModel
 * 支持分页加载
 */
class UserArtworkViewModel : ViewModel() {

    companion object {
        const val PAGE_SIZE = 20 // 每页数据量
    }

    private val _artworkListState =
        MutableStateFlow<ApiResponse<ArtworkResponse>>(ApiResponse.Initial)
    val artworkListState: StateFlow<ApiResponse<ArtworkResponse>> = _artworkListState.asStateFlow()

    private var currentPage = 0

    /**
     * 加载数据（分页）
     * @param page 页码（从0开始）
     */
    fun loadData(page: Int) {
        viewModelScope.launch {
            try {
                _artworkListState.value = ApiResponse.Loading("Loading")
                // 模拟网络延迟
                delay(1500)
                val newData = generateMockData(page, PAGE_SIZE)
                _artworkListState.value = ApiResponse.Success(ArtworkResponse(newData, PAGE_SIZE))
                currentPage = page
            } catch (e: Exception) {
                _artworkListState.value = ApiResponse.BusinessError(100, e.message ?: "")
            }
        }
    }

    /**
     * 生成模拟数据
     */
    private fun generateMockData(page: Int, pageSize: Int): List<UserArtworkModel> {
        val dataList = mutableListOf<UserArtworkModel>()
        val startIndex = page * pageSize

        for (i in 0 until pageSize) {
            val index = startIndex + i + 1
            val model = UserArtworkModel(
                id = "artwork_$index",
                imageUrl = "https://cos.chelun.com/static/20251121/4901baa7-0a57-462c-9c4a-226cb9b2d4e1_216_216.jpeg",
                title = "艺术品标题 $index",
                content = "这是第 $index 个艺术品的详细描述内容，可以包含多行文字描述",
                price = "${(index * 10)}",
                discount = if (index % 3 == 0) "8折" else null,
                isUnlocked = index % 2 == 0 // 偶数索引表示已解锁
            )
            dataList.add(model)
        }

        return emptyList()
    }

    /**
     * 重置数据（用于刷新）
     */
    fun resetData() {
        currentPage = 0
    }

    /**
     * 获取当前页码
     */
    fun getCurrentPage(): Int = currentPage

}

