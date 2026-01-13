package com.otto.monika.subscribe.rank

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.utils.getView
import com.otto.monika.common.views.MonikaTabItem
import com.otto.monika.subscribe.rank.fragment.RankListFragment

class SubscribeRankActivity : MonikaBaseActivity() {

    companion object {
        private const val EXTRA_TAB_INDEX = "tabIndex"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param index Tab 索引
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, index: Int? = null): Intent {
            val intent = Intent(context, SubscribeRankActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            index?.let {
                intent.putExtra(EXTRA_TAB_INDEX, it)
            }
            return intent
        }

        /**
         * 跳转到订阅排名页面
         * @param context 上下文
         * @param index Tab 索引
         */
        @JvmStatic
        fun enter(context: Context, index: Int? = null) {
            val intent = getIntent(context, index)
            context.startActivity(intent)
        }
    }

    private val tabLayout: ConstraintLayout by getView(R.id.rank_tab_layout)
    private val viewPager: ViewPager2 by getView(R.id.view_pager)
    private val backBtn: ImageView by getView(R.id.iv_rank_list_back)


    private val tabList = mutableListOf<MonikaTabItem?>()
    private val fragments = mutableListOf<MonikaBaseFragment>()
    private var tabIndex: Int = 0

    override fun getContentViewId(): Int {
        return R.layout.activity_subscribe_rank
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        tabIndex = intent.getIntExtra(EXTRA_TAB_INDEX, tabIndex)
        initTabs()
        initFragments()
        setupViewPager()
        setupTabListeners()
        // 默认选中第一个 Tab
        onTabSelected(tabIndex, true)
        initBackView()
    }


    override fun isActionBarVisible(): Boolean {
        return false
    }

    private fun initBackView() {
        backBtn.setOnClickListener {
            finish()
        }
    }

    /**
     * 初始化 Tab 列表
     */
    private fun initTabs() {
        tabList.clear()
        tabLayout.forEach { child ->
            tabList.add(child as? MonikaTabItem)
        }
    }

    /**
     * 初始化 Fragment 列表
     */
    private fun initFragments() {
        fragments.clear()
        fragments.add(RankListFragment.newWeekInstance())
        fragments.add(RankListFragment.newMonthInstance())
        fragments.add(RankListFragment.newHistoryInstance())
    }

    /**
     * 设置 ViewPager
     */
    private fun setupViewPager() {
        val pageAdapter = ContentPageAdapter(supportFragmentManager, lifecycle, fragments)
        viewPager.adapter = pageAdapter
        viewPager.offscreenPageLimit = 3
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onTabSelected(position)
            }
        })
    }

    /**
     * 设置 Tab 点击监听
     */
    private fun setupTabListeners() {
        tabList.forEachIndexed { index, cyTabItem ->
            cyTabItem?.setOnClickListener {
                onTabSelected(index, true)
            }
        }
    }

    /**
     * Tab 选中处理
     * @param position Tab 位置
     * @param pageChange 是否需要切换 ViewPager 页面
     */
    private fun onTabSelected(position: Int, pageChange: Boolean = false) {
        tabList.forEachIndexed { index, cyTabItem ->
            cyTabItem?.setSelected(position == index)
            if (pageChange) {
                viewPager.setCurrentItem(position, true)
            }
        }
    }

    /**
     * ViewPager 适配器
     */
    class ContentPageAdapter(
        fm: FragmentManager, lifecycle: Lifecycle, private val data: List<MonikaBaseFragment>
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun createFragment(position: Int): Fragment {
            return data[position]
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}