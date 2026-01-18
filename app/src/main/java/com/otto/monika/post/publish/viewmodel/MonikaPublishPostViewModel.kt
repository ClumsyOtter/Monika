package com.otto.monika.post.publish.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.common.dialog.model.CommonBottomSheetData
import com.otto.monika.common.dialog.model.CommonBottomSheetItem
import com.otto.network.client.MonikaClient
import com.otto.network.common.ApiResponse
import com.otto.network.common.asFlow
import com.otto.network.common.collectSimple
import com.otto.network.model.home.MonikaRankData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 发布动态 ViewModel
 */
class MonikaPublishPostViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    // 标签列表状态
    private val _tagState =
        MutableStateFlow<ApiResponse<CommonBottomSheetData>>(ApiResponse.Initial)
    val tagState: StateFlow<ApiResponse<CommonBottomSheetData>> = _tagState.asStateFlow()

    // 可见性设置状态
    var currentVisibility: CommonBottomSheetData? = null


    /**
     * 加载标签列表
     */
    fun loadTags() {
        viewModelScope.launch {
            suspend { api.getTags() }.asFlow().collectSimple(
                onLoading = {
                    _tagState.value = ApiResponse.Loading()
                },
                onSuccess = { response ->
                    response?.let {
                        // 将 TagItem 转换为 CommonBottomSheetItem
                        val tagItems = it.list.map { tag ->
                            CommonBottomSheetItem(
                                content = tag.name ?: "",
                                identify = tag.id.toString(),
                                isSelected = false
                            )
                        }
                        val tagData = CommonBottomSheetData(
                            title = "内容标签",
                            description = "最多可以选择 5 个内容标签参与",
                            itemList = tagItems
                        )
                        _tagState.value = ApiResponse.Success(tagData)
                    }
                },
                onFailure = { message ->
                    _tagState.value = ApiResponse.BusinessError(100, message)
                }
            )
        }
    }

    /**
     * 更新标签数据
     */
    fun updateTagData(data: CommonBottomSheetData?) {
        data?.let {
            _tagState.value = ApiResponse.Success(it)
        }
    }

    /**
     * 生成可见性数据
     */
    fun generateVisibilityData(): CommonBottomSheetData {
        if (currentVisibility == null) {
            currentVisibility = CommonBottomSheetData(
                title = "权限设置",
                description = "",
                itemList = generateVisibility()
            )
        }
        return currentVisibility!!
    }

    /**
     * 生成可见性选项列表
     */
    private fun generateVisibility(): List<CommonBottomSheetItem> {
        val items = mutableListOf<CommonBottomSheetItem>()
        items.add(CommonBottomSheetItem("所有人可见", "0", true))
        items.add(CommonBottomSheetItem("仅主页可见", "1"))
        items.add(CommonBottomSheetItem("订阅者可见", "2"))
        return items
    }

    fun createPost(
        title: String,
        content: String,
        topic: String?,
        tags: String?,
        images: String?,
        visibleType: Int
    ): Flow<ApiResponse<MonikaRankData>> {
        return suspend {
            MonikaClient.monikaApi.createPost(
                title,
                content,
                topic,
                tags,
                images,
                visibleType
            )
        }.asFlow()
    }

}

