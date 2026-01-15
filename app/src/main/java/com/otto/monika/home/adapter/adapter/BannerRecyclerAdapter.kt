package com.otto.monika.home.adapter.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.home.model.MonikaBannerItem

class BannerRecyclerAdapter() :
    BaseQuickAdapter<MonikaBannerItem, BannerRecyclerAdapter.BannerViewHolder>() {
    companion object {
        const val ITEM_WIDTH_RATIO = 0.56f
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): BannerViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.home_banner_item_view, parent, false)
        val screenWidth = DipUtils.getScreenWidth(context)
        // 设置卡片宽度：中间选中卡片的宽度
        val cardWidthFactor = ITEM_WIDTH_RATIO
        val cardWidth = (screenWidth * cardWidthFactor).toInt()
        itemView.layoutParams.width = cardWidth
        return BannerViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: BannerViewHolder,
        position: Int,
        item: MonikaBannerItem?
    ) {
        item?.let { holder.bind(it) }
    }


    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.row_img)
        private val titleView = itemView.findViewById<TextView>(R.id.tv_title)
        private val detailView = itemView.findViewById<TextView>(R.id.tv_ad_tag)
        fun bind(bannerItem: MonikaBannerItem) {
            // 设置标题
            if (!TextUtils.isEmpty(bannerItem.title)) {
                titleView.visibility = View.VISIBLE
                titleView.text = bannerItem.title
            } else {
                titleView.visibility = View.GONE
            }

            // 小标题
            if (!TextUtils.isEmpty(bannerItem.detail)) {
                detailView.visibility = View.VISIBLE
                detailView.text = bannerItem.title
            } else {
                detailView.visibility = View.GONE
            }

            // 加载图片
            val imageUrl = bannerItem.image_url
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(context).load(imageUrl).into(imageView)
            }
        }
    }
}