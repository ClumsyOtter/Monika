package com.otto.monika.account.subscriberPost

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.home.fragment.post.PostSource
import com.otto.monika.home.fragment.post.UserPostListFragment

class UserSubscribePostListActivity : MonikaBaseActivity() {
    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param userId 用户ID（可选）
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, userId: Long? = null): Intent {
            val intent = Intent(context, UserSubscribePostListActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            userId?.let {
                intent.putExtra(EXTRA_USER_ID, it)
            }
            return intent
        }

        /**
         * 跳转到用户的订阅者动态列表
         * @param context 上下文
         * @param userId 用户ID（可选）
         */
        @JvmStatic
        fun enter(context: Context, userId: Long? = null) {
            val intent = getIntent(context, userId)
            context.startActivity(intent)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_user_subscribe_post_list
    }

    override fun isActionBarVisible(): Boolean {
        return true
    }


    override fun enableWindowInsets(): Boolean {
        return true
    }

    override fun getTitleText(): String {
        return "我的订阅"
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()

        // 如果 Fragment 还未添加，则添加
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            val userId = intent.getStringExtra(EXTRA_USER_ID)
            val fragment = UserPostListFragment.newInstance(
                uid = userId,
                isOwner = false,
                sourceFrom = PostSource.UserSubscriber
            )

            supportFragmentManager.commit {
                add(R.id.fragment_container, fragment)
            }
        }
    }
}