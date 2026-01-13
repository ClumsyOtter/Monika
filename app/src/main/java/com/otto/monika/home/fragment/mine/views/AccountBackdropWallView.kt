package com.otto.monika.home.fragment.mine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.otto.monika.R

/**
 * 账户背景墙 View
 * 四列瀑布流布局，第一列和第三列平行，第二列和第四列平行
 * 第一列和第二列高度差为30dp
 * 第一列和第四列部分在屏幕外
 * 支持覆盖层和透明度设置
 */
class AccountBackdropWallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var overlayView: View
    private var backdropWallView: BackdropWallView

    // 当前图片列表（用于判断是否需要重新设置）
    private var currentImageList: List<String>? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_account_backdrop_wall, this, true)
        overlayView = findViewById(R.id.v_backdrop_overlay)
        backdropWallView = findViewById(R.id.bw_backdrop_wall)
    }

    /**
     * 设置图片列表
     * 如果图片列表没有变化（比较图片链接内容），则不需要重新设置
     */
    fun setImageList(images: List<String>?) {
        // 比较图片列表内容（图片链接），如果内容相同则不需要重新设置
        if (isImageListEqual(currentImageList, images)) {
            return
        }
        
        // 更新当前图片列表
        currentImageList = images
        
        // 设置图片列表
        backdropWallView.setImageUrls(images)
    }

    /**
     * 比较两个图片列表的内容是否相同
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @return 如果列表内容相同（图片链接相同）返回 true，否则返回 false
     */
    private fun isImageListEqual(list1: List<String>?, list2: List<String>?): Boolean {
        // 如果两个都是 null，认为相同
        if (list1 == null && list2 == null) {
            return true
        }
        // 如果一个是 null 另一个不是，认为不同
        if (list1 == null || list2 == null) {
            return false
        }
        // 比较列表大小
        if (list1.size != list2.size) {
            return false
        }
        // 逐个比较图片链接
        return list1.zip(list2).all { (url1, url2) -> url1 == url2 }
    }

    /**
     * 设置覆盖层透明度
     * @param alpha 透明度值，范围 0.0f - 1.0f，0.0f 为完全透明，1.0f 为完全不透明
     */
    fun setOverlayAlpha(alpha: Float) {
        overlayView.alpha = alpha.coerceIn(0f, 1f)
    }

    /**
     * 设置覆盖层颜色
     * @param color 颜色值（ColorInt）
     */
    fun setOverlayColor(color: Int) {
        overlayView.setBackgroundColor(color)
    }

}
