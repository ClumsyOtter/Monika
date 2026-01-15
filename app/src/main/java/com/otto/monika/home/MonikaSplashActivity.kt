package com.otto.monika.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.otto.monika.R
import com.otto.monika.application.MonikaApplication
import com.otto.monika.login.LoginActivity
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.datastore.TokenDataStore
import com.otto.monika.common.token.TokenManager
import kotlinx.coroutines.launch

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
        lifecycleScope.launch {
            this@MonikaSplashActivity.TokenDataStore.data.collect { acToken ->
                if (acToken.token == null) {
                    LoginActivity.enter(this@MonikaSplashActivity)
                } else {
                    TokenManager.token = acToken.token
                    HomePageActivity.enter(this@MonikaSplashActivity)
                }
                finish()
            }
        }
    }
}