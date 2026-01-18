package com.otto.monika.post.detail.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.otto.monika.R
import com.otto.monika.common.decoration.HorizontalSpacingItemDecoration
import com.otto.monika.subscribe.rank.fragment.adapter.TagAdapter
import com.otto.network.model.post.response.PostItem

/**
 * 帖子内容自定义 View
 * 封装了图片列表、内容文字、标签、日期、分割线、评论总数等元素
 */
class MonikaPostContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val imagePagerView: MonikaPostImagePagerView
    val contentText: TextView
    val titleText: TextView
    val tagsRecycler: RecyclerView
    val dateLocationText: TextView
    val commentCountText: TextView

    private var currentTagAdapter: TagAdapter? = null

    var onImagesClick: ((imageList: List<String>, index: Int) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post_content, this, true)
        imagePagerView = findViewById(R.id.view_post_image_pager)
        titleText = findViewById(R.id.tv_post_title)
        contentText = findViewById(R.id.tv_post_content)
        tagsRecycler = findViewById(R.id.rv_post_tags)
        dateLocationText = findViewById(R.id.tv_post_date_location)
        commentCountText = findViewById(R.id.tv_post_comment_count)

        // 初始化 Tags RecyclerView
        tagsRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        tagsRecycler.isNestedScrollingEnabled = false
        tagsRecycler.setHasFixedSize(true)
        tagsRecycler.setItemViewCacheSize(20) // 增加缓存大小
        imagePagerView.onImagesClick = { mageList: List<String>, index: Int ->
            onImagesClick?.invoke(mageList, index)
        }
    }

    fun setTitle(title: String?) {
        titleText.text = title ?: ""
        titleText.isVisible = title?.isNotEmpty() == true
    }

    /**
     * 设置内容文字
     */
    fun setContent(content: String?) {
        contentText.text = content ?: ""
        contentText.isVisible = content?.isNotEmpty() == true
    }

    /**
     * 设置日期和地点
     */
    fun setDateLocation(date: String?, location: String?) {
        val dateLocation = buildString {
            date?.let { append(it) }
            if (!location.isNullOrEmpty()) {
                if (date != null) append(" ")
                append(location)
            }
        }
        dateLocationText.text = dateLocation
    }

    /**
     * 设置评论总数
     */
    fun setCommentCount(count: Int) {
        commentCountText.isVisible = count > 0
        commentCountText.text = "ゝ∀･ 评论 $count"
    }

    /**
     * 设置标签列表
     * @param tags 标签列表
     */
    fun setTags(tags: List<String>) {
        if (tags.isNotEmpty()) {
            tagsRecycler.isVisible = true
            // 如果 adapter 不存在，创建它
            if (currentTagAdapter == null) {
                currentTagAdapter = TagAdapter(
                    showHashTag = true,
                    backgroundBgDrawable = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.monika_tag_gray_bg,
                        null
                    )
                )
                tagsRecycler.adapter = currentTagAdapter
                tagsRecycler.addItemDecoration(HorizontalSpacingItemDecoration())
            }
            // 更新数据，而不是重新创建 adapter
            currentTagAdapter?.updateTags(tags)
        } else {
            tagsRecycler.isVisible = false
            // 如果标签为空，清除 adapter
            if (currentTagAdapter != null) {
                tagsRecycler.adapter = null
                currentTagAdapter = null
            }
        }
    }


    /**
     * 设置帖子内容数据
     * @param images 图片列表
     * @param content 内容文字
     * @param tags 标签列表
     * @param date 日期
     * @param location 地点
     * @param
     */
    fun setPostData(
        postItem: PostItem
    ) {
        val images = postItem.images
        val content = postItem.content
        val tags = postItem.tags?.map { it.name ?: "" } ?: emptyList()
        val date = postItem.createTime ?: postItem.createdAt
        val location = postItem.ipAddr
        val commentCount = postItem.commentNum ?: 0
        val title = postItem.title ?: ""

        // 设置图片列表
        if (images.isNotEmpty()) {
            imagePagerView.isVisible = true
            imagePagerView.setImages(images)
        } else {
            imagePagerView.isVisible = false
        }

        setTitle(title)

        // 设置内容文字
        setContent(content)

        // 设置 Tags 列表
        setTags(tags)

        // 设置日期-地点
        setDateLocation(date, location)

        // 设置评论数量
        setCommentCount(commentCount)
    }
}

