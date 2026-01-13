package com.otto.monika.subscribe.rank.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.common.dialog.model.CommonBottomSheetData
import com.otto.monika.common.dialog.model.CommonBottomSheetItem
import com.otto.monika.home.model.MonikaRankData
import com.otto.monika.subscribe.rank.fragment.RankListFragment
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 榜单 ViewModel
 * 负责处理日期范围生成等业务逻辑
 */
class RankViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    private val dateFormatIdentify = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val dateRangeLiveData = MutableLiveData<CommonBottomSheetData?>()

    fun updateDataRange(data: CommonBottomSheetData?) {
        dateRangeLiveData.value = data
    }


    fun getDataRangeData(rankType: RankListFragment.Companion.RankType): CommonBottomSheetData? {
        if (dateRangeLiveData.value == null) {
            val dateList: List<CommonBottomSheetItem> = when (rankType) {
                RankListFragment.Companion.RankType.WEEK -> generateWeekDateList()
                RankListFragment.Companion.RankType.MONTH -> generateMonthDateList()
                RankListFragment.Companion.RankType.HISTORY -> emptyList()
            }
            val (title, description) = when (rankType) {
                RankListFragment.Companion.RankType.WEEK -> "选择周榜日期" to "请选择要查看的周榜日期范围"
                RankListFragment.Companion.RankType.MONTH -> "选择月榜日期" to "请选择要查看的月榜日期范围"
                RankListFragment.Companion.RankType.HISTORY -> "" to ""
            }
            dateRangeLiveData.value = CommonBottomSheetData(
                title = title,
                description = description,
                itemList = dateList
            )
        }
        return dateRangeLiveData.value
    }

    /**
     * 生成周榜日期列表（最近24周）
     */
    fun generateWeekDateList(): List<CommonBottomSheetItem> {
        val dateList = mutableListOf<CommonBottomSheetItem>()
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        for (i in 0 until 24) {
            val weekStart = calendar.clone() as Calendar
            val weekEnd = calendar.clone() as Calendar
            weekEnd.add(Calendar.DAY_OF_MONTH, 6)
            val dateRange =
                "${dateFormat.format(weekStart.time)} - ${dateFormat.format(weekEnd.time)}"
            dateList.add(
                CommonBottomSheetItem(
                    content = dateRange,
                    identify = dateFormatIdentify.format(weekStart.time),
                    isSelected = i == 0
                )
            )
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }
        return dateList
    }

    /**
     * 生成月榜日期列表（最近12个月）
     */
    fun generateMonthDateList(): List<CommonBottomSheetItem> {
        val dateList = mutableListOf<CommonBottomSheetItem>()
        val calendar = Calendar.getInstance()

        // 生成最近12个月
        for (i in 0 until 12) {
            val monthStart = calendar.clone() as Calendar
            monthStart.set(Calendar.DAY_OF_MONTH, 1)
            val monthEnd = calendar.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
            val dateRange =
                "${dateFormat.format(monthStart.time)} - ${dateFormat.format(monthEnd.time)}"
            dateList.add(
                CommonBottomSheetItem(
                    content = dateRange,
                    identify = dateFormatIdentify.format(monthStart.time),
                    isSelected = i == 0
                )
            )
            calendar.add(Calendar.MONTH, -1)
        }
        return dateList
    }


    fun rankingStats(statType: Int, statDate: String?): Flow<ApiResponse<MonikaRankData>> {
        return suspend { MonikaClient.monikaApi.rankingStats(50, statType, statDate) }.asFlow()
    }

}