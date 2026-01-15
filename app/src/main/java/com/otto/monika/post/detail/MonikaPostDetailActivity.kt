package com.otto.monika.post.detail

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.chad.library.adapter4.loadState.LoadState
import com.flyjingfish.openimagelib.OpenImage
import com.otto.monika.R
import com.otto.monika.api.common.collectSimple
import com.otto.monika.api.model.comment.response.CommentItem
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.dialog.CommentActionBottomSheet
import com.otto.monika.common.dialog.CommentInputBottomDialog
import com.otto.monika.common.dialog.MonikaConfirmBottomDialog
import com.otto.monika.common.dialog.model.CommonActionGroup
import com.otto.monika.common.dialog.model.CommonActionItem
import com.otto.monika.common.model.OpenImageModel
import com.otto.monika.common.utils.getView
import com.otto.monika.common.views.MonikaCommonOptionView
import com.otto.monika.post.detail.adapter.CommentActionParams
import com.otto.monika.post.detail.adapter.ExpandSubCommentsParams
import com.otto.monika.post.detail.adapter.PostParentAdapter
import com.otto.monika.post.detail.viewmodel.PostViewModel
import com.otto.monika.post.detail.views.MonikaPostActionBarView
import com.otto.monika.post.detail.views.MonikaPostFakeInputView
import com.otto.monika.subscribe.support.SubscriptionSupportActivity

import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


/**
 * 帖子主页面
 */
class MonikaPostDetailActivity : MonikaBaseActivity() {

    companion object {
        private const val EXTRA_POST_ID = "post_id"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param postId 帖子ID
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, postId: String? = null): Intent {
            val intent = Intent(context, MonikaPostDetailActivity::class.java)
            postId?.let {
                intent.putExtra(EXTRA_POST_ID, it)
            }
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到帖子页面
         * @param context 上下文
         * @param postId 帖子ID
         */
        @JvmStatic
        fun enter(context: Context, postId: String? = null) {
            val intent = getIntent(context, postId)
            context.startActivity(intent)
        }
    }

    // AppBar 视图
    private var actionBarView: MonikaPostActionBarView? = null

    // 评论区视图
    private val postContainerRecycler: RecyclerView by getView(R.id.rv_post_container)
    private val replyInputView: MonikaPostFakeInputView by getView(R.id.cv_post_reply_input)
    private val favoriteView: MonikaCommonOptionView by getView(R.id.cv_post_favorite)
    private val likeView: MonikaCommonOptionView by getView(R.id.cv_post_like)
    private val replyView: MonikaCommonOptionView by getView(R.id.cv_post_reply)


    // ViewModel
    private val viewModel: PostViewModel by viewModels()

    private val postParentAdapter = PostParentAdapter()

