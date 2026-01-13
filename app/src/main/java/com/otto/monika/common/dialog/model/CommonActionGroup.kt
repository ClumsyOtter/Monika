package com.otto.monika.common.dialog.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 通用操作组数据模型
 * @param items 操作项列表
 */
@Parcelize
data class CommonActionGroup(
    val items: List<CommonActionItem>
) : Parcelable