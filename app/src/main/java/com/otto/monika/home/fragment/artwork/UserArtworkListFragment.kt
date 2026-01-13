package com.otto.monika.home.fragment.artwork

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.otto.monika.R
import com.otto.monika.api.common.collectSimple
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.databinding.FragmentUserArtworkListBinding
import com.otto.monika.home.fragment.artwork.adapter.UserArtworkAdapter
import com.otto.monika.home.fragment.artwork.model.UserArtworkModel
import com.otto.monika.home.fragment.artwork.viewmodel.UserArtworkViewModel
import com.otto.monika.home.fragment.mine.listener.TabCountListener
import kotlinx.coroutines.launch

/**
 * 用户艺术品列表 Fragment
 * 使用 BaseQuickAdapter 实现分页加载
 */
class UserArtworkListFragment : MonikaBaseFragment(), TabCountListener {

    companion object {
        private const val ARG_UID = "arg_uid"

        /**
         * 创建 Fragment 实例
         * @param uid 用户ID
         */
        @JvmStatic
        fun newInstance(uid: String): UserArtworkListFragment {
            return UserArtworkListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
        }
    }

    private lateinit var binding: FragmentUserArtworkListBinding
    private val viewModel: UserArtworkViewModel by viewModels()
    var quickAdapterHelper: QuickAdapterHelper? = null
    private var artworkAdapter: UserArtworkAdapter? = null

    private var uid: Long = 0L

    // 分页相关状态
    private var currentPage = 0
    private var totalCount = 0

    // TabCountListener 相关
    private var onCountChangeListener: ((Int) -> Unit)? = null
    override fun onFinishCreateView() {
        initViews()
        setupUiState()
        loadFirstPage()
    }

    override fun getContentViewId(): Int {
        return R.layout.fragment_user_artwork_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getLong(ARG_UID, 0L)
        }
    }


    /**
     * 初始化视图
     */
    private fun initViews() {
        // 初始化 RecyclerView
        artworkAdapter = UserArtworkAdapter()
        quickAdapterHelper =
            QuickAdapterHelper.Builder(artworkAdapter!!).setTrailingLoadStateAdapter(object :
                TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    onLoadMoreRequested()
                }

                override fun onFailRetry() {
                    onLoadMoreRequested()
                }

            }).build()
        binding.baseList.layoutManager = LinearLayoutManager(requireContext())
        binding.baseList.adapter = quickAdapterHelper!!.adapter
        // 添加 item 间距装饰器（20dp）
        binding.baseList.addItemDecoration(VerticalSpacingItemDecoration(20))
        // 设置分页加载监听器
        artworkAdapter?.stateView = MonikaEmptyView(requireContext()).apply {
            setEmptyText("暂无作品(｡･ω･｡)")
        }
        artworkAdapter?.onItemClickListener = { item: UserArtworkModel, position: Int ->
            Toast.makeText(requireActivity(), "点击了第 $position Item", Toast.LENGTH_SHORT).show()
        }
        artworkAdapter?.onButtonClickListener = { item: UserArtworkModel, position: Int ->
            if (item.isUnlocked) {
                Toast.makeText(
                    requireActivity(),
                    "点击了查看",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "点击了解锁",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupUiState() {
        // 观察列表数据
        lifecycleScope.launch {
            viewModel.artworkListState.collectSimple(
                onLoading = {
                    quickAdapterHelper?.trailingLoadState = LoadState.Loading
                },
                onSuccess = { response ->
                    response?.let {
                        // 保存总数
                        totalCount = it.total
                        // 通知数量变化
                        onCountChangeListener?.invoke(totalCount)
                        val userInfoList = it.list
                        if (currentPage == 0) {
                            // 第一页，替换数据
                            artworkAdapter?.submitList(userInfoList)
                        } else {
                            // 后续页，追加数据
                            artworkAdapter?.addAll(userInfoList)
                        }
                        // 判断是否还有更多数据
                        val currentDataSize = artworkAdapter?.itemCount ?: 0
                        val hasMore =
                            userInfoList.size >= UserArtworkViewModel.PAGE_SIZE && currentDataSize < totalCount
                        if (!hasMore && currentPage > 0) {
                            quickAdapterHelper?.trailingLoadState = LoadState.NotLoading(true)
                        } else {
                            quickAdapterHelper?.trailingLoadState = LoadState.NotLoading(false)
                        }
                    }
                },
                onFailure = { message ->
                    quickAdapterHelper?.trailingLoadState = LoadState.Error(Exception(message))
                })
        }
    }


    /**
     * 加载第一页数据
     */
    private fun loadFirstPage() {
        viewModel.resetData()
        viewModel.loadData(0)
    }

    /**
     * BaseQuickAdapter 分页加载回调
     */
    fun onLoadMoreRequested() {
        // 加载下一页
        val nextPage = viewModel.getCurrentPage() + 1
        viewModel.loadData(nextPage)
    }

    // TabCountListener 接口实现
    override fun setOnCountChangeListener(onCountChange: (Int) -> Unit) {
        this.onCountChangeListener = onCountChange
    }

    override fun getCurrentCount(): Int? {
        val count = artworkAdapter?.itemCount ?: 0
        return if (count > 0) count else null
    }
}
