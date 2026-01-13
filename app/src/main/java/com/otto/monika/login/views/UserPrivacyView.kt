package com.otto.monika.login.views

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.otto.monika.R
import com.otto.monika.common.utils.getView

/**
 * 用户协议同意选项View
 * 包含一个CheckBox和可点击的协议文本
 */
class UserPrivacyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val agreementCheckbox: ImageView by getView(R.id.agreement_checkbox)
    private val agreementText: TextView by getView(R.id.agreement_text)

    private var isChecked: Boolean = false
    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_user_privacy, this)
        // 设置ImageView点击事件，切换选中状态
        agreementCheckbox.setOnClickListener {
            setChecked(!isChecked)
        }
        // 设置整个容器可点击，点击时切换checkbox状态
        setOnClickListener {
            setChecked(!isChecked)
        }
        // 初始化选中状态
        updateCheckboxImage()
    }


    /**
     * 初始化协议文本
     * @param onUserAgreementClick 用户协议点击回调
     * @param onPrivacyPolicyClick 隐私政策点击回调
     */
    fun initPrivacyText(
        onUserAgreementClick: () -> Unit,
        onPrivacyPolicyClick: () -> Unit
    ) {
        val fullText = resources.getString(R.string.monika_login_privacy)
        val userKey = resources.getString(R.string.monika_login_user_key)
        val privacyKey = resources.getString(R.string.monika_login_privacy_key)
        val spannableString = SpannableString(fullText)
        // 处理用户协议链接
        dealPrivacyUrl(userKey, fullText, spannableString, onUserAgreementClick)
        // 处理隐私政策链接
        dealPrivacyUrl(privacyKey, fullText, spannableString, onPrivacyPolicyClick)
        agreementText.text = spannableString
        agreementText.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * 处理协议URL点击
     */
    private fun dealPrivacyUrl(
        key: String,
        fullText: String,
        spannableString: SpannableString,
        onClick: (() -> Unit)
    ) {
        val startIndex = fullText.indexOf(key)
        val endIndex = startIndex + key.length

        if (startIndex >= 0) {
            // 设置文字颜色为黑色
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // 设置点击事件
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onClick.invoke()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = ContextCompat.getColor(context, R.color.black)
                        ds.isUnderlineText = false // 不显示下划线
                    }
                },
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    /**
     * 获取checkbox是否选中
     */
    fun isChecked(): Boolean {
        return isChecked
    }

    /**
     * 设置checkbox选中状态
     */
    fun setChecked(checked: Boolean) {
        if (isChecked != checked) {
            isChecked = checked
            updateCheckboxImage()
            onCheckedChangeListener?.invoke(isChecked)
        }
    }

    /**
     * 更新checkbox图片
     */
    private fun updateCheckboxImage() {
        if (isChecked) {
            agreementCheckbox.setImageResource(R.drawable.monika_bottom_icon_selected)
        } else {
            agreementCheckbox.setImageResource(R.drawable.monika_bottom_icon_unselected)
        }
    }

    /**
     * 设置checkbox选中状态变化监听
     */
    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }
}

