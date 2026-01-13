package com.otto.monika.home.viewmodel

import androidx.lifecycle.ViewModel
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.home.model.MonikaBannerData
import com.otto.monika.home.model.MonikaIntroduceData
import com.otto.monika.home.model.MonikaRankData
import com.otto.monika.home.model.MonikaSubscribeData
import kotlinx.coroutines.flow.Flow

class MonikaViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    fun homeRecommendList(): Flow<ApiResponse<MonikaIntroduceData>> {
        return suspend { api.homeRecommendList() }.asFlow()
    }

    fun homeBannerList(): Flow<ApiResponse<MonikaBannerData>> {
        return suspend { api.homeBannerList() }.asFlow()
    }


    fun subscribePostList(page: Int, pageSize: Int): Flow<ApiResponse<MonikaSubscribeData>> {
        return suspend { api.subscribePostList(page, pageSize) }.asFlow()
    }

    fun homeRanking(limit: Int, statDate: String?): Flow<ApiResponse<MonikaRankData>> {
        return suspend { api.homeRanking(limit, statDate) }.asFlow()
    }


}