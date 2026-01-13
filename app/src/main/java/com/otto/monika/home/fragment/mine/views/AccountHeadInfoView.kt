package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.otto.monika.R
import com.otto.monika.api.model.user.response.MonikaUserInfoModel
import com.otto.monika.api.model.user.response.getAccountType
import com.otto.monika.api.model.user.response.getAuditStatus
import com.otto.monika.home.fragment.mine.listener.AccountHeadListener
import com.otto.monika.home.fragment.mine.model.AccountType

/**
 * 账户头部信息 View
 * 根据账户信息自动切换显示 AccountHeadCreatorView 或 AccountHeadNonCreatorView
 */
class AccountHeadInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val creatorView: AccountHeadCreatorView
    private val creatorViewContainer: LinearLayout
    private val creatorOwnViewContainer: LinearLayout
    private val nonCreatorView: AccountHeadNonCreatorView
    private val subscriptionPlansView: AccountHeadOptionView
    private val worksUploadedView: AccountHeadOptionView

    //点击订阅方案
    var accountHeadInfoListener: AccountHeadListener? = null

    init {
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_account_head_info, this, true)

        // 获取子视图
        creatorView = findViewById(R.id.view_creator)
        nonCreatorView = findViewById(R.id.view_non_creator)
        creatorOwnViewContainer =
            findViewById<LinearLayout>(R.id.view_account_info_creator_owner_container)
        creatorViewContainer = findViewById<LinearLayout>(R.id.ll_creator_container)
        subscriptionPlansView = findViewById<AccountHeadOptionView>(R.id.view_subscription_plans)
        worksUploadedView = findViewById<AccountHeadOptionView>(R.id.view_works_uploaded)
    }

    /**
     * 根据账户信息设置数据并切换显示样式
     */
    fun setAccountProfileResponse(
        data: MonikaUserInfoModel
    ) {
        // 根据用户属性显示不同的内容
        when (data.getAccountType()) {
            // 本人且不是创作者：显示创作者申请按钮
            AccountType.SELF_NON_CREATOR -> {
                creatorViewContainer.isVisible = false
                creatorOwnViewContainer.isVisible = false
                nonCreatorView.isVisible = true
                nonCreatorView.setAccountType(AccountType.SELF_NON_CREATOR)
                nonCreatorView.setAuditStatus(data.getAuditStatus())
                nonCreatorView.onItemClickListener = {
                    accountHeadInfoListener?.onApplyToCreatorClick(data)
                }
            }
            // 本人且是创作者：显示我的收入、我的粉丝、订阅方案、作品上传
            AccountType.SELF_CREATOR -> {
                creatorViewContainer.isVisible = true
                creatorOwnViewContainer.isVisible = true
                nonCreatorView.isVisible = false
                creatorView.setAccountType(AccountType.SELF_CREATOR)
                creatorView.setValue(data.income?.toString(), data.fansCount.toString())
                creatorView.onLeftItemClick = {
                    accountHeadInfoListener?.onMyIncomeClick(data)
                }
                creatorView.onRightItemClick = {
                    accountHeadInfoListener?.onMyFansClick(data)
                }

                // 设置订阅方案和作品上传
                subscriptionPlansView.updateContent(
                    R.drawable.icon_account_subscribe_plan,
                    "订阅方案",
                    "按月付费的会员制",
                    "去查看"
                )
                subscriptionPlansView.onOptionClickListener = {
                    accountHeadInfoListener?.onSubscribePlanClick(data)
                }
                worksUploadedView.updateContent(
                    R.drawable.monika_icon_post_upload,
                    "作品上传",
                    "快去发布你的新作品吧",
                    "去上传"
                )
                worksUploadedView.onOptionClickListener = {
                    accountHeadInfoListener?.onUploadPostClick(data)
                }
            }
            // 非本人且是创作者：显示他的订阅、他的粉丝
            AccountType.OTHER_CREATOR -> {
                creatorViewContainer.isVisible = true
                creatorOwnViewContainer.isVisible = false
                nonCreatorView.isVisible = false
                creatorView.setAccountType(AccountType.OTHER_CREATOR)
                creatorView.setValue(data.subscribeCount.toString(), data.fansCount.toString())
            }
            // 非本人且不是创作者：只显示他的订阅
            else -> {
                creatorViewContainer.isVisible = false
                creatorOwnViewContainer.isVisible = false
                nonCreatorView.isVisible = true
                nonCreatorView.setAccountType(AccountType.OTHER_NON_CREATOR)
                nonCreatorView.setCount(data.subscribeCount.toString())
            }
        }
    }

}

