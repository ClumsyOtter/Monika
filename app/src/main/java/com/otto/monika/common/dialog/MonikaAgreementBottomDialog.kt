package com.otto.monika.common.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.isVisible
import com.otto.monika.R

/**
 * 通用确认对话框 Dialog
 * 位于屏幕底部，四个角都是圆角
 * 内容从上到下：标题、内容、取消和确定按钮
 * 使用 Dialog 而不是 DialogFragment 实现
 */
class MonikaAgreementBottomDialog(context: Context, themeResId: Int = R.style.BottomDialog) :
    Dialog(context, themeResId) {

    private lateinit var titleText: TextView
    private lateinit var contentText: TextView
    private lateinit var cancelBtn: TextView
    private lateinit var confirmBtn: TextView

    private var gravity: Int = Gravity.BOTTOM

    private var title: String = ""
    private var content: String = ""
    private var cancelText: String? = null
    private var confirmText: String? = null

    // 回调接口
    var onCancelClickListener: (() -> Unit)? = null
    var onConfirmClickListener: (() -> Unit)? = null

    init {
        // 设置对话框样式
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(true)
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String): MonikaAgreementBottomDialog {
        this.title = title
        return this
    }

    /**
     * 设置内容
     */
    fun setContent(content: String): MonikaAgreementBottomDialog {
        this.content = content
        return this
    }

    /**
     * 设置取消按钮文字
     */
    fun setCancelText(cancelText: String?): MonikaAgreementBottomDialog {
        this.cancelText = cancelText
        return this
    }

    /**
     * 设置确定按钮文字
     */
    fun setConfirmText(confirmText: String?): MonikaAgreementBottomDialog {
        this.confirmText = confirmText
        return this
    }

    /**
     * 设置取消按钮点击监听
     */
    fun setOnCancelClickListener(listener: (() -> Unit)?): MonikaAgreementBottomDialog {
        this.onCancelClickListener = listener
        return this
    }


    fun setGravity(gravity: Int) {
        this.gravity = gravity
    }


    /**
     * 设置确定按钮点击监听
     */
    fun setOnConfirmClickListener(listener: (() -> Unit)?): MonikaAgreementBottomDialog {
        this.onConfirmClickListener = listener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_agreement_bottom, null)
        setContentView(view)

        initViews(view)
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框位置和宽度
        window?.let { window ->
            val params = window.attributes
            params.gravity = gravity

            // 获取屏幕宽度
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // 设置宽度为屏幕宽度的95%
            params.width = (screenWidth * 0.95).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT

            // 转换20dp为像素
            val bottomMargin = (20 * displayMetrics.density).toInt()
            params.y = bottomMargin

            window.attributes = params
            // 设置背景为透明
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun initViews(view: View) {
        titleText = view.findViewById(R.id.tv_agreement_dialog_title)
        contentText = view.findViewById(R.id.tv_agreement_dialog_content)
        cancelBtn = view.findViewById(R.id.btn_agreement_dialog_cancel)
        confirmBtn = view.findViewById(R.id.btn_agreement_dialog_confirm)

        if (title.isNotEmpty()) {
            titleText.text = title
        }

        // 设置文本内容
        if (content.isNotEmpty()) {
            contentText.text = content
        }

        cancelBtn.apply {
            isVisible = cancelText?.isNotEmpty() == true
            text = cancelText
        }

        confirmBtn.apply {
            text = confirmText
            isVisible = confirmText?.isNotEmpty() == true
        }
    }

    private fun setupListeners() {
        // 取消按钮点击
        cancelBtn.setOnClickListener {
            onCancelClickListener?.invoke()
            dismiss()
        }

        // 确定按钮点击
        confirmBtn.setOnClickListener {
            onConfirmClickListener?.invoke()
            dismiss()
        }
    }

    companion object {
        /**
         * 创建确认对话框实例
         * @param context Context对象
         * @param title 标题，默认为空
         * @param content 内容文本，默认为空
         * @param cancelText 取消按钮文字，默认为"取消"
         * @param confirmText 确定按钮文字，默认为"确定"
         */
        @JvmStatic
        fun newInstance(
            context: Context,
            title: String? = null,
            content: String? = null,
            cancelText: String? = "不同意",
            confirmText: String? = "同意"
        ): MonikaAgreementBottomDialog {
            return MonikaAgreementBottomDialog(context).apply {
                this.title = title ?: ""
                this.content = content ?: ""
                this.cancelText = cancelText
                this.confirmText = confirmText
            }
        }
    }
}

