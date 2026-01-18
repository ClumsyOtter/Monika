package com.otto.monika.home.fragment.artwork.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.subscribe.support.views.MonikaPriceView
import com.otto.network.model.artwork.UserArtworkModel

/**
 * 用户艺术品列表适配器
 * 使用 BaseQuickAdapter 4.x 实现分页加载
 */
class UserArtworkAdapter :
    BaseQuickAdapter<UserArtworkModel, UserArtworkAdapter.UserArtworkViewHolder>() {

    var onItemClickListener: ((item: UserArtworkModel, position: Int) -> Unit)? = null
    var onButtonClickListener: ((item: UserArtworkModel, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): UserArtworkViewHolder {
        return UserArtworkViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: UserArtworkViewHolder,
        position: Int,
        item: UserArtworkModel?
    ) {
        item ?: return
        holder.bind(
            item = item,
            onItemClickListener = onItemClickListener,
            onButtonClickListener = onButtonClickListener
        )
    }

    class UserArtworkViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_user_artwork, null)
    ) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_user_artwork_image)
        private val titleText: TextView = itemView.findViewById(R.id.tv_user_artwork_title)
        private val contentText: TextView = itemView.findViewById(R.id.tv_user_artwork_content)
        private val priceText: MonikaPriceView = itemView.findViewById(R.id.tv_user_artwork_price)
        private val discountText: TextView = itemView.findViewById(R.id.tv_user_artwork_discount)
        private val actionButton: TextView = itemView.findViewById(R.id.btn_user_artwork_action)

        fun bind(
            item: UserArtworkModel,
            onItemClickListener: ((UserArtworkModel, Int) -> Unit)?,
            onButtonClickListener: ((UserArtworkModel, Int) -> Unit)?
        ) {
            // 设置 item 点击监听
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(item, position)
                }
            }

            // 图片
            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(imageView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.generic_avatar_default)
            }

            // 标题
            titleText.text = item.title

            // 内容
            contentText.text = item.content

            // 价格
            priceText.setPriceText(item.price)

            // 折扣信息
            if (!item.discount.isNullOrEmpty()) {
                discountText.visibility = View.VISIBLE
                discountText.text = item.discount
            } else {
                discountText.visibility = View.GONE
            }

            // 按钮状态
            if (item.isUnlocked) {
                actionButton.text = "查看"
                actionButton.setBackgroundResource(R.drawable.monika_tag_gray_bg)
                actionButton.setTextColor(
                    actionButton.resources.getColor(
                        R.color.text_333333,
                        null
                    )
                )
            } else {
                actionButton.text = "解锁"
                actionButton.setBackgroundResource(R.drawable.monika_tag_black_bg)
                actionButton.setTextColor(
                    actionButton.resources.getColor(
                        R.color.text_c4ff05,
                        null
                    )
                )
            }

            // 设置按钮点击监听
            actionButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onButtonClickListener?.invoke(item, position)
                }
            }
        }
    }
}
