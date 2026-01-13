package com.otto.monika.common.dialog.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 通用BottomSheet列表项数据类
 * @param content 显示内容
 * @param identify 唯一标识
 * @param isSelected 是否选中
 */
@Parcelize
data class CommonBottomSheetItem(
    val content: String,
    val identify: String,
    var isSelected: Boolean = false
) : Parcelable