package com.otto.monika.home.fragment.mine.listener

/**
 * Tab 数量变化监听器
 * Fragment 实现此接口，当数量变化时通知 MonikaMinePageFragment 更新 Tab 文字
 */
interface TabCountListener {
    /**
     * 设置数量变化回调
     * @param onCountChange 数量变化回调，参数为新的数量值
     */
    fun setOnCountChangeListener(onCountChange: (Int) -> Unit)

    /**
     * 获取当前数量
     * @return 当前数量，如果未加载或无法获取，返回 null
     */
    fun getCurrentCount(): Int?
}

