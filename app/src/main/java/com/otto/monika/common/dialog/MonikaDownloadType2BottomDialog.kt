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
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.otto.monika.R
import com.otto.monika.common.download.ApkDownloadManager
import com.otto.monika.common.utils.getView
import com.otto.monika.common.views.DownloadProgressView

class MonikaDownloadType2BottomDialog : DialogFragment() {

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
        ): MonikaDownloadType2BottomDialog {
            return MonikaDownloadType2BottomDialog().apply {
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
    private val ivIcon1: ImageView by getView(R.id.iv_dialog_download_type2_icon)

    // 第二个图标（标题图标）
    private val ivIcon2: ImageView by getView(R.id.iv_dialog_download_type2_title)

    // 内容文字
    private val tvContent: TextView by getView(R.id.tv_dialog_download_type2_content)

    // 确定按钮
    private val btnConfirm: TextView by getView(R.id.dpv_dialog_download_type2_confirm)

    private val downloadProgress: DownloadProgressView by getView(R.id.dpv_dialog_download_type2_progress)

    // 下载完成回调
    var onDownloadCompleteListener: ((String) -> Unit)? = null

    // 下载失败回调
    var onDownloadErrorListener: ((String) -> Unit)? = null

    // 下载管理器（使用 object 单例，直接访问）
    private val downloadManager = ApkDownloadManager

    // 下载URL
    private var downloadUrl: String? = null


    // 下载监听器
    private val downloadListener = object : ApkDownloadManager.DownloadListener {
        override fun onProgress(progress: Int) {
            // 更新进度条
            downloadProgress.setProgress(progress)
        }

        override fun onComplete(filePath: String) {
            // 下载完成
            onDownloadComplete(filePath)
        }

        override fun onError(error: String) {
            // 下载失败
            onDownloadError(error)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置对话框样式
        setStyle(STYLE_NO_TITLE, R.style.BottomDialog)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_download_type2_bottom, container, false)
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
            downloadProgress.isVisible = true
            btnConfirm.isVisible = false
            //start Download
            startDownload()
        }
    }


    /**
     * 开始下载
     */
    private fun startDownload() {
        val context = requireContext()
        val url = downloadUrl
        if (url.isNullOrEmpty()) {
            onDownloadError("下载URL为空")
            return
        }
        val filePath = downloadManager.getDefaultSavePath(context)
        // 开始下载（前台模式）
        downloadManager.startDownload(
            context = context,
            url = url,
            savePath = filePath,
            listener = downloadListener,
            backgroundMode = false
        )
    }


    /**
     * 下载完成
     */
    private fun onDownloadComplete(filePath: String) {
        // 调用回调
        onDownloadCompleteListener?.invoke(filePath)
        
        // 自动安装APK（使用 Activity Context）
        activity?.let {
            downloadManager.installApk(it, filePath)
        }
        // 关闭对话框
        dismiss()
    }

    /**
     * 下载失败
     */
    private fun onDownloadError(error: String) {
        onDownloadErrorListener?.invoke(error)
        // 下载失败后可以显示错误提示
    }

    /**
     * 显示对话框
     */
    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "MonikaConfirmBottomDialogType2")
    }
}

