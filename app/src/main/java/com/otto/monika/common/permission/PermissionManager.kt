package com.otto.monika.common.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.otto.monika.common.permission.listener.CheckMultiPermissionCallBack
import com.otto.monika.common.permission.listener.CheckPermissionCallBack
import com.otto.monika.common.permission.listener.CheckSelfPermissionCallBack
import com.otto.monika.common.permission.model.Permission

/**
 * 数据通信部分
 */
object PermissionManager {
    private var listener: CheckPermissionCallBack? = null
    private var listenerMulti: CheckMultiPermissionCallBack? = null
    private var listenerCheckSelf: CheckSelfPermissionCallBack? = null
    var permissions: MutableList<String> = mutableListOf()

    /**
     * 设置申请的权限数组及监听回调
     *
     * @param listener
     * @param permission
     */
    fun applyPermissionListener(listener: CheckPermissionCallBack, vararg permission: String) {
        this.listener = listener
        permissions.addAll(permission)
    }

    /**
     * 设置申请的权限数组及监听回调
     *
     * @param listener
     * @param permission
     */
    fun applyPermissionListener(
        listener: CheckMultiPermissionCallBack,
        vararg permission: String
    ) {
        this.listenerMulti = listener
        permissions.addAll(permission)
    }

    /**
     * 设置申请的权限数组及监听回调
     *
     * @param listener
     * @param permission
     */
    fun applyPermissionListener(listener: CheckSelfPermissionCallBack, vararg permission: String) {
        this.listenerCheckSelf = listener
        permissions.addAll(permission)
    }


    fun getListener(): CheckPermissionCallBack? {
        return listener
    }

    val multiListener: CheckMultiPermissionCallBack?
        get() = listenerMulti

    val checkSelfListener: CheckSelfPermissionCallBack?
        get() = listenerCheckSelf


    /**
     * 单个权限及链式回调
     *
     * @param permission
     */
    fun granted(permission: Permission?) {
        if (getListener() != null) getListener()?.granted(permission)
    }

    fun deniedJustShow(permission: Permission?) {
        if (getListener() != null) getListener()?.deniedJustShow(permission)
    }

    fun deniedNeverShow(permission: Permission?) {
        if (getListener() != null) getListener()?.deniedNeverShow(permission)
    }


    /**
     * 多权限申请
     *
     * @param permissions
     */
    fun multiGranted(permissions: MutableList<Permission>?) {
        if (this.multiListener != null) this.multiListener?.granted(permissions)
    }

    fun multiDeniedJustShow(permissions: MutableList<Permission>?) {
        if (this.multiListener != null) this.multiListener?.deniedJustShow(permissions)
    }

    fun multiDeniedNeverShow(permissions: MutableList<Permission>?) {
        if (this.multiListener != null) this.multiListener?.deniedNeverShow(permissions)
    }

    fun multiEnd() {
        if (this.multiListener != null) this.multiListener?.end()
    }

    /**
     * 检查权限
     *
     * @param permissions
     */
    fun checkSelfGranted(permissions: Array<String?>?) {
        if (this.checkSelfListener != null) this.checkSelfListener?.granted(permissions)
    }

    fun checkSelfShouldRequest(permissions: Array<String?>?) {
        if (this.checkSelfListener != null) this.checkSelfListener?.shouldRequest(permissions)
    }

    fun clean() {
        listener = null
        listenerMulti = null
        listenerCheckSelf = null
    }


    fun checkPermission(context: Context) {
        val grantedList: MutableList<String?> = ArrayList()
        //已通过的权限
        val shouldRequest: MutableList<String?> = ArrayList()
        //需申请的权限
        val permissions = permissions
        require(!(permissions.isEmpty())) { "list is not null or size 0" }
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                shouldRequest.add(permission)
            } else {
                grantedList.add(permission)
            }
        }
        val grantedString = grantedList.toTypedArray<String?>()
        val shouldRequestString = shouldRequest.toTypedArray<String?>()
        checkSelfGranted(grantedString)
        checkSelfShouldRequest(shouldRequestString)
        clean()
    }

    /**
     * 单权限处理（可做链式调用）
     * 需要全部返回
     */
    fun applyPermission(context: Activity) {
        val noPermissions: MutableList<String> = mutableListOf()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                noPermissions.add(permission)
            } else {
                granted(Permission(permission, true))
            }
        }
        val strings = noPermissions.toTypedArray<String?>()
        if (strings.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context,
                strings,
                1001
            )
        } else {
            clean()
            return
        }
    }

    /**
     * 多权限处理，返回数组
     * 需要全部返回
     */
    var justShowList: MutableList<Permission> = mutableListOf()
    var neverShowList: MutableList<Permission> = mutableListOf()
    var grantedList: MutableList<Permission> = mutableListOf()

    fun applyMultiPermission(context: LifecycleOwner?) {
        val noPermissions: MutableList<String?> = ArrayList()
        justShowList.clear()
        neverShowList.clear()
        grantedList.clear()
        val permissions = permissions
        require(permissions.isNotEmpty()) { "list is not null or size 0" }
        if (context is FragmentActivity) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context as Context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    noPermissions.add(permission)
                } else {
                    grantedList.add(Permission(permission, true)) //多权限时在已通过的数组中添加
                }
            }
        } else if (context is Fragment) {
            if (context.activity == null) return
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context.requireContext(),
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    noPermissions.add(permission)
                } else {
                    grantedList.add(Permission(permission, true)) //多权限时在已通过的数组中添加
                }
            }
        }

        //当已通过的权限和总传入权限相等则返回结果
        if (grantedList.size == permissions.size) {
            multiGranted(grantedList)
            multiEnd()
            clean()
            return
        }

        val strings = noPermissions.toTypedArray<String?>()
        if (strings.isNotEmpty()) {
            if (context is FragmentActivity) {
                ActivityCompat.requestPermissions(
                    context,
                    strings,
                    1002
                )
            } else if (context is Fragment) {
                context.requestPermissions(
                    strings,
                    1002
                )
            }
        } else {
            clean()
        }
    }

    /**
     * 当手动时，在activity或fragment中onRequestPermissionsResult（）要手动调用方法
     *
     * @param Code
     * @param permissions
     * @param grantResults
     */
    fun onRequestPermissionsResult(
        context: Activity, Code: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (Code == 1001) {
            if (grantResults.isEmpty()) {
                clean()
                return
            }
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted(Permission(permissions[i], true))
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            context,
                            permissions[i]
                        )
                    ) {
                        deniedJustShow(Permission(permissions[i], false, true))
                    } else {
                        deniedNeverShow(Permission(permissions[i], false, false))
                    }
                }
            }
        }
        if (Code == 1002) {
            if (grantResults.isEmpty()) {
                clean()
                return
            }

            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(Permission(permissions[i], true))
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            context,
                            permissions[i]
                        )
                    ) {
                        justShowList.add(Permission(permissions[i], false, true))
                    } else {
                        neverShowList.add(Permission(permissions[i], false, false))
                    }
                }
            }
            if (grantedList.isNotEmpty()) multiGranted(grantedList)
            if (justShowList.isNotEmpty()) multiDeniedJustShow(
                justShowList
            )
            if (neverShowList.isNotEmpty()) multiDeniedNeverShow(
                neverShowList
            )
            multiEnd()
        }
        clean()
    }
}
