package com.otto.monika.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.otto.monika.R
import com.otto.monika.application.MonikaApplication
import com.otto.monika.login.LoginActivity
import com.otto.monika.common.base.MonikaBaseActivity

class MonikaSplashActivity : MonikaBaseActivity() {

    companion object {
        /**
         * 获取 Intent
         * @param context 上下文
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, MonikaSplashActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到启动页
         * @param context 上下文
         */
        @JvmStatic
        fun enter(context: Context) {
            val intent = getIntent(context)
            context.startActivity(intent)
        }
    }


    override fun getContentViewId(): Int {
        return R.layout.activity_splash_ad_view
    }

    override fun getTitleText(): String {
        return ""
    }

    override fun isActionBarVisible(): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFinishCreateView() {
        openMainActivity()
    }



    private fun openMainActivity() {
        //todo
        //val acToken = PreferencesFactory.getUserPref().acToken
        val acToken = null
        if (acToken == null) {
            LoginActivity.enter(this)
        } else {
            HomePageActivity.enter(this)
        }
        finish()
    }
}