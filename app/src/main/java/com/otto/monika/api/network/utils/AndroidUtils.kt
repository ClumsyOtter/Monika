package com.otto.monika.api.network.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Looper
import android.text.TextUtils
import android.util.Base64
import android.util.Pair
import android.view.WindowManager
import java.io.File
import java.util.Random

/**
 * Android工具类
 * 提供系统相关工具方法
 */
object AndroidUtils {

    val uniqueID: String
        /**
         * 获取一个唯一Id
         *
         * @return String
         */
        get() {
            val t1 = (System.currentTimeMillis() / 1000L).toInt()
            val t2 = System.nanoTime().toInt()
            val t3 = (Random()).nextInt()
            val t4 = (Random()).nextInt()
            val b1 = getBytes(t1)
            val b2 = getBytes(t2)
            val b3 = getBytes(t3)
            val b4 = getBytes(t4)
            val bUniqueID = ByteArray(16)
            System.arraycopy(b1, 0, bUniqueID, 0, 4)
            System.arraycopy(b2, 0, bUniqueID, 4, 4)
            System.arraycopy(b3, 0, bUniqueID, 8, 4)
            System.arraycopy(b4, 0, bUniqueID, 12, 4)
            return Base64.encodeToString(
                bUniqueID,
                Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
            )
        }

    private fun getBytes(i: Int): ByteArray {
        val bInt = ByteArray(4)
        bInt[3] = (i % 256).toByte()
        var value = i shr 8
        bInt[2] = (value % 256).toByte()
        value = value shr 8
        bInt[1] = (value % 256).toByte()
        value = value shr 8
        bInt[0] = (value % 256).toByte()
        return bInt
    }

    /**
     * 通过包名查看app是否安装
     * [Build.VERSION_CODES.R]及以上需要注意Query适配问题
     *
     * @param packageName 目标应用的包名
     * @return -true: 应用以安装; -otherwise: 应用未安装
     */
    fun checkAppExist(context: Context?, packageName: String?): Boolean {
        if (context == null || TextUtils.isEmpty(packageName)) return false
        try {
            val pm = context.packageManager
            pm.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 打开对应包名的App
     *
     * @param packageName 目标应用的包名
     */
    fun launchApp(mContext: Context?, packageName: String?) {
        if (mContext != null && !TextUtils.isEmpty(packageName)) {
            val launchIntent = mContext.packageManager.getLaunchIntentForPackage(packageName!!)
            if (launchIntent != null) {
                mContext.startActivity(launchIntent)
            }
        }
    }


    private val kSystemRootStateUnknown = -1
    private const val kSystemRootStateDisable = 0
    private const val kSystemRootStateEnable = 1
    private var systemRootState = kSystemRootStateUnknown

    val isAndroidRoot: Boolean
        /**
         * 判断设备已root
         *
         * @return -true: 已root; -otherwise: 未root
         */
        get() {
            if (systemRootState == kSystemRootStateEnable) {
                return true
            } else if (systemRootState == kSystemRootStateDisable) {
                return false
            }

            var f: File?
            val kSuSearchPaths = arrayOf<String?>(
                "/system/bin/",
                "/system/xbin/",
                "/system/sbin/",
                "/sbin/",
                "/vendor/bin/"
            )
            try {
                for (kSuSearchPath in kSuSearchPaths) {
                    f = File(kSuSearchPath + "su")
                    if (f.exists()) {
                        systemRootState = kSystemRootStateEnable
                        return true
                    }
                }
            } catch (ignored: Exception) {
            }

            try {
                //root过的android手机都会有superuser这个apk
                val file = File("/system/app/Superuser.apk")
                if (file.exists()) {
                    systemRootState = kSystemRootStateEnable
                    return true
                }
            } catch (ignored: Exception) {
            }

            systemRootState = kSystemRootStateDisable
            return false
        }


    /**
     * 获取软件名称
     */
    fun getApp(context: Context?): String {
        var appName: String = getAppNameFromMetaData(context)
        if (TextUtils.isEmpty(appName)) {
            appName = "Other"
        }
        return appName
    }

    fun getAppNameFromMetaData(context: Context?): String {
        return getAppMetaData(context, "application_identity", "")
    }

    /**
     * 获取app version name
     *
     * @return VersionName, 如果获取不到则会返回空字符串
     */
    fun getAppVersionName(context: Context?): String? {
        try {
            val pkg = context?.packageManager?.getPackageInfo(context.packageName, 0)
            return pkg?.versionName
        } catch (e: java.lang.Exception) {
            return ""
        }
    }


    /**
     * 获取Manifests MetaData 字段
     *
     * @param key    MetaData的key
     * @param defVal 默认值
     * @return key对应的value, 如果获取不到则返回 defVal
     */
    fun getAppMetaData(context: Context?, key: String?, defVal: String): String {
        if (context == null) {
            return defVal
        }
        try {
            val appi = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = appi.metaData
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key)!!
            }
        } catch (ignore: Exception) {
        }
        return defVal
    }

    /**
     * 获取屏幕分辨率字符串
     */
    fun getDeviceResolution(context: Context): String {
        val deviceHWPixels = getDeviceWHPixels(context)
        return deviceHWPixels.second.toString() + "x" + deviceHWPixels.first
    }

    /**
     * 获取屏幕分辨率
     *
     * @return 屏幕分辨率 [Pair.first]为宽度, [Pair.second]为高度
     */
    fun getDeviceWHPixels(context: Context): Pair<Int?, Int?> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            return Pair<Int?, Int?>(bounds.width(), bounds.height())
        } else {
            val point = Point()
            windowManager.getDefaultDisplay().getRealSize(point)
            return Pair<Int?, Int?>(point.x, point.y)
        }
    }


    /**
     * 获取屏幕宽度
     *
     * @return 屏幕宽度(像素)
     */
    fun getDisplayWidth(context: Context): Int {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return manager.getCurrentWindowMetrics().getBounds().width()
        } else {
            val point = Point()
            manager.getDefaultDisplay().getRealSize(point)
            return point.x
        }
    }

    /**
     * 获取屏幕高度
     *
     * @return 屏幕高度(像素)
     */
    fun getDisplayHeight(context: Context): Int {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return manager.currentWindowMetrics.bounds.height()
        } else {
            val point = Point()
            manager.getDefaultDisplay().getRealSize(point)
            return point.y
        }
    }

    val isMainThread: Boolean
        /**
         * 判断当前线程是否为主线程
         *
         * @return -true: 当前线程是主线程; -false: 当前线程不是主线程
         */
        get() = Looper.getMainLooper().thread === Thread.currentThread()
}