package com.otto.monika.api.model.user.response

import android.os.Parcelable
import com.otto.monika.home.fragment.mine.model.AccountType
import com.otto.monika.home.fragment.mine.views.AccountHeadNonCreatorView
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 创作者信息
 */
@Parcelize
data class Creator(
    val status: Int? = null,
    val id: Long? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val intro: String? = null,
    val gender: Int? = null
) : Parcelable

/**
 * 用户信息响应数据
 * 根据查询对象不同，某些字段可能为 null
 */
@Parcelize
data class MonikaUserInfoModel(
    // 基础字段
    val uid: String? = null,
    //用来展示的id
    val showId: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val intro: String? = null,
    val gender: Int? = null, // 性别：0-未知，1-男，2-女
    val address: String? = null,
    val creatorVerified: Int? = null, // 是否审核通过的创作者：0-未审核，1-审核通过
    val createdAt: String? = null, // 创建时间
    val joinTime: String? = null, // 加入时间

    // 统计字段
    val subscribeCount: Int? = null, // 订阅数
    @SerializedName("collect_num")
    var collectNum: Int? = null, // 收藏数
    //背景墙图片
    val collectBg: List<String> = emptyList(),
    // 创作者字段
    val creator: Creator? = null, // 创作者信息
    val fansCount: Int? = null, // 粉丝数
    val status: Int? = null, // 状态
    val goodAtNames: List<String> = emptyList(), // 擅长领域
    val selfTagNames: List<String> = emptyList(), // 自我标签

    // 自己独有字段（查询自己时才有）
    val phone: String? = null,
    val openid: String? = null,
    val deviceId: String? = null,
    val username: String? = null,
    val isVisitor: Int? = null, // 是否是游客：0-否，1-是
    val updatedAt: String? = null, // 更新时间
    val income: Double? = null, // 收入（自己且是审核通过的创作者时才有）

    // 查询别人时才有
    var isCollected: Boolean? = null, // 是否收藏该用户
    @SerializedName("is_subscribed")
    var isSubscribed: Boolean? = null, // 是否订阅
    @SerializedName("subscribe_remaining_time")
    var subscribeRemainingTime: Int? = null, // 订阅剩余时间（天）
    var subscribedtime: Int? = null // 订阅时间（天）
) : Parcelable

fun MonikaUserInfoModel?.isCreator(): Boolean {
    return this?.creatorVerified == 1
}

fun MonikaUserInfoModel?.isSelf(): Boolean {
    return this?.openid?.isNotEmpty() == true || this?.deviceId?.isNotEmpty() == true
}

fun MonikaUserInfoModel?.getTags(): List<String> {
    val tags = mutableListOf<String>()
    this?.showId?.takeIf { it.isNotEmpty() }?.let { tags.add("ID：$it") }
    this?.address?.takeIf { it.isNotEmpty() }?.let {
        tags.add(it)
    }
    if (isCreator()) {
        tags.add("创作者")
    }
    this?.goodAtNames?.let { tags.addAll(goodAtNames) }
    this?.selfTagNames?.let { tags.addAll(selfTagNames) }
    return tags
}

//用户创作者状态：0申请中，1审核通过，2审核不通过，3未申请
fun MonikaUserInfoModel?.getAuditStatus(): AccountHeadNonCreatorView.AuditStatus {
    val audioStatus = this?.status ?: 3
    return when (audioStatus) {
        0 -> {
            AccountHeadNonCreatorView.AuditStatus.REVIEWING
        }

        1 -> {
            AccountHeadNonCreatorView.AuditStatus.SUCCESS
        }

        2 -> {
            AccountHeadNonCreatorView.AuditStatus.FAILED
        }

        else -> {
            AccountHeadNonCreatorView.AuditStatus.NONE
        }
    }
}

//获取用户类型
fun MonikaUserInfoModel?.getAccountType(): AccountType {
    return if (isSelf() && !isCreator()) {
        // 本人且不是创作者：显示创作者申请按钮
        AccountType.SELF_NON_CREATOR
    } else if (isSelf() && isCreator()) {
        // 本人且是创作者：显示我的收入、我的粉丝、订阅方案、作品上传
        AccountType.SELF_CREATOR
    } else if (!isSelf() && isCreator()) {
        // 非本人且是创作者：显示他的订阅、他的粉丝
        AccountType.OTHER_CREATOR
    } else {
        // 非本人且不是创作者：只显示他的订阅
        AccountType.OTHER_NON_CREATOR
    }
}


