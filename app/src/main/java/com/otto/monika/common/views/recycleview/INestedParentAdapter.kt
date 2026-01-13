package com.otto.monika.common.views.recycleview

/**
 * ParentAdapter 需实现此接口
 */
interface INestedParentAdapter {
    /**
     * 获取当前需要联动的子RecyclerView
     */
    fun getCurrentChildRecyclerView(): ChildRecyclerView?
}
