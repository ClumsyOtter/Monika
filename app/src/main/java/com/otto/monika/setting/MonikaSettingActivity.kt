package com.otto.monika.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.otto.common.token.TokenManager
import com.otto.common.utils.getView
import com.otto.datastore.TokenDataStore
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.dialog.MonikaConfirmBottomDialog
import com.otto.monika.login.LoginActivity
import com.otto.monika.setting.viewmodel.MonikaSettingViewModel
import com.otto.monika.common.dialog.MonikaDownloadBottomDialog
import com.otto.monika.common.views.MonikaCustomButton
import com.otto.network.common.collectSimple
import kotlinx.coroutines.launch

/**
 * 设置界面
 * 包含用户协议、隐私条款、检测更新等功能
 */
class MonikaSettingActivity : MonikaBaseActivity() {

    companion object {
        /**
         * 获取 Intent
         * @param context 上下文
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, MonikaSettingActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到设置页面
         * @param context 上下文
         */
        @JvmStatic
        fun enter(context: Context) {
            val intent = getIntent(context)
            context.startActivity(intent)
        }
    }

    // 用户协议相关视图
    private val llUserAgreement: View by getView(R.id.ll_setting_user_agreement)

    // 隐私条款相关视图
    private val llPrivacyPolicy: View by getView(R.id.ll_setting_privacy_policy)

    // 检测更新相关视图
    private val llCheckUpdate: View by getView(R.id.ll_setting_check_update)

    //退出登录
    private val cyLogoutBtn: MonikaCustomButton by getView(R.id.cycb_setting_logout)

    // ViewModel
    private val viewModel: MonikaSettingViewModel by viewModels()

    override fun getContentViewId(): Int {
        return R.layout.activity_setting
    }

    override fun isActionBarVisible(): Boolean {
        return true
    }


    override fun onFinishCreateView() {
        super.onFinishCreateView()
        initViews()
    }

    override fun getTitleText(): String {
        return "设置"
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        // 用户协议点击事件
        llUserAgreement.setOnClickListener {
            openUserAgreement()
        }

        // 隐私条款点击事件
        llPrivacyPolicy.setOnClickListener {
            openPrivacyPolicy()
        }

        // 检测更新点击事件
        llCheckUpdate.setOnClickListener {
            checkUpdate()
        }
        //点击退出登录
        cyLogoutBtn.setOnClickListener {
            val newInstance = MonikaConfirmBottomDialog.newInstance("确定退出登录吗？")
            newInstance.onConfirmClickListener = {
                loginOut()
            }
            newInstance.show(supportFragmentManager)
        }
    }

    /**
     * 退出登录
     */
    private fun loginOut() {
        lifecycleScope.launch {
            viewModel.logoutFlow().collectSimple(
                onLoading = {
                    // 可以显示加载状态
                },
                onSuccess = { _ ->
                    TokenManager.token = null
                    lifecycleScope.launch {
                        this@MonikaSettingActivity.TokenDataStore.updateData {
                            it.copy(token = null)
                        }
                    }
                    navigateToLogin()
                },
                onFailure = { message ->
                    Toast.makeText(
                        this@MonikaSettingActivity,
                        "退出登录失败: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    /**
     * 跳转到登录页面并清空 activity 栈
     */
    private fun navigateToLogin() {
        val intent = LoginActivity.getIntent(this).apply {
            // 清空 activity 栈
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * 打开用户协议
     */
    private fun openUserAgreement() {
    }

    /**
     * 打开隐私条款
     */
    private fun openPrivacyPolicy() {
    }

    /**
     * 检测更新
     */
    private fun checkUpdate() {
        // TODO: 实际项目中应该调用接口检测是否有新版本
        // 这里模拟检测到新版本
        val downloadUrl = "https://example.com/app_update.apk" // 替换为实际的下载URL
        showDownloadDialog("1.0.0", downloadUrl)
    }


    /**
     * 显示下载对话框
     * @param version 版本号
     * @param downloadUrl 下载URL
     */
    private fun showDownloadDialog(version: String, downloadUrl: String) {
        val downloadDialog = MonikaDownloadBottomDialog.newInstance(
            version = version,
            downloadUrl = downloadUrl,
            savePath = null // 使用默认路径
        )

        // 后台下载点击回调
        downloadDialog.onBackgroundDownloadClickListener = {
            // 点击后台下载后，对话框消失，但后台继续下载
            // 下载任务已经在后台继续执行，会通过通知栏显示进度
        }

        // 下载完成回调
        downloadDialog.onDownloadCompleteListener = { filePath ->
            // 下载完成，通知栏会显示安装提示
            // 可以在这里添加额外的处理逻辑
        }

        // 下载失败回调
        downloadDialog.onDownloadErrorListener = { error ->
            // 下载失败，通知栏会显示错误提示
            Toast.makeText(this, "下载失败：$error", Toast.LENGTH_SHORT).show()
        }

        downloadDialog.show(supportFragmentManager)
    }

}
