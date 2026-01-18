package com.otto.monika.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.otto.common.utils.DipUtils
import com.otto.monika.home.viewmodel.MonikaViewModel
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.decoration.GridSpacingItemDecoration
import com.otto.monika.databinding.FragMonikaViewBinding
import com.otto.monika.home.adapter.adapter.BannerRecyclerAdapter
import com.otto.monika.home.adapter.adapter.RecommendAdapter
import com.otto.monika.post.detail.MonikaPostDetailActivity
import com.otto.network.common.collectSimple
import com.otto.network.common.transform
import com.otto.network.model.post.response.PostItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch

open class MonikaHomeFragment : MonikaBaseFragment() {

    companion object {
        fun newInstance(): MonikaHomeFragment {
            return MonikaHomeFragment()
        }
    }

    private lateinit var fragMonikaViewBinding: FragMonikaViewBinding

    private val viewModel: MonikaViewModel by viewModels()
    private var homeBannerAdapter: BannerRecyclerAdapter? = null
    private var homeRecommendAdapter: RecommendAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragMonikaViewBinding = FragMonikaViewBinding.inflate(inflater)
        return fragMonikaViewBinding.root
    }

    override fun onFinishCreateView() {
        //initHomeBanner()
        initRecommend()
        fragMonikaViewBinding.ptrListRefresh.setOnRefreshListener {
            getInitData()
        }
        getInitData()
    }

    fun initRecommend() {
        fragMonikaViewBinding.recommendBaseList.itemAnimator = null
        homeRecommendAdapter = RecommendAdapter()
        homeRecommendAdapter?.onItemClickListener = object : RecommendAdapter.OnItemClickListener {
            override fun onItemClick(
                position: Int,
                item: PostItem?
            ) {
                MonikaPostDetailActivity.enter(requireActivity(), item?.id)
            }
        }
        val layoutManager = GridLayoutManager(context, 2)
        fragMonikaViewBinding.recommendBaseList.layoutManager = layoutManager
        fragMonikaViewBinding.recommendBaseList.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                DipUtils.dpToPx(10),
                false
            )
        )
        fragMonikaViewBinding.recommendBaseList.adapter = homeRecommendAdapter
    }

//    fun initHomeBanner() {
//        fragMonikaViewBinding.bannerBaseList.clipChildren = false
//        fragMonikaViewBinding.bannerBaseList.itemAnimator = null
//        homeBannerAdapter = BannerRecyclerAdapter()
//        val layoutManager =
//            CarouselLayoutManager(MultiBrowseCarouselStrategy(), RecyclerView.HORIZONTAL)
//        fragMonikaViewBinding.bannerBaseList.layoutManager = layoutManager
//        fragMonikaViewBinding.bannerBaseList.adapter = homeBannerAdapter
//        // 滚动到中间位置，让中间卡片居中显示
//        fragMonikaViewBinding.bannerBaseList.post {
//            if (homeBannerAdapter?.items?.isNotEmpty() == true) {
//                val centerPosition = homeBannerAdapter!!.itemCount / 2
//                // 使用 smoothScrollToPosition 让滚动更平滑
//                fragMonikaViewBinding.bannerBaseList.smoothScrollToPosition(centerPosition)
//            }
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getInitData() {
        lifecycleScope.launch {
            showLoadingDialog()
            // 并行执行三个 API 请求，跳过 Loading 状态，只获取实际结果
            val bannerDeferred = async {
                viewModel.homeBannerList()
                    .collectSimple(onLoading = {
                    }, onSuccess = {
                        homeBannerAdapter?.submitList(it?.list)
                    }, onFailure = {

                    })
            }

            val subscribeDeferred = async {
                viewModel.homeRecommendList()
                    .flatMapConcat { response ->
                        response.transform { data ->
                            // 从 homeRecommendList 返回的数据中提取 id 列表
                            val ids =
                                data?.list?.filter { it.value != "0" }?.mapNotNull { it.value }
                                    ?.toMutableList() ?: mutableListOf()
                            // 使用 id 列表调用第二个 API
                            viewModel.getListByIds(ids)
                        }
                    }
                    .collectSimple(
                        onLoading = {
                        }, onSuccess = {
                            homeRecommendAdapter?.submitList(it?.list)
                        }, onFailure = {}
                    )
            }
            // 等待所有请求完成
            awaitAll(bannerDeferred, subscribeDeferred)
            hideLoadingDialog()
            fragMonikaViewBinding.ptrListRefresh.isRefreshing = false
        }
    }

}
