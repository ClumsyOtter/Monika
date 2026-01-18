package com.otto.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.otto.permission.model.Permission

/**
 * Created by qian on 2017/9/28.
 */
class SimpleApplyPermissionActivity : AppCompatActivity() {
    private var noPermissions: MutableList<String> = mutableListOf()
    private var permissions: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissions = PermissionManager.permissions
        if (permissions.isEmpty()) {
            finish()
            return
        }
        if (TextUtils.equals(permissions[0], Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestDrawOverLays()
            }
            return
        }

        if (TextUtils.equals(permissions[0], Manifest.permission.WRITE_SETTINGS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestWriteSettings()
            }
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
                PermissionManager.granted(Permission(permission, true))
            }
        }
        val strings = noPermissions.toTypedArray<String?>()
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
                        PermissionManager.granted(Permission(permissions[i], true))
                        this.finish()
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permissions[i]
                            )
                        ) {
                            PermissionManager.deniedJustShow(Permission(permissions[i], false, true))
                        } else {
                            PermissionManager.deniedNeverShow(
                                Permission(
                                    permissions[i],
                                    false,
                                    false
                                )
                            )
                        }
                    }
                    i++
                }
                finish()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun requestDrawOverLays() {
        if (!Settings.canDrawOverlays(this@SimpleApplyPermissionActivity)) {
            Toast.makeText(this, "can not DrawOverlays", Toast.LENGTH_SHORT).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                ("package:" + this@SimpleApplyPermissionActivity.packageName).toUri()
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        } else {
            PermissionManager.granted(Permission(Manifest.permission.SYSTEM_ALERT_WINDOW, true))
            finish()
        }
    }


    @RequiresApi(23)
    fun requestWriteSettings() {
        if (!Settings.System.canWrite(this@SimpleApplyPermissionActivity)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                ("package:" + this@SimpleApplyPermissionActivity.packageName).toUri()
            )
            startActivityForResult(intent, WRITE_SETTINGS_REQ_CODE)
        } else {
            PermissionManager.granted(Permission(Manifest.permission.WRITE_SETTINGS, true))
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                PermissionManager.deniedJustShow(
                    Permission(
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        false
                    )
                )
            } else {
                PermissionManager.granted(Permission(Manifest.permission.SYSTEM_ALERT_WINDOW, true))
            }
        }

        if (requestCode == WRITE_SETTINGS_REQ_CODE) {
            if (!Settings.System.canWrite(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                PermissionManager.deniedJustShow(Permission(Manifest.permission.WRITE_SETTINGS, false))
            } else {
                PermissionManager.granted(Permission(Manifest.permission.WRITE_SETTINGS, true))
            }
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        PermissionManager.clean()
    }

    companion object {
        private const val requestCode = 1
        private const val OVERLAY_PERMISSION_REQ_CODE = 2
        private const val WRITE_SETTINGS_REQ_CODE = 3
    }
}
