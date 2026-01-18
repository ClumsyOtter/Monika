package com.otto.monika.home.fragment.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.flyjingfish.openimagelib.OpenImage
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.DividerItemDecoration
import com.otto.monika.common.dialog.MonikaConfirmBottomDialog
import com.otto.monika.common.model.MonikaFragmentRefreshState
import com.otto.monika.common.model.OpenImageModel
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.databinding.FragmentUserPostListBinding
import com.otto.monika.home.fragment.mine.listener.TabCountListener
import com.otto.monika.home.fragment.post.adapter.UserPostAdapter
import com.otto.monika.home.fragment.post.viewmodel.UserPostViewModel
import com.otto.monika.post.detail.MonikaPostDetailActivity
import com.otto.monika.post.detail.model.ItemChanged
import com.otto.monika.post.detail.model.PostChangeEvent
import com.otto.monika.post.publish.MonikaPublishPostActivity
import com.otto.network.common.collectSimple
import com.otto.network.model.post.response.PostItem
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 帖子列表来源类型
 */
enum class PostSource {
    User,      // 用户来源
    Favorite,   // 收藏来源

    UserSubscriber //订阅的用户动态
}

/**
 * 用户帖子列表 Fragment
 * 使用 BaseQuickAdapter 实现分页加载
 */
class UserPostListFragment : MonikaBaseFragment(), TabCountListener {

