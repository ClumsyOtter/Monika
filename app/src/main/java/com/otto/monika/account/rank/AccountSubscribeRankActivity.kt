package com.otto.monika.account.rank

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.otto.monika.R
import com.otto.monika.account.rank.fragment.RankSource
import com.otto.monika.account.rank.fragment.UserSubscribeRankFragment
import com.otto.monika.common.base.MonikaBaseActivity

/**
 * 账户订阅排行榜 Activity
 * Fragment 容器
 */
class AccountSubscribeRankActivity : MonikaBaseActivity() {

    // 用户信息iD
    private val accountUid: String?
        get() = intent.getStringExtra(EXTRA_ACCOUNT_UID)

    companion object {
        private const val EXTRA_ACCOUNT_UID = "extra_account_uid_data"
        private const val EXTRA_SOURCE = "extra_source"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param uid 用户ID
         * @param source 来源类型，默认为 ACCOUNT
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(
            context: Context,
            uid: String? = null,
            source: RankSource = RankSource.ACCOUNT
        ): Intent {
            val intent = Intent(context, AccountSubscribeRankActivity::class.java).apply {
                putExtra(EXTRA_SOURCE, source.name)
                putExtra(EXTRA_ACCOUNT_UID, uid)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            return intent
        }

        /**
         * 跳转到账户订阅排行榜页面
         * @param activity Activity 上下文
         * @param uid 用户ID
         * @param source 来源类型，默认为 ACCOUNT
         */
        @JvmStatic
        fun enter(
            activity: Activity,
            uid: String? = null,
            source: RankSource = RankSource.ACCOUNT
        ) {
            val intent = getIntent(activity, uid, source)
            activity.startActivity(intent)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_account_subscribe_rank
    }

    override fun isActionBarVisible(): Boolean {
        return true
    }


    override fun getTitleText(): String {
        return "订阅榜"
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        // 创建并添加 Fragment
        val fragment = UserSubscribeRankFragment.newInstance(RankSource.ACCOUNT, uid = accountUid)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }
}
