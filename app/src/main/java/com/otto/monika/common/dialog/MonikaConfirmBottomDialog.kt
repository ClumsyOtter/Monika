package com.otto.monika.common.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.otto.monika.R

/**
 * 通用确认对话框 DialogFragment
 * 位于屏幕底部，四个角都是圆角
 * 内容从上到下：标题、内容、取消和确定按钮
 */
class MonikaConfirmBottomDialog : DialogFragment() {

    private lateinit var contentText: TextView
    private lateinit var cancelBtn: TextView
    private lateinit var confirmBtn: TextView

    private var content: String = ""
    private var cancelText: String? = null
    private var confirmText: String? = null

    // 回调接口
    var onCancelClickListener: (() -> Unit)? = null
    var onConfirmClickListener: (() -> Unit)? = null

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"
        private const val ARG_CANCEL_TEXT = "arg_cancel_text"
        private const val ARG_CONFIRM_TEXT = "arg_confirm_text"

        /**
         * 创建确认对话框实例
         * @param title 标题，默认为"提示"
         * @param content 内容文本
         * @param cancelText 取消按钮文字，默认为"取消"
         * @param confirmText 确定按钮文字，默认为"确定"
         */
        fun newInstance(
            content: String,
            cancelText: String? = "取消",
            confirmText: String? = "确定"
        ): MonikaConfirmBottomDialog {
            return MonikaConfirmBottomDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT, content)
                    putString(ARG_CANCEL_TEXT, cancelText)
                    putString(ARG_CONFIRM_TEXT, confirmText)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置对话框样式
        setStyle(STYLE_NO_TITLE, R.style.BottomDialog)

        arguments?.let {
            content = it.getString(ARG_CONTENT, "")
            cancelText = it.getString(ARG_CANCEL_TEXT, cancelText)
            confirmText = it.getString(ARG_CONFIRM_TEXT, confirmText)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_confirm_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框位置和宽度
        dialog?.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM

            // 获取屏幕宽度
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // 设置宽度为屏幕宽度的90%
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
        contentText = view.findViewById(R.id.tv_dialog_content)
        cancelBtn = view.findViewById(R.id.btn_dialog_cancel)
        confirmBtn = view.findViewById(R.id.btn_dialog_confirm)

        // 设置文本内容
        contentText.text = content
        cancelBtn.apply {
            isVisible = cancelText?.isNotEmpty() == true
            text = cancelText
            background = if (confirmBtn.isVisible && isVisible) {
                ResourcesCompat.getDrawable(resources, R.drawable.monika_custom_btn_gray_short, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.monika_custom_btn_empty_gray, null)
            }
        }
        confirmBtn.apply {
            text = confirmText
            isVisible = confirmText?.isNotEmpty() == true
            background = if (cancelBtn.isVisible && isVisible) {
                ResourcesCompat.getDrawable(resources, R.drawable.monika_custom_btn_black_short, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.monika_custom_btn_empty_black, null)
            }
        }
    }

    private fun setupListeners() {
        // 取消按钮点击
        cancelBtn.setOnClickListener {
            onCancelClickListener?.invoke()
            dismissAllowingStateLoss()
        }

        // 确定按钮点击
        confirmBtn.setOnClickListener {
            onConfirmClickListener?.invoke()
            dismissAllowingStateLoss()
        }
    }

    /**
     * 显示对话框
     */
    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "MonikaConfirmBottomDialog")
    }
}

