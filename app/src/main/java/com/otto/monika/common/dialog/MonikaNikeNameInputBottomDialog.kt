package com.otto.monika.common.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import com.otto.monika.R
import com.otto.monika.common.ext.disableButton
import com.otto.monika.common.ext.enableButton
import com.otto.monika.common.views.MonikaCustomButton

class MonikaNikeNameInputBottomDialog(context: Context) : BaseInputBottomDialog(context) {
    private lateinit var closeBtn: ImageView
    private lateinit var nikeNameInput: EditText
    private lateinit var confirmBtn: MonikaCustomButton

    var nikeName: String? = null

    private var onConfirmClickListener: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_nike_name_input, null)
        setContentView(view)
        initViews(view)
        setupListeners()
    }

    override fun dismiss() {
        hideKeyboard(nikeNameInput)
        super.dismiss()
    }

    /**
     * 设置按钮点击监听
     */
    fun setOnConfirmClickListener(listener: (String) -> Unit) {
        this.onConfirmClickListener = listener
    }

    private fun initViews(view: View) {
        closeBtn = view.findViewById(R.id.iv_dialog_nike_name_close)
        nikeNameInput = view.findViewById(R.id.et_dialog_nike_name_input)
        confirmBtn = view.findViewById(R.id.cycb_dialog_nike_name_confirm)
        nikeNameInput.setText(nikeName)
        nikeNameInput.setSelection(nikeName?.length ?: 0)
    }

    private fun setupListeners() {
        confirmBtn.setOnClickListener {
            val content = nikeNameInput.text.toString().trim()
            if (content.isNotEmpty()) {
                onConfirmClickListener?.invoke(content)
                dismiss()
            }
        }
        nikeNameInput.doAfterTextChanged {
            if (it?.toString()?.isEmpty() == true) {
                confirmBtn.disableButton()
            } else {
                confirmBtn.enableButton()
            }
        }

        // 设置 IME Action 监听
        nikeNameInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val content = nikeNameInput.text.toString().trim()
                if (content.isNotEmpty()) {
                    onConfirmClickListener?.invoke(content)
                    dismiss()
                }
                true
            } else {
                false
            }
        }
        closeBtn.setOnClickListener {
            dismiss()
        }

        // 使用 setOnShowListener 在显示时设置键盘监听和显示键盘
        setOnShowListener {
            // 延迟显示键盘，确保 Dialog 已完全显示
            showKeyboard(nikeNameInput)
        }
    }
}