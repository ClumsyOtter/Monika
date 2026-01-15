package com.otto.monika.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.otto.monika.R
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.collectSimple
import com.otto.monika.home.model.MonikaSubscribeData
import com.otto.monika.home.viewmodel.MonikaViewModel
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.databinding.FragMonikaViewBinding
import com.otto.monika.home.adapter.adapter.HomeBannerAdapter
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

    private lateinit var fragMonikaViewBinding: FragMonikaViewBinding

    private val viewModel: MonikaViewModel by viewModels()
    private val subscribeModel: MonikaSubscribeData = MonikaSubscribeData()

    private var homeBannerAdapter: HomeBannerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragMonikaViewBinding = FragMonikaViewBinding.inflate(inflater)
        return fragMonikaViewBinding.root
    }

    override fun onFinishCreateView() {
        fragMonikaViewBinding.baseList.clipChildren = false
        fragMonikaViewBinding.baseList.itemAnimator = null
        fragMonikaViewBinding.baseList.layoutManager = LinearLayoutManager(activity)
        val concatAdapter = ConcatAdapter()
        homeBannerAdapter = HomeBannerAdapter()
        concatAdapter.addAdapter(homeBannerAdapter!!)
        fragMonikaViewBinding.baseList.adapter = concatAdapter
        fragMonikaViewBinding.ptrListRefresh.setOnRefreshListener {
            getInitData()
        }
        getInitData()
    }

    private fun getInitData() {
        lifecycleScope.launch {
            // 并行执行三个 API 请求，跳过 Loading 状态，只获取实际结果
            val bannerDeferred = async {
                viewModel.homeBannerList()
                    .collectSimple(onLoading = {

                    }, onSuccess = {
                        homeBannerAdapter?.bannerData = it
                    }, onFailure = {

                    })
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
            // 等待所有请求完成
            awaitAll(bannerDeferred, subscribeDeferred)
            fragMonikaViewBinding.ptrListRefresh.isRefreshing = false
        }
    }

}
