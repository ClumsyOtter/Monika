package com.otto.monika.home.fragment.favorite.creator

import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.otto.monika.R
import com.otto.monika.account.home.AccountHomeActivity
import com.otto.monika.api.common.collectSimple
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.model.MonikaFragmentRefreshState
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.databinding.FragmentUserFavoriteCreatorListBinding
import com.otto.monika.home.fragment.favorite.creator.adapter.UserFavoriteCreatorAdapter
import com.otto.monika.home.fragment.favorite.creator.viewmodel.UserFavoriteCreatorViewModel
import com.otto.monika.home.fragment.mine.listener.TabCountListener
import com.otto.monika.home.fragment.mine.model.CollectCreatorEvent
import com.otto.monika.subscribe.support.SubscriptionSupportActivity
import com.otto.monika.subscribe.support.model.SubscribeSuccessEvent
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe


/**
 * 用户喜欢的创作者列表 Fragment
 * 使用 BaseQuickAdapter 实现分页加载
 */
class UserFavoriteCreatorListFragment : MonikaBaseFragment(), TabCountListener {

    companion object {
        private const val ARG_UID = "arg_uid"

        /**
         * 创建 Fragment 实例
         * @param uid 用户ID
         */
        @JvmStatic
        fun newInstance(uid: String?): UserFavoriteCreatorListFragment {
            return UserFavoriteCreatorListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
        }
    }

    private lateinit var binding: FragmentUserFavoriteCreatorListBinding
    private val viewModel: UserFavoriteCreatorViewModel by viewModels()
    private var creatorAdapter: UserFavoriteCreatorAdapter? = null
    var quickAdapterHelper: QuickAdapterHelper? = null
    private var uid: String? = null

    // 分页相关状态
    private var currentPage = 0
    private var totalCount = 0

    // TabCountListener 相关
    private var onCountChangeListener: ((Int) -> Unit)? = null

    //刷新状态
    val cyFragmentRefreshState = MonikaFragmentRefreshState(this) {
        if (it) {
            loadFirstPage()
            creatorAdapter?.needUpdatePostIdList?.clear()
        } else {
            val deleteItemSize =
                creatorAdapter?.needUpdatePostIdList?.filter { it.second == 1 }?.size ?: 0
            if (deleteItemSize > 0) {
                totalCount -= deleteItemSize
                onCountChangeListener?.invoke(totalCount)
            }
            creatorAdapter?.notifyChangedIfNeedUpdate()
        }
    }

    override fun onFinishCreateView() {
        initViews()
        setupUiState()
        loadFirstPage()
    }


    override fun getContentViewId(): Int {
        return R.layout.fragment_user_favorite_creator_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        // 初始化 RecyclerView
        creatorAdapter = UserFavoriteCreatorAdapter()
        quickAdapterHelper =
            QuickAdapterHelper.Builder(creatorAdapter!!).setTrailingLoadStateAdapter(object :
                TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    onLoadMoreRequested()
                }

                override fun onFailRetry() {
                    onLoadMoreRequested()
                }

            }).build()
        binding.baseList.layoutManager = LinearLayoutManager(requireContext())
        binding.baseList.adapter = quickAdapterHelper?.adapter
        // 添加 item 间距装饰器（8dp）
        binding.baseList.addItemDecoration(VerticalSpacingItemDecoration(8))

        creatorAdapter?.stateView = MonikaEmptyView(requireContext()).apply {
            setEmptyText("暂无订阅者(｡･ω･｡)")
        }
        creatorAdapter?.onItemClickListener = { item, position: Int ->
            AccountHomeActivity.enter(requireActivity(), item.id)
        }
        creatorAdapter?.onSubscribeClickListener = { item, position: Int ->
            SubscriptionSupportActivity.enter(requireActivity(), item.id)
        }
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 收集 creatorListState 来获取分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.creatorListState.collectSimple(
                onLoading = {
                    quickAdapterHelper?.trailingLoadState = LoadState.Loading
                },
                onSuccess = { response ->
                    response?.let {
                        // 保存总数
                        totalCount = it.total
                        // 通知数量变化
                        onCountChangeListener?.invoke(totalCount)
                        // 提取 userInfo 列表
                        val userInfoList =
                            it.list.mapNotNull { collectItem -> collectItem.userInfo }
                        if (currentPage == 0) {
                            // 第一页，替换数据
                            creatorAdapter?.submitList(userInfoList)
                            // 检查是否满一屏，决定是否启用加载更多
                        } else {
                            // 后续页，追加数据
                            creatorAdapter?.addAll(userInfoList)
                        }
                        // 判断是否还有更多数据
                        val currentDataSize = creatorAdapter?.itemCount ?: 0
                        val hasMore =
                            userInfoList.size >= UserFavoriteCreatorViewModel.PAGE_SIZE && currentDataSize < totalCount
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
        viewModel.resetData()
        viewModel.loadData(0, uid)
    }

    /**
     * BaseQuickAdapter 分页加载回调
     */
    fun onLoadMoreRequested() {
        // 加载下一页
        currentPage++
        viewModel.loadData(currentPage, uid)
    }

    // TabCountListener 接口实现
    override fun setOnCountChangeListener(onCountChange: (Int) -> Unit) {
        this.onCountChangeListener = onCountChange
    }

    override fun getCurrentCount(): Int? {
        return if (totalCount > 0) totalCount else null
    }

    override fun registerBus(): Boolean {
        return true
    }

    override fun onFragmentSelected(isSelected: Boolean) {
        super.onFragmentSelected(isSelected)
        cyFragmentRefreshState.onFragmentSelected(isSelected)
    }

    override fun onResume() {
        super.onResume()
        cyFragmentRefreshState.onResume()
    }

    /**
     * 订阅信息改变
     */
    @Subscribe
    fun onSubscribeChanged(subscribeSuccessEvent: SubscribeSuccessEvent) {
        creatorAdapter?.items?.find { it.id == subscribeSuccessEvent.uid }?.let {
            cyFragmentRefreshState.refresh()
        }
    }

    /**
     * 收藏的创作者改变
     */
    @Subscribe
    fun onCollectCreatorChange(collectCreatorEvent: CollectCreatorEvent) {
        if (collectCreatorEvent.isCollected == true) {
            cyFragmentRefreshState.refresh()
        } else {
            creatorAdapter?.items?.find { it.id == collectCreatorEvent.creatorUid }?.let {
                creatorAdapter?.needUpdatePostIdList?.add(Pair(it.id, 1))
            }
        }
    }

}
