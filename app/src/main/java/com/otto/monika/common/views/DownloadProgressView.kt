package com.otto.monika.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.otto.monika.R
import com.otto.monika.common.utils.getView

/**
 * 自定义下载进度条View
 * 使用ProgressBar实现，包含灰色背景、绿色进度条和进度文字
 */
class DownloadProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // ProgressBar
    private val pbProgress: ProgressBar by getView(R.id.pb_download_progress)

    // 状态文字
    private val tvStatus: TextView by getView(R.id.tv_download_progress_status)
    

    // 百分比文字
    private val tvPercent: TextView by getView(R.id.tv_download_progress_percent)

    private var maxProgress: Int = 100
    private var currentProgress: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_download_progress_view, this, true)
        // 初始化ProgressBar
        pbProgress.max = maxProgress
        pbProgress.progress = currentProgress
    }

    /**
     * 设置最大进度值
     */
    fun setMax(max: Int) {
        maxProgress = max
        pbProgress.max = maxProgress
        updateProgress()
    }

    /**
     * 设置当前进度值（0-100）
     */
    fun setProgress(progress: Int) {
        currentProgress = progress.coerceIn(0, maxProgress)
        updateProgress()
    }

    /**
     * 设置当前进度值（0.0f-1.0f）
     */
    fun setProgress(progress: Float) {
        currentProgress = (progress.coerceIn(0f, 1f) * maxProgress).toInt()
        updateProgress()
    }

    /**
     * 设置状态文字
     */
    fun setStatusText(text: String) {
        tvStatus.text = text
    }

    /**
     * 更新进度显示
     */
    private fun updateProgress() {
        // 更新ProgressBar进度
        pbProgress.progress = currentProgress
        
        // 更新百分比文字
        tvPercent.text = "${currentProgress}%"
    }

    /**
     * 获取当前进度
     */
    fun getProgress(): Int = currentProgress

    /**
     * 获取最大进度
     */
    fun getMax(): Int = maxProgress
}

