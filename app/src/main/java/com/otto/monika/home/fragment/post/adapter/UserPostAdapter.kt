package com.otto.monika.home.fragment.post.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.otto.monika.R
import com.otto.monika.api.model.post.response.PostItem
import com.otto.monika.common.utils.setEmptyAreaClickListener
import com.otto.monika.common.views.MonikaCommonOptionView
import com.otto.monika.home.fragment.post.PostSource
import com.otto.monika.home.fragment.post.model.ReviewStatus
import com.otto.monika.post.detail.model.PostChangeEvent
import com.otto.monika.subscribe.rank.fragment.adapter.TagAdapter
import com.otto.monika.subscribe.support.adapter.PaymentMethodAdapter

/**
 * 用户帖子列表适配器
 * 使用 BaseQuickAdapter 4.x 实现分页加载
 */
class UserPostAdapter(val isOwner: Boolean = false) :
    BaseQuickAdapter<PostItem, UserPostAdapter.UserPostViewHolder>() {

    // 回调接口
    var onItemClickListener: ((item: PostItem, position: Int) -> Unit)? = null
    var onImagesClick: ((imageList: List<String>, index: Int) -> Unit)? = null
    var onFavoriteClickListener: ((item: PostItem, position: Int) -> Unit)? = null
    var onItemDeleteClickListener: ((item: PostItem, position: Int) -> Unit)? = null
    var onReplyClickListener: ((item: PostItem, position: Int) -> Unit)? = null

    // 用来存储哪些位置数据变化了
    var needUpdatePostIdList: MutableList<Pair<String?, Int>> = mutableListOf()

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): UserPostViewHolder {
        return UserPostViewHolder(parent, isOwner)
    }

    override fun onBindViewHolder(holder: UserPostViewHolder, position: Int, item: PostItem?) {
        item ?: return
        holder.bind(
            item = item,
            position = position,
            onItemClickListener = onItemClickListener,
            onImagesClick = onImagesClick,
            onFavoriteClickListener = onFavoriteClickListener,
            onItemDeleteClickListener = onItemDeleteClickListener,
            onReplyClickListener = onReplyClickListener,
            onLikeViewUpdate = { likeView, postItem -> updateLikeView(likeView, postItem) }
        )
    }

    private fun updateLikeView(favoriteCountText: MonikaCommonOptionView, item: PostItem) {
        item.isLiked = item.isLiked != true
        if (item.isLiked == true) {
            item.likeNum = (item.likeNum ?: 0) + 1
        } else {
            item.likeNum = (item.likeNum ?: 0) - 1
        }
        favoriteCountText.setCountValue(item.likeNum ?: 0)
        favoriteCountText.isOptionSelected = item.isLiked == true
    }

    fun rollbackLikeState(item: PostItem, position: Int) {
        item.isLiked = item.isLiked != true
        if (item.isLiked == true) {
            item.likeNum = maxOf(0, (item.likeNum ?: 0) + 1)
        } else {
            item.likeNum = maxOf(0, (item.likeNum ?: 0) - 1)
        }
        notifyItemChanged(position)
    }

    fun notifyChangedIfNeedUpdate() {
        needUpdatePostIdList.forEach { post ->
            val postIndex = items.indexOfFirst { it.id == post.first }
            postIndex.let {
                when (post.second) {
                    0 -> notifyItemChanged(it)
                    1 -> removeAt(it)
                    else -> notifyItemChanged(it)
                }
            }
        }
        needUpdatePostIdList.clear()
    }

    fun onPostChanged(sourceFrom: PostSource, postChangeEvent: PostChangeEvent) {
        // 帖子删除
        if (postChangeEvent.postDelete?.newState == true) {
            items.find { it.id == postChangeEvent.postId }?.let {
                needUpdatePostIdList.add(Pair(postChangeEvent.postId, 1))
            }
        } else {
            // 收藏页面收到了收藏删除了动态，那么也不需要刷新喜欢的状态了
            if (sourceFrom == PostSource.Favorite && postChangeEvent.collectChanged != null && postChangeEvent.collectChanged.newState == false) {
                items.find { it.id == postChangeEvent.postId }?.let {
                    needUpdatePostIdList.add(Pair(postChangeEvent.postId, 1))
                }
            } else {
                if (postChangeEvent.likeChanged != null || postChangeEvent.replayChanged != null) {
                    val indexOfFirst = items.indexOfFirst { it.id == postChangeEvent.postId }
                    items.getOrNull(indexOfFirst)?.let { postItem ->
                        // 数据有更新
                        needUpdatePostIdList.add(Pair(postChangeEvent.postId, 0))
                        if (postChangeEvent.likeChanged != null) {
                            postItem.isLiked =
                                postChangeEvent.likeChanged.newState ?: postItem.isLiked
                            postItem.likeNum = postChangeEvent.likeChanged.count ?: postItem.likeNum
                        }
                        if (postChangeEvent.replayChanged != null) {
                            postItem.commentNum =
                                postChangeEvent.replayChanged.count ?: postItem.commentNum
                        }
                    }
                }
            }
        }
    }

    /**
     * ViewHolder 类
     */
    class UserPostViewHolder(parent: View, private val isOwner: Boolean) :
        RecyclerView.ViewHolder(
            View.inflate(parent.context, R.layout.item_user_post, null)
        ) {

        private val avatarImage: ImageView = itemView.findViewById(R.id.iv_user_post_avatar)
        private val titleText: TextView = itemView.findViewById(R.id.tv_user_post_title)
        private val contentText: TextView = itemView.findViewById(R.id.tv_user_post_content)
        private val reviewStatusText: TextView =
            itemView.findViewById(R.id.tv_user_post_review_status)
        private val postTitleText: TextView =
            itemView.findViewById(R.id.tv_user_post_post_content_title)
        private val postContentText: TextView =
            itemView.findViewById(R.id.tv_user_post_post_content)
        private val imageRecycler: RecyclerView = itemView.findViewById(R.id.rv_user_post_images)
        private val tagRecycler: RecyclerView = itemView.findViewById(R.id.rv_user_post_tags)
        private val publishTimeText: TextView =
            itemView.findViewById(R.id.tv_user_post_publish_time)
        private val deleteButton: TextView = itemView.findViewById(R.id.btn_user_post_delete)
        private val deleteDivider: View = itemView.findViewById(R.id.tv_user_post_publish_divider)
        private val favoriteCountText: MonikaCommonOptionView =
            itemView.findViewById(R.id.tv_user_post_favorite_count)
        private val replyCountText: MonikaCommonOptionView =
            itemView.findViewById(R.id.tv_user_post_reply_count)

        fun bind(
            item: PostItem,
            position: Int,
            onItemClickListener: ((PostItem, Int) -> Unit)?,
            onImagesClick: ((List<String>, Int) -> Unit)?,
            onFavoriteClickListener: ((PostItem, Int) -> Unit)?,
            onItemDeleteClickListener: ((PostItem, Int) -> Unit)?,
            onReplyClickListener: ((PostItem, Int) -> Unit)?,
            onLikeViewUpdate: (MonikaCommonOptionView, PostItem) -> Unit
        ) {
            // 设置 item 点击监听
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(item, pos)
                }
            }

            // 第一行：头像
            val avatar = item.user?.avatar
            if (!avatar.isNullOrEmpty()) {
                Glide.with(avatarImage.context)
                    .load(avatar)
                    .circleCrop()
                    .placeholder(R.drawable.generic_avatar_default)
                    .error(R.drawable.generic_avatar_default)
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.generic_avatar_default)
            }

            // 第一行：标题
            titleText.text = item.user?.nickname ?: ""

            // 第一行：内容
            contentText.text = item.ipAddr ?: ""

            // 审核状态
            val reviewStatus = when (item.status) {
                1 -> ReviewStatus.SUCCESS
                0 -> ReviewStatus.PENDING
                else -> ReviewStatus.FAILED
            }
            when (reviewStatus) {
                ReviewStatus.PENDING -> {
                    reviewStatusText.isVisible = true
                    reviewStatusText.text = "审核中"
                    reviewStatusText.setTextColor(
                        ResourcesCompat.getColor(
                            reviewStatusText.resources,
                            R.color.text_808080,
                            null
                        )
                    )
                    reviewStatusText.setBackgroundResource(R.drawable.monika_tag_gray_bg)
                }

                ReviewStatus.SUCCESS -> {
                    reviewStatusText.isVisible = false
                }

                ReviewStatus.FAILED -> {
                    reviewStatusText.isVisible = true
                    reviewStatusText.text = "审核失败"
                    reviewStatusText.setTextColor(
                        ResourcesCompat.getColor(
                            reviewStatusText.resources,
                            R.color.text_FA2B19,
                            null
                        )
                    )
                }
            }

            // 第二行：post
            val postTitle = item.title ?: ""
            postTitleText.text = postTitle
            postTitleText.isVisible = postTitle.isNotEmpty()

            val postContent = item.content ?: ""
            postContentText.text = postContent
            postContentText.isVisible = postContent.isNotEmpty()

            // 第三行：图片列表
            setupImageRecycler(imageRecycler, item, position, onItemClickListener, onImagesClick)

            // 第四行：标签列表
            setupTagRecycler(tagRecycler, item)

            // 第五行：发表时间
            publishTimeText.text = (item.createdAt ?: "") + "发表"

            // 第五行：删除按钮
            if (isOwner && reviewStatus != ReviewStatus.PENDING) {
                deleteButton.isVisible = true
                deleteDivider.isVisible = true
            } else {
                deleteButton.isVisible = false
                deleteDivider.isVisible = false
            }
            deleteButton.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemDeleteClickListener?.invoke(item, pos)
                }
            }

            // 第五行：收藏数量和回复数量
            replyCountText.setCountValue(item.commentNum ?: 0)
            favoriteCountText.isOptionSelected = item.isLiked == true
            favoriteCountText.setCountValue(item.likeNum ?: 0)

            // 设置收藏点击监听
            favoriteCountText.onOptionClickListener = {
                onLikeViewUpdate(favoriteCountText, item)
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onFavoriteClickListener?.invoke(item, pos)
                }
            }

            // 设置回复点击监听
            replyCountText.onOptionClickListener = {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onReplyClickListener?.invoke(item, pos)
                }
            }
        }

        private fun setupImageRecycler(
            imageRecycler: RecyclerView,
            item: PostItem,
            position: Int,
            onItemClickListener: ((PostItem, Int) -> Unit)?,
            onImagesClick: ((List<String>, Int) -> Unit)?
        ) {
            val imageList = item.images
            if (imageList.isEmpty()) {
                imageRecycler.visibility = View.GONE
            } else {
                imageRecycler.visibility = View.VISIBLE
                imageRecycler.itemAnimator = null
                imageRecycler.setEmptyAreaClickListener {
                    onItemClickListener?.invoke(item, position)
                }
                val displayImageList = imageList.take(3)
                var imageAdapter = imageRecycler.adapter as? PostImageAdapter
                if (imageAdapter == null) {
                    imageAdapter = PostImageAdapter(displayImageList)
                    imageRecycler.adapter = imageAdapter
                    imageAdapter.onImagesClick = onImagesClick
                    var imageLayoutManager = imageRecycler.layoutManager
                    if (imageLayoutManager == null) {
                        imageLayoutManager = GridLayoutManager(imageRecycler.context, 3)
                        imageRecycler.layoutManager = imageLayoutManager
                        imageRecycler.addItemDecoration(
                            PaymentMethodAdapter.createSpacingDecoration(3, 5)
                        )
                    }
                }
                imageAdapter.updateData(displayImageList)
            }
        }

        private fun setupTagRecycler(tagRecycler: RecyclerView, item: PostItem) {
            val tagList = item.tags
            if (tagList.isNullOrEmpty()) {
                tagRecycler.visibility = View.GONE
            } else {
                tagRecycler.visibility = View.VISIBLE
                var tagAdapter = tagRecycler.adapter as? TagAdapter
                if (tagAdapter == null) {
                    tagAdapter = TagAdapter(true)
                    tagRecycler.adapter = tagAdapter
                    var tagLayoutManager = tagRecycler.layoutManager
                    if (tagLayoutManager == null) {
                        tagLayoutManager = LinearLayoutManager(
                            tagRecycler.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        tagRecycler.layoutManager = tagLayoutManager
                    }
                }
                tagAdapter.updateTags(tagList.map { it.name ?: "" })
            }
        }
    }
}
