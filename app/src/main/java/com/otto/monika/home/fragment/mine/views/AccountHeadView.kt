package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import com.otto.common.utils.DipUtils
import com.otto.monika.common.views.TagItemView
import com.otto.monika.home.fragment.mine.listener.AccountHeadListener
import com.otto.monika.R
import com.otto.monika.common.views.CircleImageView
import com.otto.network.model.user.response.MonikaUserInfoModel
import com.otto.network.model.user.response.getTags
import com.otto.network.model.user.response.isSelf

/**
 * 用户信息头部 View
 * 封装用户头像、名称、标签、个人信息等
 */
class AccountHeadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val stableView: ViewGroup
    private val avatarImage: CircleImageView
    private val nameText: TextView
    private val descText: TextView
    private val editBtn: ImageView
    private val tagsFlowLayout: FlexboxLayout
    private val accountHeadInfoView: AccountHeadInfoView
    private val accountDropDownView: AccountHeadDropDownView

    private var cyUserInfoModel: MonikaUserInfoModel? = null
    private var mAccountHeadListener: AccountHeadListener? = null

    init {
        // 设置垂直方向
        orientation = VERTICAL
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.view_account_head, this, true)
        // 获取子视图
        avatarImage = findViewById(R.id.iv_account_avatar)
        nameText = findViewById(R.id.tv_account_name)
        editBtn = findViewById(R.id.btn_account_edit)
        tagsFlowLayout = findViewById(R.id.fl_account_tags)
        descText = findViewById(R.id.tv_account_desc)
        accountHeadInfoView = findViewById(R.id.view_account_info)
        stableView = findViewById(R.id.stable_view)
        accountDropDownView = findViewById<AccountHeadDropDownView>(R.id.view_account_drop_down)
        initListener()
    }

    fun getAccountHeadDropDownViewHeight(): Int {
        return stableView.height + DipUtils.dpToPx(120 + 46)
    }

    fun initListener() {
        // 设置点击监听
        editBtn.setOnClickListener {
            cyUserInfoModel?.let {
                mAccountHeadListener?.onEditNameClick(it)
            }
        }
    }

    fun setAccountHeadListener(accountHeadListener: AccountHeadListener) {
        mAccountHeadListener = accountHeadListener
        accountHeadInfoView.accountHeadInfoListener = accountHeadListener
    }

    fun updateDropDownState(dropDown: Boolean) {
        accountDropDownView.isVisible = dropDown
    }

    /**
     * 设置用户数据
     */
    fun setProfileResponse(data: MonikaUserInfoModel) {
        cyUserInfoModel = data
        // 设置头像
        if (!data.avatar.isNullOrEmpty()) {
            Glide.with(context).load(data.avatar).apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.generic_avatar_default)
                .error(R.drawable.generic_avatar_default).into(avatarImage)
        } else {
            avatarImage.setImageResource(R.drawable.generic_avatar_default)
        }
        // 设置名称
        nameText.text = data.nickname ?: ""
        // 设置描述
        descText.text = data.intro ?: ""
        descText.isVisible = data.intro?.isNotEmpty() == true
        // 设置编辑按钮（本人时显示）
        editBtn.isVisible = data.isSelf()
        // 设置标签
        setTags(data.isSelf(), data.getTags())
        accountHeadInfoView.setAccountProfileResponse(data)
        //设置下拉View信息
        accountDropDownView.setDays(data.joinTime)
        accountDropDownView.setFavoriteCount(data.collectNum ?: 0)
    }

    /**
     * 设置标签（流式布局）
     */
    private fun setTags(isSelf: Boolean, tags: List<String>) {
        tagsFlowLayout.removeAllViews()
        if (tags.isNotEmpty()) {
            tagsFlowLayout.isVisible = true
            tags.forEach { tag ->
                val tagView = createTagView(tag)
                tagsFlowLayout.addView(tagView)
            }
        } else {
            tagsFlowLayout.isVisible = false
        }
    }

    /**
     * 创建标签 View
     */
    private fun createTagView(tag: String): TagItemView {
        return TagItemView(context).apply {
            setTagText(tag)
            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            ).apply {
                // 设置标签之间的间距为 5dp
                val spacing = resources.getDimensionPixelSize(R.dimen.dimen_5dp)
                setMargins(
                    spacing / 2,  // 左边距 2.5dp
                    spacing / 2,
                    spacing / 2,  // 右边距 2.5dp
                    spacing / 2,
                )
            }
            setLayoutParams(layoutParams)
        }
    }
}