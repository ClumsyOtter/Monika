package com.otto.monika.account.creator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.otto.monika.R
import com.otto.monika.account.creator.adapter.ApplyCreatorTagAdapter
import com.otto.monika.account.creator.viewmodel.ApplyToCreatorViewModel
import com.otto.monika.api.common.collectSimple
import com.otto.monika.api.model.creator.request.ApplyCreatorRequest
import com.otto.monika.api.model.user.response.MonikaUserInfoModel
import kotlinx.coroutines.launch
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.utils.getView
import com.otto.monika.common.utils.disableButton
import com.otto.monika.common.utils.enableButton
import com.otto.monika.common.views.MonikaCustomButton
import com.otto.monika.subscribe.support.adapter.PaymentMethodAdapter

/**
 * 申请成为创作者页面
 */
class ApplyToCreatorActivity : MonikaBaseActivity() {

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
            val intent = Intent(context, ApplyToCreatorActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_RESPONSE_DATA, profileResponse)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到申请成为创作者页面
         * @param activity Activity 上下文
         * @param profileResponse 用户信息数据
         */
        @JvmStatic
        fun enter(activity: Activity, profileResponse: MonikaUserInfoModel) {
            val intent = getIntent(activity, profileResponse)
            activity.startActivity(intent)
        }
    }

    private val fieldsRecycler: RecyclerView by getView(R.id.rv_apply_creator_fields)
    private val tagsRecycler: RecyclerView by getView(R.id.rv_apply_creator_tags)
    private val socialsRecycler: RecyclerView by getView(R.id.rv_apply_creator_socials)
    private val socialUrlInput: EditText by getView(R.id.et_apply_creator_social_url)
    private val nameInput: EditText by getView(R.id.et_apply_creator_name)
    private val contactInput: EditText by getView(R.id.et_apply_creator_contact)
    private val submitButton: MonikaCustomButton by getView(R.id.cycb_apply_creator_submit)

    private var fieldsAdapter: ApplyCreatorTagAdapter? = null
    private var tagsAdapter: ApplyCreatorTagAdapter? = null
    private var socialsAdapter: ApplyCreatorTagAdapter? = null

    // ViewModel
    private val viewModel: ApplyToCreatorViewModel by viewModels()

    // 用户信息数据
    private val profileResponse: MonikaUserInfoModel?
        get() = intent.getParcelableExtra(EXTRA_ACCOUNT_RESPONSE_DATA)

    override fun getContentViewId(): Int {
        return R.layout.activity_apply_to_creator
    }


    override fun onFinishCreateView() {
        setupRecyclerViews()
        setupUiState()
        setupListeners()
        updateSaveBtnStatus(false)
        viewModel.loadData()
    }

    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerViews() {
        // 擅长领域列表 - 多选模式，允许取消选中
        fieldsAdapter = ApplyCreatorTagAdapter(isMultiSelect = true, allowDeselect = true)
        fieldsRecycler.layoutManager = GridLayoutManager(this, 3)
        fieldsRecycler.adapter = fieldsAdapter
        fieldsRecycler.addItemDecoration(
            PaymentMethodAdapter.createSpacingDecoration(
                spanCount = 3,
                spacingDp = 5
            )
        )
        fieldsAdapter?.onSelectionChangedListener = {
            checkValidationState()
        }

        // 自我标签列表 - 多选模式，允许取消选中
        tagsAdapter = ApplyCreatorTagAdapter(isMultiSelect = true, allowDeselect = true)
        tagsRecycler.layoutManager = GridLayoutManager(this, 3)
        tagsRecycler.adapter = tagsAdapter
        tagsRecycler.addItemDecoration(
            PaymentMethodAdapter.createSpacingDecoration(
                spanCount = 3,
                spacingDp = 5
            )
        )
        tagsAdapter?.onSelectionChangedListener = {
            checkValidationState()
        }

        // 社交媒体验证列表 - 单选模式（不可取消）
        socialsAdapter = ApplyCreatorTagAdapter(isMultiSelect = false, allowDeselect = false)
        socialsRecycler.layoutManager = GridLayoutManager(this, 4)
        socialsRecycler.adapter = socialsAdapter
        socialsRecycler.addItemDecoration(
            PaymentMethodAdapter.createSpacingDecoration(
                spanCount = 4,
                spacingDp = 5
            )
        )
        socialsAdapter?.onSelectionChangedListener = {
            checkValidationState()
        }
    }

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 观察创作者元数据状态
        lifecycleScope.launch {
            viewModel.creatorMetadataState.collectSimple(
                onLoading = { showLoadingDialog() },
                onSuccess = { data ->
                    hideLoadingDialog()
                    // 设置擅长领域数据
                    data?.goodAt?.let { fields ->
                        fieldsAdapter?.setData(fields)
                    }
                    // 设置自我标签数据
                    data?.selfTag?.let { tags ->
                        tagsAdapter?.setData(tags)
                    }
                    // 设置社交媒体验证数据
                    data?.socialMedia?.let { socials ->
                        socialsAdapter?.setData(socials)
                    }
                },
                onFailure = { hideLoadingDialog() }
            )
        }
    }

    /**
     * 设置监听器
     */
    private fun setupListeners() {
        submitButton.setOnClickListener {
            handleSubmit()
        }

        // 监听输入框变化
        setupInputWatchers()
    }

    /**
     * 设置输入框监听器
     */
    private fun setupInputWatchers() {
        socialUrlInput.addTextChangedListener(createTextWatcher())
        nameInput.addTextChangedListener(createTextWatcher())
        contactInput.addTextChangedListener(createTextWatcher())
    }

    /**
     * 创建文本监听器
     */
    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkValidationState()
            }
        }
    }

    /**
     * 检查验证状态
     * 验证所有必填项是否都已填写
     */
    private fun checkValidationState() {
        // 1. 擅长领域：至少选一个
        val hasSelectedField = fieldsAdapter?.getSelectedTags()?.isNotEmpty() == true

        // 2. 自我标签：至少选一个
        val hasSelectedTag = tagsAdapter?.getSelectedTags()?.isNotEmpty() == true

        // 3. 社交媒体验证：必须选一个
        val hasSelectedSocial = socialsAdapter?.getSelectedTags()?.isNotEmpty() == true

        // 4. 社交媒体主页地址：不能为空
        val hasSocialUrl = socialUrlInput.text.toString().trim().isNotEmpty()

        // 5. 姓名：不能为空
        val hasName = nameInput.text.toString().trim().isNotEmpty()

        // 6. 联系方式：不能为空
        val hasContact = contactInput.text.toString().trim().isNotEmpty()

        // 所有必填项都已填写
        val isValid = hasSelectedField && hasSelectedTag && hasSelectedSocial &&
                hasSocialUrl && hasName && hasContact

        updateSaveBtnStatus(isValid)
    }

    /**
     * 处理提交申请
     */
    private fun handleSubmit() {
        // 再次验证必填项
        val hasSelectedField = fieldsAdapter?.getSelectedTags()?.isNotEmpty() == true
        val hasSelectedTag = tagsAdapter?.getSelectedTags()?.isNotEmpty() == true
        val hasSelectedSocial = socialsAdapter?.getSelectedTags()?.isNotEmpty() == true
        val hasSocialUrl = socialUrlInput.text.toString().trim().isNotEmpty()
        val hasName = nameInput.text.toString().trim().isNotEmpty()
        val hasContact = contactInput.text.toString().trim().isNotEmpty()
        if (!hasSelectedField || !hasSelectedTag || !hasSelectedSocial || !hasSocialUrl || !hasName || !hasContact) {
            Toast.makeText(this, "请填写完整的申请信息", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取选中的擅长领域ID数组，转换为字符串（逗号分隔）
        val goodAtIds = fieldsAdapter?.getSelectedTags()?.map { it.id } ?: emptyList()
        val goodAtIdString = goodAtIds.joinToString(",")

        // 获取选中的自我标签ID数组，转换为字符串（逗号分隔）
        val selfTagIds = tagsAdapter?.getSelectedTags()?.map { it.id } ?: emptyList()
        val selfTagIdString = selfTagIds.joinToString(",")

        // 获取选中的社交媒体ID（单选，只有一个）
        val selectedSocial = socialsAdapter?.getSelectedTags()?.firstOrNull()
        val socialMediaId = selectedSocial?.id

        // 获取其他输入信息
        val socialMediaUrl = socialUrlInput.text.toString().trim()
        val realName = nameInput.text.toString().trim()
        val contact = contactInput.text.toString().trim()

        // 获取用户ID（从 profileResponse 中获取，如果为空则传 null）
        val uid = profileResponse?.uid

        // 构建请求参数
        val request = ApplyCreatorRequest(
            uid = uid,
            goodAtId = goodAtIdString,
            selfTagId = selfTagIdString,
            socialMediaId = socialMediaId,
            socialMediaUrl = socialMediaUrl,
            realName = realName,
            contact = contact
        )
        // 调用 ViewModel 提交申请
        lifecycleScope.launch {
            viewModel.applyCreatorFlow(request).collectSimple(
                onLoading = {
                    showLoadingDialog()
                },
                onSuccess = { success ->
                    hideLoadingDialog()
                    if (success == true) {
                        Toast.makeText(
                            this@ApplyToCreatorActivity,
                            "申请已提交",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 设置返回结果并关闭页面
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(
                            this@ApplyToCreatorActivity,
                            "申请提交失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onFailure = { message ->
                    hideLoadingDialog()
                    Toast.makeText(this@ApplyToCreatorActivity, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateSaveBtnStatus(isReady: Boolean) {
        if (isReady.not()) {
            submitButton.disableButton(R.color.color_E6E6E6)
        } else {
            submitButton.enableButton()
        }
    }
}
