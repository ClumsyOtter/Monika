package com.otto.monika.account.income

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.otto.common.utils.getView
import com.otto.monika.R
import com.otto.monika.account.income.viewmodel.AccountIncomeViewModel
import com.otto.monika.account.income.views.AmountWithUnitView
import com.otto.monika.account.income.views.IncomeItemView
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.network.common.collectSimple
import kotlinx.coroutines.launch


/**
 * 我的收入页面
 * 显示收入信息和已提现金额
 */
class AccountIncomeActivity : MonikaBaseActivity() {

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
        fun getIntent(context: Context, uid: String?): Intent {
            val intent = Intent(context, AccountIncomeActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_UID, uid)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到我的收入页面
         * @param activity Activity 上下文
         * @param uid 用户ID
         */
        @JvmStatic
        fun enter(activity: Activity, uid: String? = null) {
            val intent = getIntent(activity, uid)
            activity.startActivity(intent)
        }
    }

    private val incomeItemTotalIncome: IncomeItemView by getView(R.id.income_item_total_income)
    private val incomeItemUnsettledIncome: IncomeItemView by getView(R.id.income_item_unsettled_income)
    private val amountWithUnitSettledIncome: AmountWithUnitView by getView(R.id.amount_with_unit_settled_income)

    // 已提现金额相关视图
    private val tvWithdrawnAmount: AmountWithUnitView by getView(R.id.tv_account_income_withdrawn_amount)
    private val tvWithdrawableAmount: TextView by getView(R.id.tv_account_income_withdrawable_amount)

    private val viewModel: AccountIncomeViewModel by viewModels()

    override fun getContentViewId(): Int {
        return R.layout.activity_account_income
    }

    override fun isActionBarVisible(): Boolean {
        return true
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        setupUiState()
        // 从接口获取数据
        viewModel.loadIncome()
    }

    override fun getTitleText(): String {
        return "我的收入"
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        lifecycleScope.launch {
            viewModel.incomeState.collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { response ->
                    hideLoadingDialog()
                    // 更新收入数据
                    response?.let { income ->
                        updateIncomeData(
                            totalIncome = income.totalIncome ?: "",
                            unsettledIncome = income.unsettledIncome ?: "",
                            settledIncome = income.settledIncome ?: "",
                            withdrawnAmount = income.withdrawnIncome ?: "",
                            withdrawableAmount = income.withdrawnableIncome ?: ""
                        )
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(this@AccountIncomeActivity, message, Toast.LENGTH_SHORT).show()
                    // 加载失败时显示默认值
                    updateIncomeData("0.00", "0.00", "0.00", "0", "0")
                }
            )
        }
    }

    /**
     * 更新收入数据
     * @param totalIncome 总收入
     * @param unsettledIncome 未结算收入
     * @param settledIncome 已完成结算收入
     * @param withdrawnAmount 已提现金额
     * @param withdrawableAmount 当前可提现额度
     */
    fun updateIncomeData(
        totalIncome: String,
        unsettledIncome: String,
        settledIncome: String,
        withdrawnAmount: String,
        withdrawableAmount: String
    ) {
        incomeItemTotalIncome.setAmount(totalIncome)
        incomeItemUnsettledIncome.setAmount(unsettledIncome)
        amountWithUnitSettledIncome.setAmount(settledIncome)
        tvWithdrawnAmount.setAmount(withdrawnAmount)
        tvWithdrawableAmount.text = "当前可提现额度：${withdrawableAmount}元"
    }
}
