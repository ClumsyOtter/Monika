package com.otto.monika.home.fragment.favorite.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.otto.monika.common.base.MonikaBaseFragment

/**
 * 收藏页面 ViewPager2 适配器
 * 管理"动态"和"创作者"两个 Tab
 */
class FavoritePagerAdapter(fm: Fragment, private val data: List<MonikaBaseFragment>) :
    FragmentStateAdapter(fm) {

    override fun createFragment(position: Int): Fragment {
        return data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): MonikaBaseFragment? {
        return data.getOrNull(position)
    }
}

