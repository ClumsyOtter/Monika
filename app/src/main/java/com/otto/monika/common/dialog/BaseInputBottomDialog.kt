package com.otto.monika.common.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.otto.monika.R

open class BaseInputBottomDialog(context: Context) : Dialog(context, R.style.BottomDialog) {
    init {
        // 设置对话框样式
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }


    override fun onStart() {
        super.onStart()
        // 设置窗口属性
        window?.let {
            // 设置窗口位置在底部
            it.setGravity(Gravity.BOTTOM)
            // 设置窗口布局参数
            val params = it.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            it.attributes = params
            // 设置软键盘模式，确保 Dialog 在键盘上方
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    /**
     * 显示键盘
     */
    fun showKeyboard(editText: EditText?, showFlags: Int = InputMethodManager.SHOW_FORCED) {
        if (editText == null) return
        val context = editText.context
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (imm != null) {
            editText.requestFocus()
            editText.postDelayed({
                imm.showSoftInput(editText, showFlags)
            }, 100)
        }
    }

    fun hideKeyboard(view: View?) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}