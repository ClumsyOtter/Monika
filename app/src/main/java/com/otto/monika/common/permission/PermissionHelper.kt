package com.otto.monika.common.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.otto.monika.common.permission.listener.CheckMultiPermissionCallBack
import com.otto.monika.common.permission.listener.CheckPermissionCallBack
import com.otto.monika.common.permission.listener.CheckSelfPermissionCallBack
import com.otto.monika.common.permission.model.Permission

object PermissionHelper {

    /**
     * 申请权限，权限设置在回调之后参数
     * 一次返回（数组形式）
     * @param context
     * @param callBack
     * @param permissions
     */
    fun applyPermission(
        context: Context,
        permissions: MutableList<String>,
        callBack: CheckMultiPermissionCallBack
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (permissions.isNotEmpty()) {
                val sdkSmall: MutableList<Permission> = mutableListOf()
                for (permission in permissions) {
                    sdkSmall.add(Permission(permission, false, false))
                }
                callBack.granted(sdkSmall)
            }
            return
        }
        PermissionManager.applyPermissions(permissions)
        PermissionManager.multiPermissionCallBack = callBack
        if (context is Activity) {
            context.startActivity(Intent(context, SimpleApplyMultiPermissionActivity::class.java))
        } else {
            val intent = Intent(context, SimpleApplyMultiPermissionActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }


    /**
     * 特殊权限
     * @param context
     * @param callBack
     */
    fun applyDrawOverLaysPermission(context: Context, callBack: CheckPermissionCallBack) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callBack.granted(Permission(Manifest.permission.SYSTEM_ALERT_WINDOW, false))
            return
        }
        PermissionManager.checkPermissionCallBack = callBack
        PermissionManager.applyPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
        if (context is Activity) {
            context.startActivity(Intent(context, SimpleApplyPermissionActivity::class.java))
        } else {
            val intent = Intent(context, SimpleApplyPermissionActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * 特殊权限
     * @param context
     * @param callBack
     */
    fun applyWriteSettings(context: Context, callBack: CheckPermissionCallBack) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callBack.granted(Permission(Manifest.permission.WRITE_SETTINGS, false))
            return
        }
        PermissionManager.checkPermissionCallBack = callBack
        PermissionManager.applyPermissions(Manifest.permission.WRITE_SETTINGS)
        if (context is Activity) {
            context.startActivity(Intent(context, SimpleApplyPermissionActivity::class.java))
        } else {
            val intent = Intent(context, SimpleApplyPermissionActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * 仅检查权限通过情况
     * @param context
     * @param permissions
     * @param callBack
     */
    fun checkSelfPermission(
        context: Context,
        permissions: MutableList<String>,
        callBack: CheckSelfPermissionCallBack
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (permissions.isNotEmpty()) {
                val noPermissions = mutableListOf<String>()
                for (permission in permissions) {
                    noPermissions.add(permission)
                }
                callBack.granted(noPermissions)
            }
            return
        }
        PermissionManager.selfPermissionCallBack = callBack
        PermissionManager.applyPermissions(permissions)
        PermissionManager.checkPermission(context)
    }


    /**
     * 多个权限申请
     * @param permissions
     */
    fun applyManualMultiPermission(
        context: Activity,
        permissions: MutableList<String>,
        callBack: CheckMultiPermissionCallBack
    ) {
        if (context is FragmentActivity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (permissions.isNotEmpty()) {
                    val sdkSmall: MutableList<Permission> = mutableListOf()
                    for (permission in permissions) {
                        sdkSmall.add(Permission(permission, false, false))
                    }
                    callBack.granted(sdkSmall)
                }
                return
            }
            PermissionManager.applyPermissions(permissions)
            PermissionManager.multiPermissionCallBack = callBack
            PermissionManager.applyMultiPermission(context)
        }
    }

    /**
     * 单个权限申请
     * @param context
     * @param permission
     * @param callBack
     */
    fun applyManualPermission(
        context: Activity,
        permission: String,
        callBack: CheckPermissionCallBack
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callBack.granted(Permission(permission, false, false))
            return
        }
        PermissionManager.checkPermissionCallBack = callBack
        PermissionManager.applyPermissions(permission)
        PermissionManager.applyPermission(context)
    }


    fun onRequestPermissionsResult(
        context: Activity,
        code: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        PermissionManager.onRequestPermissionsResult(context, code, permissions, grantResults)
    }

}
