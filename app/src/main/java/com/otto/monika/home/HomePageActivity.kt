package com.otto.monika.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseFragment
import com.otto.monika.common.views.MonikaTabItem
import com.otto.monika.home.fragment.MonikaHomeFragment
import com.otto.monika.home.fragment.mine.MonikaMinePageFragment
import com.otto.monika.home.fragment.mine.listener.TabSelectListener
import com.otto.monika.post.publish.MonikaPublishPostActivity
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.utils.getView
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.common.utils.StatusBarUtil

class HomePageActivity : MonikaBaseActivity() {

    override fun getContentViewId(): Int {
        return R.layout.activity_main_new
    }

    companion object {
        /**
         * 获取 Intent
         * @param context 上下文
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, HomePageActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到主页
         * @param activity Activity 上下文
         */
        @JvmStatic
        fun enter(activity: Activity?) {
            activity?.let {
                val intent = getIntent(it)
                it.startActivity(intent)
            }
        }
    }

    private val navBarLayout: LinearLayout by getView(R.id.nav_bar_layout)
    private val tabLayout: ConstraintLayout by getView(R.id.home_tab_layout)
    private val viewPager: ViewPager2 by getView(R.id.view_pager)

    private val tabList = mutableListOf<MonikaTabItem?>()
    private val fragments = mutableListOf<MonikaBaseFragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val statusBarHeight = StatusBarUtil.getStatusBarHeight(this) + DipUtils.dpToPx(10)
        navBarLayout.updatePadding(top = statusBarHeight)
    }

    override fun isActionBarVisible(): Boolean {
        return false
    }

    override fun onFinishCreateView() {
        navBarLayout.setOnClickListener {

        }
        tabList.clear()
        tabLayout.forEach { child ->
            if (child.isVisible) {
                tabList.add(child as? MonikaTabItem)
            }
        }

        fragments.clear()
        fragments.add(MonikaHomeFragment.newInstance())
        fragments.add(MonikaMinePageFragment.newInstance())
        val pageAdapter = ContentPageAdapter(supportFragmentManager, lifecycle, fragments)
        viewPager.adapter = pageAdapter
        viewPager.offscreenPageLimit = 4
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(p0: Int) {
                onTabSelected(p0)
            }
        })

        tabList.forEachIndexed { index, cyTabItem ->
            cyTabItem?.setOnClickListener {
                onTabSelected(index, true)
            }
        }

        onTabSelected(0, true)
    }

    private fun onTabSelected(position: Int, pageChange: Boolean = false) {
        tabList.forEachIndexed { index, cyTabItem ->
            cyTabItem?.setSelected(position == index)
            if (pageChange) {
                viewPager.setCurrentItem(position, true)
            }
        }
        fragments.forEachIndexed { index, fragment ->
            (fragment as? TabSelectListener)?.onFragmentSelected(index == position)
        }
    }


    class ContentPageAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
        private val data: List<MonikaBaseFragment>
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun createFragment(position: Int): Fragment {
            return data[position]
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }


}