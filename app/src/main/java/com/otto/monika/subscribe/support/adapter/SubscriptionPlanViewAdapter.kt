package com.otto.monika.subscribe.support.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.otto.monika.R
import com.otto.monika.subscribe.support.model.SubscriptionSupportPlan
import com.otto.monika.subscribe.support.views.MonikaPriceView

/**
 * 订阅方案 ViewPager2 适配器（使用 View 而不是 Fragment）
 */
class SubscriptionPlanViewAdapter(
    private val plans: List<SubscriptionSupportPlan>
) : RecyclerView.Adapter<SubscriptionPlanViewAdapter.PlanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_subscription_plan_detail, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plans.getOrNull(position) ?: return
        holder.bind(plan)
    }

    override fun getItemCount(): Int = plans.size

    class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tv_plan_title)
        private val avatarImage: ImageView = itemView.findViewById(R.id.iv_plan_avatar)
        private val authorNameText: TextView = itemView.findViewById(R.id.tv_plan_author_name)
        private val contentText: TextView = itemView.findViewById(R.id.tv_plan_content)
        private val priceView: MonikaPriceView = itemView.findViewById(R.id.view_plan_price)

        fun bind(plan: SubscriptionSupportPlan) {
            // 设置标题
            titleText.text = plan.subscribePlan?.title ?: ""

            // 设置头像
            if (!plan.avatar.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(plan.avatar)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.generic_avatar_default)
            }

            // 设置用户名
            authorNameText.text = plan.nickname ?: ""

            // 设置内容
            contentText.text = plan.subscribePlan?.rightsDesc ?: ""

            // 设置价格
            priceView.setPriceText((plan.subscribePlan?.price ?: 0f).toString())
        }
    }
}

