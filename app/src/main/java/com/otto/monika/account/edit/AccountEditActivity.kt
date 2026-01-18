package com.otto.monika.account.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.otto.common.utils.getView
import com.otto.monika.R
import com.otto.monika.account.edit.viewmodel.AccountEditViewModel
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.dialog.CommentActionBottomSheet
import com.otto.monika.common.dialog.MonikaNikeNameInputBottomDialog
import com.otto.monika.common.dialog.MonikaUserDescInputBottomDialog
import com.otto.monika.common.dialog.model.CommonActionGroup
import com.otto.monika.common.dialog.model.CommonActionItem
import com.otto.monika.login.PhoneLoginActivity
import com.otto.monika.login.model.PhoneLogin
import com.otto.network.common.collectSimple
import com.otto.network.model.user.response.MonikaUserInfoModel
import kotlinx.coroutines.launch

/**
 * 账号信息编辑页面
 * 显示和编辑头像、昵称、手机号等信息
 */
class AccountEditActivity : MonikaBaseActivity() {

    companion object {
        private const val EXTRA_ACCOUNT_RESPONSE_DATA = "extra_account_response_data"

        /**
         * 获取 Intent
         * @param context 上下文
         * @param profileResponse 用户信息数据
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, profileResponse: MonikaUserInfoModel): Intent {
            val intent = Intent(context, AccountEditActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_RESPONSE_DATA, profileResponse)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到账号信息编辑页面
         * @param activity Activity 上下文
         * @param profileResponse 用户信息数据
         */
        @JvmStatic
        fun enter(activity: Activity, profileResponse: MonikaUserInfoModel) {
            val intent = getIntent(activity, profileResponse)
            activity.startActivity(intent)
        }
    }

    // 用户信息数据
    private var profileResponse: MonikaUserInfoModel? = null


    // ViewModel
    private val viewModel: AccountEditViewModel by viewModels()

