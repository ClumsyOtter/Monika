package com.otto.monika.common.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.otto.monika.R
import com.otto.monika.common.utils.getView

class MonikaConfirmBottomType2Dialog : DialogFragment() {

    companion object {
        private const val ARG_ICON_1 = "arg_icon_1"
        private const val ARG_ICON_2 = "arg_icon_2"
        private const val ARG_CONTENT = "arg_content"
        private const val ARG_BUTTON_TEXT = "arg_button_text"

        /**
         * 创建对话框实例
         * @param icon1 第一个图标资源ID
         * @param icon2 第二个图标资源ID（标题图标）
         * @param content 内容文字
         * @param buttonText 按钮文字
         */
        fun newInstance(
            icon1: Int,
            icon2: Int,
            content: String,
            buttonText: String
        ): MonikaConfirmBottomType2Dialog {
            return MonikaConfirmBottomType2Dialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ICON_1, icon1)
                    putInt(ARG_ICON_2, icon2)
                    putString(ARG_CONTENT, content)
                    putString(ARG_BUTTON_TEXT, buttonText)
                }
            }
        }
    }

    // 第一个图标
    private val ivIcon1: ImageView by getView(R.id.iv_dialog_type2_icon)
    
    // 第二个图标（标题图标）
    private val ivIcon2: ImageView by getView(R.id.iv_dialog_type2_title)
    
    // 内容文字
    private val tvContent: TextView by getView(R.id.tv_dialog_type2_content)
    
    // 确定按钮
    private val btnConfirm: TextView by getView(R.id.dpv_dialog_type2_confirm)

    // 按钮点击回调
    var onConfirmClickListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置对话框样式
        setStyle(STYLE_NO_TITLE, R.style.BottomDialog)

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
        return inflater.inflate(R.layout.dialog_confirm_type2_bottom, container, false)
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
        // 设置第一个图标
        val icon1 = arguments?.getInt(ARG_ICON_1, 0) ?: 0
        if (icon1 != 0) {
            ivIcon1.setImageResource(icon1)
        }

        // 设置第二个图标（标题图标）
        val icon2 = arguments?.getInt(ARG_ICON_2, 0) ?: 0
        if (icon2 != 0) {
            ivIcon2.setImageResource(icon2)
        }

        // 设置内容文字
        val content = arguments?.getString(ARG_CONTENT) ?: ""
        tvContent.text = content

        // 设置按钮文字
        val buttonText = arguments?.getString(ARG_BUTTON_TEXT) ?: "确定"
        btnConfirm.text = buttonText
    }

    private fun setupListeners() {
        // 确定按钮点击事件
        btnConfirm.setOnClickListener {
            onConfirmClickListener?.invoke()
            dismiss()
        }
    }

    /**
     * 显示对话框
     */
    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "MonikaConfirmBottomDialogType2")
    }
}

