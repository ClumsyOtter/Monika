package com.otto.monika.common.permission.listener

interface CheckSelfPermissionCallBack {
    /**
     * 第一次检查权限时已通过的权限
     * @param permissions
     */
    fun granted(permissions: Array<String?>?)

    /**
     * 第一次检查权限时需要申请的权限
     * @param permissions
     */
    fun shouldRequest(permissions: Array<String?>?)
}