    companion object {
        private const val ARG_UID = "arg_uid"
        private const val ARG_OWNER = "arg_owner"
        private const val ARG_SOURCE = "arg_source"

        /**
         * 创建 Fragment 实例
         * @param uid 用户ID
         * @param sourceFrom 来源类型，默认为 User
         */
        @JvmStatic
        fun newInstance(
            uid: String?,
            isOwner: Boolean = false,
            sourceFrom: PostSource = PostSource.User
        ): UserPostListFragment {
            return UserPostListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                    putBoolean(ARG_OWNER, isOwner)
                    putString(ARG_SOURCE, sourceFrom.name)
                }
            }
        }
    }

    private lateinit var userPostListBinding: FragmentUserPostListBinding
    private val viewModel: UserPostViewModel by viewModels()
    private var postAdapter: UserPostAdapter? = null
    var quickAdapterHelper: QuickAdapterHelper? = null

    // 分页相关状态
    private var currentPage = 0
    private var totalCount = 0

    //用户数据
    private var uid: String? = null
    private var isOwner = false

    //来源
    private var sourceFrom: PostSource = PostSource.User

    val cyFragmentRefreshState = MonikaFragmentRefreshState(this) {
        if (it) {
            loadFirstPage()
            postAdapter?.needUpdatePostIdList?.clear()
        } else {
            val deleteItemSize =
                postAdapter?.needUpdatePostIdList?.filter { it.second == 1 }?.size ?: 0
            if (deleteItemSize > 0) {
                totalCount -= deleteItemSize
                onCountChangeListener?.invoke(totalCount)
            }
            postAdapter?.notifyChangedIfNeedUpdate()

        }
    }

    private var onCountChangeListener: ((Int) -> Unit)? = null
    override fun onFinishCreateView() {
        initViews()
        setupUiState()
        loadFirstPage()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userPostListBinding = FragmentUserPostListBinding.inflate(inflater)
        return userPostListBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
            isOwner = it.getBoolean(ARG_OWNER, false)
            it.getString(ARG_SOURCE)?.let { sourceName ->
                sourceFrom = try {
                    PostSource.valueOf(sourceName)
                } catch (_: IllegalArgumentException) {
                    PostSource.User
                }
            }
        }
        registerEventBus()
    }


    /**
     * 初始化视图
     */
    private fun initViews() {
        // 初始化 RecyclerView
        postAdapter = UserPostAdapter(isOwner)
        quickAdapterHelper =
            QuickAdapterHelper.Builder(postAdapter!!).setTrailingLoadStateAdapter(object :
                TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    onLoadMoreRequested()
                }

                override fun onFailRetry() {
                    onLoadMoreRequested()
                }

            }).build()
        userPostListBinding.baseList.layoutManager = LinearLayoutManager(requireContext())
        userPostListBinding.baseList.adapter = quickAdapterHelper?.adapter
        // 添加分割线装饰器
        userPostListBinding.baseList.addItemDecoration(
            DividerItemDecoration(
                3,
                R.color.rank_list_bg
            )
        )
        // 设置分页加载监听器
        postAdapter?.stateView = MonikaEmptyView(requireContext()).apply {
            setEmptyText("暂无动态(｡･ω･｡)")
        }
        postAdapter?.onFavoriteClickListener = { item, position ->
            // 根据 isLiked 状态决定是点赞还是取消点赞
            handlePostLike(item, position)
        }
        postAdapter?.onItemClickListener = { item, position ->
            // status: 1 表示正常，0 表示审核中，-1 表示审核失败
            when (item.status) {
                -1 -> {
                    val confirmBottomDialog =
                        MonikaConfirmBottomDialog.newInstance(
                            "内容审核未通过，是否需要\n" +
                                    "重新编辑后进行发布？", "取消", "重新编辑"
                        )
                    confirmBottomDialog.onConfirmClickListener = {
                        MonikaPublishPostActivity.enter(requireActivity(), item)
                    }
                    confirmBottomDialog.show(childFragmentManager)
                }

                0 -> {
                    Toast.makeText(
                        requireContext(),
                        "内容正在审核中，请稍后再尝试操作",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    goToPostCyPostDetailActivity(item.id)
                }
            }
        }
        postAdapter?.onImagesClick = { imageList: List<String>, index: Int ->
            imageList.getOrNull(index)?.let {
                //在点击时调用
                OpenImage.with(requireActivity())
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

        postAdapter?.onReplyClickListener = { item, position ->
            goToPostCyPostDetailActivity(item.id)
        }

        postAdapter?.onItemDeleteClickListener = { item, position ->
            val confirmBottomDialog =
                MonikaConfirmBottomDialog.newInstance("确定要删除动态吗？", "取消", "确认删除")
            confirmBottomDialog.onConfirmClickListener = {
                // 执行删除操作
                handlePostDelete(item, position)
            }
            confirmBottomDialog.show(childFragmentManager)
        }
    }

    private fun goToPostCyPostDetailActivity(postId: String?) {
        MonikaPostDetailActivity.enter(requireActivity(), postId)
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 收集 postListState 来获取分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.postListState.collectSimple(
                onLoading = {
                    quickAdapterHelper?.trailingLoadState = LoadState.Loading
                },
                onSuccess = { response ->
                    response?.let {
                        // 保存总数
                        totalCount = it.total
                        // 通知数量变化
                        onCountChangeListener?.invoke(totalCount)
                        val newData = it.list
                        if (currentPage == 0) {
                            // 第一页，替换数据
                            postAdapter?.submitList(newData)
                            // 检查是否满一屏，决定是否启用加载更多
                        } else {
                            // 后续页，追加数据
                            postAdapter?.addAll(newData)
                        }
                        // 判断是否还有更多数据
                        val currentDataSize = postAdapter?.itemCount ?: 0
                        val hasMore =
                            newData.size >= UserPostViewModel.PAGE_SIZE && currentDataSize < totalCount
                        if (!hasMore && currentPage > 0) {
                            quickAdapterHelper?.trailingLoadState = LoadState.NotLoading(true)
                        } else {
                            quickAdapterHelper?.trailingLoadState = LoadState.NotLoading(false)
                        }
                    }
                },
                onFailure = { message ->
                    quickAdapterHelper?.trailingLoadState = LoadState.Error(Exception(message))
                }
            )
        }
    }

    /**
     * 加载第一页数据
     */
    private fun loadFirstPage() {
        currentPage = 0
        totalCount = 0
        postAdapter?.needUpdatePostIdList?.clear()
        viewModel.resetData()
        viewModel.loadData(0, uid, sourceFrom)
    }

    /**
     * BaseQuickAdapter 分页加载回调
     */
    fun onLoadMoreRequested() {
        // 加载下一页
        currentPage++
        viewModel.loadData(currentPage, uid, sourceFrom)
    }

    /**
     * 处理帖子点赞/取消点赞
     */
    private fun handlePostLike(item: PostItem, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            //本地已经更新好状态了
            val likeFlow = if (item.isLiked == true) {
                viewModel.addPostLikeFlow(item.id)
            } else {
                viewModel.removePostLikeFlow(item.id)
            }
            likeFlow.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { _ ->
                    postLikeChangeEvent(item)
                    hideLoadingDialog()
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    postAdapter?.rollbackLikeState(item, position)
                }
            )
        }
    }

    private fun postLikeChangeEvent(postItem: PostItem) {
        unregisterEventBus()
        EventBus.getDefault().post(
            PostChangeEvent(
                postId = postItem.id,
                likeChanged = ItemChanged(postItem.isLiked, postItem.likeNum)
            )
        )
        registerEventBus()

    }

    private fun postDeleteEvent(postItem: PostItem) {
        unregisterEventBus()
        EventBus.getDefault()
            .post(PostChangeEvent(postId = postItem.id, postDelete = ItemChanged(newState = true)))
        registerEventBus()
    }


    /**
     * 处理帖子删除
     */
    private fun handlePostDelete(item: PostItem, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.removePostFlow(item.id ?: "").collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { success ->
                    hideLoadingDialog()
                    if (success == true) {
                        // 删除成功，从列表中移除该项
                        postAdapter?.removeAt(position)
                        // 更新总数
                        totalCount--
                        // 通知数量变化
                        onCountChangeListener?.invoke(totalCount)
                        // 显示成功提示
                        Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                        postDeleteEvent(item)
                    } else {
                        Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // TabCountListener 接口实现
    override fun setOnCountChangeListener(onCountChange: (Int) -> Unit) {
        if (sourceFrom == PostSource.Favorite) {
            this.onCountChangeListener = onCountChange
        }
    }

    override fun getCurrentCount(): Int? {
        return if (totalCount > 0) totalCount else null
    }

    fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterEventBus()
    }

    @Subscribe
    fun onPostChanged(postChangeEvent: PostChangeEvent) {
        if (postChangeEvent.postDelete != null) {
            postAdapter?.onPostChanged(sourceFrom, postChangeEvent)
        } else {
            //如果是收藏的变化，那么会涉及到删除
            if (sourceFrom == PostSource.Favorite && postChangeEvent.collectChanged != null && postChangeEvent.collectChanged.newState == true) {
                postListChanged()
            } else {
                //删除了收藏
                postAdapter?.onPostChanged(sourceFrom, postChangeEvent)
            }
        }
    }


    /**
     * 帖子列表变化了，需要重新拉取
     */
    fun postListChanged() {
        cyFragmentRefreshState.refresh()
    }

    override fun onFragmentSelected(isSelected: Boolean) {
        super.onFragmentSelected(isSelected)
        cyFragmentRefreshState.onFragmentSelected(isSelected)
    }

    override fun onResume() {
        super.onResume()
        cyFragmentRefreshState.onResume()
    }
}
