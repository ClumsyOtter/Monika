package com.otto.monika.login.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.otto.monika.R
import com.otto.monika.common.utils.getView

/**
 * 手机号输入框自定义View
 * 包含：左侧弧形图片 + 国家代码输入框 + 分割线 + 电话号码输入框 + 右侧弧形图片
 */
class PhoneInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val leftArcView: View by getView(R.id.left_arc_view)
    private val rightArcView: View by getView(R.id.right_arc_view)
    private val countryCodeInput: EditText by getView(R.id.country_code_input)
    private val dividerText: TextView by getView(R.id.divider_text)
    private val phoneNumberInput: EditText by getView(R.id.phone_number_input)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_phone_input, this)
        setupFocusListeners()
    }

    /**
     * 设置焦点监听，切换弧形图片颜色
     */
    private fun setupFocusListeners() {
        // 监听国家代码输入框焦点
        countryCodeInput.setOnFocusChangeListener { _, hasFocus ->
            updateArcViews(hasFocus || phoneNumberInput.hasFocus())
        }

        // 监听电话号码输入框焦点
        phoneNumberInput.setOnFocusChangeListener { _, hasFocus ->
            updateArcViews(hasFocus || countryCodeInput.hasFocus())
        }

        // 监听电话号码输入框文本变化
        phoneNumberInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 当有输入时，如果输入框有焦点，保持黑色
                val hasFocus = phoneNumberInput.hasFocus() || countryCodeInput.hasFocus()
                updateArcViews(hasFocus)
            }
        })

    }

    /**
     * 更新弧形图片颜色
     * @param isActive true表示激活状态（黑色），false表示未激活状态（灰色）
     */
    private fun updateArcViews(isActive: Boolean) {
        if (isActive) {
            // 激活状态：黑色
            leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_selected)
            rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_selected)
        } else {
            // 未激活状态：灰色
            leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_unselected)
            rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_unselected)
        }
    }

    /**
     * 获取国家代码
     */
    fun getCountryCode(): String {
        return countryCodeInput.text.toString().trim()
    }

    /**
     * 设置国家代码
     */
    fun setCountryCode(code: String) {
        countryCodeInput.setText(code)
    }

    /**
     * 获取电话号码
     */
    fun getPhoneNumber(): String {
        return phoneNumberInput.text.toString().trim()
    }

    /**
     * 设置电话号码
     */
    fun setPhoneNumber(phone: String) {
        phoneNumberInput.setText(phone)
        phoneNumberInput.setSelection(phone.length)
        updateArcViews(true)
    }

    /**
     * 获取完整手机号（国家代码 + 电话号码）
     */
    fun getFullPhoneNumber(): String {
        val countryCode = getCountryCode()
        val phoneNumber = getPhoneNumber()
        return if (countryCode.startsWith("+")) {
            "$countryCode$phoneNumber"
        } else {
            "+$countryCode$phoneNumber"
        }
    }

    /**
     * 设置电话号码输入框文本变化监听
     */
    fun setPhoneNumberTextWatcher(watcher: TextWatcher) {
        phoneNumberInput.addTextChangedListener(watcher)
    }

    /**
     * 设置国家代码输入框文本变化监听
     */
    fun setCountryCodeTextWatcher(watcher: TextWatcher) {
        countryCodeInput.addTextChangedListener(watcher)
    }

    /**
     * 设置电话号码输入框提示文字
     */
    fun setPhoneNumberHint(hint: String) {
        phoneNumberInput.hint = hint
    }

    /**
     * 设置分割线文字
     */
    fun setDividerText(text: String) {
        dividerText.text = text
    }
}

