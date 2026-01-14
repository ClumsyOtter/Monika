package com.otto.monika.common.permission.listener

import com.otto.monika.common.permission.model.Permission


/**
 * 多权限返回
 */
interface CheckMultiPermissionCallBack {
    /**
     * 权限通过（已获取或弹窗申请后获取）
     * @param permissions
     */
    fun granted(permissions: MutableList<Permission>)

    /**
     * 权限申请已弹窗提示且被拒绝且再也不询问
     * @param permissions
     */
    fun deniedJustShow(permissions: MutableList<Permission>)

    /**
     * 权限申请已弹窗提示且被拒绝且再也不询问或已被永久拒绝
     * @param permissions
     */
    fun deniedNeverShow(permissions: MutableList<Permission>)

    /**
     * 回调全部结束
     */
    fun end()
}
