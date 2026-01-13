package com.otto.monika.account.rank.fragment

import android.os.Bundle
import android.util.Log
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.otto.monika.R
import com.otto.monika.account.home.AccountHomeActivity
import com.otto.monika.account.rank.fragment.adapter.UserSubscribeRankAdapter
import com.otto.monika.account.rank.fragment.viewmodel.UserSubscribeRankViewModel
import com.otto.monika.api.common.collectSimple
import com.otto.monika.api.model.subscribe.response.SubscribeUserItem
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.model.MonikaFragmentRefreshState
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.subscribe.support.model.SubscribeSuccessEvent
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe


/**
 * 订阅排行榜来源类型
 */
enum class RankSource {
    ACCOUNT,  // 账户来源
    USER      // 用户来源
}

/**
 * 账户订阅排行榜 Fragment
 * 使用 BaseQuickAdapter 实现分页加载
 */
class UserSubscribeRankFragment : MonikaBaseFragment(){

    companion object {
        private const val ARG_SOURCE = "arg_source"
        private const val ARG_UID = "arg_uid"

        /**
         * 创建 Fragment 实例
         * @param source 来源类型，默认为 ACCOUNT
         * @param uid 用户ID，默认为 0
         */
        @JvmStatic
        fun newInstance(
            source: RankSource = RankSource.ACCOUNT,
            uid: String? = null
        ): UserSubscribeRankFragment {
            return UserSubscribeRankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SOURCE, source.name)
                    putString(ARG_UID, uid)
                }
            }
        }
    }

    private val viewModel: UserSubscribeRankViewModel by viewModels()
    private var rankList: RecyclerView? = null
    private var rankAdapter: UserSubscribeRankAdapter? = null
    var quickAdapterHelper: QuickAdapterHelper? = null
    private var source: RankSource = RankSource.ACCOUNT

    // 分页相关状态
    private var currentPage = 0
    private var totalCount = 0
    private var uid: String? = null

    //刷新状态
    val cyFragmentRefreshState = MonikaFragmentRefreshState(this) {
        if (it) {
            loadFirstPage()
        }
    }


    override fun getContentViewId(): Int {
        return R.layout.fragment_account_subscribe_rank
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getString(ARG_SOURCE)?.let { sourceName ->
                source = try {
                    RankSource.valueOf(sourceName)
                } catch (_: IllegalArgumentException) {
                    RankSource.ACCOUNT
                }
            }
            uid = it.getString(ARG_UID)
        }
    }

    override fun onFinishCreateView() {

        rankList = findViewById(R.id.base_list)

        // 初始化 RecyclerView
        rankAdapter = UserSubscribeRankAdapter()
        quickAdapterHelper = QuickAdapterHelper.Builder(rankAdapter!!)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    onLoadMoreRequested()
                }

                override fun onFailRetry() {
                    onLoadMoreRequested()
                }

            }).build()
        rankList?.layoutManager = LinearLayoutManager(requireContext())
        rankList?.adapter = quickAdapterHelper?.adapter
        // 添加 item 间距装饰器（10dp）
        rankList?.addItemDecoration(VerticalSpacingItemDecoration(20))

        // 设置分页加载监听器
        rankAdapter?.stateView = MonikaEmptyView(requireContext()).apply {
            setEmptyText("暂无数据(｡･ω･｡)")
        }
        rankAdapter?.onItemClickListener = { item: SubscribeUserItem, position: Int ->
            AccountHomeActivity.enter(requireActivity(), item.subscriber?.uid)
        }
        // 观察数据变化
        setupUiState()
        // 加载第一页数据
        loadFirstPage()
    }

    /**
     * 获取来源类型
     */
    fun getSource(): RankSource {
        return source
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 收集 rankListState 来获取分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rankListState.collectSimple(
                onLoading = {
                    quickAdapterHelper?.trailingLoadState = LoadState.Loading
                },
                onSuccess = { response ->
                    response?.let {
                        // 保存总数
                        totalCount = it.total
                        val newData = it.list
                        if (currentPage == 0) {
                            // 第一页，替换数据
                            rankAdapter?.submitList(newData)
                        } else {
                            // 后续页，追加数据
                            rankAdapter?.addAll(newData)
                        }
                        // 判断是否还有更多数据
                        val currentDataSize = rankAdapter?.items?.size ?: 0
                        val hasMore =
                            newData.size >= UserSubscribeRankViewModel.PAGE_SIZE && currentDataSize < totalCount
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

    override fun registerBus(): Boolean {
        return true
    }

    @Subscribe
    fun onSubscribeChanged(subscribeSuccessEvent: SubscribeSuccessEvent) {
        if (subscribeSuccessEvent.uid == uid) {
            subscribeListChanged()
        }
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
     * 收藏列表变化了，需要重新拉取
     */
    fun subscribeListChanged() {
        cyFragmentRefreshState.refresh()
    }
}
