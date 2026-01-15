package com.otto.monika.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.utils.getView
import com.otto.monika.home.fragment.MonikaHomeFragment
import com.otto.monika.home.fragment.mine.MonikaMinePageFragment

class HomePageActivity : MonikaBaseActivity() {

    private val bottomNav by getView<BottomNavigationView>(R.id.bnv_home_nav_bar)

    private var currentFragment: Fragment? = null

    // Fragment 实例，使用 lazy 延迟初始化
    private val homeFragment by lazy { MonikaHomeFragment.newInstance() }
    private val mineFragment by lazy { MonikaMinePageFragment.newInstance() }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun isActionBarVisible(): Boolean {
        return false
    }

    override fun onFinishCreateView() {
        // 设置默认选中首页
        bottomNav.selectedItemId = R.id.navigation_home

        // 监听底部导航栏点击事件
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.navigation_mine -> {
                    switchFragment(mineFragment)
                    true
                }
                else -> false
            }
        }
        // 默认显示首页 Fragment
        switchFragment(homeFragment)
    }

    /**
     * 切换 Fragment
     */
    private fun switchFragment(fragment: Fragment) {
        if (fragment !== currentFragment) {
            val transaction = supportFragmentManager.beginTransaction()
            currentFragment?.let {
                transaction.hide(it)
            }
            fragment.let {
                if (!it.isAdded) {
                    transaction.add(R.id.fg_home_container, it)
                } else {
                    transaction.show(it)
                }
            }
            transaction.commit()
            currentFragment = fragment
        }
    }
}
