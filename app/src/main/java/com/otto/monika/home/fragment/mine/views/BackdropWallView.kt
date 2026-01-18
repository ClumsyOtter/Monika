package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.otto.common.utils.DipUtils
import com.otto.monika.R
import com.otto.monika.common.views.RoundedImageView

/**
 * 背景墙View
 * 动态添加ImageView，形成网格布局
 */
class BackdropWallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var itemWidthDp = 100  // ImageView宽度
    private var itemHeightDp = 110  // ImageView高度
    private var startXDp = -20  // 起始X坐标
    private var startYDp = -50 // 起始Y坐标

    private var itemGapDp = 10
    private var itemWidthPx = 0
    private var itemHeightPx = 0
    private var startXPx = 0
    private var startYPx = 0

    private var itemGapPx = 0

    private var viewWidth = 0
    private var viewHeight = 0
    private var isInitialized = false
    private var imageUrls: List<String>? = null

    init {
        // 设置不裁剪子View，允许子View超出边界显示
        clipChildren = false
        clipToPadding = false

        // 转换dp为px
        itemWidthPx = DipUtils.dpToPx(itemWidthDp)
        itemHeightPx = DipUtils.dpToPx(itemHeightDp)
        startXPx = DipUtils.dpToPx(startXDp)
        startYPx = DipUtils.dpToPx(startYDp)
        itemGapPx = DipUtils.dpToPx(itemGapDp)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && (w != oldw || h != oldh)) {
            viewWidth = w
            viewHeight = h
            addImageViews()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left
        val height = bottom - top
        if (width > 0 && height > 0 && changed) {
            isInitialized = true
            viewWidth = width
            viewHeight = height
            addImageViews()
        }
    }

    /**
     * 添加ImageView
     * 从起始位置开始，一列一列添加，每列从上到下添加，直到超出高度
     */
    private fun addImageViews() {
        // 清除之前的子View
        removeAllViews()

        if (viewWidth <= 0 || viewHeight <= 0) {
            return
        }
        var currentX = startXPx
        var currentY = startYPx
        var imageIndex = 0
        var columnIndex = 0

        // 循环添加列，直到x超出view宽度
        while (currentX < viewWidth) {
            // 重置当前列的Y坐标
            currentY = startYPx + (if (columnIndex % 2 == 1) DipUtils.dpToPx(30) else 0)

            // 循环添加行，直到y超出view高度
            while (currentY < viewHeight) {
                // 创建ImageView
                val imageView = RoundedImageView(context)
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)

                // 设置圆角（如果需要）
                imageView.setCornerRadius(DipUtils.dip2px(context, 10f).toFloat())

                // 创建LayoutParams
                val layoutParams = LayoutParams(itemWidthPx, itemHeightPx)
                layoutParams.leftMargin = currentX
                layoutParams.topMargin = currentY

                // 添加到父容器
                addView(imageView, layoutParams)

                // 如果有图片URL列表，加载图片（循环使用URL）
                imageUrls?.let { urls ->
                    if (urls.isNotEmpty()) {
                        val urlIndex = imageIndex % urls.size
                        loadImage(imageView, urls[urlIndex])
                    } else {
                        // URL列表为空，设置默认图片
                        imageView.setImageResource(R.drawable.generic_avatar_default)
                    }
                } ?: run {
                    // 没有URL列表，设置默认图片
                    imageView.setImageResource(R.drawable.generic_avatar_default)
                }

                imageIndex++

                // 移动到下一行（垂直方向）
                currentY += itemHeightPx + itemGapPx
            }

            // 移动到下一列（水平方向）
            currentX += itemWidthPx + itemGapPx
            columnIndex++
        }
    }

    /**
     * 使用Glide加载图片
     */
    private fun loadImage(imageView: RoundedImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            // URL为空时设置默认占位图
            imageView.setImageResource(R.drawable.generic_avatar_default)
            return
        }

        try {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.generic_avatar_default)
                .error(R.drawable.generic_avatar_default)
                .centerCrop()
                .into(imageView)
        } catch (e: Exception) {
            // 加载失败时设置默认图片
            imageView.setImageResource(R.drawable.generic_avatar_default)
        }
    }

    /**
     * 获取所有ImageView
     */
    fun getImageViews(): List<RoundedImageView> {
        val imageViews = mutableListOf<RoundedImageView>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is RoundedImageView) {
                imageViews.add(child)
            }
        }
        return imageViews
    }

    /**
     * 刷新布局（当View大小改变时调用）
     */
    fun refreshLayout() {
        if (viewWidth > 0 && viewHeight > 0) {
            removeAllViews()
            addImageViews()
        }
    }

    /**
     * 设置图片URL列表
     * @param urls 图片URL列表，会循环使用这些URL填充所有ImageView
     */
    fun setImageUrls(urls: List<String>?) {
        imageUrls = urls
        // 如果已经初始化，重新加载图片
        if (isInitialized) {
            loadImagesToViews()
        }
    }

    /**
     * 将图片URL加载到已创建的ImageView中
     */
    private fun loadImagesToViews() {
        val imageViews = getImageViews()
        if (imageUrls != null && imageUrls?.isNotEmpty() == true) {
            imageViews.forEachIndexed { index, imageView ->
                if (index < imageUrls!!.size) {
                    loadImage(imageView, imageUrls!![index])
                } else {
                    // 如果URL数量少于ImageView数量，循环使用
                    val urlIndex = index % imageUrls!!.size
                    loadImage(imageView, imageUrls!![urlIndex])
                }
            }
        }
    }
}
