package com.otto.monika.common.permission

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.otto.monika.common.permission.model.Permission

class SimpleApplyMultiPermissionActivity : AppCompatActivity() {
    private var noPermissions: MutableList<String> = mutableListOf()  //需要申请权限的数组
    private var permissions: MutableList<String> = mutableListOf()  //外部申请权限的数组
    private var grantedList: MutableList<Permission> = mutableListOf() //已通过的权限
    private var justShowList: MutableList<Permission> = mutableListOf()  //未通过但下次还会提醒的权限
    private var neverShowList: MutableList<Permission> = mutableListOf()  //未通过且不会再次提醒的权限

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noPermissions = ArrayList()
        permissions = PermissionManager.permissions
        grantedList = ArrayList()
        justShowList = ArrayList()
        neverShowList = ArrayList()
        if (permissions.isEmpty()) {
            finish()
            return
        }
        checkPermission()
    }


    private fun checkPermission() {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                noPermissions.add(permission)
            } else {
                grantedList.add(Permission(permission, true)) //多权限时在已通过的数组中添加
            }
        }
        //当已通过的权限和总传入权限相等则返回结果
        if (grantedList.size == permissions.size) {
            PermissionManager.multiGranted(grantedList)
            PermissionManager.multiEnd()
            finish()
            return
        }

        val strings = noPermissions.toTypedArray<String>()
        if (strings.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                strings,
                requestCode
            )
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        code: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(code, permissions, grantResults)
        when (code) {
            requestCode -> {
                if (grantResults.isEmpty()) return
                var i = 0
                while (i < grantResults.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        grantedList.add(Permission(permissions[i], true, true))
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permissions[i]
                            )
                        ) {
                            justShowList.add(Permission(permissions[i], false, true))
                        } else {
                            neverShowList.add(Permission(permissions[i], false, true))
                        }
                    }
                    i++
                }
                if (grantedList.isNotEmpty()) PermissionManager.multiGranted(
                    grantedList
                )
                if (justShowList.isNotEmpty()) PermissionManager.multiDeniedJustShow(
                    justShowList
                )
                if (neverShowList.isNotEmpty()) PermissionManager.multiDeniedNeverShow(
                    neverShowList
                )
                PermissionManager.multiEnd()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        grantedList = mutableListOf()
        justShowList = mutableListOf()
        neverShowList = mutableListOf()
        PermissionManager.clean()
    }

    companion object {
        private const val requestCode = 1
    }
}
