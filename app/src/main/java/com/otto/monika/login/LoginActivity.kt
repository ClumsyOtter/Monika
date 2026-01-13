package com.otto.monika.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.otto.monika.R
import com.otto.monika.common.utils.getView
import com.otto.monika.login.model.PhoneLogin
import com.otto.monika.login.views.LoginOptionsView
import com.otto.monika.login.views.UserPrivacyView

class LoginActivity : BaseLoginActivity() {

    companion object {
        /**
         * 获取 Intent
         * @param context Context对象
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到登录页面
         * @param context Context对象
         */
        @JvmStatic
        fun enter(context: Context) {
            val intent = getIntent(context)
            context.startActivity(intent)
        }
    }

    // 主界面视图
    private val logoContainer: View by getView(R.id.logo_container)
    private val loginOptionsContainer: View by getView(R.id.login_options_container)
    private val loginOptionsView: LoginOptionsView by getView(R.id.login_options_view)
    private val userPrivacyView: UserPrivacyView by getView(R.id.user_privacy_view)


    override fun onFinishCreateView() {
        initViews()
        setupClickListeners()
        setupAgreementText()
    }


    override fun getTitleText(): String {
        return ""
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_login
    }

    override fun isActionBarVisible(): Boolean {
        // 登录页面隐藏ActionBar
        return false
    }

    private fun initViews() {
        // 初始状态：显示logo和登录选项
        logoContainer.visibility = View.VISIBLE
        loginOptionsContainer.visibility = View.VISIBLE
    }


    private fun setupClickListeners() {
        // 手机号登录 - 跳转到PhoneLoginActivity，传入Login来源
        loginOptionsView.setOnPhoneLoginClickListener {
            PhoneLoginActivity.enter(this,
                PhoneLogin(PhoneLoginActivity.Companion.Source.LOGIN_PHONE)
            )
        }
        // 游客登录
        loginOptionsView.setOnGuestLoginClickListener {
            if (userPrivacyView.isChecked().not()) {
                check2Policy {
                    userPrivacyView.setChecked(true)
                    handleGuestLogin()
                }
            } else {
                handleGuestLogin()
            }
        }
        // 微信登录
        loginOptionsView.setOnWechatLoginClickListener {
            if (userPrivacyView.isChecked().not()) {
                check2Policy {
                    userPrivacyView.setChecked(true)
                    handleWechatLogin()
                }
            } else {
                handleWechatLogin()
            }
        }
    }

    private fun setupAgreementText() {
        userPrivacyView.initPrivacyText(onUserAgreementClick = {
            val userKey = resources.getString(R.string.monika_login_user_key)
            val agreementUrl = ""
        }, onPrivacyPolicyClick = {
            val privacyKey = resources.getString(R.string.monika_login_privacy_key)
            val agreementUrl =""
        })
    }
}