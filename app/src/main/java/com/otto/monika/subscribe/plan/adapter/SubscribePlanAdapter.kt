package com.otto.monika.subscribe.plan.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.utils.ChineseNumberConverter
import com.otto.monika.common.utils.DecimalDigitsInputFilter
import com.otto.monika.common.utils.addInputFilter
import com.otto.monika.common.utils.removeInputFilter
import com.otto.monika.subscribe.plan.model.SubscribePlan

/**
 * 订阅方案列表 Adapter
 * 支持两种 item 类型：方案 view 和添加按钮
 * 最后一个 position 自动显示添加按钮
 */
class SubscribePlanAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PLAN = 0
        private const val VIEW_TYPE_ADD = 1
    }

    private var plans: MutableList<SubscribePlan> = mutableListOf()
    private var totalPlanCount: Int = 0 // 总方案数量，用于判断是否显示方案编号

    var onAddButtonClickListener: (() -> Unit)? = null
    var onPlanDeleteClickListener: ((SubscribePlan) -> Unit)? = null
    var onAddRightsClickListener: ((SubscribePlan) -> Unit)? = null
    var onValidationStateChangeListener: ((Boolean) -> Unit)? = null // 验证状态变化回调，参数表示是否所有必填项都填好了

    /**
     * 设置数据
     */
    fun setData(newPlans: List<SubscribePlan>) {
        plans.clear()
        plans.addAll(newPlans)
        totalPlanCount = newPlans.size
        notifyDataSetChanged()
    }

    /**
     * 删除指定位置的方案
     * 删除后需要全部刷新，因为方案编号会变化
     */
    fun removePlan(plan: SubscribePlan) {
        val index = plans.indexOfFirst { it == plan }
        if (index != -1) {
            plans.removeAt(index)
            totalPlanCount = plans.size
            // 全部刷新，因为删除后方案编号会变化
            notifyDataSetChanged()
            // 检查验证状态
            checkValidationState()
        }
    }

    /**
     * 添加新方案到列表底部
     */
    fun addNewPlan(plan: SubscribePlan) {
        val insertPosition = plans.size
        val oldCount = totalPlanCount
        plans.add(plan)
        totalPlanCount = plans.size

        // 如果方案数量从 1 变成 2，需要更新第一个 item（因为方案编号显示状态改变）
        if (oldCount == 1 && totalPlanCount == 2) {
            notifyItemRangeChanged(0, 2)
        } else {
            notifyItemChanged(insertPosition)
        }
        // 检查验证状态（新方案是空的，所以验证状态会变为 false）
        checkValidationState()
    }

    /**
     * 检查所有方案的必填项是否都填好了
     * price 和 rightsDesc 是必填项
     */
    private fun checkValidationState() {
        // 触发验证状态变化回调
        onValidationStateChangeListener?.invoke(checkValidation())
    }

    fun checkValidation(): Boolean {
        return plans.isNotEmpty() && plans.all { plan ->
            // 检查 price 和 rightsDesc 是否都不为空
            val price = plan.price?.toDoubleOrNull() ?: 0.0
            val priceValid = price > 5.0
            priceValid && !plan.rightsDesc.isNullOrBlank()
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 最后一个 position 是添加按钮
        return if (position == plans.size) {
            VIEW_TYPE_ADD
        } else {
            VIEW_TYPE_PLAN
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLAN -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_subscribe_plan, parent, false)
                PlanViewHolder(view)
            }

            VIEW_TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_subscribe_plan_add, parent, false)
                AddButtonViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == plans.size) {
            // 最后一个 position 是添加按钮
            (holder as AddButtonViewHolder).bind()
        } else {
            // 其他 position 是方案
            (holder as PlanViewHolder).bind(plans[position])
        }
    }

    override fun getItemCount(): Int = plans.size + 1

    /**
     * 根据 plan 更新权益描述
     * 将选中的权益标题添加到权益描述输入框中
     */
    fun updatePlanRightsDesc(plan: SubscribePlan, rightsTitle: String) {
        val position = plans.indexOfFirst { it == plan }
        if (position >= 0 && position < plans.size) {
            val currentDesc = plan.rightsDesc ?: ""
            val updatedDesc = if (currentDesc.isBlank()) {
                rightsTitle
            } else {
                "$currentDesc\n$rightsTitle"
            }
            plan.rightsDesc = updatedDesc
            // 通知该 item 更新，这会触发 bind 方法重新设置 EditText 的内容
            notifyItemChanged(position)
            // 检查验证状态（因为权益描述可能从空变为非空）
            checkValidationState()
        }
    }

    fun getPlans(): MutableList<SubscribePlan> {
        return plans
    }

    /**
     * 方案 ViewHolder
     */
    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 第一行
        private val planNumberText: TextView = itemView.findViewById(R.id.tv_plan_item_number)
        private val deleteImage: ImageView = itemView.findViewById(R.id.iv_plan_item_delete)

        // 第二行
        private val titleInput: EditText = itemView.findViewById(R.id.et_plan_item_title_input)

        // 第三行
        private val priceHintText: TextView = itemView.findViewById(R.id.tv_plan_item_price_hint)

        // 第四行
        private val priceInput: EditText = itemView.findViewById(R.id.et_plan_item_price)

        // 第六行
        private val rightsDescInput: EditText = itemView.findViewById(R.id.et_plan_item_rights_desc)

        // 第七行
        private val addRightsLayout: LinearLayout =
            itemView.findViewById(R.id.ll_plan_item_add_rights)

        // 第九行
        private val discountSwitch: Switch = itemView.findViewById(R.id.switch_plan_item_discount)

        // 第十行
        private val discountRulesRecycler: RecyclerView =
            itemView.findViewById(R.id.rv_plan_item_discount_rules)
        private var discountRuleAdapter: SubscribePlanDiscountRuleAdapter? = null
        private var titleTextWatcher: TextWatcher? = null
        private var priceTextWatcher: TextWatcher? = null
        private var rightsDescTextWatcher: TextWatcher? = null

        fun bind(plan: SubscribePlan) {
            val position = bindingAdapterPosition
            if (position < 0 || position >= plans.size) return
            // 如果方案超过一个，显示方案编号
            if (totalPlanCount > 1) {
                // 1. 转换为中文数字（小写）
                val number = ChineseNumberConverter.toChinese(position + 1)
                planNumberText.visibility = View.VISIBLE
                planNumberText.text = "方案$number"
            } else {
                planNumberText.visibility = View.GONE
            }

            deleteImage.setOnClickListener {
                onPlanDeleteClickListener?.invoke(plan)
            }
            // 第二行：标题输入框
            bindTitle(plan)

            // 第四行：价格输入框
            bindPrice(plan)

            // 第六行：权益描述输入框
            bindRightDesc(plan)

            // 第七行：增加权益按钮
            addRightsLayout.setOnClickListener {
                onAddRightsClickListener?.invoke(plan)
            }
            // 第九行：折扣开关
            discountSwitch.setOnCheckedChangeListener(null) // 先移除旧的监听器
            discountSwitch.isChecked = plan.openDiscount == 1
            discountSwitch.setOnCheckedChangeListener { _, isChecked ->
                plan.openDiscount = if (isChecked) 1 else 0
                discountRulesRecycler.visibility = if (isChecked) View.VISIBLE else View.GONE
                checkValidationState()
            }
            // 第十行：折扣规则列表
            setupDiscountRulesRecycler(plan)
            discountRulesRecycler.isVisible = plan.openDiscount == 1
        }

        fun bindPrice(plan: SubscribePlan) {
            //添加小数点限制
            priceInput.removeInputFilter(DecimalDigitsInputFilter::class.java)
            priceInput.addInputFilter(DecimalDigitsInputFilter())

            if (plan.price != null) {
                priceInput.setText(plan.price)
            }
            updatePriceHintVisibility(plan.price.toString())

            priceTextWatcher?.let { priceInput.removeTextChangedListener(it) }
            priceTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val priceText = s?.toString() ?: ""
                    updatePriceHintVisibility(priceText)
                    // 更新计划数据并更新折扣规则价格
                    plan.price = priceText
                    // 更新折扣规则中的价格
                    discountRuleAdapter?.updateDiscountRulesPrices(plan.price)
                    // 检查验证状态
                    checkValidationState()
                }
            }
            priceInput.addTextChangedListener(priceTextWatcher)
        }

        fun bindTitle(plan: SubscribePlan) {
            titleInput.setText(plan.title ?: "")
            titleTextWatcher?.let { titleInput.removeTextChangedListener(it) }
            titleTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val titleText = s?.toString() ?: ""
                    plan.title = titleText
                    checkValidationState()
                }
            }
            titleInput.addTextChangedListener(titleTextWatcher)
        }

        fun bindRightDesc(plan: SubscribePlan) {
            rightsDescInput.setText(plan.rightsDesc ?: "")
            rightsDescTextWatcher?.let { rightsDescInput.removeTextChangedListener(it) }
            rightsDescTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val rightsDescText = s?.toString() ?: ""
                    plan.rightsDesc = rightsDescText
                    // 检查验证状态
                    checkValidationState()
                }
            }
            rightsDescInput.addTextChangedListener(rightsDescTextWatcher)
        }

        /**
         * 更新价格提示语的显示/隐藏
         * 只有当价格小于5时才显示
         */
        private fun updatePriceHintVisibility(priceText: String?) {
            val price = priceText?.toDoubleOrNull() ?: 0.0
            priceHintText.visibility = if (price > 0 && price < 5) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }


        private fun setupDiscountRulesRecycler(plan: SubscribePlan) {
            if (discountRuleAdapter == null) {
                discountRuleAdapter = SubscribePlanDiscountRuleAdapter()
                discountRulesRecycler.layoutManager = LinearLayoutManager(itemView.context)
                discountRulesRecycler.adapter = discountRuleAdapter
                discountRulesRecycler.addItemDecoration(VerticalSpacingItemDecoration())
            }
            discountRuleAdapter?.setData(plan.discountRules)
            // 设置基础价格
            discountRuleAdapter?.updateDiscountRulesPrices(plan.price)
        }
    }


    /**
     * 添加按钮 ViewHolder
     */
    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                onAddButtonClickListener?.invoke()
            }
        }
    }
}

