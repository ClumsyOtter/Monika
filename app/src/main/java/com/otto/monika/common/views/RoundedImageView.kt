package com.otto.monika.common.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.otto.monika.R
import com.otto.monika.common.views.RoundedImageView
import androidx.core.content.withStyledAttributes

class RoundedImageView : AppCompatImageView {
    var cornerRadius: Float = DEFAULT_RADIUS
        private set
    private var borderWidth: Float = DEFAULT_BORDER_WIDTH
    var borderColors: ColorStateList? = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
    private var isOval = false
    private var mutateBackground = false
    private var tileModeX: Shader.TileMode? = DEFAULT_TILE_MODE
    private var tileModeY: Shader.TileMode? = DEFAULT_TILE_MODE

    private var mResource = 0
    private var mDrawable: Drawable? = null
    private var mBackgroundDrawable: Drawable? = null

    private var mScaleType: ScaleType? = null

    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attrs,
        defStyle
    ) {
        context.withStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0) {

            val index = getInt(R.styleable.RoundedImageView_android_scaleType, -1)
            if (index >= 0) {
                setScaleType(SCALE_TYPES[index]!!)
            } else {
                // default scaletype to FIT_CENTER
                setScaleType(ScaleType.FIT_CENTER)
            }

            cornerRadius =
                getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius, -1).toFloat()
            borderWidth =
                getDimensionPixelSize(R.styleable.RoundedImageView_riv_border_width, -1).toFloat()

            // don't allow negative values for radius and border
            if (cornerRadius < 0) {
                cornerRadius = DEFAULT_RADIUS
            }
            if (borderWidth < 0) {
                borderWidth = DEFAULT_BORDER_WIDTH
            }

            borderColors = getColorStateList(R.styleable.RoundedImageView_riv_border_color)
            if (borderColors == null) {
                borderColors = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
            }

            mutateBackground = getBoolean(R.styleable.RoundedImageView_riv_mutate_background, false)
            isOval = getBoolean(R.styleable.RoundedImageView_riv_oval, false)

            val tileMode = getInt(R.styleable.RoundedImageView_riv_tile_mode, TILE_MODE_UNDEFINED)
            if (tileMode != TILE_MODE_UNDEFINED) {
                setTileModeX(parseTileMode(tileMode))
                setTileModeY(parseTileMode(tileMode))
            }

            val tileModeX =
                getInt(R.styleable.RoundedImageView_riv_tile_mode_x, TILE_MODE_UNDEFINED)
            if (tileModeX != TILE_MODE_UNDEFINED) {
                setTileModeX(parseTileMode(tileModeX))
            }

            val tileModeY =
                getInt(R.styleable.RoundedImageView_riv_tile_mode_y, TILE_MODE_UNDEFINED)
            if (tileModeY != TILE_MODE_UNDEFINED) {
                setTileModeY(parseTileMode(tileModeY))
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(true)

        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see ScaleType
     */
    override fun getScaleType(): ScaleType? {
        return mScaleType
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    override fun setScaleType(scaleType: ScaleType) {
        checkNotNull(scaleType)

        if (mScaleType != scaleType) {
            mScaleType = scaleType

            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )

                else -> super.setScaleType(scaleType)
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(getDrawable())
    }

    private fun resolveResource(): Drawable? {
        val rsrc = getResources()
        if (rsrc == null) {
            return null
        }

        var d: Drawable? = null

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: " + mResource, e)
                // Don't try again.
                mResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    override fun setBackground(background: Drawable?) {
        setBackgroundDrawable(background)
    }

    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable)
    }

    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable)
            }
            updateAttrs(mBackgroundDrawable)
        }
    }

    private fun updateAttrs(drawable: Drawable?) {
        if (drawable == null) {
            return
        }

        if (drawable is RoundedDrawable) {
            (drawable as RoundedDrawable)
                .setScaleType(mScaleType)
                .setCornerRadius(cornerRadius)
                .setBorderWidth(borderWidth)
                .setBorderColor(this.borderColors)
                .setOval(isOval)
                .setTileModeX(tileModeX)
                .setTileModeY(tileModeY)
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            val ld = drawable
            var i = 0
            val layers = ld.getNumberOfLayers()
            while (i < layers) {
                updateAttrs(ld.getDrawable(i))
                i++
            }
        }
    }

    @Deprecated("")
    override fun setBackgroundDrawable(background: Drawable?) {
        mBackgroundDrawable = background
        updateBackgroundDrawableAttrs(true)
        super.setBackgroundDrawable(mBackgroundDrawable)
    }

    fun setCornerRadius(resId: Int) {
        setCornerRadius(getResources().getDimension(resId))
    }

    fun setCornerRadius(radius: Float) {
        if (cornerRadius == radius) {
            return
        }

        cornerRadius = radius
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
    }

    fun getBorderWidth(): Float {
        return borderWidth
    }

    fun setBorderWidthResId(resId: Int) {
        setBorderWidth(getResources().getDimension(resId))
    }

    fun setBorderWidth(width: Float) {
        if (borderWidth == width) {
            return
        }

        borderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun getBorderColor(): Int {
        return borderColors!!.getDefaultColor()
    }

    fun setBorderColor(color: Int) {
        setBorderColor(ColorStateList.valueOf(color))
    }

    fun setBorderColor(colors: ColorStateList?) {
        if (this.borderColors == colors) {
            return
        }

        this.borderColors =
            if (colors != null) colors else ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (borderWidth > 0) {
            invalidate()
        }
    }

    fun isOval(): Boolean {
        return isOval
    }

    fun setOval(oval: Boolean) {
        isOval = oval
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun getTileModeX(): Shader.TileMode? {
        return tileModeX
    }

    fun setTileModeX(tileModeX: Shader.TileMode?) {
        if (this.tileModeX == tileModeX) {
            return
        }

        this.tileModeX = tileModeX
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun getTileModeY(): Shader.TileMode? {
        return tileModeY
    }

    fun setTileModeY(tileModeY: Shader.TileMode?) {
        if (this.tileModeY == tileModeY) {
            return
        }

        this.tileModeY = tileModeY
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun mutatesBackground(): Boolean {
        return mutateBackground
    }

    fun mutateBackground(mutate: Boolean) {
        if (mutateBackground == mutate) {
            return
        }

        mutateBackground = mutate
        updateBackgroundDrawableAttrs(true)
        invalidate()
    }

    companion object {
        // Constants for tile mode attributes
        private const val TILE_MODE_UNDEFINED = -2
        private const val TILE_MODE_CLAMP = 0
        private const val TILE_MODE_REPEAT = 1
        private const val TILE_MODE_MIRROR = 2

        const val TAG: String = "RoundedImageView"
        const val DEFAULT_RADIUS: Float = 0f
        const val DEFAULT_BORDER_WIDTH: Float = 0f
        val DEFAULT_TILE_MODE: Shader.TileMode = Shader.TileMode.CLAMP
        private val SCALE_TYPES = arrayOf<ScaleType?>(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )

        private fun parseTileMode(tileMode: Int?): Shader.TileMode? {
            return when (tileMode) {
                TILE_MODE_CLAMP -> Shader.TileMode.CLAMP
                TILE_MODE_REPEAT -> Shader.TileMode.REPEAT
                TILE_MODE_MIRROR -> Shader.TileMode.MIRROR
                else -> null
            }
        }
    }
}