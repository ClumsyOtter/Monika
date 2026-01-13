package com.otto.monika.login.viewmodel

import androidx.lifecycle.ViewModel
import com.otto.monika.api.client.MonikaClient
import com.otto.monika.api.common.ApiResponse
import com.otto.monika.api.common.asFlow
import com.otto.monika.api.model.login.request.NvsLoginRequest
import com.otto.monika.api.model.login.request.PhoneBindRequest
import com.otto.monika.api.model.login.request.PhoneLoginRequest
import com.otto.monika.api.model.login.request.PhoneVerifyRequest
import com.otto.monika.api.model.login.request.SmsCodeRequest
import com.otto.monika.api.model.login.request.WechatLoginRequest
import com.otto.monika.api.model.login.responese.PhoneLoginResponse
import com.otto.monika.api.model.login.responese.PhoneVerifyResponse
import com.otto.monika.api.model.login.responese.VisitorResponse
import kotlinx.coroutines.flow.Flow

class LoginViewModel : ViewModel() {

    private val api = MonikaClient.monikaApi

    var acToken: String? = null

    /**
     * 游客登录（使用 Flow 方式，自动处理 Loading 状态）
     */
    fun asVisitorFlow(): Flow<ApiResponse<VisitorResponse>> {
        return suspend { api.asVisitor() }.asFlow()
    }

    /**
     * 获取短信验证码（使用 Flow 方式，自动处理 Loading 状态）
     * @param phone 手机号
     */
    fun getSmsCodeFlow(phone: String): Flow<ApiResponse<Unit>> {
        return suspend { api.getSmsCode(SmsCodeRequest(phone)) }.asFlow()
    }

    /**
     * 通过短信验证码登录（使用 Flow 方式，自动处理 Loading 状态）
     * @param phone 手机号
     * @param code 验证码
     */
    fun loginViaCaptchaFlow(phone: String, code: String): Flow<ApiResponse<PhoneLoginResponse>> {
        return suspend { api.loginViaCaptcha(PhoneLoginRequest(phone, code)) }.asFlow()
    }

    /**
     * 验证手机号（使用 Flow 方式，自动处理 Loading 状态）
     * @param phone 手机号
     * @param code 验证码
     */
    fun verifyPhoneFlow(phone: String, code: String): Flow<ApiResponse<PhoneVerifyResponse>> {
        return suspend { api.verifyPhone(PhoneVerifyRequest(phone, code)) }.asFlow()
    }

    /**
     * 绑定手机号（使用 Flow 方式，自动处理 Loading 状态）
     * @param oldPhone 旧手机号
     * @param newPhone 新手机号
     * @param code 验证码
     * @param acToken 访问令牌
     */
    fun bindPhoneFlow(
        oldPhone: String,
        newPhone: String,
        code: String,
        acToken: String
    ): Flow<ApiResponse<Boolean>> {
        return suspend {
            api.bindPhone(
                PhoneBindRequest(
                    oldPhone,
                    newPhone,
                    code,
                    acToken
                )
            )
        }.asFlow()
    }

    /**
     * 通过一键登录登录（使用 Flow 方式，自动处理 Loading 状态）
     * @param phone 手机号
     * @param salt TOKEN
     */
    fun nvsLogin(phone: String, salt: String): Flow<ApiResponse<VisitorResponse>> {
        return suspend { api.nvsLogin(NvsLoginRequest(phone, salt)) }.asFlow()
    }

    /**
     * 通过微信登录（使用 Flow 方式，自动处理 Loading 状态）
     * @param code 微信授权码
     * @return 登录结果，包含 token
     */
    fun loginViaWechatFlow(code: String): Flow<ApiResponse<PhoneLoginResponse>> {
        return suspend { api.loginViaWechat(WechatLoginRequest(code)) }.asFlow()
    }
}