package com.otto.monika.common.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.otto.monika.R
import com.otto.monika.home.fragment.mine.listener.TabSelectListener
import org.greenrobot.eventbus.EventBus

abstract class MonikaBaseFragment : Fragment(), TabSelectListener {
    var rootView: View? = null
        private set

    //only use when fragment in viewpager
    var isFragmentSelected: Boolean = true

    private var loadingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this.rootView == null) {
            this.rootView = inflateView(inflater, container, savedInstanceState)
        }
        return this.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onFinishCreateView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (registerBus()) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (registerBus()) {
            EventBus.getDefault().unregister(this)
        }
    }

    open fun registerBus(): Boolean {
        return false
    }

    fun inflateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getContentViewId(), container, false)
    }

    abstract fun onFinishCreateView()

    open fun getContentViewId(): Int {
        return 0
    }


    fun <T : View?> findViewById(@IdRes id: Int): T? {
        if (id == View.NO_ID || this.rootView == null) {
            return null
        }
        return rootView!!.findViewById<T?>(id)
    }

    override fun onFragmentSelected(isSelected: Boolean) {
        isFragmentSelected = isSelected
    }

    /**
     * 显示加载对话框
     * @param message 加载提示文字，默认为"加载中..."
     * @param cancelable 是否允许通过点击外部或返回键关闭，默认为 false
     */
    protected fun showLoadingDialog(message: String = "加载中...", cancelable: Boolean = false) {
        if (loadingDialog == null) {
            loadingDialog =
                Dialog(requireActivity(), android.R.style.Theme_Material_Light_Dialog).apply {
                    setContentView(R.layout.widget_dialog_loading)
                    setCancelable(cancelable)
                    window?.setLayout(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
        }
        loadingDialog?.findViewById<TextView>(R.id.dialog_loading_title)?.text = message
        if (loadingDialog?.isShowing != true) {
            loadingDialog?.show()
        }
    }

    /**
     * 隐藏加载对话框
     */
    protected fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

}
