package com.otto.monika.home.fragment.subscribe.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.api.model.subscribe.response.MyCreatorItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户订阅列表适配器
 * 使用 BaseQuickAdapter 4.x 实现分页加载
 */
class UserSubscribeAdapter :
    BaseQuickAdapter<MyCreatorItem, UserSubscribeAdapter.UserSubscribeViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

    // 回调接口（只在点击右边信息框时触发）
    var onInfoBoxClickListener: ((item: MyCreatorItem, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(context: Context,parent: ViewGroup, viewType: Int): UserSubscribeViewHolder {
        return UserSubscribeViewHolder(parent)
    }

    override fun onBindViewHolder(
        holder: UserSubscribeViewHolder,
        position: Int,
        item: MyCreatorItem?
    ) {
        item ?: return
        holder.bind(
            item = item,
            dateFormat = dateFormat,
            calculateRemainingDays = { expiredAt -> calculateRemainingDays(expiredAt) },
            formatSubscribeDate = { createdAt -> formatSubscribeDate(createdAt) },
            onInfoBoxClickListener = onInfoBoxClickListener
        )
    }

    /**
     * 计算剩余天数
     */
    private fun calculateRemainingDays(expiredAt: String?): Int {
        if (expiredAt.isNullOrEmpty()) {
            return 0
        }
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expiredDate = dateFormat.parse(expiredAt)
            val currentDate = Date()
            if (expiredDate != null) {
                val diff = expiredDate.time - currentDate.time
                val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                return maxOf(0, days)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 格式化订阅日期
     */
    private fun formatSubscribeDate(createdAt: String?): String {
        if (createdAt.isNullOrEmpty()) {
            return ""
        }
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            if (date != null) {
                return dateFormat.format(date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return createdAt ?: ""
    }

    /**
     * ViewHolder 类
     */
    class UserSubscribeViewHolder(parent: View) : RecyclerView.ViewHolder(
        View.inflate(parent.context, R.layout.item_user_subscribe, null)
    ) {
        private val remainingDaysText: TextView =
            itemView.findViewById(R.id.tv_user_subscribe_remaining_days)
        private val remainingDaysLabel: TextView =
            itemView.findViewById(R.id.tv_user_subscribe_remaining_days_label)
        private val infoBox: ConstraintLayout =
            itemView.findViewById(R.id.cl_user_subscribe_info_box)
        private val avatarImage: ImageView = itemView.findViewById(R.id.iv_user_subscribe_avatar)
        private val nameText: TextView = itemView.findViewById(R.id.tv_user_subscribe_name)
        private val subscribeDateText: TextView = itemView.findViewById(R.id.tv_user_subscribe_date)
        private val contentText: TextView = itemView.findViewById(R.id.tv_user_subscribe_content)

        fun bind(
            item: MyCreatorItem,
            dateFormat: SimpleDateFormat,
            calculateRemainingDays: (String?) -> Int,
            formatSubscribeDate: (String?) -> String,
            onInfoBoxClickListener: ((MyCreatorItem, Int) -> Unit)?
        ) {
            // 计算剩余天数
            val remainingDays = calculateRemainingDays(item.expiredAt)
            remainingDaysText.text = "$remainingDays"
            remainingDaysLabel.text = "剩余天"

            // 设置右边信息框的点击监听
            infoBox.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onInfoBoxClickListener?.invoke(item, position)
                }
            }

            // 头像
            val avatar = item.creator?.avatar
            if (!avatar.isNullOrEmpty()) {
                Glide.with(avatarImage.context)
                    .load(avatar)
                    .circleCrop()
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.generic_avatar_default)
            }

            // 名称
            nameText.text = item.creator?.nickname ?: ""

            // 订阅日期
            val subscribeDate = formatSubscribeDate(item.createdAt)
            subscribeDateText.text = subscribeDate

            // 内容
            contentText.text = item.plan?.rightsDesc ?: ""
        }
    }
}