    // PhoneLoginActivity Result Launcher
    private val phoneLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 获取新的手机号
            val newPhone = result.data?.getStringExtra(PhoneLoginActivity.EXTRA_RESULT_PHONE)
            if (!newPhone.isNullOrEmpty()) {
                // 更新界面显示新的手机号
                setPhoneNumber(newPhone)
                profileResponse = profileResponse?.copy(phone = newPhone)
                Toast.makeText(this, "手机号已更新", Toast.LENGTH_SHORT).show()
                // 设置返回结果，通知调用方数据已更新
                setResult(RESULT_OK)
            }
        }
    }

    // 头像相关视图
    private val rivAvatar: ImageView by getView(R.id.riv_account_edit_avatar)
    private val rivAvatarArrow: View by getView(R.id.iv_account_edit_avatar_arrow)

    // 昵称相关视图
    private val tvNickname: TextView by getView(R.id.riv_account_edit_nike_name)
    private val tvNicknameArrow: View by getView(R.id.iv_account_edit_nike_name_arrow)

    //个人简介
    private val tvDesc: TextView by getView(R.id.tv_account_edit_desc_content)
    private val tvDescArrow: View by getView(R.id.iv_account_edit_desc_arrow)


    // 手机号相关视图
    private val tvPhoneContainer: LinearLayout by getView(R.id.ll_account_edit_phone_content)
    private val tvPhoneUnbound: TextView by getView(R.id.tv_account_edit_phone_unbound)
    private val llPhoneBound: LinearLayout by getView(R.id.ll_account_edit_phone_bound)
    private val tvPhoneNumber: TextView by getView(R.id.tv_account_edit_phone_number)

    override fun getContentViewId(): Int {
        return R.layout.activity_account_edit
    }

    override fun isActionBarVisible(): Boolean {
        return true
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        profileResponse = intent.getParcelableExtra(EXTRA_ACCOUNT_RESPONSE_DATA)
        // 初始化视图
        initViews()
        // 初始化数据
        initData()
    }

    override fun getTitleText(): String {
        return "编辑资料"
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        // 设置点击事件
        rivAvatar.setOnClickListener {
            showAvatarActionDialog()
        }
        rivAvatarArrow.setOnClickListener {
            showAvatarActionDialog()
        }
        tvNickname.setOnClickListener {
            showNikeNameActionDialog()
        }
        tvNicknameArrow.setOnClickListener {
            showNikeNameActionDialog()
        }

        tvDesc.setOnClickListener {
            showDescActionDialog()
        }

        tvDescArrow.setOnClickListener {
            showDescActionDialog()
        }

        tvPhoneContainer.setOnClickListener {
            val phoneLogin = if (tvPhoneUnbound.isVisible) {
                PhoneLogin(
                    source = PhoneLoginActivity.Companion.Source.EDIT_BIND_PHONE,
                    null, userId = profileResponse?.uid
                )
            } else {
                PhoneLogin(
                    source = PhoneLoginActivity.Companion.Source.EDIT_CHANGE_PHONE_VERIFY,
                    profileResponse?.phone, userId = profileResponse?.uid
                )
            }
            // 使用 Activity Result API 启动 PhoneLoginActivity
            val intent = Intent(this, PhoneLoginActivity::class.java).apply {
                putExtra(PhoneLoginActivity.EXTRA_LOGIN_PARAM, phoneLogin)
            }
            phoneLoginLauncher.launch(intent)
        }
    }

    private fun showNikeNameActionDialog() {
        val nameInputBottomDialog =
            MonikaNikeNameInputBottomDialog(this)
        nameInputBottomDialog.nikeName = tvNickname.text.toString()
        nameInputBottomDialog.setOnConfirmClickListener { newNickname ->
            if (tvNickname.text.toString() != newNickname) {
                updateUserInfo(nickname = newNickname)
            }
        }
        nameInputBottomDialog.show()
    }

    private fun showDescActionDialog() {
        val userDescInputBottomDialog =
            MonikaUserDescInputBottomDialog(this)
        userDescInputBottomDialog.desc = tvDesc.text.toString()
        userDescInputBottomDialog.setOnConfirmClickListener { desc ->
            if (tvDesc.text.toString() != desc) {
                updateUserInfo(userDesc = desc)
            }
        }
        userDescInputBottomDialog.show()
    }

    /**
     * 显示头像操作
     */
    private fun showAvatarActionDialog() {
        // 构建操作组
        val actionGroups = listOf(
            // 第一组：回复、复制
            CommonActionGroup(
                items = listOf(
                    CommonActionItem(
                        icon = R.drawable.monika_publish_choose_xiangce_icon,
                        content = "从相册选择",
                        type = "album"
                    ),
                    CommonActionItem(
                        icon = R.drawable.monika_publish_choose_xiangji_icon,
                        content = "拍照",
                        type = "camera"
                    )
                )
            ),
        )

        val dialog = CommentActionBottomSheet.newInstance(actionGroups, "操作")
        dialog.onActionItemClickListener = { actionItem ->
            when (actionItem.type) {
                "album" -> {
                    // 打开相册
                }

                "camera" -> {
                    // 打开相机
                }
            }
        }
        dialog.show(supportFragmentManager, "CommentActionBottomSheet")
    }


    /**
     * 初始化数据
     * 使用传入的 AccountHeadData 初始化视图
     */
    private fun initData() {
        profileResponse?.let { data ->
            // 设置头像
            setAvatar(data.avatar)
            // 设置昵称
            setNickname(data.nickname)
            setPhoneNumber(data.phone)
            setUserContent(data.intro)
        } ?: run {
            // 如果没有传入数据，使用默认值
            setAvatar("")
            setNickname("")
            setPhoneNumber(null)
        }
    }

    /**
     * 设置头像
     * @param avatarUrl 头像URL，如果为空则显示默认头像
     */
    fun setAvatar(avatarUrl: String?) {
        if (!TextUtils.isEmpty(avatarUrl)) {
            Glide.with(this).load(avatarUrl).circleCrop().into(rivAvatar)
        } else {
            // 显示默认头像
            rivAvatar.setImageResource(R.drawable.ic_launcher_app)
        }
    }

    /**
     * 更新用户信息（昵称和头像）
     * @param nickname 新昵称
     * @param avatar 新头像URL
     */
    private fun updateUserInfo(
        nickname: String? = null,
        avatar: String? = null,
        userDesc: String? = null
    ) {
        lifecycleScope.launch {
            viewModel.editUserFlow(nickname, avatar, userDesc).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { success ->
                    hideLoadingDialog()
                    if (success == true) {
                        // 更新成功，更新本地显示
                        nickname?.let { tvNickname.text = it }
                        avatar?.let { setAvatar(it) }
                        userDesc?.let { setUserContent(it) }
                        Toast.makeText(this@AccountEditActivity, "修改成功", Toast.LENGTH_SHORT)
                            .show()
                        // 设置返回结果，通知调用方数据已更新
                        setResult(RESULT_OK)
                    } else {
                        Toast.makeText(this@AccountEditActivity, "修改失败", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(this@AccountEditActivity, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /**
     * 设置昵称
     * @param nickname 昵称
     */
    fun setNickname(nickname: String?) {
        tvNickname.text = nickname ?: ""
    }

    /**
     * 设置手机号
     * @param phoneNumber 手机号，如果为空则显示"未绑定"状态
     */
    fun setPhoneNumber(phoneNumber: String?) {
        if (TextUtils.isEmpty(phoneNumber)) {
            // 未绑定状态
            tvPhoneUnbound.visibility = View.VISIBLE
            llPhoneBound.visibility = View.GONE
        } else {
            // 已绑定状态
            tvPhoneUnbound.visibility = View.GONE
            llPhoneBound.visibility = View.VISIBLE
            tvPhoneNumber.text = phoneNumber
        }
    }

    /**
     * 设置用户
     */
    fun setUserContent(userDesc: String?) {
        if (userDesc?.isNotEmpty() == true) {
            tvDesc.isVisible = true
            tvDesc.text = userDesc
        } else {
            tvDesc.isVisible = false
        }

    }
}
