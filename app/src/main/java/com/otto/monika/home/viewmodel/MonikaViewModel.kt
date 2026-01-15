package com.otto.monika.home.viewmodel

import androidx.lifecycle.ViewModel
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.post.response.PostListResponse
import com.otto.monika.home.model.MonikaBannerData
import com.otto.monika.home.model.MonikaIntroduceData
import com.otto.monika.home.model.MonikaPostData
import com.otto.monika.home.model.MonikaRankData
import com.otto.monika.home.model.MonikaSubscribeData
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