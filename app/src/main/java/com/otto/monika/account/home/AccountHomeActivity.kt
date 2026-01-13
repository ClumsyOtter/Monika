package com.otto.monika.account.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.home.fragment.mine.MonikaMinePageFragment

/**
 * 查看其他账号信息的页面
 * Activity 作为容器，内容使用 MonikaMinePageFragment
 */
class AccountHomeActivity : MonikaBaseActivity() {

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param userId 用户ID（可选）
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, userId: String? = null): Intent {
            val intent = Intent(context, AccountHomeActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            userId?.let {
                intent.putExtra(EXTRA_USER_ID, it)
            }
            return intent
        }

        /**
         * 跳转到账号信息页面
         * @param context 上下文
         * @param userId 用户ID（可选）
         */
        @JvmStatic
        fun enter(context: Context, userId: String? = null) {
            val intent = getIntent(context, userId)
            context.startActivity(intent)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_account_home
    }


    override fun enableWindowInsets(): Boolean {
        return false
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()

        // 如果 Fragment 还未添加，则添加
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            val userId = intent.getStringExtra(EXTRA_USER_ID)
            val fragment = MonikaMinePageFragment.newInstance(userId, true)

            supportFragmentManager.commit {
                add(R.id.fragment_container, fragment)
            }
        }
    }
}