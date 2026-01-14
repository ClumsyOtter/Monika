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
    var checkPermissionCallBack: CheckPermissionCallBack? = null
    var multiPermissionCallBack: CheckMultiPermissionCallBack? = null
    var selfPermissionCallBack: CheckSelfPermissionCallBack? = null
    var permissions: MutableList<String> = mutableListOf()

    fun applyPermissions(vararg permission: String) {
        permissions.addAll(permission)
    }

    fun applyPermissions(permissions: MutableList<String>) {
        this.permissions.addAll(permissions)
    }


    /**
     * 单个权限及链式回调
     *
     * @param permission
     */
    fun granted(permission: Permission?) {
        checkPermissionCallBack?.granted(permission)
    }

    fun deniedJustShow(permission: Permission?) {
        checkPermissionCallBack?.deniedJustShow(permission)
    }

    fun deniedNeverShow(permission: Permission?) {
        checkPermissionCallBack?.deniedNeverShow(permission)
    }


    /**
     * 多权限申请
     *
     * @param permissions
     */
    fun multiGranted(permissions: MutableList<Permission>) {
        this.multiPermissionCallBack?.granted(permissions)
    }

    fun multiDeniedJustShow(permissions: MutableList<Permission>) {
        multiPermissionCallBack?.deniedJustShow(permissions)
    }

    fun multiDeniedNeverShow(permissions: MutableList<Permission>) {
        multiPermissionCallBack?.deniedNeverShow(permissions)
    }

    fun multiEnd() {
        multiPermissionCallBack?.end()
    }

    /**
     * 检查权限
     *
     * @param permissions
     */
    fun checkSelfGranted(permissions: MutableList<String>) {
        selfPermissionCallBack?.granted(permissions)
    }

    fun checkSelfShouldRequest(permissions: MutableList<String>) {
        selfPermissionCallBack?.shouldRequest(permissions)
    }

    fun clean() {
        checkPermissionCallBack = null
        multiPermissionCallBack = null
        selfPermissionCallBack = null
    }


    fun checkPermission(context: Context) {
        val grantedList: MutableList<String> = mutableListOf()
        //已通过的权限
        val shouldRequest: MutableList<String> = mutableListOf()
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
        checkSelfGranted(grantedList)
        checkSelfShouldRequest(grantedList)
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
        context: Activity,
        Code: Int,
        permissions: Array<String>,
        grantResults: IntArray
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
