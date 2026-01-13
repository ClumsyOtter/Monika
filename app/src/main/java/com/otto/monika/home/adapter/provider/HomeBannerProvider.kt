package com.otto.monika.home.adapter.provider

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.google.android.material.carousel.CarouselLayoutManager
import com.otto.monika.R
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.home.model.MonikaBannerData
import com.otto.monika.home.model.MonikaBannerItem


class HomeBannerProvider(
    private val fragment: Fragment, val onItemClick: ((MonikaBannerItem) -> Unit)? = null
) : BaseMultiItemAdapter<MonikaBannerData, HomeBannerProvider.Holder>() {

    companion object {
        const val ITEM_WIDTH_RATIO = 0.56f
        const val ITEM_W_H_RATIO = 0.8f
    }

    override fun onCreateViewHolder(context: Context,inflater: LayoutInflater, parent: ViewGroup): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monika_fragment_banner_view, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, model: MonikaBannerData) {
        // 获取 banner 列表数据，如果为空则使用测试数据
        val bannerList = mutableListOf<MonikaBannerItem>()
        model.list?.let {
            bannerList.addAll(it)
        }
        holder.itemView.isRecyclerViewItemVisible = bannerList.isNotEmpty()
        if (bannerList.isEmpty())
            return

        val screenWidth = DipUtils.getScreenWidth(fragment.requireContext())
        // 设置卡片宽度：中间选中卡片的宽度
        val cardWidthFactor = ITEM_WIDTH_RATIO
        val cardWidth = (screenWidth * cardWidthFactor).toInt()
        val cardHeight = cardWidth / ITEM_W_H_RATIO
        holder.recyclerView.layoutParams?.let {
            it.height = cardHeight.toInt()
            holder.recyclerView.layoutParams = it
        }

        // 使用 RecyclerView 实现卡片堆叠效果（推荐方案）
        val recyclerAdapter = BannerRecyclerAdapter(fragment.requireContext(), bannerList)
        val layoutManager = CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false)

        holder.recyclerView.layoutManager = layoutManager
        holder.recyclerView.adapter = recyclerAdapter
        holder.recyclerView.addOnScrollListener(CenterScrollListener())
        layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())

        // 滚动到中间位置，让中间卡片居中显示
        holder.recyclerView.post {
            if (bannerList.isNotEmpty()) {
                val centerPosition = bannerList.size / 2
                // 使用 smoothScrollToPosition 让滚动更平滑
                holder.recyclerView.smoothScrollToPosition(centerPosition)
            }
        }

    }

    private inner class BannerRecyclerAdapter(
        private val context: Context, private val bannerList: List<MonikaBannerItem>
    ) : RecyclerView.Adapter<BannerRecyclerAdapter.BannerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.home_banner_item_view, parent, false)
            val screenWidth = DipUtils.getScreenWidth(context)
            // 设置卡片宽度：中间选中卡片的宽度
            val cardWidthFactor = ITEM_WIDTH_RATIO
            val cardWidth = (screenWidth * cardWidthFactor).toInt()
            itemView.layoutParams.width = cardWidth
            return BannerViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            if (position < bannerList.size) {
                val bannerItem = bannerList[position]
                holder.bind(bannerItem)
            }
        }

        override fun getItemCount(): Int = bannerList.size

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
                    // 普通图片
                    GlideUtils.loadImageView(context, imageUrl, imageView)
                }
                // 设置点击事件
                itemView.setOnClickListener {
                    onItemClick?.invoke(bannerItem)
                }
            }
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view)
    }
}