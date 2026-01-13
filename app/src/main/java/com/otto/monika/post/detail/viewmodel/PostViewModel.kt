package com.otto.monika.post.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.collect.request.CollectRequest
import com.otto.monika.api.model.comment.request.CommentCreateRequest
import com.otto.monika.api.model.comment.request.CommentListRequest
import com.otto.monika.api.model.comment.request.CommentRemoveRequest
import com.otto.monika.api.model.comment.request.CommentReplyRequest
import com.otto.monika.api.model.comment.request.CommentSubListRequest
import com.otto.monika.api.model.comment.request.CommentToggleLikeRequest
import com.otto.monika.api.model.comment.response.CommentItem
import com.otto.monika.api.model.comment.response.CommentListResponse
import com.otto.monika.api.model.post.request.PostDetailRequest
import com.otto.monika.api.model.post.request.PostLikeRequest
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.api.model.subscribe.request.SubscribePlanListRequest
import com.otto.monika.api.model.subscribe.response.SubscribePlanListResponse
import com.otto.monika.post.detail.model.ItemChanged
import com.otto.monika.post.detail.model.PostChangeEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 帖子 ViewModel
 * 负责处理帖子数据和评论数据的生成逻辑
 */
class PostViewModel : ViewModel() {

    companion object {
        const val PAGE_SIZE = 20
    }

    private val api = MonikaClient.monikaApi

    // 帖子详情数据（使用 StateFlow）
    private val _postDetailState = MutableStateFlow<ApiResponse<PostItem>>(ApiResponse.Initial)
    val postDetailState: StateFlow<ApiResponse<PostItem>> = _postDetailState.asStateFlow()

    //存储原始postItem，用来判断是否更新了
    var originalPostItem: PostItem? = null

    /**
     * 加载帖子详情
     * @param postId 帖子ID
     */
    fun loadPostDetail(postId: String?) {
        viewModelScope.launch {
            val request = PostDetailRequest(postId = postId)
            suspend { api.getPostDetail(request) }.asFlow().collect { response ->
                _postDetailState.value = response
                if (response is ApiResponse.Success) {
                    originalPostItem = response.data?.copy()
                }
            }
        }
    }

    fun getPostChangeEvent(): PostChangeEvent? {
        val currentPostItem = postDetailState.value.getDataOrNull()
        val likeChanged = currentPostItem?.isLiked != originalPostItem?.isLiked
        val likeItemChanged =
            if (likeChanged) {
                ItemChanged(currentPostItem?.isLiked, currentPostItem?.likeNum)
            } else {
                null
            }
        val collectChanged = currentPostItem?.isCollected != originalPostItem?.isCollected
        val collectItemChanged = if (collectChanged) {
            ItemChanged(
                currentPostItem?.isCollected,
                currentPostItem?.collectNum
            )
        } else {
            null
        }
        val replyChanged = currentPostItem?.commentNum != originalPostItem?.commentNum
        val replyItemChanged =
            if (replyChanged) {
                ItemChanged(false, currentPostItem?.commentNum)
            } else {
                null
            }
        return if (likeChanged.not() && collectChanged.not() && replyChanged.not()) {
            null
        } else {
            PostChangeEvent(
                postId = currentPostItem?.id,
                collectChanged = collectItemChanged,
                likeChanged = likeItemChanged,
                replayChanged = replyItemChanged
            )
        }
    }

    /**
     * 加载评论列表（分页）
     * @param postId 帖子ID
     * @param uid 用户ID
     * @param page 页码（从1开始，接口要求从1开始）
     */
    fun loadCommentList(
        postId: String?,
        uid: String?,
        page: Int
    ): Flow<ApiResponse<CommentListResponse>> {
        val request = CommentListRequest(
            postId = postId,
            uid = uid,
            page = page,
            pageSize = PAGE_SIZE
        )
        return suspend { api.getCommentList(request) }.asFlow()
    }

