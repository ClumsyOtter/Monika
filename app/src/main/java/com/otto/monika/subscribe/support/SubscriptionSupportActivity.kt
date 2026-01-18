package com.otto.monika.subscribe.support

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.otto.common.utils.getView
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.subscribe.support.adapter.PaymentMethodAdapter
import com.otto.monika.subscribe.support.adapter.SubscriptionPeriodAdapter
import com.otto.monika.subscribe.support.adapter.SubscriptionPlanViewAdapter
import com.otto.monika.subscribe.support.model.SubscribeSuccessEvent
import com.otto.monika.subscribe.support.model.SubscriptionSupportPlan
import com.otto.monika.subscribe.support.viewmodel.SubscriptionSupportViewModel
import com.otto.monika.subscribe.support.views.MonikaSubscriptionTabView
import com.otto.network.common.collectSimple
import org.greenrobot.eventbus.EventBus

/**
 * 订阅支持页面
 */
class SubscriptionSupportActivity : MonikaBaseActivity() {

    companion object {
        private const val EXTRA_UID = "extra_uid"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param uid 用户ID
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: android.content.Context, uid: String?): Intent {
            val intent = Intent(context, SubscriptionSupportActivity::class.java)
            intent.putExtra(EXTRA_UID, uid)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到订阅支持页面
         * @param activity Activity 上下文
         * @param uid 用户ID
         */
        @JvmStatic
        fun enter(activity: Activity, uid: String? = null) {
            val intent = getIntent(activity, uid)
            activity.startActivity(intent)
        }
    }

    // 用户ID
    private val uid: String?
        get() = intent.getStringExtra(EXTRA_UID)

    private val tabLayout: TabLayout by getView(R.id.tab_layout_subscription)
    private val viewPager: ViewPager2 by getView(R.id.vp_subscription_plans)
    private val payButton: TextView by getView(R.id.btn_subscription_pay)
    private val paymentMethodsRecycler: RecyclerView by getView(R.id.rv_payment_methods)
    private val subscriptionPeriodsRecycler: RecyclerView by getView(R.id.rv_subscription_periods)

    // ViewModel
    private val viewModel: SubscriptionSupportViewModel by viewModels()
    private var tabLayoutMediator: TabLayoutMediator? = null

    // Adapters
    private var paymentMethodAdapter: PaymentMethodAdapter? = null
    private var subscriptionPeriodAdapter: SubscriptionPeriodAdapter? = null

    override fun getContentViewId(): Int {
        return R.layout.activity_subscription_support
    }


    override fun onDestroy() {
        super.onDestroy()
        // 分离 TabLayoutMediator，避免内存泄漏
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        // 重置初始化状态
        viewModel.resetInitializationState()
        setupUiState()
        setupListeners()
        uid?.let { viewModel.loadData(it) }
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 观察订阅方案数据（使用 StateFlow）
        lifecycleScope.launch {
            viewModel.subscriptionSupportPlansState.collectSimple(
                onLoading = {
                    // 可以显示加载状态
                },
                onSuccess = { plans ->
                    plans?.let { setupViews(it) }
                },
                onFailure = { message ->

                }
            )
        }

        // 观察选中的订阅期限，更新底部按钮价格（使用 StateFlow）
        lifecycleScope.launch {
            viewModel.selectedPlan.collect { plan ->
                plan?.apply {
                    paymentMethodAdapter?.setData(paymentMethods)
                    subscriptionPeriodAdapter?.setData(subscribePlan?.discountRules ?: emptyList())
                    //更新底部按钮价格
                    val bottomBtnPreText =
                        if (plan.subscribePlan?.isSubscribed == true) "续费" else "支付"
                    val bottomBtnPrice = viewModel.getSelectedSubscriptionPeriod()?.price ?: 0
                    payButton.text = bottomBtnPreText + bottomBtnPrice
                }
            }
        }
    }

    /**
     * 设置视图
     */
    private fun setupViews(subscriptionSupportPlans: List<SubscriptionSupportPlan>) {
        // 先设置 ViewPager（必须先设置 adapter）
        setupViewPager(subscriptionSupportPlans)

        // 然后根据订阅方案数量决定是否显示 TabLayout
        if (subscriptionSupportPlans.size > 1) {
            tabLayout.visibility = View.VISIBLE
            setupTabLayout()
        } else {
            tabLayout.visibility = View.GONE
        }
        // 初始化 Adapter（如果还未初始化）
        initAdapters()
        // 初始化支付方式和订阅期限（使用第一个方案的数据）
        if (subscriptionSupportPlans.isNotEmpty()) {
            updatePaymentMethodsAndPeriods(subscriptionSupportPlans[0])
        }

    }

