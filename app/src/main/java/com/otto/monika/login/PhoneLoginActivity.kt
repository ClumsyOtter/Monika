package com.otto.monika.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.otto.common.token.TokenManager
import com.otto.common.utils.MD5Utils
import com.otto.common.utils.getView
import com.otto.common.utils.isValidatePhoneNumber
import com.otto.datastore.TokenDataStore
import com.otto.monika.R
import com.otto.monika.common.dialog.MonikaConfirmBottomDialog
import com.otto.monika.common.ext.disableButton
import com.otto.monika.common.ext.enableButton
import com.otto.monika.common.views.MonikaCustomButton
import com.otto.monika.login.model.PhoneLogin
import com.otto.monika.login.views.CodeInputView
import com.otto.monika.login.views.PhoneInputView
import com.otto.monika.login.views.UserPrivacyView
import com.otto.network.common.collectSimple
import kotlinx.coroutines.launch

class PhoneLoginActivity : BaseLoginActivity() {

    companion object {
        const val EXTRA_LOGIN_PARAM = "extra_login_param"
        const val EXTRA_RESULT_PHONE = "extra_result_phone"

        /**
         * Source来源枚举
         */
        enum class Source {
            LOGIN_PHONE,    // 从登录页面进入
            EDIT_BIND_PHONE,   // 从设置页面
            EDIT_CHANGE_PHONE_VERIFY,   // 从设置页面进入
            EDIT_CHANGE_PHONE_BIND   // 从设置页面进入

        }

        /**
         * 获取 Intent
         * @param context Context对象
         * @param phoneLogin PhoneLogin对象，包含来源和手机号信息
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, phoneLogin: PhoneLogin): Intent {
            val intent = Intent(context, PhoneLoginActivity::class.java)
            intent.putExtra(EXTRA_LOGIN_PARAM, phoneLogin)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到手机号登录页面
         * @param context Context对象
         * @param phoneLogin PhoneLogin对象，包含来源和手机号信息
         */
        @JvmStatic
        fun enter(context: Context, phoneLogin: PhoneLogin) {
            val intent = getIntent(context, phoneLogin)
            context.startActivity(intent)
        }
    }

    private val titleTextView: TextView by getView(R.id.phone_login_title)
    private val hintTextView: TextView by getView(R.id.phone_login_hint)
    private val phoneInput: PhoneInputView by getView(R.id.phone_input)
    private val codeInput: CodeInputView by getView(R.id.code_input)
    private val phoneLoginSubmitBtn: MonikaCustomButton by getView(R.id.phone_login_submit_btn)

    // 底部功能区域（仅Login来源显示）
    private val bottomFunctionContainer: LinearLayout by getView(R.id.bottom_function_container)
    private val guestLoginBtn: MonikaCustomButton by getView(R.id.guest_login_btn)
    private val wechatLoginBtn: MonikaCustomButton by getView(R.id.wechat_login_btn)
    private val bottomPrivacyView: UserPrivacyView by getView(R.id.bottom_privacy_view)

    private var mSourceFrom: Source = Source.LOGIN_PHONE
    private var phoneLogin: PhoneLogin? = null

