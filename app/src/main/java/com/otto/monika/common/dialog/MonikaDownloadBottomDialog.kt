package com.otto.monika.common.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.otto.common.download.ApkDownloadManager
import com.otto.common.utils.getView
import com.otto.monika.R
import com.otto.monika.common.views.DownloadProgressView

/**
 * 下载新版本对话框
 * 位于屏幕底部，四个角都是圆角
 * 内容从上到下：标题、版本信息、下载进度条、后台下载按钮
 */
class MonikaDownloadBottomDialog : DialogFragment() {

    companion object {
        private const val ARG_VERSION = "arg_version"
        private const val ARG_DOWNLOAD_URL = "arg_download_url"
        private const val ARG_SAVE_PATH = "arg_save_path"

        /**
         * 创建下载对话框实例
         * @param version 版本号，如 "1.0.0"
         * @param downloadUrl 下载URL
         * @param savePath 保存路径（可选，默认使用外部存储）
         */
        fun newInstance(
            version: String = "1.0.0",
            downloadUrl: String? = null,
            savePath: String? = null
        ): MonikaDownloadBottomDialog {
            return MonikaDownloadBottomDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_VERSION, version)
                    putString(ARG_DOWNLOAD_URL, downloadUrl)
                    putString(ARG_SAVE_PATH, savePath)
            }
        }
    }
    }

    // 版本号文字
    private val tvVersion: TextView by getView(R.id.tv_download_dialog_version)

    // 下载进度条
    private val dpvProgress: DownloadProgressView by getView(R.id.dpv_download_progress)

    // 后台下载按钮
    private val tvBackground: TextView by getView(R.id.tv_download_dialog_background)
    private val tvCancel: TextView by getView(R.id.btn_dialog_download_cancel)
    private val tvConfirm: TextView by getView(R.id.btn_dialog_download_confirm)
    private val tvBtnContainer: LinearLayout by getView(R.id.ll_dialog_download_btn_container)

    // 下载URL
    private var downloadUrl: String? = null


    // 下载管理器（使用 object 单例，直接访问）
    private val downloadManager = ApkDownloadManager

    // 下载监听器
    private val downloadListener = object : ApkDownloadManager.DownloadListener {
        override fun onProgress(progress: Int) {
            // 更新进度条
            dpvProgress.setProgress(progress)
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

    // 后台下载点击回调
    var onBackgroundDownloadClickListener: (() -> Unit)? = null

    // 下载完成回调
    var onDownloadCompleteListener: ((String) -> Unit)? = null

    // 下载失败回调
    var onDownloadErrorListener: ((String) -> Unit)? = null


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
        return inflater.inflate(R.layout.dialog_dowload_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
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


    private fun initViews() {
        // 设置版本号
        val version = arguments?.getString(ARG_VERSION) ?: "1.0.0"
        tvVersion.text = "正在更新到最新版本$version"

        // 获取下载URL和保存路径
        downloadUrl = arguments?.getString(ARG_DOWNLOAD_URL)

        // 初始化进度条
        dpvProgress.setMax(100)
        dpvProgress.setProgress(0)
    }

    private fun setupListeners() {
        // 后台下载点击事件
        tvBackground.setOnClickListener {
            // 切换到后台模式
            downloadManager.switchToBackground(requireContext())
            // 调用回调
            onBackgroundDownloadClickListener?.invoke()
            // 关闭对话框，但下载继续
            dismiss()
        }
        tvCancel.setOnClickListener {
            // 取消下载
            downloadManager.cancelDownload(requireContext())
            dismissAllowingStateLoss()
        }
        tvConfirm.setOnClickListener {
            tvBtnContainer.isVisible = false
            tvBackground.isVisible = true
            dpvProgress.isVisible = true
            // 开始下载
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
     * 设置下载进度
     * @param progress 进度值（0-100）
     */
    fun setProgress(progress: Int) {
        dpvProgress.setProgress(progress)
    }

    /**
     * 设置下载进度
     * @param progress 进度值（0.0f-1.0f）
     */
    fun setProgress(progress: Float) {
        dpvProgress.setProgress(progress)
    }

    /**
     * 设置版本号
     */
    fun setVersion(version: String) {
        tvVersion.text = "正在更新到最新版本$version"
    }

    /**
     * 显示对话框
     */
    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, this.javaClass.simpleName)
    }
}

