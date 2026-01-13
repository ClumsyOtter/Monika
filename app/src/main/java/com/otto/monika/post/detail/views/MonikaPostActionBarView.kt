package com.otto.monika.post.detail.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.otto.monika.R

/**
 * 帖子页面顶部导航栏自定义 View
 * 封装了返回键、头像、名称、订阅按钮等元素
 */
class MonikaPostActionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val backBtn: ImageView
    private val avatarImage: ImageView
    private val nameText: TextView
    private val subscribeBtn: TextView

    // 回调接口
    var onBackClickListener: (() -> Unit)? = null
    var onSubscribeClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post_action_bar, this, true)

        backBtn = findViewById(R.id.iv_action_bar_back)
        avatarImage = findViewById(R.id.iv_action_bar_avatar)
        nameText = findViewById(R.id.tv_action_bar_name)
        subscribeBtn = findViewById(R.id.btn_action_bar_subscribe)

        // 设置点击事件
        backBtn.setOnClickListener {
            onBackClickListener?.invoke()
        }

        subscribeBtn.setOnClickListener {
            onSubscribeClickListener?.invoke()
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

    /**
     * 设置名称
     */
    fun setName(name: String?) {
        nameText.text = name ?: ""
    }

    /**
     * 设置订阅按钮文字
     */
    fun setSubscribeText(text: String?) {
        subscribeBtn.text = text ?: "订阅"
    }

    /**
     * 设置订阅按钮是否选中
     */
    fun setSubscribeSelected(isSelected: Boolean) {
        if (isSelected) {
            val drawable =
                ResourcesCompat.getDrawable(resources, R.drawable.monika_tag_gray_bg, null)?.mutate()
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                DrawableCompat.setTint(
                    wrappedDrawable,
                    resources.getColor(R.color.color_f52b1c, null)
                )
                subscribeBtn.background = wrappedDrawable
            }
            subscribeBtn.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.text_666666,
                    null
                )
            )
        } else {
            subscribeBtn.setBackgroundResource(R.drawable.monika_tag_black_bg)
            subscribeBtn.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.color_C2FE00,
                    null
                )
            )
        }
    }
}

