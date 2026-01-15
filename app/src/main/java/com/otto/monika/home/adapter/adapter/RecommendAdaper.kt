package com.otto.monika.home.adapter.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.common.views.RoundedImageView

open class RecommendAdapter() :
    BaseQuickAdapter<PostItem, RecommendAdapter.RecommendViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): RecommendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_recommand_item, parent, false)
        val viewHolder = RecommendViewHolder(view)
        val lp = viewHolder.cardLayout?.layoutParams
        lp?.width = (DipUtils.getScreenWidth(context) * (252f / 375f)).toInt()
        viewHolder.cardLayout?.layoutParams = lp
        return viewHolder
    }

    override fun onBindViewHolder(
        holder: RecommendViewHolder,
        position: Int,
        item: PostItem?
    ) {
        item?.let { holder.bind(it) }
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position, item)
        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, item: PostItem?)
    }

    inner class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView = itemView.findViewById<TextView>(R.id.tv_title)
        private val detailView = itemView.findViewById<TextView>(R.id.tv_detail)
        private val userAvatarView = itemView.findViewById<RoundedImageView>(R.id.user_avatar_view)
        private val imageView = itemView.findViewById<ImageView>(R.id.row_img)
        private val userNameView = itemView.findViewById<TextView>(R.id.tv_user_name)
        val cardLayout: CardView? = itemView.findViewById<CardView>(R.id.card_layout)


        fun bind(item: PostItem) {
            // 设置主图片
            val imageUrl = item.images.getOrNull(0)
            if (imageUrl?.isNotEmpty() == true) {
                Glide.with(imageView).load(imageUrl).into(imageView)
            } else {
                imageView.setImageBitmap(null)
            }

            // 设置用户头像
            val avatarUrl = item.user?.avatar
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(userAvatarView).load(avatarUrl).into(userAvatarView)
            } else {
                userAvatarView.setImageResource(R.drawable.generic_avatar_default)
            }

            // 设置用户名
            userNameView.text = item.user?.nickname

            // 设置标题
            val title = item.title
            val hasTitle = !title.isNullOrEmpty()
            if (hasTitle) {
                titleView.text = title
                titleView.visibility = View.VISIBLE
            } else {
                titleView.text = ""  // 先清空文本
                titleView.visibility = View.GONE  // 使用 GONE 隐藏，不占据空间
            }
            val detail = item.content
            if (!detail.isNullOrEmpty()) {
                detailView.text = detail
                detailView.visibility = View.VISIBLE
                // 动态设置最大行数
                if (hasTitle) {
                    // 有标题时，只显示一行
                    detailView.maxLines = 1
                } else {
                    // 无标题时，显示两行
                    detailView.maxLines = 2
                }
                // 确保省略号在末尾显示
                detailView.ellipsize = TextUtils.TruncateAt.END
            } else {
                detailView.text = ""  // 先清空文本
                detailView.visibility = View.GONE  // 使用 GONE 隐藏，不占据空间
            }
        }
    }
}
