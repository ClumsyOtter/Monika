package com.otto.monika.common.dialog

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.otto.common.utils.AndroidUtils
import com.otto.permission.PermissionHelper
import com.otto.permission.listener.CheckMultiPermissionCallBack
import com.otto.permission.model.Permission


interface OnPermResult {
    fun granted()

    fun rejected()
}

open class SimplePermResult : OnPermResult {
    override fun granted() {
    }

    override fun rejected() {
    }
}

object PermissionDialog {

    fun requestCamera(ctx: Activity?, onPermResult: OnPermResult? = null) {
        request(
            ctx, "请先同意相机权限, 才能使用该功能.",
            "相机权限",
            mutableListOf(Manifest.permission.CAMERA),
            onPermResult
        )
    }

    fun requestStorageAndCamera(ctx: Activity?, onPermResult: OnPermResult? = null) {
        request(
            ctx, "请先同意读取权限, 才能使用该功能.",
            "存储权限",
            mutableListOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            onPermResult
        )
    }

    fun requestStorage(ctx: Activity?, onPermResult: OnPermResult? = null) {
        request(
            ctx, "请先同意读取权限, 才能使用该功能.",
            "存储权限",
            mutableListOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            onPermResult
        )
    }

    fun requestContact(ctx: Activity?, onPermResult: OnPermResult? = null) {
        request(
            ctx, "请先同意读取通讯录权限, 才能使用该功能.",
            "通讯录权限",
            mutableListOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
            onPermResult
        )
    }

    fun request(
        ctx: Activity?,
        info: String,
        permissionText: String,
        permissions: MutableList<String>,
        onPermResult: OnPermResult? = null
    ) {
        if (ctx == null) return
        permissions.filter { per ->
            ContextCompat.checkSelfPermission(ctx, per) != PackageManager.PERMISSION_GRANTED
        }.takeIf { it.isNotEmpty() }?.let { needRequestPermission ->
            val monikaAgreementBottomDialog = MonikaAgreementBottomDialog(ctx)
            monikaAgreementBottomDialog.setTitle("温馨提示")
            monikaAgreementBottomDialog.setCancelText("好")
            monikaAgreementBottomDialog.setConfirmText("不允许")
            monikaAgreementBottomDialog.setOnConfirmClickListener {
                PermissionHelper.applyPermission(
                    ctx, needRequestPermission.toMutableList(),
                    object : CheckMultiPermissionCallBack {

                        override fun granted(permissions: MutableList<Permission>) {
                            onPermResult?.granted()
                        }

                        override fun deniedJustShow(permissions: MutableList<Permission>) {
                            val builder = AlertDialog.Builder(ctx)
                            builder.setCancelable(false)
                            builder.setTitle(String.format("请允许获取%s", permissionText))
                            builder.setMessage(
                                String.format(
                                    "我们需要获取%s,否则您将无法正常使用。",
                                    permissionText
                                )
                            )
                            builder.setNegativeButton("取消") { _, _ -> onPermResult?.rejected() }
                            builder.setPositiveButton("去授权") { _, _ ->
                                request(
                                    ctx,
                                    info,
                                    permissionText,
                                    needRequestPermission.toMutableList(),
                                    onPermResult
                                )
                            }
                            val dialog = builder.create()
                            dialog.show()
                        }

                        override fun deniedNeverShow(permissions: MutableList<Permission>) {
                            val builder = AlertDialog.Builder(ctx)
                            builder.setCancelable(false)
                            builder.setTitle(String.format("请允许获取%s", permissionText))
                            builder.setMessage(
                                String.format(
                                    "由于%s无法获取%s，不能正常运行，请开启权限后再使用。\n设置路径：系统设置->%s->权限",
                                    AndroidUtils.getApp(ctx),
                                    permissionText,
                                    AndroidUtils.getApp(ctx),
                                )
                            )
                            builder.setNegativeButton("拒绝") { _, _ -> onPermResult?.rejected() }
                            builder.setPositiveButton("去设置") { _, _ ->
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.fromParts("package", ctx.packageName, null)
                                ctx.startActivity(intent)
                            }
                            val dialog = builder.create()
                            dialog.show()
                        }

                        override fun end() {
                        }
                    })
            }
            monikaAgreementBottomDialog.show()
        } ?: onPermResult?.granted()
    }

    fun requestStorageNoTip(ctx: Activity?, cb: OnPermResult?) {
        requestNoTip(
            ctx,
            "存储权限",
            mutableListOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            cb
        )
    }

    fun requestContactNoTip(ctx: Activity?, cb: OnPermResult?) {
        requestNoTip(
            ctx,
            "通讯录权限",
            mutableListOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
            cb
        )
    }

    fun requestNoTip(
        ctx: Activity?,
        permissionText: String,
        permissions: MutableList<String>,
        cb: OnPermResult?
    ) {
        if (ctx == null) return
        if (cb == null) return
        permissions.filter { per ->
            ContextCompat.checkSelfPermission(
                ctx,
                per
            ) != PackageManager.PERMISSION_GRANTED
        }.takeIf { it.isNotEmpty() }?.let { needRequestPermission ->
            PermissionHelper.applyPermission(
                ctx, needRequestPermission.toMutableList(),
                object : CheckMultiPermissionCallBack {

                    override fun granted(permissions: MutableList<Permission>) {
                        cb.granted()
                    }

                    override fun deniedJustShow(permissions: MutableList<Permission>) {
                        val builder = AlertDialog.Builder(ctx)
                        builder.setCancelable(false)
                        builder.setTitle(String.format("请允许获取%s", permissionText))
                        builder.setMessage(
                            String.format(
                                "我们需要获取%s,否则您将无法正常使用。",
                                permissionText
                            )
                        )
                        builder.setNegativeButton("取消") { _, _ -> cb.rejected() }
                        builder.setPositiveButton("去授权") { _, _ ->
                            requestNoTip(
                                ctx,
                                permissionText,
                                needRequestPermission.toMutableList(),
                                cb
                            )
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }

                    override fun deniedNeverShow(permissions: MutableList<Permission>) {
                        val builder = AlertDialog.Builder(ctx)
                        builder.setCancelable(false)
                        builder.setTitle(String.format("请允许获取%s", permissionText))
                        builder.setMessage(
                            String.format(
                                "由于%s无法获取%s，不能正常运行，请开启权限后再使用。\n设置路径：系统设置->%s->权限",
                                AndroidUtils.getApp(ctx),
                                permissionText,
                                AndroidUtils.getApp(ctx)
                            )
                        )
                        builder.setNegativeButton("拒绝") { _, _ -> cb.rejected() }
                        builder.setPositiveButton("去设置") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", ctx.packageName, null)
                            ctx.startActivity(intent)
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }

                    override fun end() {
                    }
                })
        } ?: cb.granted()
    }
}