package com.otto.common.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.otto.common.R
import java.io.File

/**
 * APK下载管理器
 * 支持后台下载和通知栏显示
 * 使用 object 关键字实现单例模式，线程安全且延迟初始化
 */
object ApkDownloadManager {
    private const val NOTIFICATION_ID_DOWNLOAD = 1001
    private const val NOTIFICATION_ID_COMPLETE = 1002

    private const val CHANNEL_ID_DOWNLOAD = "DownloadChannel"

    private val handler = Handler(Looper.getMainLooper())
    private var currentTask: BaseDownloadTask? = null
    private var downloadListener: DownloadListener? = null
    private var isBackgroundMode = false

    /**
     * 下载监听器
     */
    interface DownloadListener {
        /**
         * 下载进度更新
         * @param progress 进度（0-100）
         */
        fun onProgress(progress: Int)

        /**
         * 下载完成
         * @param filePath 文件路径
         */
        fun onComplete(filePath: String)

        /**
         * 下载失败
         * @param error 错误信息
         */
        fun onError(error: String)
    }

    /**
     * 开始下载
     * @param context 上下文
     * @param url 下载URL
     * @param savePath 保存路径
     * @param listener 下载监听器（可选，用于前台更新UI）
     * @param backgroundMode 是否为后台模式
     */
    fun startDownload(
        context: Context,
        url: String,
        savePath: String,
        listener: DownloadListener? = null,
        backgroundMode: Boolean = false
    ) {
        // 如果已有下载任务，先取消
        cancelDownload(context)
        this.downloadListener = listener
        this.isBackgroundMode = backgroundMode

        val file = File(savePath)
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        // 显示下载开始通知
        if (backgroundMode) {
            showDownloadNotification(context, 0, "开始下载...")
        }
        currentTask = FileDownloader.getImpl().create(url)
            .setPath(savePath)
            .setListener(object : FileDownloadListener() {
                override fun pending(
                    task: BaseDownloadTask?,
                    soFarBytes: Int,
                    totalBytes: Int
                ) {
                    // 下载准备中
                }

                override fun progress(
                    task: BaseDownloadTask?,
                    soFarBytes: Int,
                    totalBytes: Int
                ) {
                    if (totalBytes > 0) {
                        val progress = (soFarBytes * 100 / totalBytes).coerceIn(0, 100)

                        // 更新UI（如果在前台）
                        handler.post {
                            downloadListener?.onProgress(progress)
                        }

                        // 更新通知栏（如果在后台模式）
                        if (isBackgroundMode) {
                            val progressText = "下载中 $progress%"
                            showDownloadNotification(context, progress, progressText)
                        }
                    }
                }

                override fun completed(task: BaseDownloadTask?) {
                    handler.post {
                        // 通知UI（如果在前台）
                        downloadListener?.onComplete(savePath)
                        // 总是显示完成通知（无论前台还是后台）
                        showDownloadCompleteNotification(context, savePath)
                        cleanup()
                    }
                }

                override fun paused(
                    task: BaseDownloadTask?,
                    soFarBytes: Int,
                    totalBytes: Int
                ) {
                    // 下载暂停
                }

                override fun error(
                    task: BaseDownloadTask?,
                    e: Throwable?
                ) {
                    handler.post {
                        val errorMsg = e?.message ?: "下载失败"
                        // 通知UI（如果在前台）
                        downloadListener?.onError(errorMsg)

                        // 总是显示失败通知（无论前台还是后台）
                        showDownloadErrorNotification(context, errorMsg)

                        cleanup()
                    }
                }

                override fun warn(task: BaseDownloadTask?) {
                    // 警告，通常可以忽略
                }
            })
        currentTask?.start()
    }

    /**
     * 切换到后台模式
     */
    fun switchToBackground(context: Context) {
        isBackgroundMode = true
        showDownloadNotification(context, getCurrentProgress(), "后台下载中...")
    }

    /**
     * 取消下载
     */
    fun cancelDownload(context: Context) {
        currentTask?.let { task ->
            if (task.isUsing) {
                task.pause()
            }
        }
        handler.post {
            downloadListener?.onError("下载已取消")
            cleanup()
        }

        // 取消通知
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOAD)
    }

    /**
     * 暂停下载
     */
    fun pauseDownload() {
        currentTask?.let { task ->
            if (task.isUsing) {
                task.pause()
            }
        }
    }

    /**
     * 继续下载
     */
    fun resumeDownload() {
        currentTask?.let { task ->
            if (task.isUsing) {
                task.start()
            }
        }
    }

    /**
     * 显示下载进度通知
     */
    private fun showDownloadNotification(context: Context, progress: Int, progressText: String) {
        val channelId = getDownloadChannelId(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("正在下载应用更新")
            .setContentText(progressText)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID_DOWNLOAD, notification)
    }

    fun getDownloadChannelId(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getNotificationManager(context)
            if (manager.getNotificationChannel(CHANNEL_ID_DOWNLOAD) == null) {
                val name = "下载"
                val channel = NotificationChannel(
                    CHANNEL_ID_DOWNLOAD,
                    name,
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(channel)
            }
        }
        return CHANNEL_ID_DOWNLOAD
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * 显示下载完成通知
     */
    private fun showDownloadCompleteNotification(context: Context, filePath: String) {
        val channelId = getDownloadChannelId(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建安装Intent
        val file = File(filePath)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

            val uri =
                // Android 7.0+ 使用 FileProvider
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

            setDataAndType(uri, "application/vnd.android.package-archive")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("下载完成")
            .setContentText("点击安装应用更新")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_COMPLETE, notification)

        // 取消下载进度通知
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOAD)
    }

    /**
     * 显示下载失败通知
     */
    private fun showDownloadErrorNotification(context: Context, error: String) {
        val channelId = getDownloadChannelId(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("下载失败")
            .setContentText(error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID_COMPLETE, notification)

        // 取消下载进度通知
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOAD)
    }

    /**
     * 获取当前下载进度
     */
    private fun getCurrentProgress(): Int {
        // 这里可以从任务中获取当前进度
        return 0
    }

    /**
     * 清理资源
     */
    private fun cleanup() {
        currentTask = null
        downloadListener = null
    }

    /**
     * 获取当前下载任务
     */
    fun getCurrentTask(): BaseDownloadTask? = currentTask

    /**
     * 是否正在下载
     */
    fun isDownloading(): Boolean {
        return currentTask?.isUsing() == true
    }

    /**
     * 获取默认保存路径
     */
    fun getDefaultSavePath(context: Context): String {
        val externalDir = context.getExternalFilesDir(null)
        val downloadDir = if (externalDir != null) {
            File(externalDir, "downloads")
        } else {
            File(context.filesDir, "downloads")
        }
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        return File(downloadDir, "app_update.apk").absolutePath
    }

    /**
     * 安装APK
     * @param context 上下文（必须是Activity Context，不能是Application Context）
     * @param filePath APK文件路径
     */
    fun installApk(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            return
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

            val uri =
                // Android 7.0+ 使用 FileProvider
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

            setDataAndType(uri, "application/vnd.android.package-archive")
        }

        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

