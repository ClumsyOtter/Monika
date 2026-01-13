package com.otto.monika.account.rank.fragment.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.api.model.subscribe.response.SubscribeUserItem

/**
 * 账户订阅排行榜适配器
 * 使用 BaseQuickAdapter 4.x 实现分页加载
 */
class UserSubscribeRankAdapter :
    BaseQuickAdapter<SubscribeUserItem, UserSubscribeRankAdapter.UserSubscribeRankViewHolder>() {

    var onItemClickListener: ((item: SubscribeUserItem, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): UserSubscribeRankViewHolder {
        return UserSubscribeRankViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: UserSubscribeRankViewHolder,
        position: Int,
        item: SubscribeUserItem?
    ) {
        item ?: return
        holder.bind(
            item = item,
            position = position,
            getRankDataByPosition = { pos -> getRankDataByPosition(pos) },
            onItemClickListener = onItemClickListener
        )
    }

    private fun getRankDataByPosition(position: Int): Pair<Int?, Int> {
        return when (position) {
            0 -> Pair(R.drawable.monika_subscribe_rank_icon_1, R.color.rank_list_number_one_color)
            1 -> Pair(R.drawable.monika_subscribe_rank_icon_2, R.color.rank_list_number_two_color)
            2 -> Pair(R.drawable.monika_subscribe_rank_icon_3, R.color.rank_list_number_three_color)
            else -> Pair(null, R.color.rank_list_number_default_color)
        }
    }

    class UserSubscribeRankViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_account_subscribe_rank, null)
    ) {
        private val rankIcon: ImageView = itemView.findViewById(R.id.iv_account_rank_icon)
        private val rankText: TextView = itemView.findViewById(R.id.tv_account_rank_text)
        private val avatarImage: ImageView = itemView.findViewById(R.id.iv_account_rank_avatar)
        private val nameText: TextView = itemView.findViewById(R.id.tv_account_rank_name)
        private val creatorIcon: ImageView = itemView.findViewById(R.id.iv_account_rank_creator)

        fun bind(
            item: SubscribeUserItem,
            position: Int,
            getRankDataByPosition: (Int) -> Pair<Int?, Int>,
            onItemClickListener: ((SubscribeUserItem, Int) -> Unit)?
        ) {
            // 设置 item 点击监听
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(item, pos)
                }
            }

            val rank = position + 1
            val (imageResId, colorResId) = getRankDataByPosition(position)

            if (imageResId != null) {
                rankIcon.visibility = View.VISIBLE
                rankText.visibility = View.GONE
                rankIcon.setImageResource(imageResId)
            } else {
                rankIcon.visibility = View.GONE
                rankText.visibility = View.VISIBLE
                rankText.text = "NO.$rank"
                rankText.setTextColor(ContextCompat.getColor(rankText.context, colorResId))
            }

            // 头像
            item.subscriber?.avatar?.let {
                Glide.with(avatarImage.context)
                    .load(it)
                    .circleCrop()
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            }

            // 名称
            nameText.text = item.subscriber?.nickname

            // 创作者图标
            creatorIcon.visibility =
                if (item.subscriber?.isCreator == true) View.VISIBLE else View.GONE
        }
    }
}
