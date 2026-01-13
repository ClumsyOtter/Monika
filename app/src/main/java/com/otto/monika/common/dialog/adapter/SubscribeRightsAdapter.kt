package com.otto.monika.common.dialog.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.otto.monika.R
import com.otto.monika.common.dialog.model.SubscribeRights

/**
 * 订阅权益列表 Adapter
 */
class SubscribeRightsAdapter : RecyclerView.Adapter<SubscribeRightsAdapter.RightsViewHolder>() {

    private var rights: List<SubscribeRights> = emptyList()

    var onAddClickListener: ((SubscribeRights) -> Unit)? = null

    /**
     * 设置数据
     */
    fun setData(newRights: List<SubscribeRights>) {
        val diffCallback = RightsDiffCallback(rights, newRights)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        rights = newRights
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RightsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscribe_rights, parent, false)
        return RightsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RightsViewHolder, position: Int) {
        holder.bind(rights[position])
    }

    override fun getItemCount(): Int = rights.size

    inner class RightsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImage: ImageView = itemView.findViewById(R.id.iv_rights_item_avatar)
        private val titleText: TextView = itemView.findViewById(R.id.tv_rights_item_title)
        private val contentText: TextView = itemView.findViewById(R.id.tv_rights_item_content)
        private val addButton: TextView = itemView.findViewById(R.id.btn_rights_item_add)

        fun bind(right: SubscribeRights) {
            // 加载头像
            if (!right.rightsAvatar.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(right.rightsAvatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            } else {
                avatarImage.isVisible = false
                avatarImage.setImageResource(R.drawable.generic_avatar_default)
            }

            titleText.text = right.rightsTitle ?: ""
            contentText.text = right.rightsContent ?: ""

            addButton.setOnClickListener {
                onAddClickListener?.invoke(right)
            }
        }
    }

    /**
     * DiffUtil Callback
     */
    private class RightsDiffCallback(
        private val oldList: List<SubscribeRights>,
        private val newList: List<SubscribeRights>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

