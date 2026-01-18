package com.otto.monika.post.publish

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.otto.common.utils.getView
import com.otto.monika.R
import com.otto.monika.common.base.MonikaBaseActivity
import com.otto.monika.common.decoration.HorizontalSpacingItemDecoration
import com.otto.monika.common.dialog.CommonBottomSheet
import com.otto.monika.common.dialog.model.CommonBottomSheetData
import com.otto.monika.common.dialog.model.CommonBottomSheetItem
import com.otto.monika.common.file.IPhotoUploader
import com.otto.monika.common.file.PhotoUploader
import com.otto.monika.common.views.MonikaCommonOptionView
import com.otto.monika.common.views.MonikaCustomButton
import com.otto.monika.post.publish.adapter.PublishImageAdapter
import com.otto.monika.post.publish.viewmodel.MonikaPublishPostViewModel
import com.otto.network.common.ApiResponse
import com.otto.network.common.collectSimple
import com.otto.network.model.post.response.PostItem
import kotlinx.coroutines.launch
import java.io.File

/**
 * 发布动态页面
 */
class MonikaPublishPostActivity : MonikaBaseActivity() {

    companion object {

        const val POST_DATA = "post_data"

        /**
         * 获取 Intent
         * @param context 上下文
         * @return Intent 对象
         */
        @JvmStatic
        fun getIntent(context: Context, postItem: PostItem? = null): Intent {
            val intent = Intent(context, MonikaPublishPostActivity::class.java)
            postItem?.let {
                intent.putExtra(POST_DATA, postItem)
            }
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }

        /**
         * 跳转到发布动态页面
         * @param context 上下文
         */
        @JvmStatic
        fun enter(context: Context, postItem: PostItem? = null) {
            val intent = getIntent(context, postItem)
            context.startActivity(intent)
        }
    }

    // 图片列表
    private val rvImageList: RecyclerView by getView(R.id.rv_publish_image_list)

    // 标题输入框
    private val etTitle: EditText by getView(R.id.et_publish_title)

    // 标签流式布局容器
    private val flTags: FlexboxLayout by getView(R.id.fl_publish_tags)

    // 正文输入框
    private val etContent: EditText by getView(R.id.et_publish_content)

    // 添加标签按钮
    private val btnAddTag: MonikaCommonOptionView by getView(R.id.btn_publish_add_tag)

    // 可见性设置
    private val llVisibility: LinearLayout by getView(R.id.ll_publish_visibility)


    // 发布按钮
    private val btnPublish: MonikaCustomButton by getView(R.id.btn_publish_submit)

    // 图片适配器
    private lateinit var imageAdapter: PublishImageAdapter

    // ViewModel
    private val viewModel: MonikaPublishPostViewModel by viewModels()

    // 图片上传器
    private var photoUploader: PhotoUploader? = null

    /**
     * 携带进来的post数据
     */
    private val postItem: PostItem?
        get() = intent.getParcelableExtra(POST_DATA)

    override fun getContentViewId(): Int {
        return R.layout.activity_publish_post
    }

    override fun isActionBarVisible(): Boolean {
        return true
    }

    override fun getTitleText(): String {
        return "发布动态"
    }

    override fun onFinishCreateView() {
        super.onFinishCreateView()
        initViews()
        setupListeners()
        setupUiState()
        recoverPostItem()
    }

    private fun initViews() {
        // 初始化图片列表
        imageAdapter = PublishImageAdapter()
        rvImageList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImageList.adapter = imageAdapter
        rvImageList.addItemDecoration(HorizontalSpacingItemDecoration())
        // 初始化发布按钮状态
        updatePublishButtonState()
    }

    private fun recoverPostItem() {
        postItem?.let {
            val imageList = it.images
            imageAdapter.setData(imageList)
            it.tags?.let { tags ->
                val tagsItem = tags.map { tag ->
                    CommonBottomSheetItem(tag.name ?: "", tag.id.toString(), true)
                }
                updateTagLayout(tagsItem)
            }
            if (it.title.isNullOrEmpty().not()) {
                etTitle.setText(it.title)
            }
            if (it.content.isNullOrEmpty().not()) {
                etContent.setText(it.content)
            }
        }
    }


