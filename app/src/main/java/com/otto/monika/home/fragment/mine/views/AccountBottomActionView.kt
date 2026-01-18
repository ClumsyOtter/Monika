package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.otto.monika.R
import com.otto.monika.common.views.MonikaCommonOptionView
import com.otto.network.model.user.response.MonikaUserInfoModel
import com.otto.network.model.user.response.isCreator
import com.otto.network.model.user.response.isSelf

/**
 * 账户底部操作栏 View
 * 包含订阅按钮和收藏按钮
 */
class AccountBottomActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dividerView: View
    private var subscribeButton: ImageView
    private var subscribeText: TextView
    private var collectionButton: MonikaCommonOptionView
    var onSubscribeClick: ((MonikaUserInfoModel?) -> Unit)? = null
    var onCollectionClick: ((MonikaUserInfoModel?) -> Unit)? = null

    private var mCyUserInfoModel: MonikaUserInfoModel? = null

    init {
        inflate(context, R.layout.view_account_bottom_action, this)
        dividerView = findViewById(R.id.account_bottom_subscribe_divider)
        subscribeButton = findViewById(R.id.iv_account_bottom_subscribe_button)
        subscribeText = findViewById(R.id.iv_account_bottom_subscribe_text)
        collectionButton = findViewById(R.id.iv_account_bottom_collection_button)
        subscribeButton.setOnClickListener {
            onSubscribeClick?.invoke(mCyUserInfoModel)
        }
        subscribeText.setOnClickListener {
            onSubscribeClick?.invoke(mCyUserInfoModel)
        }
        collectionButton.setOnClickListener {
            onCollectionClick?.invoke(mCyUserInfoModel)
        }
    }

    /**
     * 设置订阅按钮状态
     * @param isSubscribed 是否已订阅
     */
    fun setSubscribeState(isSubscribed: Boolean) {
        if (isSubscribed) {
            subscribeButton.visibility = GONE
            subscribeText.visibility = VISIBLE
        } else {
            subscribeButton.visibility = VISIBLE
            subscribeText.visibility = GONE
        }
    }

    fun setCyUserInfoModel(cyUserInfoModel: MonikaUserInfoModel) {
        mCyUserInfoModel = cyUserInfoModel
        isVisible = cyUserInfoModel.isSelf().not() && cyUserInfoModel.isCreator()
        setCollectionCount(cyUserInfoModel.collectNum ?: 0)
        setCollectionSelected(cyUserInfoModel.isCollected == true)
        setSubscribeState(cyUserInfoModel.isSubscribed == true)
        setSubscribeText("剩余(ゝ∀･${cyUserInfoModel.subscribeRemainingTime}天)")
    }

    /**
     * 设置订阅按钮文本
     */
    fun setSubscribeText(text: String) {
        subscribeText.text = text
    }

    /**
     * 设置收藏数量
     */
    fun setCollectionCount(count: Int) {
        collectionButton.setCountValue(count)
    }

    /**
     * 设置收藏状态
     */
    fun setCollectionSelected(selected: Boolean) {
        collectionButton.isOptionSelected = selected
    }

    /**
     * 获取收藏按钮
     */
    fun getCollectionButton(): MonikaCommonOptionView {
        return collectionButton
    }

    /**
     * 设置分割线可见性
     */
    fun setDividerVisible(visible: Boolean) {
        dividerView.visibility = if (visible) View.VISIBLE else View.GONE
    }
}

