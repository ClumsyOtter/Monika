package com.otto.monika.login.views

import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.otto.common.utils.getView
import com.otto.monika.R

/**
 * 验证码输入框自定义View
 * 包含：左侧弧形图片 + 验证码文字 + *A* + 发送验证码按钮 + 右侧弧形图片
 * 底部：验证码输入框（点击发送后显示）
 */
class CodeInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val leftArcView: View by getView(R.id.left_arc_view)
    private val rightArcView: View by getView(R.id.right_arc_view)
    private val codeLabelText: TextView by getView(R.id.code_label_text)
    private val asteriskText: TextView by getView(R.id.asterisk_text)
    private val sendCodeBtn: TextView by getView(R.id.send_code_btn)
    private val codeInput: EditText by getView(R.id.verify_code_input)

    private var countDownTimer: CountDownTimer? = null
    private var onSendCodeClickListener: (() -> Unit)? = null
    private var isPhoneValid: Boolean = false
    private var lastPhoneNumber: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.view_code_input, this)
        setupClickListeners()
        setupFocusListeners()
        setupCodeInputWatcher()
    }

    /**
     * 设置点击监听
     */
    private fun setupClickListeners() {
        sendCodeBtn.setOnClickListener {
            if (isPhoneValid) {
                // 先调用外部监听器（发送验证码接口），成功后再开始倒计时
                onSendCodeClickListener?.invoke()
            }
        }
    }

    /**
     * 设置焦点监听，切换输入框弧形图片颜色
     */
    private fun setupFocusListeners() {
        codeInput.setOnFocusChangeListener { _, hasFocus ->
            updateArcViews(hasFocus)
        }
    }

    /**
     * 设置验证码输入框文本变化监听
     * 当用户手动删除所有验证码时，显示发送验证码按钮
     */
    private fun setupCodeInputWatcher() {
        codeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim() ?: ""
                // 如果输入框为空且倒计时已结束，显示发送验证码按钮
                if (text.isEmpty() && countDownTimer == null) {
                    sendCodeBtn.visibility = VISIBLE
                    codeInput.visibility = GONE
                    codeInput.clearFocus()
                }
            }
        })
    }

    /**
     * 更新弧形图片颜色（根据输入框焦点状态）
     */
    private fun updateArcViews(hasFocus: Boolean) {
        if (hasFocus) {
            // 有焦点：黑色
            leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_selected)
            rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_selected)
        } else {
            // 无焦点：根据手机号验证状态决定
            if (isPhoneValid) {
                leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_selected)
                rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_selected)
            } else {
                leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_unselected)
                rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_unselected)
            }
        }
    }

    /**
     * 设置手机号是否有效
     * @param isValid true表示手机号符合规则
     * @param phoneNumber 当前手机号（用于检测手机号是否变化）
     */
    fun setPhoneValid(isValid: Boolean, phoneNumber: String = "") {
        // 检测手机号是否变化
        val phoneChanged = phoneNumber.isNotEmpty() && phoneNumber != lastPhoneNumber

        // 如果手机号变化且新号码有效，且正在倒计时，则重置
        if (phoneChanged && isValid && isCountDownRunning()) {
            reset()
        }

        // 更新手机号记录
        if (phoneNumber.isNotEmpty()) {
            lastPhoneNumber = phoneNumber
        }

        isPhoneValid = isValid
        updateSendCodeButtonState()
        updateArcViewsState()
    }

    /**
     * 检查是否正在倒计时
     */
    fun isCountDownRunning(): Boolean {
        return countDownTimer != null && codeInput.visibility == VISIBLE
    }

    /**
     * 更新发送验证码按钮状态
     */
    private fun updateSendCodeButtonState() {
        if (isPhoneValid) {
            // 有效：蓝色
            sendCodeBtn.setTextColor(ContextCompat.getColor(context, R.color.text_c4ff05))
        } else {
            // 无效：灰色
            sendCodeBtn.setTextColor(ContextCompat.getColor(context, R.color.text_808080))
        }
    }

    /**
     * 更新弧形图片状态（根据手机号验证状态）
     */
    private fun updateArcViewsState() {
        // 如果输入框有焦点，优先显示黑色
        if (codeInput.hasFocus()) {
            leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_selected)
            rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_selected)
        } else {
            // 根据手机号验证状态决定
            if (isPhoneValid) {
                leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_selected)
                rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_selected)
            } else {
                leftArcView.setBackgroundResource(R.drawable.monika_login_huohao_left_unselected)
                rightArcView.setBackgroundResource(R.drawable.monika_login_huohao_right_unselected)
            }
        }
    }

    /**
     * 开始倒计时（公开方法，供外部调用）
     */
    fun startCountDown() {
        // 隐藏发送按钮
        sendCodeBtn.visibility = GONE

        // 显示输入框
        codeInput.visibility = VISIBLE
        codeInput.requestFocus()

        // 取消之前的倒计时
        countDownTimer?.cancel()

        // 创建新的倒计时
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                codeInput.hint = "${seconds}秒后重发"
            }

            override fun onFinish() {
                // 倒计时结束，取消倒计时引用
                countDownTimer = null

                // 如果输入框中有内容，不清空，只更新hint
                val hasContent = codeInput.text.toString().trim().isNotEmpty()
                if (hasContent) {
                    // 有内容：保持输入框显示，只更新hint
                    codeInput.hint = "请输入验证码"
                } else {
                    // 无内容：显示发送按钮，隐藏输入框
                    sendCodeBtn.visibility = VISIBLE
                    codeInput.visibility = GONE
                    codeInput.hint = "请输入验证码"
                    codeInput.clearFocus()
                }
            }
        }
        countDownTimer?.start()
    }

    /**
     * 设置发送验证码点击监听
     */
    fun setOnSendCodeClickListener(listener: () -> Unit) {
        onSendCodeClickListener = listener
    }

    /**
     * 获取验证码
     */
    fun getCode(): String {
        return codeInput.text.toString().trim()
    }

    /**
     * 设置验证码
     */
    fun setCode(code: String) {
        codeInput.setText(code)
    }

    /**
     * 设置验证码输入框文本变化监听
     */
    fun setCodeTextWatcher(watcher: TextWatcher) {
        codeInput.addTextChangedListener(watcher)
    }

    /**
     * 设置发送验证码按钮是否可用
     * @param enabled true表示可用，false表示禁用
     */
    fun setSendCodeEnabled(enabled: Boolean) {
        sendCodeBtn.isEnabled = enabled
        if (enabled) {
            updateSendCodeButtonState()
        } else {
            // 禁用状态：灰色
            sendCodeBtn.setTextColor(ContextCompat.getColor(context, R.color.text_808080))
        }
    }

    /**
     * 重置验证码输入框（取消倒计时，恢复初始状态）
     */
    fun reset() {
        countDownTimer?.cancel()
        countDownTimer = null
        sendCodeBtn.visibility = VISIBLE
        codeInput.visibility = GONE
        codeInput.hint = "请输入验证码"
        codeInput.setText("")
        codeInput.clearFocus()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清理倒计时，避免内存泄漏
        countDownTimer?.cancel()
        countDownTimer = null
    }
}