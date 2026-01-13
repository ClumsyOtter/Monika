package com.otto.monika.home.fragment.post.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.collect.request.CollectListRequest
import com.otto.monika.api.model.post.request.PostLikeRequest
import com.otto.monika.api.model.post.request.PostListRequest
import com.otto.monika.api.model.post.request.PostRemoveRequest
import com.otto.monika.api.model.post.response.PostListResponse
import com.otto.monika.home.fragment.post.PostSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 用户帖子列表 ViewModel
 * 支持分页加载
 */
class UserPostViewModel : ViewModel() {

    companion object {
        const val PAGE_SIZE = 20 // 每页数据量
    }

    private val api = MonikaClient.monikaApi

    // 列表数据（使用 StateFlow）
    private val _postListState =
        MutableStateFlow<ApiResponse<PostListResponse>>(ApiResponse.Initial)
    val postListState: StateFlow<ApiResponse<PostListResponse>> =
        _postListState.asStateFlow()

    private var currentUid: String? = null
    private var currentSource: PostSource = PostSource.User

    /**
     * 加载数据（分页）
     * @param page 页码（从0开始，内部使用）
     * @param uid 用户ID，如果为 null，则使用上次设置的 uid 或 0
     * @param source 来源类型，默认为 User
     */
    fun loadData(page: Int, uid: String? = null, source: PostSource = PostSource.User) {
        // 如果传入了新的 uid，更新 currentUid
        if (uid != null) {
            currentUid = uid
        }
        // 更新 source
        currentSource = source

        val requestUid = currentUid ?: ""
        val requestPage = page + 1 // 将内部页码（从0开始）转换为 API 页码（从1开始）

        viewModelScope.launch {
            when (source) {
                PostSource.User -> {
                    // 用户来源：调用 getPostList 接口
                    val request = PostListRequest(
                        uid = requestUid,
                        page = requestPage,
                        pageSize = PAGE_SIZE
                    )
                    suspend { api.getPostList(request) }.asFlow().collect { response ->
                        _postListState.value = response
                    }
                }

                PostSource.UserSubscriber -> {
                    val request = PostListRequest(
                        uid = requestUid,
                        page = requestPage,
                        pageSize = PAGE_SIZE
                    )
                    suspend { api.getSubscriberPostList(request) }.asFlow().collect { response ->
                        _postListState.value = response
                    }
                }

                PostSource.Favorite -> {
                    // 收藏来源：调用 getCollectList 接口，并转换为 PostListResponse
                    val request = CollectListRequest(
                        uid = uid,
                        targetType = 2, // 2 代表获取 post
                        createdAtStart = null, // 可以根据需要设置
                        createdAtEnd = null, // 可以根据需要设置
                        page = requestPage,
                        pageSize = PAGE_SIZE
                    )
                    suspend { api.getCollectList(request) }.asFlow()
                        .map { collectResponse ->
                            // 将 CollectListResponse 转换为 PostListResponse
                            when (collectResponse) {
                                is ApiResponse.Success -> {
                                    val collectList = collectResponse.data
                                    if (collectList != null) {
                                        // 将 CollectItem 列表映射为 PostItem 列表
                                        val postItems = collectList.list.mapNotNull { collectItem ->
                                            collectItem.postInfo
                                        }
                                        ApiResponse.Success(
                                            PostListResponse(
                                                list = postItems,
                                                total = collectList.total
                                            ),
                                            collectResponse.code,
                                            collectResponse.message
                                        )
                                    } else {
                                        ApiResponse.Success(
                                            PostListResponse(emptyList(), 0),
                                            collectResponse.code,
                                            collectResponse.message
                                        )
                                    }
                                }

                                is ApiResponse.Loading -> ApiResponse.Loading()
                                is ApiResponse.BusinessError -> ApiResponse.BusinessError(
                                    collectResponse.code,
                                    collectResponse.message
                                )

                                is ApiResponse.NetworkError -> ApiResponse.NetworkError(
                                    collectResponse.throwable
                                )

                                is ApiResponse.Initial -> ApiResponse.Initial
                            }
                        }
                        .collect { response ->
                            _postListState.value = response
                        }
                }
            }
        }
    }

    /**
     * 重置数据（用于刷新）
     */
    fun resetData() {
        _postListState.value = ApiResponse.Initial
    }

    /**
     * 点赞帖子
     * @param postId 帖子ID
     * @return Flow<ApiResponse<Boolean>> 点赞结果
     */
    fun addPostLikeFlow(postId: String?): Flow<ApiResponse<Boolean>> {
        val request = PostLikeRequest(postId = postId)
        return suspend { api.addPostLike(request) }.asFlow()
    }

    /**
     * 取消点赞帖子
     * @param postId 帖子ID
     * @return Flow<ApiResponse<Boolean>> 取消点赞结果
     */
    fun removePostLikeFlow(postId: String?): Flow<ApiResponse<Boolean>> {
        val request = PostLikeRequest(postId = postId)
        return suspend { api.removePostLike(request) }.asFlow()
    }

    /**
     * 删除帖子
     * @param postId 帖子ID
     * @return Flow<ApiResponse<Boolean>> 删除结果
     */
    fun removePostFlow(postId: String?): Flow<ApiResponse<Boolean>> {
        val request = PostRemoveRequest(postId = postId)
        return suspend { api.removePost(request) }.asFlow()
    }
}

