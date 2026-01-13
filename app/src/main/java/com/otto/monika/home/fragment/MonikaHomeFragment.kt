package com.otto.monika.home.fragment

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.otto.monika.R
import com.otto.monika.account.home.AccountHomeActivity
import com.otto.monika.account.subscriberPost.UserSubscribePostListActivity
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.home.adapter.listener.MonikaPageListener
import com.otto.monika.home.model.MonikaBannerData
import com.otto.monika.home.model.MonikaBannerItem
import com.otto.monika.home.model.MonikaRankData
import com.otto.monika.home.model.MonikaSubscribeData
import com.otto.monika.home.model.SubscribeModel
import com.otto.monika.home.viewmodel.MonikaViewModel
import com.otto.monika.post.detail.MonikaPostDetailActivity
import com.otto.monika.subscribe.rank.SubscribeRankActivity
import com.otto.monika.subscribe.rank.model.RankModel
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.common.utils.StatusBarUtil
import com.otto.monika.databinding.FragMonikaViewBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

open class MonikaHomeFragment : MonikaBaseFragment() {

    companion object {
        fun newInstance(): MonikaHomeFragment {
            return MonikaHomeFragment()
        }
    }

    private lateinit var mDataBinding: FragMonikaViewBinding
    //private lateinit var mAdapter: MonikaPageAdapter
    private val viewModel: MonikaViewModel by viewModels()

    private val bannerData: MonikaBannerData = MonikaBannerData()
    private val subscribeModel: MonikaSubscribeData = MonikaSubscribeData()
    private val rankModel: MonikaRankData = MonikaRankData()
    override fun onFinishCreateView() {
        val marginTop =
            StatusBarUtil.getStatusBarHeight(requireContext()) + DipUtils.dpToPx(57)
        (mDataBinding.ptrListRefresh.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topMargin = marginTop
            mDataBinding.ptrListRefresh.layoutParams = it
        }
        mDataBinding.baseList.clipChildren = false
        mDataBinding.baseList.itemAnimator = null
        mDataBinding.baseList.layoutManager = LinearLayoutManager(activity)
//        mAdapter = MonikaPageAdapter(this, object : MonikaPageListener() {
//            override fun onRankMoreClick() {
//                SubscribeRankActivity.enter(requireActivity())
//            }
//
//            override fun onRankItemClick(model: RankModel) {
//                AccountHomeActivity.enter(requireActivity(), model.creator?.uid)
//            }
//
//            override fun onSubscribeItemClick(subscribeModel: SubscribeModel) {
//                MonikaPostDetailActivity.enter(requireActivity(), subscribeModel.id)
//            }
//
//            override fun onBannerItemClick(bannerItem: MonikaBannerItem) {
//            }
//
//            override fun oSubscribeMoreClick() {
//                UserSubscribePostListActivity.enter(requireActivity())
//            }
//        })
        //mDataBinding.baseList.adapter = mAdapter

        mDataBinding.ptrListRefresh.setOnRefreshListener {
            getInitData()
        }
        getInitData()
    }

    override fun getContentViewId(): Int {
        return R.layout.frag_monika_view
    }

    private fun getInitData() {
        lifecycleScope.launch {
            // 并行执行三个 API 请求，跳过 Loading 状态，只获取实际结果
            val bannerDeferred = async {
                viewModel.homeBannerList()
                    .filter { it !is ApiResponse.Loading && it !is ApiResponse.Initial }
                    .first()
                    .let { response ->
                        when (response) {
                            is ApiResponse.Success -> {
                                response.data?.let { newBannerData ->
                                    bannerData.list = newBannerData.list
                                }
                            }

                            else -> {
                                // 可以在这里处理错误
                            }
                        }
                    }
            }

            val subscribeDeferred = async {
                viewModel.subscribePostList(1, 10)
                    .filter { it !is ApiResponse.Loading && it !is ApiResponse.Initial }
                    .first()
                    .let { response ->
                        when (response) {
                            is ApiResponse.Success -> {
                                response.data?.let { data ->
                                    subscribeModel.list = data.list
                                }
                            }

                            else -> {
                                // 可以在这里处理错误
                            }
                        }
                    }
            }

            val rankDeferred = async {
                viewModel.homeRanking(20, null)
                    .filter { it !is ApiResponse.Loading && it !is ApiResponse.Initial }
                    .first()
                    .let { response ->
                        when (response) {
                            is ApiResponse.Success -> {
                                response.data?.let { data ->
                                    rankModel.list = data.list
                                }
                            }

                            else -> {
                                // 可以在这里处理错误
                            }
                        }
                    }
            }

            // 等待所有请求完成
            awaitAll(bannerDeferred, subscribeDeferred, rankDeferred)
            mDataBinding.ptrListRefresh.isRefreshing = false
        }
    }

}