    // SubscriptionSupportActivity Result Launcher
    private val subscriptionSupportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 订阅成功，重新加载帖子数据以更新订阅状态
            loadPostData()
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_post_detail
    }


    override fun onFinishCreateView() {
        super.onFinishCreateView()
        initViews()
        setupListeners()
        loadPostData()
    }

    override fun getCustomActionBarView(): View? {
        if (actionBarView == null) {
            actionBarView = MonikaPostActionBarView(context = this)
        }
        return actionBarView
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        // 设置评论列表 RecyclerView
        val layoutManager = LinearLayoutManager(this)
        postContainerRecycler.layoutManager = layoutManager
        postContainerRecycler.itemAnimator = null
        postContainerRecycler.adapter = postParentAdapter
        postParentAdapter.init()
        postParentAdapter.onImagesClick = { imageList: List<String>, index: Int ->
            imageList.getOrNull(index)?.let {
                //在点击时调用
                OpenImage.with(this)
                    //打开大图页面时没有点击的ImageView则用这个
                    .setNoneClickView()
                    //图片的数据
                    .setImageUrlList(OpenImageModel.getImage(it))
                    //默认展示数据的位置
                    .setClickPosition(0)
                    //开始展示大图
                    .show()
            }
        }
        val commentAdapter = postParentAdapter.getCommentAdapter()

        postParentAdapter.onLoadMoreComment = {
            loadComments()
        }
        // 设置展开二级评论的回调
        commentAdapter.onExpandSubCommentsListener = { params ->
            loadSubComments(params)
        }
        // 设置评论项的回调和点赞回调（使用 CommentActionParams 封装参数）
        commentAdapter.onReplyClickListener = { params ->
            // 弹出回复框
            showCommentInputDialog(params)
        }
        commentAdapter.onLikeClickListener = { params ->
            // 处理点赞逻辑
            handleCommentLike(params)
        }
        commentAdapter.onLongClickListener = { params ->
            // 显示评论操作选项框
            showCommentActionDialog(params)
        }
        commentAdapter.onItemClickListener = { params ->
            // 弹出回复框
            showCommentInputDialog(params)
        }
        likeView.setCountValue(0)
        favoriteView.setCountValue(0)
        replyView.setCountValue(0)
    }


    override fun onDestroy() {
        sendPostChangedEvent()
        super.onDestroy()
    }


    /**
     * 发送收藏状态，点赞状态，回复数量的变化，当页面关闭的时候
     */
    fun sendPostChangedEvent() {
        viewModel.getPostChangeEvent()?.let {
            EventBus.getDefault().post(it)
        }
    }

    /**
     * 设置监听器
     */
    private fun setupListeners() {
        // 返回按钮
        actionBarView?.onBackClickListener = {
            onBackPressed()
        }

        // 订阅按钮
        actionBarView?.onSubscribeClickListener = {
            viewModel.postDetailState.value.getDataOrNull()?.let { post ->
                // 从 post 中获取 uid
                val uid = post.user?.id
                uid?.let {
                    lifecycleScope.launch {
                        viewModel.getUserPlanList(uid)
                            .collectSimple(
                                onLoading = { showLoadingDialog() },
                                onSuccess = { response ->
                                    hideLoadingDialog()
                                    if (response?.list?.isNotEmpty() == true) {
                                        val intent = SubscriptionSupportActivity.getIntent(
                                            this@MonikaPostDetailActivity,
                                            uid
                                        )
                                        subscriptionSupportLauncher.launch(intent)
                                    } else {
                                        Toast.makeText(
                                            this@MonikaPostDetailActivity,
                                            "该创作者还未设置订阅方案",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onFailure = { hideLoadingDialog() })
                    }
                }

            }
        }

        // 底部假输入框点击事件
        replyInputView.onClickListener = {
            showCommentInputDialog(null)
        }

        // 收藏按钮
        favoriteView.onOptionClickListener = {
            handlePostCollect()
        }

        // 点赞按钮
        likeView.onOptionClickListener = {
            handlePostLike()
        }

        replyView.onOptionClickListener = {
            showCommentInputDialog(null)
        }

    }

    /**
     * 加载帖子数据
     */
    private fun loadPostData() {
        val postId = intent.getStringExtra(EXTRA_POST_ID)
        // 观察帖子详情 StateFlow
        lifecycleScope.launch {
            viewModel.postDetailState.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { postItem ->
                    hideLoadingDialog()
                    updatePostViews(postItem)
                    // 帖子加载成功后，加载第一页评论
                    loadComments()
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "加载帖子失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            )
        }
        // 触发加载
        viewModel.loadPostDetail(postId)
    }

    /**
     * 更新帖子视图
     */
    private fun updatePostViews(postItem: PostItem?) {
        postItem?.let { post ->
            postParentAdapter.setData(post)
            // 头像
            actionBarView?.setAvatar(post.user?.avatar)
            // 名称
            actionBarView?.setName(post.user?.nickname)
            // 订阅按钮
            updateSubscribeButton(postItem)
            // 更新点赞和收藏和回复按钮状态
            updatePostActionButtons(postItem)
        }
    }


    /**
     * 更新帖子操作按钮（点赞、收藏）
     */
    private fun updatePostActionButtons(postItem: PostItem?) {
        postItem?.let { post ->
            // 更新点赞按钮
            likeView.isOptionSelected = post.isLiked == true
            likeView.setCountValue(post.likeNum ?: 0)
            // 更新收藏按钮
            favoriteView.isOptionSelected = post.isCollected == true
            favoriteView.setCountValue(post.collectNum ?: 0)
            //回复按钮
            replyView.setCountValue(postItem.commentNum ?: 0)
        }
    }

    /**
     * 更新订阅按钮
     */
    private fun updateSubscribeButton(postItem: PostItem?) {
        postItem?.let { post ->
            actionBarView?.setSubscribeText(if (post.user?.isSubscribed == true) "已订阅 ${postItem.user?.subscribeRemainingTime ?: 0}天" else "订阅")
            actionBarView?.setSubscribeSelected(post.user?.isSubscribed == true)
        }
    }

    /**
     * 加载更多一级评论
     */
    private fun loadComments() {
        val postId = intent.getStringExtra(EXTRA_POST_ID)
        // 获取当前用户 uid（从帖子详情中获取，或者使用默认值 0）
        val uid = viewModel.postDetailState.value.getDataOrNull()?.uid
        // 获取当前页码并计算下一页
        val commentAdapter = postParentAdapter.getCommentAdapter()
        val nextPage = commentAdapter.getCurrentPage() + 1
        lifecycleScope.launch {
            postId?.let { uid?.let { it1 -> viewModel.loadCommentList(it, it1, nextPage) } }
                ?.collectSimple(
                    onLoading = {
                        postParentAdapter.quickAdapterHelper.trailingLoadState = LoadState.Loading
                    },
                    onSuccess = { response ->
                        response?.let {
                            // 直接传递给 Adapter 处理（Adapter 内部会更新页码）
                            commentAdapter.addComments(it.list, it.total, nextPage)
                            val hasMore = commentAdapter.itemCount < it.total
                            if (hasMore) {
                                postParentAdapter.quickAdapterHelper.trailingLoadState =
                                    LoadState.NotLoading(false)
                            } else {
                                postParentAdapter.quickAdapterHelper.trailingLoadState =
                                    LoadState.NotLoading(true)
                            }
                        }

                    },
                    onFailure = {
                        // 加载失败，不增加页码
                        postParentAdapter.quickAdapterHelper.trailingLoadState = LoadState.Error(
                            Exception(it)
                        )
                    }
                )
        }
    }


    /**
     * 加载二级评论列表
     * @param expandSubCommentsParams
     */
    private fun loadSubComments(
        expandSubCommentsParams: ExpandSubCommentsParams
    ) {
        val postId = intent.getStringExtra(EXTRA_POST_ID)
        val uid = viewModel.postDetailState.value.getDataOrNull()?.uid
        lifecycleScope.launch {
            viewModel.loadCommentSubListFlow(
                expandSubCommentsParams.parentId,
                postId,
                uid,
                expandSubCommentsParams.page
            ).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { response ->
                    hideLoadingDialog()
                    response?.let {
                        // 更新对应评论的二级评论列表
                        postParentAdapter.getCommentAdapter().updateSubComments(
                            expandSubCommentsParams,
                            it.list,
                            it.total
                        )
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "加载回复失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    /**
     * 显示评论输入 Dialog
     */
    private fun showCommentInputDialog(commentActionParams: CommentActionParams?) {
        val inputBottomDialog = CommentInputBottomDialog(this)
        inputBottomDialog.setReplyHit(commentActionParams?.commentItem?.name)
        inputBottomDialog.setOnSendClickListener { content ->
            handleCommentSubmit(content, commentActionParams)
        }
        inputBottomDialog.show()
    }

    /**
     * 处理评论提交
     * @param content 评论内容
     */
    private fun handleCommentSubmit(content: String, commentActionParams: CommentActionParams?) {
        val postId = intent.getStringExtra(EXTRA_POST_ID)
        lifecycleScope.launch {
            val commentFlow = if (commentActionParams == null) {
                // 创建新评论
                viewModel.createCommentFlow(content, postId)
            } else {
                // 回复评论
                viewModel.replyCommentFlow(content, postId, commentActionParams.commentItem?.id)
            }

            commentFlow.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { commentItem ->
                    hideLoadingDialog()
                    commentItem?.let {
                        // 直接传递单个 CommentItem
                        postParentAdapter.getCommentAdapter()
                            .replyNewComment(it, commentActionParams)
                        Toast.makeText(
                            this@MonikaPostDetailActivity,
                            "评论发布成功",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        //更新帖子的回复数量
                        updatePostComment(true)
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "评论发布版失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    /**
     * 更新帖子的回复总数
     * @param addComment true代表添加，false 代表删除
     */
    fun updatePostComment(addComment: Boolean = false) {
        viewModel.postDetailState.value.getDataOrNull()?.let { postItem ->
            var currentCommentNum = postItem.commentNum ?: 0
            if (addComment) {
                currentCommentNum += 1
                postItem.commentNum = currentCommentNum
            } else {
                currentCommentNum -= 1
                postItem.commentNum = currentCommentNum
            }
            postParentAdapter.setData(postItem)
            updatePostActionButtons(postItem)
        }

    }


    /**
     * 处理评论点赞/取消点赞
     */
    private fun handleCommentLike(commentActionParams: CommentActionParams) {
        // 然后触发网络请求
        lifecycleScope.launch {
            viewModel.toggleCommentLikeFlow(commentActionParams.commentItem.id).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = {
                    hideLoadingDialog()
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    // 失败：回滚UI状态
                    postParentAdapter.getCommentAdapter()
                        .rollbackCommentLikeState(commentActionParams)
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "操作失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }


    /**
     * 显示评论操作选项框
     */
    private fun showCommentActionDialog(commentActionParams: CommentActionParams) {
        val groupOne = CommonActionGroup(
            items = listOf(
                CommonActionItem(
                    icon = R.drawable.monika_post_detail_subcribe_icon, // 可以添加图标资源ID
                    content = "回复",
                    type = "reply"
                ),
                CommonActionItem(
                    icon = R.drawable.monika_list_alert_copy_icon,
                    content = "复制",
                    type = "copy"
                )
            )
        )
        val groupTwoItems = mutableListOf<CommonActionItem>()
        if (commentActionParams.commentItem.canDel == 1) {
            groupTwoItems.add(
                CommonActionItem(
                    icon = R.drawable.monika_list_alert_delete_icon,
                    content = "删除",
                    type = "delete"
                )
            )
        }

        val groupTwo = CommonActionGroup(
            items = groupTwoItems
        )
        // 构建操作组
        val actionGroups = listOf(
            // 第一组：回复、复制
            groupOne,
            // 第二组：删除
            groupTwo
        )

        val dialog = CommentActionBottomSheet.newInstance(actionGroups, "操作")
        dialog.onActionItemClickListener = { actionItem ->
            when (actionItem.type) {
                "reply" -> {
                    // 弹出回复框
                    showCommentInputDialog(commentActionParams)
                }

                "copy" -> {
                    // 复制评论内容
                    copyCommentContent(commentActionParams.commentItem)
                }


                "delete" -> {
                    // 显示删除确认对话框
                    showDeleteConfirmDialog(commentActionParams)
                }
            }
        }
        dialog.show(supportFragmentManager, "CommentActionBottomSheet")
    }

    /**
     * 复制评论内容
     */
    private fun copyCommentContent(commentItem: CommentItem) {
        val content = commentItem.content ?: ""
        if (content.isNotEmpty()) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("评论内容", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * 处理帖子点赞/取消点赞
     */
    private fun handlePostLike() {
        val postItem = viewModel.postDetailState.value.getDataOrNull()
        val postId = postItem?.id ?: return
        lifecycleScope.launch {
            viewModel.togglePostLikeFlow(postId).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = {
                    hideLoadingDialog()
                    postItem.isLiked = postItem.isLiked != true
                    postItem.likeNum =
                        (postItem.likeNum ?: 0) + if (postItem.isLiked == true) 1 else -1
                    postItem.likeNum = maxOf(0, postItem.likeNum ?: 0)
                    updatePostActionButtons(postItem)
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "操作失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    /**
     * 处理帖子收藏/取消收藏
     */
    private fun handlePostCollect() {
        val postItem = viewModel.postDetailState.value.getDataOrNull()
        val postId = postItem?.id ?: return
        lifecycleScope.launch {
            viewModel.togglePostCollectFlow(postId).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = {
                    hideLoadingDialog()
                    postItem.isCollected = postItem.isCollected != true
                    postItem.collectNum =
                        (postItem.collectNum ?: 0) + if (postItem.isCollected == true) 1 else -1
                    postItem.collectNum = maxOf(0, postItem.collectNum ?: 0)
                    updatePostActionButtons(postItem)
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "操作失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(commentActionParams: CommentActionParams) {
        val dialog = MonikaConfirmBottomDialog.newInstance(
            content = "确定要删除这条评论吗？删除后将无法恢复。",
            cancelText = "取消",
            confirmText = "确定"
        )
        dialog.onConfirmClickListener = {
            // 处理删除评论
            handleCommentDelete(commentActionParams)
        }
        dialog.show(supportFragmentManager)
    }

    /**
     * 处理评论删除
     */
    private fun handleCommentDelete(commentActionParams: CommentActionParams) {
        val postId = intent.getStringExtra(EXTRA_POST_ID)
        lifecycleScope.launch {
            viewModel.removeCommentFlow(commentActionParams.commentItem.id, postId).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = {
                    hideLoadingDialog()
                    // 删除成功，局部更新评论列表
                    postParentAdapter.getCommentAdapter()
                        .deleteComment(commentActionParams)
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "删除成功",
                        Toast.LENGTH_SHORT
                    ).show()
                    updatePostComment(false)
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@MonikaPostDetailActivity,
                        "删除失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }


}