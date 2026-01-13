package com.otto.monika.common.dialog.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 通用操作项数据模型
 * @param icon 图标资源ID，可为null
 * @param content 操作内容文本
 * @param type 操作类型，用于区分不同的操作
 */
@Parcelize
data class CommonActionItem(
    val icon: Int? = null,
    val content: String,
    val type: String
) : Parcelable