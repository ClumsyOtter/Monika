package com.otto.monika.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.util.AttributeSet
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat

internal class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var mListener: Animation.AnimationListener? = null
    var mShadowRadius: Int

    init {
        val density = this.context.resources.displayMetrics.density
        val shadowYOffset = (density * 1.75f).toInt()
        val shadowXOffset = (density * 0.0f).toInt()
        this.mShadowRadius = (density * 3.5f).toInt()
        val circle: ShapeDrawable?
        if (this.elevationSupported()) {
            circle = ShapeDrawable(OvalShape())
            ViewCompat.setElevation(this, 4.0f * density)
        } else {
            val oval: OvalShape = OvalShadow(this.mShadowRadius)
            circle = ShapeDrawable(oval)
            this.setLayerType(LAYER_TYPE_SOFTWARE, circle.paint)
            circle.paint.setShadowLayer(
                this.mShadowRadius.toFloat(),
                shadowXOffset.toFloat(),
                shadowYOffset.toFloat(),
                503316480
            )
            val padding = this.mShadowRadius
            this.setPadding(padding, padding, padding, padding)
        }
        this.background = circle
    }

    private fun elevationSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 21
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!this.elevationSupported()) {
            this.setMeasuredDimension(
                this.measuredWidth + this.mShadowRadius * 2,
                this.measuredHeight + this.mShadowRadius * 2
            )
        }
    }

    fun setAnimationListener(listener: Animation.AnimationListener?) {
        this.mListener = listener
    }

    public override fun onAnimationStart() {
        super.onAnimationStart()
        if (this.mListener != null) {
            this.mListener!!.onAnimationStart(this.getAnimation())
        }
    }

    public override fun onAnimationEnd() {
        super.onAnimationEnd()
        if (this.mListener != null) {
            this.mListener!!.onAnimationEnd(this.getAnimation())
        }
    }

    fun setBackgroundColorRes(colorRes: Int) {
        this.setBackgroundColor(ContextCompat.getColor(this.getContext(), colorRes))
    }

    override fun setBackgroundColor(color: Int) {
        if (this.background is ShapeDrawable) {
            (this.background as ShapeDrawable).paint.setColor(color)
        }
    }

    private inner class OvalShadow(shadowRadius: Int) : OvalShape() {
        private var mRadialGradient: RadialGradient? = null
        private val mShadowPaint = Paint()

        init {
            this@CircleImageView.mShadowRadius = shadowRadius
            this.updateRadialGradient(this.rect().width().toInt())
        }

        override fun onResize(width: Float, height: Float) {
            super.onResize(width, height)
            this.updateRadialGradient(width.toInt())
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            val viewWidth = this@CircleImageView.getWidth()
            val viewHeight = this@CircleImageView.getHeight()
            canvas.drawCircle(
                (viewWidth / 2).toFloat(),
                (viewHeight / 2).toFloat(),
                (viewWidth / 2).toFloat(),
                this.mShadowPaint
            )
            canvas.drawCircle(
                (viewWidth / 2).toFloat(),
                (viewHeight / 2).toFloat(),
                (viewWidth / 2 - this@CircleImageView.mShadowRadius).toFloat(),
                paint
            )
        }

        fun updateRadialGradient(diameter: Int) {
            this.mRadialGradient = RadialGradient(
                (diameter / 2).toFloat(),
                (diameter / 2).toFloat(),
                this@CircleImageView.mShadowRadius.toFloat(),
                intArrayOf(1023410176, 0),
                null,
                Shader.TileMode.CLAMP
            )
            this.mShadowPaint.setShader(this.mRadialGradient)
        }
    }
}
