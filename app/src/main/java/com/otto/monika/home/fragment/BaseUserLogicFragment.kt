package com.otto.monika.home.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.otto.monika.common.utils.MonikaBottomSheetBehavior
import com.otto.monika.common.utils.DipUtils
import com.otto.monika.common.utils.StatusBarUtil
import com.otto.monika.databinding.FragmentMyPageBinding
import kotlin.math.abs

/**
 * 用户页面逻辑基类
 * 封装了用户页面通用的高度计算和滚动监听逻辑
 */
abstract class BaseUserLogicFragment : BaseHomeFragment() {

    protected lateinit var myPageBinding: FragmentMyPageBinding

    private var transViewHeight: Int = 0
    private var alphaChangeHeight: Int = 0

    /**
     * 上次的 offset 值，用于检测滚动是否停止和判断方向
     */
    private var lastOffset: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myPageBinding = FragmentMyPageBinding.inflate(inflater)
        return myPageBinding.root
    }


    fun updatePeekHeight(hasActionBar: Boolean = false) {
        val coordinatorViewPadding = getCoordinatorViewPadding(hasActionBar)
        val behavior = BottomSheetBehavior.from(myPageBinding.bottomSheet)
        // headerView高度
        val headerViewHeight = myPageBinding.viewAccountHead.getAccountHeadDropDownViewHeight()
        // 设置 peekHeight（折叠状态时显示的高度 = TextView高度 + Header高度）
        val peekHeight = transViewHeight + headerViewHeight + coordinatorViewPadding
        behavior.peekHeight = peekHeight
    }

    private fun getCoordinatorViewPadding(hasActionBar: Boolean = false): Int {
        //没有actionBar的情况下才需要去计算statusBar和导航栏
        val coordinatorViewPadding = if (hasActionBar.not()) {
            //(状态栏+顶部导航栏)
            StatusBarUtil.getStatusBarHeight(requireContext()) + DipUtils.dpToPx(42)
        } else {
            0
        }
        return coordinatorViewPadding
    }

    /**
     * 设置 BottomSheetBehavior
     * 注意：需要在 View 布局完成后调用，所以使用 post
     */
    fun initBottomSheetBehavior(hasActionBar: Boolean = false) {
        val behavior = BottomSheetBehavior.from(myPageBinding.bottomSheet)
        //没有actionBar的情况下才需要去计算statusBar和导航栏
        val coordinatorViewPadding = getCoordinatorViewPadding(hasActionBar)
        myPageBinding.navBarMaskView.layoutParams?.let {
            it.height = coordinatorViewPadding
        }
        myPageBinding.innerCoordinatorLayout.updatePadding(top = coordinatorViewPadding)
        transViewHeight = DipUtils.dpToPx(80)
        //设置透明占位view的高度
        myPageBinding.transView.layoutParams?.let {
            it.height = transViewHeight
            myPageBinding.transView.layoutParams = it
        }
        updatePeekHeight(hasActionBar)
        behavior.isHideable = false
        behavior.skipCollapsed = false
        // 注意：初始状态在 CtyBottomSheetBehavior 构造函数中已设置为 STATE_EXPANDED
        // 这样在 onLayoutChild 时会直接设置位置，不会有动画效果
        myPageBinding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (behavior is MonikaBottomSheetBehavior) {
                behavior.setVerticalOffset(verticalOffset * 1.0f)
            }
            // 当AppBarLayout完全展开时，允许BottomSheet展开
            if (verticalOffset == 0) {
                behavior.isDraggable = true
            } else {
                behavior.isDraggable = false
            }
            val absVOffset = abs(verticalOffset)
            if (absVOffset != 0) {
                var alpha = absVOffset * 1.0f / alphaChangeHeight
                if (alpha > 1.0f) {
                    alpha = 1.0f
                }
                onNavBarAlphaChange(alpha)
            } else {
                onNavBarAlphaChange(0.0f)
            }
            lastOffset = verticalOffset
        }

        // 添加状态监听
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: android.view.View, newState: Int) {
                val stateName = when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> "EXPANDED"
                    BottomSheetBehavior.STATE_COLLAPSED -> "COLLAPSED"
                    BottomSheetBehavior.STATE_DRAGGING -> "DRAGGING"
                    BottomSheetBehavior.STATE_SETTLING -> "SETTLING"
                    BottomSheetBehavior.STATE_HIDDEN -> "HIDDEN"
                    else -> "UNKNOWN($newState)"
                }
            }

            override fun onSlide(bottomSheet: android.view.View, slideOffset: Float) {
                myPageBinding.accountBackdropWall.setOverlayAlpha(slideOffset)
                myPageBinding.viewAccountHead.updateDropDownState(slideOffset == 0f)
            }
        })
    }


    /**
     * 设置到展开状态（STATE_EXPANDED）
     * BottomSheet 默认状态为 STATE_EXPANDED
     */
    private fun scrollToExpanded() {
        myPageBinding.bottomSheet.post {
            val behavior = BottomSheetBehavior.from(myPageBinding.bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onNavBarAlphaChange(value: Float) {
        super.onNavBarAlphaChange(value)
        myPageBinding.navBarMaskView.setBackgroundColor(
            Color.argb(
                (value * 255).toInt(),
                255,
                255,
                255
            )
        )
    }
}