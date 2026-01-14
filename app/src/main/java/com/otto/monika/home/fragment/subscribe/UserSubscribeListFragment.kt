package com.otto.monika.home.fragment.subscribe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.otto.monika.api.model.subscribe.response.MyCreatorItem
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.model.MonikaFragmentRefreshState
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.databinding.FragmentUserSubscribeListBinding
import com.otto.monika.home.fragment.mine.listener.TabCountListener
import com.otto.monika.home.fragment.subscribe.adapter.UserSubscribeAdapter
import com.otto.monika.home.fragment.subscribe.viewmodel.UserSubscribeViewModel
import com.otto.monika.subscribe.support.model.SubscribeSuccessEvent
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe

/**
 * 用户订阅列表 Fragment
 * 使用 BaseQuickAdapter 实现分页加载
 */
class UserSubscribeListFragment : MonikaBaseFragment(), TabCountListener {

    companion object {
        private const val ARG_UID = "arg_uid"

        /**
         * 创建 Fragment 实例
         * @param uid 用户ID
         */
        @JvmStatic
        fun newInstance(uid: String): UserSubscribeListFragment {
            return UserSubscribeListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
        }
    }

    private lateinit var binding: FragmentUserSubscribeListBinding
    private val viewModel: UserSubscribeViewModel by viewModels()
    private var subscribeAdapter: UserSubscribeAdapter? = null
    var quickAdapterHelper: QuickAdapterHelper? = null

    // 分页相关状态
    private var currentPage = 0
    private var totalCount = 0
    private var uid: String? = null

    // TabCountListener 相关
    private var onCountChangeListener: ((Int) -> Unit)? = null

    //刷新状态
    val cyFragmentRefreshState = MonikaFragmentRefreshState(this) {
        if (it) {
            loadFirstPage()
        }
    }

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
        binding = FragmentUserSubscribeListBinding.inflate(inflater)
        return binding.root
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
        subscribeAdapter = UserSubscribeAdapter()
        quickAdapterHelper =
            QuickAdapterHelper.Builder(subscribeAdapter!!).setTrailingLoadStateAdapter(object :
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
        // 设置分页加载监听器
        subscribeAdapter?.stateView = MonikaEmptyView(requireContext()).apply {
            setEmptyText("暂无订阅(｡･ω･｡)")
        }
        subscribeAdapter?.onInfoBoxClickListener = { item: MyCreatorItem, position: Int ->
            AccountHomeActivity.enter(requireActivity(), item.creator?.uid)
        }
    }


    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 收集 subscribeListState 来获取分页数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subscribeListState.collectSimple(
                onLoading = {
                    quickAdapterHelper?.trailingLoadState = LoadState.Loading
                },
                onSuccess = { response ->
                    response?.let {
                        // 保存总数
                        totalCount = it.total ?: 0
                        // 通知数量变化
                        onCountChangeListener?.invoke(totalCount)
                        val newData = it.list
                        if (currentPage == 0) {
                            // 第一页，替换数据
                            subscribeAdapter?.submitList(newData)
                        } else {
                            // 后续页，追加数据
                            subscribeAdapter?.addAll(newData)
                        }
                        // 判断是否还有更多数据
                        val currentDataSize = subscribeAdapter?.items?.size ?: 0
                        val hasMore =
                            newData.size >= UserSubscribeViewModel.PAGE_SIZE && currentDataSize < totalCount
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

    @Subscribe
    fun onSubscribeChanged(subscribeSuccessEvent: SubscribeSuccessEvent) {
        subscribeListChanged()
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
