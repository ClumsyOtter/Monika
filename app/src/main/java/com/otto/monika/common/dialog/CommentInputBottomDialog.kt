package com.otto.monika.common.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.otto.monika.R
import com.otto.monika.common.utils.FirstCharacterNoSpaceFilter
import com.otto.monika.common.utils.addInputFilter


/**
 * 评论输入 Dialog
 */
class CommentInputBottomDialog(context: Context) : BaseInputBottomDialog(context) {
    private lateinit var inputEdit: EditText
    private lateinit var sendButton: TextView
    private var onSendClickListener: ((String) -> Unit)? = null

    private var inputHit: String? = null

    init {
        setCanceledOnTouchOutside(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 加载布局
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_comment_input, null)
        setContentView(view)
        // 初始化视图和监听
        initViews(view)
        setupListeners()
    }


    override fun dismiss() {
        hideKeyboard(inputEdit)
        super.dismiss()
    }

    fun setReplyHit(hit: String?) {
        if (hit != null && hit.isNotEmpty()) {
            inputHit = "回复 $hit"
        }

    }


    /**
     * 设置发送按钮点击监听
     */
    fun setOnSendClickListener(listener: (String) -> Unit) {
        this.onSendClickListener = listener
    }

    private fun initViews(view: View) {
        inputEdit = view.findViewById(R.id.et_dialog_comment_input)
        sendButton = view.findViewById(R.id.btn_dialog_send)
        inputEdit.addInputFilter(FirstCharacterNoSpaceFilter())
        inputHit?.let {
            inputEdit.setHint(it)
        }

    }

    private fun setupListeners() {
        sendButton.setOnClickListener {
            val content = inputEdit.text.toString().trim()
            if (content.isNotEmpty()) {
                onSendClickListener?.invoke(content)
                dismiss()
            }
        }

        // 设置 IME Action 监听
        inputEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val content = inputEdit.text.toString().trim()
                if (content.isNotEmpty()) {
                    onSendClickListener?.invoke(content)
                    dismiss()
                }
                true
            } else {
                false
            }
        }

        inputEdit.doAfterTextChanged { text ->
            val isEmpty = text.isNullOrBlank()  // 包括空字符串、空格、制表符等
            if (isEmpty) {
                // 输入框为空（或只有空白字符）
                sendButton.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_B3B3B3
                    )
                )
                sendButton.setBackgroundResource(R.drawable.monika_tag_gray_bg)
                sendButton.isClickable = false
            } else {
                // 输入框有内容
                sendButton.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_C4FF05
                    )
                )
                sendButton.setBackgroundResource(R.drawable.monika_tag_black_bg)
                sendButton.isClickable = true
            }
        }
        // 使用 setOnShowListener 在显示时设置键盘监听和显示键盘
        setOnShowListener {
            // 延迟显示键盘，确保 Dialog 已完全显示
            showKeyboard(inputEdit)
        }
    }
}

