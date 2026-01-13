package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.otto.monika.R

/**
 * 用户页面顶部导航栏自定义 View
 * 封装了返回键、头像、名称、订阅按钮等元素
 */
class AccountActionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val backBtn: ImageView
    private val avatarImage: ImageView
    private val nameText: TextView

    private val actionBarContainer: ConstraintLayout

    // 回调接口
    var onBackClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_account_action_bar, this, true)
        backBtn = findViewById(R.id.iv_account_action_bar_back)
        avatarImage = findViewById(R.id.iv_account_action_bar_avatar)
        nameText = findViewById(R.id.tv_account_action_bar_name)
        actionBarContainer = findViewById(R.id.iv_account_action_bar_container)

        // 设置点击事件
        backBtn.setOnClickListener {
            onBackClickListener?.invoke()
        }
    }

    /**
     * 设置头像
     */
    fun setAvatar(avatarUrl: String?) {
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(avatarUrl)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(R.drawable.generic_avatar_default)
                .error(R.drawable.generic_avatar_default)
                .into(avatarImage)
        } else {
            avatarImage.setImageResource(R.drawable.generic_avatar_default)
        }
    }

    fun setAvatarVisibility(visibility: Boolean) {
        avatarImage.isVisible = visibility
    }

    /**
     * 设置名称
     */
    fun setName(name: String?) {
        nameText.text = name ?: ""
    }

    fun setNameVisibility(visibility: Boolean) {
        nameText.isVisible = visibility
    }

    fun setContainerBackground(resId: Int) {
        actionBarContainer.setBackgroundResource(resId)
    }


    fun fullShow(fullShow: Boolean) {
        if (fullShow) {
            setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.white,
                    null
                )
            )
            nameText.isVisible = true
            avatarImage.isVisible = true

        } else {
            setBackgroundResource(0)
            nameText.isVisible = false
            avatarImage.isVisible = false
        }
    }

}