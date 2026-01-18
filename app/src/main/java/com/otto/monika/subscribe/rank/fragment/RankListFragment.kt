package com.otto.monika.subscribe.rank.fragment

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.account.home.AccountHomeActivity
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.dialog.CommonBottomSheet
import com.otto.monika.common.views.MonikaEmptyView
import com.otto.monika.subscribe.rank.fragment.adapter.RankListAdapter
import com.otto.monika.subscribe.rank.viewmodel.RankViewModel
import com.otto.network.common.collectSimple
import com.otto.network.model.home.RankModel
import kotlinx.coroutines.launch

/**
 * 榜单列表 Fragment（统一处理周榜、月榜、历史榜单）
 */
class RankListFragment : MonikaBaseFragment() {

    companion object {
        private const val ARG_RANK_TYPE = "arg_rank_type"

        /**
         * 榜单类型
         * @param value 统计类型值：2-周榜，3-月榜，4-所有历史榜单列表
         */
        enum class RankType(val value: Int) {
            WEEK(2),    // 周榜
            MONTH(3),   // 月榜
            HISTORY(4)  // 所有历史榜单列表
        }

        /**
         * 根据数值获取 RankType
         * @param value 统计类型值：2-周榜，3-月榜，4-所有历史榜单列表
         * @return 对应的 RankType，如果找不到则返回 WEEK
         */
        fun fromValue(value: Int): RankType {
            return RankType.values().find { it.value == value } ?: RankType.WEEK
        }

        /**
         * 创建周榜 Fragment
         */
        fun newWeekInstance(): RankListFragment {
            return newInstance(RankType.WEEK)
        }

        /**
         * 创建月榜 Fragment
         */
        fun newMonthInstance(): RankListFragment {
            return newInstance(RankType.MONTH)
        }

        /**
         * 创建历史榜单 Fragment
         */
        fun newHistoryInstance(): RankListFragment {
            return newInstance(RankType.HISTORY)
        }

        /**
         * 创建 Fragment
         */
        private fun newInstance(rankType: RankType): RankListFragment {
            return RankListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RANK_TYPE, rankType.ordinal)
                }
            }
        }
    }

    private val viewModel: RankViewModel by viewModels()
    private var dateSelectorLayout: LinearLayout? = null
    private var dateSelectorText: TextView? = null
    private var emptyView: MonikaEmptyView? = null
    private var rankList: RecyclerView? = null
    private var rankAdapter: RankListAdapter? = null
    private var rankType: RankType = RankType.WEEK

    override fun getContentViewId(): Int {
        return R.layout.fragment_rank_list
    }

    override fun onFinishCreateView() {
        // 获取榜单类型
        arguments?.let {
            val typeOrdinal = it.getInt(ARG_RANK_TYPE, RankType.WEEK.ordinal)
            rankType = RankType.values().getOrElse(typeOrdinal) { RankType.WEEK }
        }
        emptyView = findViewById(R.id.empty_view)
        emptyView?.setEmptyText("暂未数据")
        dateSelectorLayout = findViewById(R.id.date_selector_layout)
        dateSelectorText = findViewById(R.id.date_selector_text)
        rankList = findViewById(R.id.rank_list)
        // 历史榜单隐藏日期选择器
        if (rankType == RankType.HISTORY) {
            dateSelectorLayout?.isVisible = false
        }
        // 初始化 RecyclerView
        rankAdapter = RankListAdapter()
        rankList?.layoutManager = LinearLayoutManager(requireContext())
        rankList?.adapter = rankAdapter
        // 添加 item 间距装饰器（10dp）
        rankList?.addItemDecoration(VerticalSpacingItemDecoration(10))
        rankAdapter?.onItemClickListener = { item: RankModel, position: Int ->
            AccountHomeActivity.enter(requireActivity(), item.creator?.uid)
        }
        // 设置日期选择区域点击事件
        dateSelectorLayout?.setOnClickListener {
            showDateSelectorDialog()
        }
        viewModel.dateRangeLiveData.observe(this) {
            it?.itemList?.firstOrNull { it.isSelected }?.let {
                loadRankData(it.identify)
                updateDateButtonText(it.content)
            }
        }

        if (rankType == RankType.HISTORY) {
            // History 直接加载数据，不管有没有日期
            loadRankData("")
        } else {
            viewModel.getDataRangeData(rankType)
        }
    }

    private fun updateDateButtonText(content: String) {
        dateSelectorText?.text = content
    }

    private fun showDateSelectorDialog() {
        viewModel.getDataRangeData(rankType)?.let {
            val dialog = CommonBottomSheet.newInstance(it, maxHeight = 500)
            dialog.setOnItemChangeListener { data ->
                viewModel.updateDataRange(data)
            }
            dialog.show(parentFragmentManager, "CommonBottomSheet")
        }

    }

    private fun loadRankData(dateStart: String) {
        //统计类型：2-周榜，3-月榜，4-所有历史榜单列表
        lifecycleScope.launch {
            viewModel.rankingStats(rankType.value, dateStart)
                .collectSimple(
                    onLoading = {},
                    onSuccess = { response ->
                        response?.let { rankData ->
                            // 更新榜单数据
                            rankAdapter?.setData(rankData.list ?: emptyList())
                            emptyView?.isVisible = rankData.list?.isEmpty() == true
                        }
                    },
                    onFailure = { emptyView?.isVisible = true })
        }
    }
}