    private fun setupListeners() {
        // 图片适配器的添加按钮点击
        imageAdapter.onAddImageClickListener = {
            addImage()
        }

        // 图片适配器的删除按钮点击
        imageAdapter.onDeleteClickListener = { position ->
            imageAdapter.removeImage(position)
            updatePublishButtonState()
        }

        // 添加标签按钮
        btnAddTag.setOnClickListener {
            when (val currentState = viewModel.tagState.value) {
                is ApiResponse.Success -> {
                    // 已有数据，直接显示
                    currentState.data?.let { tagData ->
                        showTagBottomSheet(tagData)
                    }
                }

                is ApiResponse.Initial -> {
                    // 初始状态，加载标签（加载完成后自动显示对话框）
                    shouldShowTagDialogAfterLoad = true
                    viewModel.loadTags()
                }

                is ApiResponse.Loading -> {
                    // 正在加载，标记需要在加载完成后显示对话框
                    shouldShowTagDialogAfterLoad = true
                }

                is ApiResponse.BusinessError, is ApiResponse.NetworkError -> {
                    shouldShowTagDialogAfterLoad = true
                    viewModel.loadTags()
                }
            }
        }

        // 可见性设置
        llVisibility.setOnClickListener {
            viewModel.generateVisibilityData().let {
                val commonBottomSheet =
                    CommonBottomSheet.newInstance(data = it, isMultiSelect = false)
                commonBottomSheet.setOnItemChangeListener { visibility ->
                    viewModel.currentVisibility = visibility
                }
                commonBottomSheet.show(supportFragmentManager, javaClass.simpleName)
            }

        }

        // 发布按钮
        btnPublish.setOnClickListener {
            publishPost()
        }

        // 设置标题输入框限制
        etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 50) {
                    etTitle.setText(s.take(50))
                    etTitle.setSelection(50)
                    Toast.makeText(
                        this@MonikaPublishPostActivity,
                        "标题最多50个字符",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                updatePublishButtonState()
            }
        })

        // 设置正文输入框限制
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 5000) {
                    etContent.setText(s.take(5000))
                    etContent.setSelection(5000)
                    Toast.makeText(
                        this@MonikaPublishPostActivity,
                        "正文最多5000个字符",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                updatePublishButtonState()
            }
        })
    }

    /**
     * 添加图片
     */
    private fun addImage() {
        if (!imageAdapter.canAddMore()) {
            Toast.makeText(this, "最多只能添加12张图片", Toast.LENGTH_SHORT).show()
            return
        }

        if (photoUploader == null) {
            photoUploader = PhotoUploader(this)
        }

        // 直接打开相册选择图片
        photoUploader?.selectGallery(photoListener = object : IPhotoUploader.PhotoListener {
            override fun onReceive(filePath: String?, uri: Uri?) {
                filePath?.let {
                    // 检查文件大小
                    val file = File(filePath)
                    val fileSizeMB = file.length() / 1024.0 / 1024.0
                    if (fileSizeMB > 30) {
                        Toast.makeText(
                            this@MonikaPublishPostActivity,
                            "图片大小不能超过30M",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    // 添加到适配器
                    imageAdapter.addImage(filePath)
                    updatePublishButtonState()
                }

            }
        }, false) // false 表示不裁剪
    }

    /**
     * 处理Activity返回结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoUploader?.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 更新发布按钮状态
     */
    private fun updatePublishButtonState() {
        val hasContent =
            etTitle.text.toString().trim().isNotEmpty() ||
                    etContent.text.toString().trim().isNotEmpty() ||
                    imageAdapter.getImageCount() > 0
        btnPublish.isEnabled = hasContent
        if (hasContent) {
            // 有内容：黑色按钮
            btnPublish.btnImageView.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.monika_custom_btn_empty_black,
                    null
                )
            )
            btnPublish.btnTitleView.setTextColor(
                ResourcesCompat.getColor(resources, R.color.text_c4ff05, null)
            )
        } else {
            // 无内容：灰色按钮
            btnPublish.btnImageView.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.monika_custom_btn_empty_gray,
                    null
                )
            )
            btnPublish.btnTitleView.setTextColor(
                ResourcesCompat.getColor(resources, R.color.text_999999, null)
            )
        }
    }

    // 标记是否需要在加载完成后显示标签对话框
    private var shouldShowTagDialogAfterLoad = false

    /**
     * 设置观察者
     */
    private fun setupUiState() {
        // 观察标签列表变化
        lifecycleScope.launch {
            viewModel.tagState.collectSimple(
                onLoading = { showLoadingDialog() },
                onSuccess = {
                    hideLoadingDialog()
                    // 如果是因为点击按钮而加载的，加载完成后自动显示对话框
                    if (shouldShowTagDialogAfterLoad && it != null) {
                        shouldShowTagDialogAfterLoad = false
                        showTagBottomSheet(it)
                    }
                }, onFailure = { hideLoadingDialog() })
        }
    }

    /**
     * 显示标签选择对话框
     */
    private fun showTagBottomSheet(tagData: CommonBottomSheetData) {
        val commonBottomSheet =
            CommonBottomSheet.newInstance(tagData, isMultiSelect = true, maxSelectCount = 5)
        commonBottomSheet.setOnItemChangeListener { tag ->
            viewModel.updateTagData(tag)
            updateTagLayout(tag?.itemList?.filter { it.isSelected } ?: emptyList())
        }
        commonBottomSheet.show(supportFragmentManager, javaClass.simpleName)
    }


    /**
     * 更新标签布局
     */
    private fun updateTagLayout(tags: List<CommonBottomSheetItem>) {
        flTags.removeAllViews()
        // 将 10dp 转换为像素
        val spacingPx = resources.getDimensionPixelSize(R.dimen.dimen_10dp)
        tags.forEach { tag ->
            val textView = TextView(this).apply {
                text = tag.content
                textSize = 16f
                setTextColor(ResourcesCompat.getColor(resources,R.color.color_0056A8, null))
            }
            val layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // 设置右边距和下边距为 10dp，实现标签之间的间隔
                setMargins(0, 0, spacingPx, 0)
            }
            flTags.addView(textView, layoutParams)
        }
    }

    /**
     * 发布动态
     */
    private fun publishPost() {
        // 1. 验证数据
        val publishData = preparePublishData() ?: return

        // 2. 禁用按钮并显示 Loading
        btnPublish.isEnabled = false
        showLoadingDialog("正在发布...")

        // 3. 执行上传和发布流程
        lifecycleScope.launch {
            try {
                uploadAndPublish(publishData)
            } catch (e: Exception) {
                handlePublishError("发布失败: ${e.message}")
            }
        }
    }

    /**
     * 准备发布数据
     * @return 发布数据，如果验证失败返回 null
     */
    private data class PublishData(
        val title: String,
        val content: String,
        val images: MutableList<String>,
        val tags: String?,
        val visibleType: Int,
        val topic: String? = null
    )

    private fun preparePublishData(): PublishData? {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "请填写内容", Toast.LENGTH_SHORT).show()
            return null
        }

        val images = imageAdapter.imageList
        if (images.isEmpty()) {
            return null
        }
        val tags = viewModel.tagState.value.getDataOrNull()?.itemList?.filter { it.isSelected }
            ?.joinToString(",") { it.identify }
        val visibleType = viewModel.currentVisibility?.itemList
            ?.firstOrNull { it.isSelected }
            ?.identify
            ?.toIntOrNull() ?: 0
        return PublishData(title, content, images, tags, visibleType)
    }

    /**
     * 上传图片并发布
     */
    private suspend fun uploadAndPublish(data: PublishData) {
        // 1. 上传图片
        val imageUrls = uploadImages(data.images)
        if (imageUrls.isEmpty()) {
            handlePublishError("图片上传失败，未获取到图片URL")
            return
        }

        // 2. 提交发布请求
        viewModel.createPost(
            data.title,
            data.content,
            data.topic,
            data.tags,
            imageUrls,
            data.visibleType
        )
            .collectSimple(
                onLoading = {},
                onSuccess = {
                    handlePublishSuccess()
                },
                onFailure = { message ->
                    handlePublishError("发布失败: $message")
                }
            )
    }

    /**
     * 上传图片
     * @return 图片URL列表（逗号分隔），失败返回空字符串
     */
    private fun uploadImages(images: MutableList<String>): String {
        return images.toString()
    }

    /**
     * 处理发布成功
     */
    private fun handlePublishSuccess() {
        hideLoadingDialog()
        setResult(RESULT_OK)
        finish()
    }

    /**
     * 处理发布错误
     */
    private fun handlePublishError(message: String) {
        hideLoadingDialog()
        btnPublish.isEnabled = true
    }

}
