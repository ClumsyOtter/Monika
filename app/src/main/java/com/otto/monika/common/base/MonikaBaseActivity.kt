package com.otto.monika.common.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.otto.monika.R
import com.otto.monika.common.views.MonikaCommonActionView

abstract class MonikaBaseActivity : FragmentActivity() {

    private var contentView: View? = null
    private var loadingDialog: Dialog? = null

    open fun enableWindowInsets(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        // 获取内容容器
        val container = findViewById<FrameLayout>(R.id.sub_activity_content)

        // 获取 ActionBar（可选）
        val actionBar = findViewById<FrameLayout>(R.id.custom_bar_container)

        // 如果启用了 WindowInsets，设置 ActionBar 的 insets
        if (enableWindowInsets() && actionBar != null) {
            setupActionBarWindowInsets(actionBar)
        }
        // 将子类的 layout 添加到容器中
        contentView = LayoutInflater.from(this).inflate(getContentViewId(), container, false)
        container.addView(contentView)

        //将actionBar加入容器
        getCustomActionBarView()?.let {
            if (isActionBarVisible()) {
                actionBar?.visibility = View.VISIBLE
                actionBar?.addView(it)
            } else {
                actionBar?.visibility = View.GONE
                actionBar?.removeAllViews()
            }
        }

        // 通知子类初始化
        onFinishCreateView()
    }

    open fun getCustomActionBarView(): View? {
        return MonikaCommonActionView(this).apply {
            setTitle(getTitleText())
            onBackClickListener = {
                finish()
            }
        }
    }

    open fun getTitleText(): String {
        return ""
    }

    open fun isActionBarVisible(): Boolean {
        return true
    }


    /**
     * 设置ActionBar的WindowInsets，向下padding状态栏高度
     */
    private fun setupActionBarWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 获取状态栏高度
            val statusBarHeight = systemBars.top
            // 设置paddingTop，将ActionBar向下调整
            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )
            // 返回消耗的insets
            insets
        }
    }

    /**
     * 子类实现：返回内容视图的 layout ID
     */
    abstract fun getContentViewId(): Int

    /**
     * 子类实现：视图创建完成后的回调，在此进行初始化操作
     */
    open fun onFinishCreateView() {

    }

    /**
     * 获取内容视图
     */
    protected fun getContentView(): View? = contentView

    /**
     * 显示加载对话框
     * @param message 加载提示文字，默认为"加载中..."
     * @param cancelable 是否允许通过点击外部或返回键关闭，默认为 false
     */
    protected fun showLoadingDialog(message: String = "加载中...", cancelable: Boolean = false) {
        if (loadingDialog == null) {
            loadingDialog = Dialog(this, android.R.style.Theme_Material_Light_Dialog).apply {
                setContentView(R.layout.widget_dialog_loading)
                setCancelable(cancelable)
                window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        loadingDialog?.findViewById<TextView>(R.id.dialog_loading_title)?.text = message
        if (!isFinishing && loadingDialog?.isShowing != true) {
            loadingDialog?.show()
        }
    }

    /**
     * 隐藏加载对话框
     */
    protected fun hideLoadingDialog() {
        if (!isFinishing) {
            loadingDialog?.dismiss()
        }
        loadingDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoadingDialog()
    }

}