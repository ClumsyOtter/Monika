package com.otto.permission.listener

import com.otto.permission.model.Permission


/**
 * 单个权限获取途径
 * 支持链式调用
 */
interface CheckPermissionCallBack {
    /**
     * 权限通过（已获取或弹窗申请后获取）
     * @param permission
     */
    fun granted(permission: Permission?)

    /**
     * 权限申请已弹窗提示且被拒绝且再也不询问
     * @param permission
     */
    fun deniedJustShow(permission: Permission?)

    /**
     * 权限申请已弹窗提示且被拒绝且再也不询问或已被永久拒绝
     * @param permission
     */
    fun deniedNeverShow(permission: Permission?)
}
