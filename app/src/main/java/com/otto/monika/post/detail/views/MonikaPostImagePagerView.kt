package com.otto.monika.post.detail.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.otto.monika.R
import com.otto.monika.post.detail.adapter.PostImagePagerAdapter

/**
 * 帖子图片列表自定义 View
 * 封装了 ViewPager 和图片指示器
 */
class MonikaPostImagePagerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val imageViewPager: ViewPager
    val imageIndicator: TextView

    var onImagesClick: ((imageList: List<String>, index: Int) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post_image_pager, this, true)

        imageViewPager = findViewById(R.id.vp_post_images)
        imageIndicator = findViewById(R.id.tv_post_image_indicator)

        // 设置 ViewPager 页面变化监听
        imageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // 不需要处理
            }

            override fun onPageSelected(position: Int) {
                val adapter = imageViewPager.adapter
                adapter?.let {
                    updateImageIndicator(position, it.count)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // 不需要处理
            }
        })
    }

    /**
     * 设置图片列表
     * @param images 图片URL列表
     */
    fun setImages(images: List<String>) {
        if (images.isNotEmpty()) {
            val imageAdapter = PostImagePagerAdapter(images)
            imageViewPager.adapter = imageAdapter
            imageAdapter.onImageClicked = {
                onImagesClick?.invoke(images,it)
            }
            // 设置图片指示器
            updateImageIndicator(0, images.size)
        } else {
            imageIndicator.visibility = GONE
        }
    }

    /**
     * 更新图片指示器
     * @param currentPosition 当前图片位置（从0开始）
     * @param totalCount 图片总数
     */
    private fun updateImageIndicator(currentPosition: Int, totalCount: Int) {
        val currentPage = currentPosition + 1 // 转换为从1开始
        imageIndicator.text = "$currentPage/$totalCount"
        // 如果只有一张图片，隐藏指示器
        if (totalCount <= 1) {
            imageIndicator.visibility = GONE
        } else {
            imageIndicator.visibility = VISIBLE
        }
    }
}

