package com.otto.monika.common.views

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.createBitmap

class RoundedDrawable(private val mBitmap: Bitmap) : Drawable() {
    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int
    private val mBitmapHeight: Int
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()

    private var mBitmapShader: BitmapShader? = null
    var tileModeX: Shader.TileMode? = Shader.TileMode.CLAMP
        private set
    var tileModeY: Shader.TileMode? = Shader.TileMode.CLAMP
        private set
    private var mRebuildShader = true

    var cornerRadius: Float = 0f
        private set
    var isOval: Boolean = false
        private set
    var borderWidth: Float = 0f
        private set
    var borderColors: ColorStateList = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
        private set
    private var mScaleType: ScaleType = ScaleType.FIT_CENTER

    init {
        mBitmapWidth = mBitmap.getWidth()
        mBitmapHeight = mBitmap.getHeight()
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapPaint = Paint()
        mBitmapPaint.setStyle(Paint.Style.FILL)
        mBitmapPaint.setAntiAlias(true)

        mBorderPaint = Paint()
        mBorderPaint.setStyle(Paint.Style.STROKE)
        mBorderPaint.setAntiAlias(true)
        mBorderPaint.setColor(borderColors.getColorForState(getState(), DEFAULT_BORDER_COLOR))
        mBorderPaint.setStrokeWidth(this.borderWidth)
    }

    override fun isStateful(): Boolean {
        return borderColors.isStateful()
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        if (mBorderPaint.getColor() != newColor) {
            mBorderPaint.setColor(newColor)
            return true
        } else {
            return super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float

        when (mScaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)

                mShaderMatrix.reset()
                mShaderMatrix.setTranslate(
                    ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f).toInt().toFloat(),
                    ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat()
                )
            }

            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)

                mShaderMatrix.reset()

                dx = 0f
                dy = 0f

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + this.borderWidth,
                    (dy + 0.5f).toInt() + this.borderWidth
                )
            }

            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()

                if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    scale = 1.0f
                } else {
                    scale = min(
                        mBounds.width() / mBitmapWidth.toFloat(),
                        mBounds.height() / mBitmapHeight.toFloat()
                    )
                }

                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)

                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset((this.borderWidth) / 2, (this.borderWidth) / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }

        mDrawableRect.set(mBorderRect)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mBounds.set(bounds)

        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader && tileModeX != null && tileModeY != null) {
            mBitmapShader = BitmapShader(mBitmap, tileModeX!!, tileModeY!!)
            if (this.tileModeX == Shader.TileMode.CLAMP && this.tileModeY == Shader.TileMode.CLAMP) {
                mBitmapShader!!.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.setShader(mBitmapShader)
            mRebuildShader = false
        }

        if (this.isOval) {
            if (this.borderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (this.borderWidth > 0) {
                canvas.drawRoundRect(
                    mDrawableRect, max(this.cornerRadius, 0f),
                    max(this.cornerRadius, 0f), mBitmapPaint
                )
                canvas.drawRoundRect(
                    mBorderRect,
                    this.cornerRadius,
                    this.cornerRadius,
                    mBorderPaint
                )
            } else {
                canvas.drawRoundRect(
                    mDrawableRect,
                    this.cornerRadius,
                    this.cornerRadius, mBitmapPaint
                )
            }
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.setAlpha(alpha)
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.setColorFilter(cf)
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.setFilterBitmap(filter)
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    fun setCornerRadius(radius: Float): RoundedDrawable {
        this.cornerRadius = radius
        return this
    }

    fun setBorderWidth(width: Float): RoundedDrawable {
        this.borderWidth = width
        mBorderPaint.setStrokeWidth(this.borderWidth)
        return this
    }

    val borderColor: Int
        get() = borderColors.getDefaultColor()

    fun setBorderColor(color: Int): RoundedDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        this.borderColors = if (colors != null) colors else ColorStateList.valueOf(0)
        mBorderPaint.setColor(borderColors.getColorForState(getState(), DEFAULT_BORDER_COLOR))
        return this
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        this.isOval = oval
        return this
    }

    val scaleType: ScaleType
        get() = mScaleType

    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) {
            scaleType = ScaleType.FIT_CENTER
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    fun setTileModeX(tileModeX: Shader.TileMode?): RoundedDrawable {
        if (this.tileModeX != tileModeX) {
            this.tileModeX = tileModeX
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun setTileModeY(tileModeY: Shader.TileMode?): RoundedDrawable {
        if (this.tileModeY != tileModeY) {
            this.tileModeY = tileModeY
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {
        const val TAG: String = "RoundedDrawable"
        const val DEFAULT_BORDER_COLOR: Int = Color.BLACK

        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
            if (bitmap != null) {
                return RoundedDrawable(bitmap)
            } else {
                return null
            }
        }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val ld = drawable
                    val num = ld.getNumberOfLayers()

                    // loop through layers to and change to RoundedDrawables if possible
                    for (i in 0..<num) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
                    }
                    return ld
                }

                // try to get a bitmap from the drawable and
                val bm: Bitmap? = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                } else {
                    Log.w(TAG, "Failed to create bitmap from drawable!")
                }
            }
            return drawable
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            var bitmap: Bitmap?
            val width = max(drawable.intrinsicWidth, 2)
            val height = max(drawable.intrinsicHeight, 2)
            try {
                bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                drawable.draw(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
                bitmap = null
            }

            return bitmap
        }
    }
}
