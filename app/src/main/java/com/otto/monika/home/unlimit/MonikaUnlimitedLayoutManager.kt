package com.otto.monika.home.unlimit

import android.animation.ValueAnimator
import android.graphics.Point
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class MonikaUnlimitedLayoutManager @JvmOverloads constructor(
    val spanCount: Int, private val startPosition: Int = -1
) : RecyclerView.LayoutManager() {

    
    private var viewGap = 0f

    val rowCount: Int by lazy {
        ceil(itemCount / spanCount.toFloat()).toInt()
    }

    private var onceCompleteScrollLengthForVer = -1f
    private var onceCompleteScrollLengthForHor = -1f

    private var firstChildCompleteScrollLengthForVer = -1f
    private var firstChildCompleteScrollLengthForHor = -1f

    private var verScrollLock = false
    private var horScrollLock = false

    private var firstVisiblePos = 0

    var childHeight = 0
        private set

    var childWidth = 0
        private set

    // 缓存每个 view 的上次 scale 值，避免不必要的更新
    private val viewScaleCache = mutableMapOf<View, Float>()

    private var verticalOffset: Long = 0
    private var horizontalOffset: Long = 0

    private val verticalMaxOffset: Float
        get() = if (childHeight == 0 || itemCount == 0) 0f else (childHeight + viewGap) * (rowCount - 1)

    private val verticalMinOffset: Float
        get() = if (childHeight == 0) 0f else (height - childHeight) / 2f

    private val horizontalMaxOffset: Float
        get() = if (childWidth == 0 || itemCount == 0) 0f else (childWidth + viewGap) * (spanCount - 1)

    private val horizontalMinOffset: Float
        get() = if (childWidth == 0) 0f else (width - childWidth) / 2f

    private var selectAnimator: ValueAnimator? = null

    /**
     * 使用 SnapHelper 自动选中最靠近中心的 Item，默认为true
     */
    var isAutoSelect = true

    /**
     * 初始化布局的时候，是否定位到中心的 Item
     */
    var isInitLayoutCenter = true
    private var isFirstLayout = true

    private var lastSelectedPosition = 0
    private var onItemSelectedListener: (Int) -> Unit = {}

    /**
     * 是否需要自动改变背景色（根据缩放比例渐变）
     * true: 根据缩放比例在选中色和未选中色之间渐变
     * false: 使用固定颜色（中心位置使用选中色，其他使用未选中色）
     */
    var autoChangeItemColor: Boolean = false

    /**
     * 选中时的背景颜色
     */
    var selectedBackgroundColor = android.graphics.Color.WHITE

    /**
     * 未选中时的背景颜色
     */
    var unselectedBackgroundColor = android.graphics.Color.GRAY

    /**
     * 设置 item 之间的间距（单位：px）
     *
     * 注意：
     * - 该间距同时作用于水平方向和垂直方向
     * - 修改后会触发布局刷新
     */
    fun setItemGap(gapPx: Float) {
        if (gapPx == viewGap) return
        viewGap = gapPx
        requestLayout()
    }

    /**
     * 滑动到指定位置
     */
    fun smoothScrollToPosition(position: Int, success: (() -> Boolean)? = null) {
        if (position > -1 && position < itemCount) {
            startValueAnimator(position, success)
        }
    }

    /**
     * 滑动到中心
     */
    fun smoothScrollToCenter() {
        val centerPos = rowCount / 2 * spanCount + spanCount / 2
        smoothScrollToPosition(centerPos)
    }

    /**
     * 设置选中 Item 的监听回调
     */
    fun setOnItemSelectedListener(listener: (Int) -> Unit) {
        onItemSelectedListener = listener
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            viewScaleCache.clear()
            return
        }
        onceCompleteScrollLengthForVer = -1f
        onceCompleteScrollLengthForHor = -1f

        detachAndScrapAttachedViews(recycler)

        onLayout(recycler, 0, 0)
    }

    private fun onLayout(recycler: RecyclerView.Recycler, dx: Int, dy: Int): Point {
        val pointResult = checkMoveLimit(dy, dx)

        // 清理已移除的 view 的缓存
        val currentViews = mutableSetOf<View>()
        for (i in 0 until childCount) {
            getChildAt(i)?.let { currentViews.add(it) }
        }
        viewScaleCache.keys.removeAll { it !in currentViews }

        detachAndScrapAttachedViews(recycler)

        var verStart: Float
        var horStart: Float

        val normalViewOffsetForVer: Float
        val normalViewOffsetForHor: Float

        var tempView: View? = null
        var tempPosition = -1

        val firstVisiblePosForVer: Int
        val firstVisiblePosForHor: Int

        if (onceCompleteScrollLengthForVer == -1f) {
            tempPosition = firstVisiblePos
            tempView = recycler.getViewForPosition(tempPosition)
            measureChildWithMargins(tempView, 0, 0)
            childHeight = getDecoratedMeasurementVertical(tempView)
            childWidth = getDecoratedMeasurementHorizontal(tempView)
        }

        firstChildCompleteScrollLengthForVer = height / 2f + childHeight / 2f
        firstChildCompleteScrollLengthForHor = width / 2f + childWidth / 2f

        if (isInitLayoutCenter && isFirstLayout) {
            isFirstLayout = false
            val centerPos = rowCount / 2 * spanCount + spanCount / 2
            val startPos = if (startPosition != -1) startPosition else centerPos
            onItemSelectedListener(startPos)
            lastSelectedPosition = startPos
            val distanceForVer = calculateScrollToPositionVerOffset(startPos)
            val distanceForHor = calculateScrollToPositionHorOffset(startPos)
            verticalOffset += distanceForVer.toLong()
            horizontalOffset += distanceForHor.toLong()
        }

        if (verticalOffset >= firstChildCompleteScrollLengthForVer) {
            verStart = viewGap
            onceCompleteScrollLengthForVer = childHeight + viewGap
            firstVisiblePosForVer = floor(abs(verticalOffset - firstChildCompleteScrollLengthForVer) / onceCompleteScrollLengthForVer.toDouble()).toInt() + 1
            normalViewOffsetForVer = abs(verticalOffset - firstChildCompleteScrollLengthForVer) % onceCompleteScrollLengthForVer
        } else {
            firstVisiblePosForVer = 0
            verStart = verticalMinOffset
            onceCompleteScrollLengthForVer = firstChildCompleteScrollLengthForVer
            normalViewOffsetForVer = abs(verticalOffset) % onceCompleteScrollLengthForVer
        }

        if (horizontalOffset >= firstChildCompleteScrollLengthForHor) {
            horStart = viewGap
            onceCompleteScrollLengthForHor = childWidth + viewGap
            firstVisiblePosForHor = floor(abs(horizontalOffset - firstChildCompleteScrollLengthForHor) / onceCompleteScrollLengthForHor.toDouble()).toInt() + 1
            normalViewOffsetForHor = abs(horizontalOffset - firstChildCompleteScrollLengthForHor) % onceCompleteScrollLengthForHor
        } else {
            firstVisiblePosForHor = 0
            horStart = horizontalMinOffset
            onceCompleteScrollLengthForHor = firstChildCompleteScrollLengthForHor
            normalViewOffsetForHor = abs(horizontalOffset) % onceCompleteScrollLengthForHor
        }
        firstVisiblePos = firstVisiblePosForVer * spanCount + firstVisiblePosForHor

        verStart -= normalViewOffsetForVer
        horStart -= normalViewOffsetForHor
        val startLeft = horStart

        var index = firstVisiblePos
        var verCount = 1

        while (index != -1) {
            val item = if (index == tempPosition && tempView != null) {
                tempView
            } else {
                recycler.getViewForPosition(index)
            }

            val focusPositionForVer = (abs(verticalOffset) / (childHeight + viewGap)).toInt()
            val focusPositionForHor = (abs(horizontalOffset) / (childWidth + viewGap)).toInt()
            val focusPosition = focusPositionForVer * spanCount + focusPositionForHor

            if (index <= focusPosition) {
                addView(item)
            } else {
                addView(item, 0)
            }

            // 测量 view
            // 注意：当 ViewHolder 被复用时，如果内容变化（如 TextView 从多行变少行），
            // 需要重新测量才能正确计算高度。因此不能只检查 measuredWidth/measuredHeight 是否为 0，
            // 还需要检查 view 是否需要重新布局（通过 isLayoutRequested() 判断）
            // 如果 view 请求了重新布局，说明内容可能发生了变化，需要重新测量
            if (item.measuredWidth == 0 || item.measuredHeight == 0 || item.isLayoutRequested) {
                measureChildWithMargins(item, 0, 0)
            }

            val left = horStart.toInt()
            val top = verStart.toInt()
            val right = left + getDecoratedMeasurementHorizontal(item)
            val bottom = top + getDecoratedMeasurementVertical(item)

            val minScale = .8f

            val childCenterY = (top + bottom) / 2
            val parentCenterY = height / 2
            val fractionScaleY = abs(parentCenterY - childCenterY) / parentCenterY.toFloat()
            val scaleX = 1.0f - (1.0f - minScale) * fractionScaleY

            val childCenterX = (right + left) / 2
            val parentCenterX = width / 2
            val fractionScaleX = abs(parentCenterX - childCenterX) / parentCenterX.toFloat()
            val scaleY = 1.0f - (1.0f - minScale) * fractionScaleX

            val currentScale = max(min(scaleX, scaleY), minScale)

            // 只有 scale 值发生变化时才更新，减少不必要的属性设置
            val lastScale = viewScaleCache[item]
            if (lastScale == null || abs(lastScale - currentScale) > 0.01f) {
                item.scaleX = currentScale
                item.scaleY = currentScale
                viewScaleCache[item] = currentScale

                // 设置背景颜色（只在 scale 变化时更新）
                if (autoChangeItemColor) {
                    val color = interpolateColor(unselectedBackgroundColor, selectedBackgroundColor, currentScale, minScale, 1.0f)
                    item.setBackgroundColor(color)
                }
            }

            layoutDecoratedWithMargins(item, left, top, right, bottom)

            val verticalIndex = {
                verStart += childHeight + viewGap
                horStart = startLeft
                if (verStart > height - paddingBottom) {
                    index = -1
                } else {
                    index = firstVisiblePos + verCount * spanCount
                    verCount++
                }
            }

            if ((index + 1) % spanCount != 0) {
                horStart += childWidth + viewGap
                if (horStart > width - paddingRight) {
                    verticalIndex()
                } else {
                    index++
                }
            } else {
                verticalIndex()
            }

            if (index >= itemCount) {
                index = -1
            }
        }
        verScrollLock = false
        horScrollLock = false
        return pointResult
    }

    private fun checkMoveLimit(dy: Int, dx: Int): Point {
        var dyResult = dy
        var dxResult = dx

        if (dyResult < 0) {
            if (verticalOffset < 0) {
                verticalOffset = 0.also { dyResult = it }.toLong()
            }
        }
        if (dyResult > 0) {
            if (verticalOffset >= verticalMaxOffset) {
                verticalOffset = verticalMaxOffset.toLong()
                dyResult = 0
            }
        }

        if (dxResult < 0) {
            if (horizontalOffset < 0) {
                horizontalOffset = 0.also { dxResult = it }.toLong()
            }
        }
        if (dxResult > 0) {
            if (horizontalOffset >= horizontalMaxOffset) {
                horizontalOffset = horizontalMaxOffset.toLong()
                dxResult = 0
            }
        }
        return Point(dxResult, dyResult)
    }

    override fun canScrollHorizontally() = horScrollLock.not()

    override fun canScrollVertically() = verScrollLock.not()

    override fun scrollHorizontallyBy(
        dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State
    ): Int {
        if (dx == 0 || childCount == 0) {
            return 0
        }
        verScrollLock = true
        if (abs(dx.toFloat()) < 0.00000001f) {
            return 0
        }
        horizontalOffset += dx
        return onLayout(recycler, dx, 0).x
    }

    override fun scrollVerticallyBy(
        dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State
    ): Int {
        if (dy == 0 || childCount == 0) {
            return 0
        }
        horScrollLock = true
        if (abs(dy.toFloat()) < 0.00000001f) {
            return 0
        }
        verticalOffset += dy
        return onLayout(recycler, 0, dy).y
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            cancelAnimator()
        }
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        if (isAutoSelect) {
            MonikaSnapHelper().attachToRecyclerView(view)
        }
    }

    fun calculateDistanceToPositionForVer(targetPos: Int): Int {
        return childHeight * (targetPos / spanCount) - verticalOffset.toInt()
    }

    fun calculateDistanceToPositionForHor(targetPos: Int): Int {
        return childWidth * (targetPos % spanCount) - horizontalOffset.toInt()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int
    ) {
        smoothScrollToPosition(position)
    }


    private fun startValueAnimator(position: Int, success: (() -> Boolean)? = null) {
        cancelAnimator()
        val distanceForVer = calculateScrollToPositionVerOffset(position)
        val distanceForHor = calculateScrollToPositionHorOffset(position)

        val minDuration: Long = 200
        val maxDuration: Long = 400

        val distanceFractionForVer: Float = abs(distanceForVer) / (childHeight + viewGap)
        val distanceFractionForHor: Float = abs(distanceForHor) / (childWidth + viewGap)

        val durationForVer = if (distanceForVer <= childHeight + viewGap) {
            (minDuration + (maxDuration - minDuration) * distanceFractionForVer).toLong()
        } else {
            (maxDuration * distanceFractionForVer).toLong()
        }

        val durationForHor = if (distanceForHor <= childWidth + viewGap) {
            (minDuration + (maxDuration - minDuration) * distanceFractionForHor).toLong()
        } else {
            (maxDuration * distanceFractionForHor).toLong()
        }

        val duration = max(durationForVer, durationForHor)

        selectAnimator = ValueAnimator.ofFloat(0.0f, duration.toFloat()).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            isAnimating = true
            val startedOffsetForVer = verticalOffset.toFloat()
            val startedOffsetForHor = horizontalOffset.toFloat()

            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                verticalOffset = (startedOffsetForVer + value * (distanceForVer / duration.toFloat())).toLong()
                horizontalOffset = (startedOffsetForHor + value * (distanceForHor / duration.toFloat())).toLong()

                requestLayout()
            }
            doOnEnd {
                if (lastSelectedPosition != position) {
                    onItemSelectedListener(position)
                    lastSelectedPosition = position
                }
                isAnimating = false

                success?.invoke()
            }
            start()
        }
    }

    var isAnimating: Boolean = false


    fun cancelAnimator() {
        isAnimating = false
        selectAnimator?.takeIf { it.isStarted || it.isRunning }?.apply {
            cancel()
        }
    }

    private fun calculateScrollToPositionVerOffset(position: Int) = position / spanCount * (childHeight + viewGap) - abs(verticalOffset)

    private fun calculateScrollToPositionHorOffset(position: Int) = position % spanCount * (childWidth + viewGap) - abs(horizontalOffset)

    private fun getDecoratedMeasurementHorizontal(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin
    }

    private fun getDecoratedMeasurementVertical(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin
    }

    /**
     * 颜色插值函数 - 根据缩放比例在两个颜色之间渐变
     */
    private fun interpolateColor(
        startColor: Int, endColor: Int, currentValue: Float, minValue: Float, maxValue: Float
    ): Int {
        // 将当前值映射到0-1范围
        val normalizedValue = ((currentValue - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)

        // 提取RGB分量
        val startA = (startColor shr 24) and 0xFF
        val startR = (startColor shr 16) and 0xFF
        val startG = (startColor shr 8) and 0xFF
        val startB = startColor and 0xFF

        val endA = (endColor shr 24) and 0xFF
        val endR = (endColor shr 16) and 0xFF
        val endG = (endColor shr 8) and 0xFF
        val endB = endColor and 0xFF

        // 插值计算
        val currentA = (startA + (endA - startA) * normalizedValue).toInt()
        val currentR = (startR + (endR - startR) * normalizedValue).toInt()
        val currentG = (startG + (endG - startG) * normalizedValue).toInt()
        val currentB = (startB + (endB - startB) * normalizedValue).toInt()

        return (currentA shl 24) or (currentR shl 16) or (currentG shl 8) or currentB
    }
}