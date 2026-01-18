package com.otto.monika.subscribe.plan

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otto.common.utils.getView
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.decoration.VerticalSpacingItemDecoration
import com.otto.monika.common.dialog.MonikaConfirmBottomDialog
import com.otto.monika.common.dialog.MonikaSubscribeRightsBottomSheet
import com.otto.monika.common.ext.disableButton
import com.otto.monika.common.ext.enableButton
import com.otto.monika.common.views.MonikaCustomButton
import com.otto.monika.subscribe.plan.adapter.SubscribePlanAdapter
import com.otto.monika.subscribe.plan.viewmodel.SubscribePlanViewModel
import com.otto.network.common.collectSimple
import kotlinx.coroutines.launch

/**
 * 订阅方案页面
 */
class SubscribePlanActivity : MonikaBaseActivity() {
    // 用户信息iD
    private val accountUid: String?
        get() = intent.getStringExtra(EXTRA_ACCOUNT_UID)

    companion object {
        private const val EXTRA_ACCOUNT_UID = "extra_account_uid_data"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param uid 用户ID
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, uid: String? = null): Intent {
            val intent = Intent(context, SubscribePlanActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_UID, uid)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到订阅方案页面
         * @param activity Activity 上下文
         * @param uid 用户ID
         */
        @JvmStatic
        fun enter(activity: Activity, uid: String? = null) {
            val intent = getIntent(activity, uid)
            activity.startActivity(intent)
        }
    }

    private val recyclerView: RecyclerView by getView(R.id.rv_subscribe_plan_list)
    private val saveButton: MonikaCustomButton by getView(R.id.cycb_subscribe_plan_save)

    private var adapter: SubscribePlanAdapter? = null

    // ViewModel
    private val viewModel: SubscribePlanViewModel by viewModels()

    override fun getContentViewId(): Int {
        return R.layout.activity_subscribe_plan
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        initView()
        setupRecyclerView()
        setupUiState()
        updateSaveBtnStatus(false)
        // 加载订阅方案列表，传入用户ID
        viewModel.initData(uid = accountUid)
    }

    private fun initView() {
        saveButton.setOnClickListener {
            if (adapter?.checkValidation() == true) {
                lifecycleScope.launch {
                    viewModel.createPlansFlow(adapter?.getPlans() ?: emptyList()).collectSimple(
                        onLoading = {
                            showLoadingDialog()
                        },
                        onSuccess = {
                            hideLoadingDialog()
                            this@SubscribePlanActivity.finish()
                        },
                        onFailure = {
                            hideLoadingDialog()
                        }
                    )
                }
            }
        }
    }


    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = SubscribePlanAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalSpacingItemDecoration(10))
        // 设置添加按钮点击监听
        adapter?.onAddButtonClickListener = {
            adapter?.addNewPlan(viewModel.generateNewPlan())
        }
        //删除
        adapter?.onPlanDeleteClickListener = { plan ->
            val confirmBottomDialog = MonikaConfirmBottomDialog.newInstance(
                content = "确定要删除订阅方案吗？",
                confirmText = "确认删除"
            )
            confirmBottomDialog.onConfirmClickListener = {
                if (plan.id != null) {
                    lifecycleScope.launch {
                        viewModel.deleteSubscribePlanFlow(plan.id ?: "")
                            .collectSimple(onLoading = {
                                showLoadingDialog()
                            }, onSuccess = {
                                hideLoadingDialog()
                                adapter?.removePlan(plan)
                            }, onFailure = {
                                hideLoadingDialog()
                            })
                    }
                } else {
                    adapter?.removePlan(plan)
                }
            }
            confirmBottomDialog.show(supportFragmentManager, "delete_plan_dialog")
        }
        //订阅权益
        adapter?.onAddRightsClickListener = { plan ->
            val generateSubscribeRights = viewModel.subscribePlanRights
            val bottomSheet = MonikaSubscribeRightsBottomSheet.newInstance(generateSubscribeRights)
            bottomSheet.onRightsAddClickListener = { rightsTitle ->
                // 将选中的权益标题添加到当前方案的权益描述中
                adapter?.updatePlanRightsDesc(plan, "-$rightsTitle")
            }
            bottomSheet.show(supportFragmentManager, "add_rights")
        }
        // 验证状态变化监听
        adapter?.onValidationStateChangeListener = { isValid ->
            updateSaveBtnStatus(isValid)
        }
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 观察订阅方案数据（使用 StateFlow）
        lifecycleScope.launch {
            viewModel.subscribePlanListState.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { response ->
                    hideLoadingDialog()
                    // 加载成功，更新列表数据
                    val plans = response?.list?.toMutableList() ?: mutableListOf()
                    //当没有方案数据的时候，添加一个默认的
                    if (plans.isEmpty()) {
                        plans.add(viewModel.generateNewPlan())
                    }
                    adapter?.setData(plans)
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    // 加载失败，显示错误提示或使用空列表
                    adapter?.setData(mutableListOf())
                }
            )
        }
    }

    private fun updateSaveBtnStatus(isReady: Boolean) {
        if (isReady.not()) {
            saveButton.disableButton(R.color.color_E6E6E6)
        } else {
            saveButton.enableButton()
        }
    }
}