    // 保存验证阶段的手机号，用于绑定时的 oldPhone
    private var verifiedPhone: String? = null

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        // 从Intent中获取PhoneLogin对象
        phoneLogin = intent.getParcelableExtra(EXTRA_LOGIN_PARAM)
        // 获取Source来源
        mSourceFrom = phoneLogin?.source ?: Source.LOGIN_PHONE
        initViews()
        setupClickListeners()
        setupInputWatchers()
        setupAgreementText()
    }


    override fun getContentViewId(): Int {
        return R.layout.activity_phone_login
    }

    private fun initViews() {
        // 设置登录按钮初始状态为灰色（禁用）
        updateLoginButtonState(false)
        // 如果PhoneLogin对象中有手机号，则填充到输入框
        phoneLogin?.phoneNumber?.let { phone ->
            if (phone.isNotEmpty()) {
                phoneInput.setPhoneNumber(phone)
                validatePhoneNumber()
            }
        }
        updateUiBySource(mSourceFrom)
    }

    private fun updateUiBySource(source: Source) {
        when (source) {
            Source.LOGIN_PHONE -> {
                // Login来源：显示底部功能（游客登录、微信登录、隐私同意）
                bottomFunctionContainer.visibility = View.VISIBLE
                titleTextView.text = resources.getString(R.string.monika_phone_login_title)
                hintTextView.text = resources.getString(R.string.monika_phone_login_content)
                phoneLoginSubmitBtn.setTitle(resources.getString(R.string.monika_phone_login_confirm_btn))
            }

            Source.EDIT_BIND_PHONE -> {
                // Setting来源：隐藏底部功能
                bottomFunctionContainer.visibility = View.GONE
                titleTextView.text = resources.getString(R.string.monika_phone_bind_phone_title)
                hintTextView.text = resources.getString(R.string.monika_phone_bind_phone_content)
                phoneLoginSubmitBtn.setTitle(resources.getString(R.string.monika_phone_edit_confirm_btn))
            }

            Source.EDIT_CHANGE_PHONE_VERIFY -> {
                // Setting来源：隐藏底部功能
                bottomFunctionContainer.visibility = View.GONE
                titleTextView.text = resources.getString(R.string.monika_phone_change_verify_title)
                hintTextView.text = resources.getString(R.string.monika_phone_change_verify_content)
                phoneLoginSubmitBtn.setTitle(resources.getString(R.string.monika_phone_edit_confirm_btn))
            }

            Source.EDIT_CHANGE_PHONE_BIND -> {
                bottomFunctionContainer.visibility = View.GONE
                titleTextView.text = resources.getString(R.string.monika_phone_change_bind_title)
                hintTextView.text = resources.getString(R.string.monika_phone_change_bind_content)
                phoneLoginSubmitBtn.setTitle(resources.getString(R.string.monika_phone_edit_confirm_btn))
            }
        }
    }

    private fun setupClickListeners() {
        // 手机号登录提交按钮
        phoneLoginSubmitBtn.setOnClickListener {
            if (needCheck2Policy()) {
                check2Policy {
                    bottomPrivacyView.setChecked(true)
                    handlePhoneLoginSubmit()
                }
            } else {
                handlePhoneLoginSubmit()
            }
        }

        // 游客登录按钮（仅Login来源显示）
        guestLoginBtn.setOnClickListener {
            if (needCheck2Policy()) {
                check2Policy {
                    bottomPrivacyView.setChecked(true)
                    handleGuestLogin()
                }
            } else {
                handleGuestLogin()
            }
        }

        // 微信登录按钮（仅Login来源显示）
        wechatLoginBtn.setOnClickListener {
            if (needCheck2Policy()) {
                check2Policy {
                    bottomPrivacyView.setChecked(true)
                    handleWechatLogin()
                }
            } else {
                handleWechatLogin()
            }
        }
    }

    private fun needCheck2Policy(): Boolean {
        return mSourceFrom == Source.LOGIN_PHONE && bottomPrivacyView.isChecked().not()
    }

    private fun setupInputWatchers() {
        // 监听手机号输入
        phoneInput.setPhoneNumberTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePhoneNumber()
                checkInputComplete()
            }
        })

        // 监听验证码输入
        codeInput.setCodeTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePhoneNumber()
                checkInputComplete()
            }
        })

        // 设置发送验证码点击监听
        codeInput.setOnSendCodeClickListener {
            val phone = phoneInput.getPhoneNumber()
            if (checkPhoneNumber(phone).not()) {
                return@setOnSendCodeClickListener
            }
            // 调用 ViewModel 获取验证码
            lifecycleScope.launch {
                viewModel.getSmsCodeFlow(phone)
                    .collectSimple(
                        onLoading = { codeInput.setSendCodeEnabled(false) },
                        onSuccess = {
                            // 发送成功
                            codeInput.setSendCodeEnabled(true)
                            Toast.makeText(
                                this@PhoneLoginActivity, "验证码已发送", Toast.LENGTH_SHORT
                            ).show()
                            // 开始倒计时
                            codeInput.startCountDown()
                        },
                        onFailure = { codeInput.setSendCodeEnabled(true) })
            }


        }
    }

    /**
     * 验证手机号格式
     */
    private fun validatePhoneNumber() {
        val phone = phoneInput.getPhoneNumber()
        // 简单验证：11位数字且以1开头
        val isValid = checkPhoneNumber(phone)
        // 传递手机号用于检测变化
        codeInput.setPhoneValid(isValid, phone)
    }

    private fun checkPhoneNumber(phone: String): Boolean {
        val isValid = phone.isValidatePhoneNumber()
        if (phone.length >= 11 && isValid.not()) {
            Toast.makeText(this, "(｡･ω･｡) 手机号格式有误", Toast.LENGTH_SHORT).show()
        }
        return isValid
    }

    private fun checkInputComplete() {
        val phone = phoneInput.getPhoneNumber()
        val code = codeInput.getCode()
        val isComplete = phone.isValidatePhoneNumber() && code.isNotEmpty()
        updateLoginButtonState(isComplete)
    }

    private fun updateLoginButtonState(enabled: Boolean) {
        if (enabled) {
            phoneLoginSubmitBtn.enableButton()
        } else {
            phoneLoginSubmitBtn.disableButton()
        }
    }

    private fun handlePhoneLoginSubmit() {
        val phone = phoneInput.getPhoneNumber()
        val code = codeInput.getCode()
        if (checkPhoneNumber(phone).not()) {
            return
        }
        // 验证验证码格式
        if (code.length < 4) {
            Toast.makeText(this, "(｡･ω･｡) 验证码输入错误", Toast.LENGTH_SHORT).show()
            return
        }
        handleSubmitBySource(mSourceFrom, phone, code)

    }

    private fun handleSubmitBySource(source: Source, phone: String, code: String) {
        when (source) {
            Source.LOGIN_PHONE -> {
                handlePhoneLogin(phone, code)
            }

            Source.EDIT_BIND_PHONE -> {
                handlePhoneBind(phone, code)
            }

            Source.EDIT_CHANGE_PHONE_BIND -> {
                handlePhoneBind(phone, code)
            }


            Source.EDIT_CHANGE_PHONE_VERIFY -> {
                handlePhoneVerify(phone, code)
            }
        }

    }

    private fun showBindSuccessDialog(phone: String) {
        // 设置来源：显示绑定成功提示（暂时保留原有逻辑）
        val cyConfirmBottomDialog = MonikaConfirmBottomDialog.newInstance(
            "手机号绑定成功！\n 绑定手机号：${phone}", cancelText = null, confirmText = "我知道了"
        )
        cyConfirmBottomDialog.onConfirmClickListener = {
            // 如果是编辑来源，返回结果
            if (mSourceFrom == Source.EDIT_BIND_PHONE || mSourceFrom == Source.EDIT_CHANGE_PHONE_BIND) {
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_RESULT_PHONE, phone)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
        cyConfirmBottomDialog.show(supportFragmentManager)
    }

    private fun handlePhoneBind(phone: String, code: String) {
        // 获取旧手机号（验证阶段的手机号）
        val oldPhone = verifiedPhone ?: phoneLogin?.phoneNumber ?: ""
        val userId = phoneLogin?.userId ?: ""
        // 获取 acToken
        val acToken = viewModel.acToken ?: MD5Utils.md5("${oldPhone}_${userId}")
        // 绑定手机号
        lifecycleScope.launch {
            viewModel.bindPhoneFlow(oldPhone, phone, code, acToken).collectSimple(onLoading = {
                // 显示加载状态，禁用按钮
                phoneLoginSubmitBtn.isEnabled = false
            }, onSuccess = { success ->
                // 绑定成功
                phoneLoginSubmitBtn.isEnabled = true
                if (success == true) {
                    // 显示绑定成功对话框（对话框确认时会返回结果）
                    showBindSuccessDialog(phone)
                } else {
                    Toast.makeText(
                        this@PhoneLoginActivity, "绑定失败", Toast.LENGTH_SHORT
                    ).show()
                }
            }, onFailure = { message ->
                // 绑定失败
                phoneLoginSubmitBtn.isEnabled = true
                Toast.makeText(
                    this@PhoneLoginActivity, message, Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    private fun handlePhoneVerify(
        phone: String,
        code: String,
    ) {
        // 验证手机号
        lifecycleScope.launch {
            viewModel.verifyPhoneFlow(phone, code).collectSimple(onLoading = {
                // 显示加载状态，禁用按钮
                phoneLoginSubmitBtn.isEnabled = false
            }, onSuccess = { response ->
                // 验证成功
                // 保存验证阶段的手机号，用于后续绑定时的 oldPhone
                verifiedPhone = phone
                mSourceFrom = Source.EDIT_CHANGE_PHONE_BIND
                updateUiBySource(mSourceFrom)
                // 清空电话和验证码输入框，让用户可以输入新的电话号码
                phoneInput.setPhoneNumber("")
                codeInput.setCode("")
                codeInput.reset()
                // 更新按钮状态（清空后按钮应该禁用）
                updateLoginButtonState(false)
                // 保存 acToken
                val acToken = response?.acToken
                if (acToken != null) {
                    viewModel.acToken = acToken
                } else {
                    Toast.makeText(this@PhoneLoginActivity, "手机验证失败", Toast.LENGTH_SHORT)
                        .show()
                }
            }, onFailure = { message ->
                // 验证失败
                phoneLoginSubmitBtn.isEnabled = true
                Toast.makeText(
                    this@PhoneLoginActivity, message, Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    private fun handlePhoneLogin(phone: String, code: String) {
        // 登录来源：调用登录接口
        lifecycleScope.launch {
            viewModel.loginViaCaptchaFlow(phone, code)
                .collectSimple(onLoading = {   // 显示加载状态，禁用登录按钮
                    phoneLoginSubmitBtn.isEnabled = false
                }, onSuccess = {
                    // 登录成功
                    phoneLoginSubmitBtn.isEnabled = true
                    // 保存 token（data 可能为 null，需要安全处理）
                    val token = it?.token
                    if (token != null) {
                        TokenManager.token = token
                        lifecycleScope.launch {
                            this@PhoneLoginActivity.TokenDataStore.updateData {
                                it.copy(token = token)
                            }
                        }
                        // 跳转到主页
                        openHomePage()
                    } else {
                        Toast.makeText(
                            this@PhoneLoginActivity, "登录失败", Toast.LENGTH_SHORT
                        ).show()
                    }

                }, onFailure = {
                    Toast.makeText(
                        this@PhoneLoginActivity, "登录失败", Toast.LENGTH_SHORT
                    ).show()
                    phoneLoginSubmitBtn.isEnabled = true
                })
        }
    }

    private fun setupAgreementText() {
        // 根据Source设置不同的隐私同意View
        val privacyView = bottomPrivacyView
        privacyView.initPrivacyText(onUserAgreementClick = {
            val userKey = resources.getString(R.string.monika_login_user_key)
        }, onPrivacyPolicyClick = {
            val privacyKey = resources.getString(R.string.monika_login_privacy_key)
        })
    }
}

