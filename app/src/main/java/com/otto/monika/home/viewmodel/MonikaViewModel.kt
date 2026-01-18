package com.otto.monika.home.viewmodel

import androidx.lifecycle.ViewModel
import com.otto.network.client.MonikaClient
import com.otto.network.common.ApiResponse
import com.otto.network.common.asFlow
import com.otto.network.model.home.MonikaBannerData
import com.otto.network.model.home.MonikaIntroduceData
import com.otto.network.model.post.response.PostListResponse
import kotlinx.coroutines.flow.Flow

class MonikaViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    fun homeRecommendList(): Flow<ApiResponse<MonikaIntroduceData>> {
        return suspend { api.homeRecommendList() }.asFlow()
    }

    fun getListByIds(ids: MutableList<String>): Flow<ApiResponse<PostListResponse>> {
        return suspend { api.getListByIds(ids.toString()) }.asFlow()
    }

    fun homeBannerList(): Flow<ApiResponse<MonikaBannerData>> {
        return suspend { api.homeBannerList() }.asFlow()
    }
}