    /**
     * 初始化 Adapter
     */
    private fun initAdapters() {
        // 初始化支付方式 Adapter
        if (paymentMethodAdapter == null) {
            paymentMethodAdapter = PaymentMethodAdapter()
            paymentMethodsRecycler.layoutManager = GridLayoutManager(this, 2)
            paymentMethodsRecycler.adapter = paymentMethodAdapter
            paymentMethodsRecycler.addItemDecoration(
                PaymentMethodAdapter.createSpacingDecoration(spanCount = 2, spacingDp = 10)
            )
            paymentMethodAdapter?.onItemClickListener = { method ->
                paymentMethodAdapter?.updateSelection(method)
            }
        }

        // 初始化订阅期限 Adapter
        if (subscriptionPeriodAdapter == null) {
            subscriptionPeriodAdapter = SubscriptionPeriodAdapter()
            subscriptionPeriodsRecycler.layoutManager = GridLayoutManager(this, 2)
            subscriptionPeriodsRecycler.adapter = subscriptionPeriodAdapter
            subscriptionPeriodsRecycler.addItemDecoration(
                SubscriptionPeriodAdapter.createSpacingDecoration(spanCount = 2, spacingDp = 10)
            )
            subscriptionPeriodAdapter?.onItemClickListener = { period ->
                subscriptionPeriodAdapter?.updateSelection(period)
            }
        }
    }

    /**
     * 设置 TabLayout
     */
    private fun setupTabLayout() {
        // 确保 ViewPager2 已经设置了 adapter
        if (viewPager.adapter == null) {
            return
        }
        // 先分离之前的 mediator（如果存在）
        tabLayoutMediator?.detach()

        // ViewPager2 与 TabLayout 的联动
        ViewCompat.setNestedScrollingEnabled(tabLayout, false)
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 使用自定义 View
            val customView = MonikaSubscriptionTabView(this)
            customView.setText("订阅${position + 1}")
            tab.customView = customView
            // 根据初始选中状态设置背景
            customView.setTabSelected(position == 0)
        }
        tabLayoutMediator?.attach()

        // 添加 tab 选中状态监听器
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateAllTabBackgrounds()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                updateAllTabBackgrounds()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                updateAllTabBackgrounds()
            }
        })
        // 初始化所有 tab 的背景
        updateAllTabBackgrounds()
    }

    /**
     * 更新所有 tab 的背景
     */
    private fun updateAllTabBackgrounds() {
        val selectedPosition = tabLayout.selectedTabPosition
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView as? MonikaSubscriptionTabView
            customView?.setTabSelected(i == selectedPosition)
        }
    }

    /**
     * 设置 ViewPager
     */
    private fun setupViewPager(subscriptionSupportPlans: List<SubscriptionSupportPlan>) {
        val adapter = SubscriptionPlanViewAdapter(subscriptionSupportPlans)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = subscriptionSupportPlans.size

        // 监听 ViewPager 页面切换，更新支付方式和订阅期限
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val plan = subscriptionSupportPlans.getOrNull(position)
                plan?.let { updatePaymentMethodsAndPeriods(it) }
            }
        })
    }

    /**
     * 更新支付方式和订阅期限
     */
    private fun updatePaymentMethodsAndPeriods(plan: SubscriptionSupportPlan) {
        // 通过 ViewModel 更新支付方式数据
        viewModel.updateSubscriptionSupportPlan(viewModel.selectedPlan.value, plan)
    }


    /**
     * 设置监听器
     */
    private fun setupListeners() {
        // 支付按钮
        payButton.setOnClickListener {
            handlePayment()
        }
    }


    /**
     * 处理支付
     */
    private fun handlePayment() {
        val selectedPlan = viewModel.getSelectedPlan()
        val subscribePlanId = selectedPlan?.subscribePlan?.id
        val selectedSubscriptionPeriod = viewModel.getSelectedSubscriptionPeriod()
        val duration = selectedSubscriptionPeriod?.month
        if (subscribePlanId == null || duration == null) {
            Toast.makeText(this, "请选择订阅方案和订阅期限", Toast.LENGTH_SHORT).show()
            return
        }
        // 模拟支付
        testCreateOrder(subscribePlanId, duration)
    }

    /**
     * 模拟创建订单（测试用）
     * @param subscribePlanId 订阅方案ID
     * @param duration 订阅期限（月数）
     */
    private fun testCreateOrder(subscribePlanId: String, duration: Int) {
        lifecycleScope.launch {
            viewModel.testCreateOrderFlow(uid, subscribePlanId, duration).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { response ->
                    hideLoadingDialog()
                    if (response != null && response.callbackResult == "success") {
                        onSubscribeSuccess()
                    } else {
                        Toast.makeText(
                            this@SubscriptionSupportActivity,
                            "订阅失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(
                        this@SubscriptionSupportActivity,
                        "订阅失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }


    fun onSubscribeSuccess() {
        Toast.makeText(
            this@SubscriptionSupportActivity,
            "订阅成功",
            Toast.LENGTH_SHORT
        ).show()
        // 订阅成功后的处理，设置返回结果并关闭页面
        setResult(RESULT_OK)
        EventBus.getDefault().post(SubscribeSuccessEvent(uid))
        finish()
    }
}