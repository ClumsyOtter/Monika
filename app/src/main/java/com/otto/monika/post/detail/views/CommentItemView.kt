package com.otto.monika.post.detail.views

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.otto.monika.R
import com.otto.monika.api.model.comment.response.CommentItem
import com.otto.monika.api.model.comment.response.updateLikeState
import com.otto.monika.common.views.MonikaCommonOptionView

/**
 * 评论项自定义 View
 * 封装了头像、名称、作者标签、内容、时间等元素
 */
class CommentItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val avatarImage: ImageView
    private val nameText: TextView
    private val authorTag: TextView
    private val contentText: TextView
    private val timeText: TextView
    private val replyText: TextView
    private val likeView: MonikaCommonOptionView

    // 回调接口
    var onReplyClickListener: ((CommentItem) -> Unit)? = null
    var onLikeClickListener: ((CommentItem) -> Unit)? = null
    var onLongClickListener: ((CommentItem) -> Unit)? = null
    var onItemClickListener: ((CommentItem) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_comment_item, this, true)

        avatarImage = findViewById(R.id.iv_comment_item_avatar)
        nameText = findViewById(R.id.tv_comment_item_name)
        authorTag = findViewById(R.id.tv_comment_item_author_tag)
        contentText = findViewById(R.id.tv_comment_item_content)
        timeText = findViewById(R.id.tv_comment_item_time)
        replyText = findViewById(R.id.tv_comment_item_reply)
        likeView = findViewById(R.id.like_view_comment_item)

        // 设置点击事件
        replyText.setOnClickListener {
            // 通过 tag 获取 CommentItem
            val item = tag as? CommentItem
            item?.let {
                onReplyClickListener?.invoke(it)
            }
        }

        // 设置点赞视图的点击回调
        likeView.onOptionClickListener = {
            // 通过 tag 获取 CommentItem
            val commentItem = tag as? CommentItem
            commentItem?.let {
                //乐观更新
                updateCommentLikeState(commentItem)
                onLikeClickListener?.invoke(it)
            }

        }

        // 设置长按事件
        setOnLongClickListener {
            // 通过 tag 获取 CommentItem
            val item = tag as? CommentItem
            item?.let {
                onLongClickListener?.invoke(it)
            }
            true // 返回 true 表示已处理事件
        }

        setOnClickListener {
            val commentItem = tag as? CommentItem
            commentItem?.let {
                onItemClickListener?.invoke(it)
            }
        }
    }

    /**
     * 乐观更新
     * @param commentItem 评论项
     */
    fun updateCommentLikeState(commentItem: CommentItem) {
        commentItem.updateLikeState()
        // 刷新UI
        likeView.isOptionSelected = commentItem.admired == 1
        likeView.setCountValue(commentItem.likes ?: 0)
    }

    /**
     * 绑定评论项数据
     */
    fun bindCommentItem(item: CommentItem) {
        // 保存 item 到 tag，用于点击事件
        tag = item
        // 用户头像
        val userAvatar = item.avatar
        if (!userAvatar.isNullOrEmpty()) {
            Glide.with(context)
                .load(userAvatar)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .placeholder(R.drawable.generic_avatar_default)
                .error(R.drawable.generic_avatar_default)
                .into(avatarImage)
        } else {
            avatarImage.setImageResource(R.drawable.generic_avatar_default)
        }

        // 用户名称
        nameText.text = item.name ?: ""

        // 作者标签
        authorTag.visibility = if (item.isAuthor == 1) VISIBLE else GONE

        // 评论内容
        val content = item.content ?: ""
        if (!item.replyToUserName.isNullOrEmpty()) {
            // 如果有回复对象，拼接 "回复 用户名: 内容"
            val replyToName = item.replyToUserName
            val fullText = "回复 $replyToName: $content"
            val spannableString = SpannableString(fullText)

            // 获取灰色颜色
            val grayColor = ContextCompat.getColor(context, R.color.text_808080)

            // 设置用户名和冒号为灰色（不包括"回复"）
            val replyPrefix = "回复 "
            val replyPrefixEnd = replyPrefix.length
            val userNameEnd = replyPrefixEnd + replyToName.length + 1 // +1 是冒号
            spannableString.setSpan(
                ForegroundColorSpan(grayColor),
                replyPrefixEnd,
                userNameEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // 内容部分保持默认颜色（text_000000）
            contentText.text = spannableString
        } else {
            // 没有回复对象，直接显示内容
            contentText.text = content
        }
        // 创建时间
        timeText.text = "${item.time} ${item.location}"
        // 设置点赞状态和数量（可以根据 item 中的点赞状态来设置）
        likeView.isOptionSelected = item.admired == 1
        likeView.setCountValue(item.likes ?: 0)
    }
}