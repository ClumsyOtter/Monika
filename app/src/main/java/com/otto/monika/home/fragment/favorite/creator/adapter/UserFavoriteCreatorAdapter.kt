package com.otto.monika.home.fragment.favorite.creator.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.network.model.post.response.UserInfo

/**
 * 用户喜欢的创作者列表适配器
 * 使用 BaseQuickAdapter 4.x 实现分页加载
 */
class UserFavoriteCreatorAdapter :
    BaseQuickAdapter<UserInfo, UserFavoriteCreatorAdapter.UserFavoriteCreatorViewHolder>() {

    var onItemClickListener: ((item: UserInfo, position: Int) -> Unit)? = null
    var onSubscribeClickListener: ((item: UserInfo, position: Int) -> Unit)? = null

    var needUpdatePostIdList: MutableList<Pair<String?, Int>> = mutableListOf()

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): UserFavoriteCreatorViewHolder {
        return UserFavoriteCreatorViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: UserFavoriteCreatorViewHolder,
        position: Int,
        item: UserInfo?
    ) {
        item ?: return
        holder.bind(
            item = item,
            onItemClickListener = onItemClickListener,
            onSubscribeClickListener = onSubscribeClickListener
        )
    }

    fun notifyChangedIfNeedUpdate() {
        needUpdatePostIdList.forEach { post ->
            val postIndex = items.indexOfFirst { it.id == post.first }
            postIndex.let {
                when (post.second) {
                    0 -> notifyItemChanged(it)
                    1 -> removeAt(it)
                    else -> notifyItemChanged(it)
                }
            }
        }
        needUpdatePostIdList.clear()
    }

    class UserFavoriteCreatorViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_user_favorite_creator, null)
    ) {
        private val avatarImage: ImageView =
            itemView.findViewById(R.id.iv_user_favorite_creator_avatar)
        private val titleText: TextView = itemView.findViewById(R.id.tv_user_favorite_creator_title)
        private val contentText: TextView =
            itemView.findViewById(R.id.tv_user_favorite_creator_content)
        private val subscribeButton: TextView =
            itemView.findViewById(R.id.btn_user_favorite_creator_subscribe)

        fun bind(
            item: UserInfo,
            onItemClickListener: ((UserInfo, Int) -> Unit)?,
            onSubscribeClickListener: ((UserInfo, Int) -> Unit)?
        ) {
            // 设置 item 点击监听
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(item, position)
                }
            }

            // 圆形头像
            if (!item.avatar.isNullOrEmpty()) {
                Glide.with(avatarImage.context)
                    .load(item.avatar)
                    .circleCrop()
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.generic_avatar_default)
            }

            // 标题
            titleText.text = item.nickname ?: ""

            // 内容
            contentText.text = item.intro ?: ""

            // 订阅按钮
            val isSubscribed = item.isSubscribed == true
            if (isSubscribed) {
                val remainingTime = item.subscribeRemainingTime ?: 0
                val daysText = if (remainingTime > 0) {
                    val days = remainingTime / (24 * 60 * 60)
                    if (days > 0) "已订阅${days}天" else "已订阅"
                } else {
                    "已订阅"
                }
                subscribeButton.text = daysText
                subscribeButton.setBackgroundResource(R.drawable.monika_tag_gray_border)
                subscribeButton.setTextColor(
                    subscribeButton.resources.getColor(
                        R.color.text_808080,
                        null
                    )
                )
                subscribeButton.setOnClickListener(null)
                subscribeButton.isClickable = false
            } else {
                subscribeButton.text = "订阅"
                subscribeButton.setBackgroundResource(R.drawable.monika_tag_black_bg)
                subscribeButton.setTextColor(
                    subscribeButton.resources.getColor(
                        R.color.color_C2FE00,
                        null
                    )
                )
                subscribeButton.isClickable = true
                subscribeButton.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onSubscribeClickListener?.invoke(item, position)
                    }
                }
            }
        }
    }
}
