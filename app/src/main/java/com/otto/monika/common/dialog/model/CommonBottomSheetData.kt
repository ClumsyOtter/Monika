package com.otto.monika.common.dialog.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 通用BottomSheet数据类
 * @param title 标题
 * @param description 描述说明
 * @param itemList 列表项数据
 */
@Parcelize
data class CommonBottomSheetData(
    val title: String,
    val description: String,
    val itemList: List<CommonBottomSheetItem>
) : Parcelable