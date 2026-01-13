package com.otto.monika.login.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.otto.monika.R

/**
 * 登录选项 View
 * 包含：手机号登录、游客登录、微信登录三个按钮
 */
class LoginOptionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var phoneLoginBtn: TextView
    private var guestLoginBtn: TextView
    private var wechatLoginBtn: TextView
    private var wechatLoginContainer: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_login_options, this)
        phoneLoginBtn = findViewById(R.id.btn_login_options_phone)
        guestLoginBtn = findViewById(R.id.btn_login_options_guest)
        wechatLoginBtn = findViewById(R.id.btn_login_options_wechat)
        wechatLoginContainer = findViewById(R.id.btn_login_options_wechat_container)
    }

    /**
     * 设置手机号登录点击监听
     */
    fun setOnPhoneLoginClickListener(listener: () -> Unit) {
        phoneLoginBtn.setOnClickListener { listener() }
    }

    /**
     * 设置游客登录点击监听
     */
    fun setOnGuestLoginClickListener(listener: () -> Unit) {
        guestLoginBtn.setOnClickListener { listener() }
    }

    /**
     * 设置微信登录点击监听
     */
    fun setOnWechatLoginClickListener(listener: () -> Unit) {
        wechatLoginContainer.setOnClickListener { listener() }
    }

    /**
     * 设置手机号登录按钮是否可见
     */
    fun setPhoneLoginVisible(visible: Boolean) {
        phoneLoginBtn.visibility = if (visible) VISIBLE else GONE
    }

    /**
     * 设置游客登录按钮是否可见
     */
    fun setGuestLoginVisible(visible: Boolean) {
        guestLoginBtn.visibility = if (visible) VISIBLE else GONE
    }

    /**
     * 设置微信登录按钮是否可见
     */
    fun setWechatLoginVisible(visible: Boolean) {
        wechatLoginBtn.visibility = if (visible) VISIBLE else GONE
    }
}

