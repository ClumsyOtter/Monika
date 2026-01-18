package com.otto.monika.subscribe.rank.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.otto.monika.R
import com.otto.monika.common.decoration.HorizontalSpacingItemDecoration
import com.otto.network.model.home.RankModel

/**
 * 榜单列表适配器
 */
class RankListAdapter(data: List<RankModel>? = null) :
    RecyclerView.Adapter<RankListAdapter.RankViewHolder>() {

    private var dataList: MutableList<RankModel> = (data?.toMutableList() ?: mutableListOf())

    // 回调接口
    var onItemClickListener: ((item: RankModel, position: Int) -> Unit)? = null

    /**
     * 更新数据
     */
    fun setData(newData: List<RankModel>?) {
        dataList.clear()
        if (newData != null) {
            dataList.addAll(newData)
        }
        notifyDataSetChanged()
    }

    /**
     * 添加数据
     */
    fun addData(newData: List<RankModel>?) {
        if (newData != null && newData.isNotEmpty()) {
            val startPosition = dataList.size
            dataList.addAll(newData)
            notifyItemRangeInserted(startPosition, newData.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rank_list, parent, false)
        return RankViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        if (position < dataList.size) {
            holder.bind(dataList[position], position)
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class RankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImage: ImageView = itemView.findViewById(R.id.avatar_image)
        private val nameText: TextView = itemView.findViewById(R.id.name_text)
        private val tagsRecycler: RecyclerView = itemView.findViewById(R.id.tags_recycler)
        private val numberText: TextView = itemView.findViewById(R.id.number_text)
        private val rankNumberImage: ImageView =
            itemView.findViewById(R.id.rank_subscribe_rank_number_image)
        private val rankNumberText: TextView =
            itemView.findViewById(R.id.rank_subscribe_rank_number)
        private val descriptionText: TextView = itemView.findViewById(R.id.description_text)

        // 缓存的 TagAdapter 和 LayoutManager，避免重复创建
        private var tagAdapter: TagAdapter? = null
        private var layoutManager: LinearLayoutManager? = null

        init {
            // 初始化 tagsRecycler 的配置（只执行一次）
            layoutManager = LinearLayoutManager(
                tagsRecycler.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            tagsRecycler.layoutManager = layoutManager
            tagAdapter = TagAdapter()
            tagsRecycler.adapter = tagAdapter
            tagsRecycler.addItemDecoration(HorizontalSpacingItemDecoration())
        }

        fun bind(item: RankModel, position: Int) {
            // 设置 item 点击监听
            itemView.setOnClickListener {
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < dataList.size) {
                    onItemClickListener?.invoke(item, adapterPosition)
                }
            }

            // 头像
            val authorAvatar = item.creator?.avatar
            if (!authorAvatar.isNullOrEmpty()) {
                Glide.with(avatarImage.context)
                    .load(authorAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.generic_avatar_default)
            }

            // 姓名
            nameText.text = item.creator?.nickname ?: ""

            // 标签列表
            val tags = mutableListOf<String>()
            item.creator?.address?.takeIf { it.isNotEmpty() }?.let {
                tags.add(it)
            }
            item.creator?.selfTagNames?.takeIf { it.isNotEmpty() }?.let {
                tags.addAll(it)
            }

            if (tags.isNotEmpty()) {
                tagsRecycler.visibility = View.VISIBLE
                // 只更新数据，不重新创建 adapter 和 layoutManager
                tagAdapter?.updateTags(tags)
            } else {
                tagsRecycler.visibility = View.GONE
            }

            // 数字
            numberText.text = item.rank ?: ""

            // 排名图片和文字
            val (colorResId, imageResId) = getRankNumberDataByPosition(position)

            // 设置排名文字颜色和文本（根据 position 生成 No.x 格式）
            rankNumberText.setTextColor(
                ContextCompat.getColor(rankNumberText.context, colorResId)
            )
            rankNumberText.text = "No.${position + 1}"

            // 设置排名图片（前3名显示图标，其他隐藏）
            if (imageResId != null) {
                rankNumberImage.visibility = View.VISIBLE
                rankNumberImage.setImageResource(imageResId)
            } else {
                rankNumberImage.visibility = View.GONE
            }

            // 描述文字
            val detail = item.creator?.intro
            if (!detail.isNullOrEmpty()) {
                descriptionText.visibility = View.VISIBLE
                descriptionText.text = detail
            } else {
                descriptionText.visibility = View.GONE
            }
        }
    }


    private fun getRankNumberDataByPosition(position: Int): Pair<Int, Int?> {
        return when (position) {
            0 -> {
                Pair(R.color.rank_list_number_one_color, R.drawable.monika_subscribe_rank_icon_1)
            }

            1 -> {
                Pair(R.color.rank_list_number_two_color, R.drawable.monika_subscribe_rank_icon_2)
            }

            2 -> {
                Pair(R.color.rank_list_number_three_color, R.drawable.monika_subscribe_rank_icon_3)
            }

            else -> {
                Pair(R.color.rank_list_number_default_color, null)
            }
        }
    }
}