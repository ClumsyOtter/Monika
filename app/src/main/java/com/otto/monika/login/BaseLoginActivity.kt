package com.otto.monika.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.otto.common.token.TokenManager
import com.otto.datastore.TokenDataStore
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.dialog.MonikaAgreementBottomDialog
import com.otto.monika.home.HomePageActivity
import com.otto.monika.login.viewmodel.LoginViewModel
import com.otto.network.common.collectSimple
import kotlinx.coroutines.launch

abstract class BaseLoginActivity : MonikaBaseActivity() {

    protected val viewModel: LoginViewModel by viewModels()

    protected fun check2Policy(
        activity: Activity = this, onUserAgree: () -> Unit
    ) {
        val newInstance = MonikaAgreementBottomDialog.newInstance(activity)
        newInstance.onConfirmClickListener = {
            onUserAgree.invoke()
        }
        newInstance.show()
    }


    fun handleGuestLogin() {
        lifecycleScope.launch {
            this@BaseLoginActivity.TokenDataStore.data.collect { token ->
                if (token.token?.isNotEmpty() == true) {
                    TokenManager.token = token.token
                    openHomePage()
                } else {
                    viewModel.asVisitorFlow().collectSimple(onLoading = {
                        showLoadingDialog()
                    }, onSuccess = {
                        // 隐藏加载对话框
                        hideLoadingDialog()
                        // 处理成功逻辑（data 可能为 null，需要安全处理）
                        val tokenInfo = it?.token
                        if (tokenInfo != null) {
                            TokenManager.token = tokenInfo
                            lifecycleScope.launch {
                                this@BaseLoginActivity.TokenDataStore.updateData { token ->
                                    token.copy(token = tokenInfo)
                                }
                            }
                            // 跳转到主页
                            openHomePage()
                        } else {
                            Toast.makeText(
                                this@BaseLoginActivity, "登录失败：token 为空", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, onFailure = {
                        hideLoadingDialog()
                    })
                }
            }
        }
    }

    fun openHomePage() {
        HomePageActivity.enter(this@BaseLoginActivity)
        finish()
    }

    fun handleWechatLogin() {
        //todo
    }

}