    /**
     * 加载二级评论列表（分页）
     * @param parentId 父评论ID
     * @param postId 帖子ID
     * @param uid 用户ID
     * @param page 页码（从1开始）
     * @return Flow<ApiResponse<CommentListResponse>>
     */
    fun loadCommentSubListFlow(
        parentId: String?,
        postId: String?,
        uid: String?,
        page: Int
    ): Flow<ApiResponse<CommentListResponse>> {
        val request = CommentSubListRequest(
            parentId = parentId,
            postId = postId,
            uid = uid,
            page = page,
            pageSize = PAGE_SIZE
        )
        return suspend { api.getCommentSubList(request) }.asFlow()
    }

    /**
     * 创建评论
     * @param content 评论内容
     * @param postId 帖子ID
     * @return Flow<ApiResponse<CommentItem>>
     */
    fun createCommentFlow(content: String, postId: String?): Flow<ApiResponse<CommentItem>> {
        val request = CommentCreateRequest(
            content = content,
            postId = postId
        )
        return suspend { api.createComment(request) }.asFlow()
    }

    /**
     * 回复评论
     * @param content 回复内容
     * @param postId 帖子ID
     * @param commentId 被回复的评论ID
     * @return Flow<ApiResponse<CommentItem>>
     */
    fun replyCommentFlow(
        content: String?,
        postId: String?,
        commentId: String?
    ): Flow<ApiResponse<CommentItem>> {
        val request = CommentReplyRequest(
            content = content,
            postId = postId,
            commentId = commentId
        )
        return suspend { api.replyComment(request) }.asFlow()
    }

    /**
     * 评论点赞/取消点赞
     * @param commentId 评论ID
     * @return Flow<ApiResponse<Unit>>
     */
    fun toggleCommentLikeFlow(commentId: String?): Flow<ApiResponse<Unit>> {
        val request = CommentToggleLikeRequest(commentId = commentId)
        return suspend { api.toggleCommentLike(request) }.asFlow()
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param postId 帖子ID
     * @return Flow<ApiResponse<Unit>>
     */
    fun removeCommentFlow(commentId: String?, postId: String?): Flow<ApiResponse<Unit>> {
        val request = CommentRemoveRequest(
            commentId = commentId,
            postId = postId
        )
        return suspend { api.removeComment(request) }.asFlow()
    }

    /**
     * 点赞/取消点赞帖子
     * @param postId 帖子ID
     * @return Flow<ApiResponse<Boolean>>
     */
    fun togglePostLikeFlow(postId: String): Flow<ApiResponse<Boolean>> {
        val request = PostLikeRequest(postId = postId)
        // 先获取当前状态，决定调用哪个接口
        val currentPost = _postDetailState.value.getDataOrNull()
        return if (currentPost?.isLiked == true) {
            // 已点赞，调用取消点赞接口
            suspend { api.removePostLike(request) }.asFlow()
        } else {
            // 未点赞，调用点赞接口
            suspend { api.addPostLike(request) }.asFlow()
        }
    }

    /**
     * 收藏/取消收藏帖子
     * @param postId 帖子ID
     * @return Flow<ApiResponse<Boolean>>
     */
    fun togglePostCollectFlow(postId: String): Flow<ApiResponse<Boolean>> {
        val request = CollectRequest(
            targetType = 2, // 2 表示帖子
            targetId = postId
        )
        // 先获取当前状态，决定调用哪个接口
        val currentPost = _postDetailState.value.getDataOrNull()
        return if (currentPost?.isCollected == true) {
            // 已收藏，调用取消收藏接口
            suspend { api.removeCollect(request) }.asFlow()
        } else {
            // 未收藏，调用收藏接口
            suspend { api.addCollect(request) }.asFlow()
        }
    }

    /**
     * 获取订阅列表，判断当前用户是否有订阅方案
     */
    fun getUserPlanList(uid: String): Flow<ApiResponse<SubscribePlanListResponse>> {
        val request = SubscribePlanListRequest(uid = uid, page = 1, pageSize = 1)
        return suspend { api.getSubscribePlanList(request) }.asFlow()
    }
}