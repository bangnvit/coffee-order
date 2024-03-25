package com.bangnv.cafeorder.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.bangnv.cafeorder.R

class AspectRatioNoRadiusImageView @JvmOverloads constructor(context: Context?,
                                                             attrs: AttributeSet? = null,
                                                             defStyleAttr: Int = 0) : AppCompatImageView(context!!, attrs, defStyleAttr) {
    private var aspectRatio = 0f
    private var aspectRatioEnabled = false
    private var dominantMeasurement = 0

    @SuppressLint("CustomViewStyleable")
    private fun loadStateFromAttrs(attributeSet: AttributeSet?) {
        if (attributeSet == null) {
            return
        }
        var a: TypedArray? = null
        try {
            a = context.obtainStyledAttributes(attributeSet, R.styleable.AspectRatioView)
            aspectRatio = a.getFloat(R.styleable.AspectRatioView_aspectRatio, DEFAULT_ASPECT_RATIO)
            aspectRatioEnabled = a.getBoolean(R.styleable.AspectRatioView_aspectRatioEnabled,
                    DEFAULT_ASPECT_RATIO_ENABLED)
            dominantMeasurement = a.getInt(R.styleable.AspectRatioView_dominantMeasurement,
                    DEFAULT_DOMINANT_MEASUREMENT)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            a?.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!aspectRatioEnabled) return
        val newWidth: Int
        val newHeight: Int
        when (dominantMeasurement) {
            MEASUREMENT_WIDTH -> {
                newWidth = measuredWidth
                newHeight = (newWidth * aspectRatio).toInt()
            }
            MEASUREMENT_HEIGHT -> {
                newHeight = measuredHeight
                newWidth = (newHeight * aspectRatio).toInt()
            }
            else -> throw IllegalStateException("Unknown measurement with ID $dominantMeasurement")
        }
        setMeasuredDimension(newWidth, newHeight)
    }

    fun getAspectRatio(): Float {
        return aspectRatio
    }

    fun setAspectRatio(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        if (aspectRatioEnabled) {
            requestLayout()
        }
    }

    fun getAspectRatioEnabled(): Boolean {
        return aspectRatioEnabled
    }

    fun setAspectRatioEnabled(aspectRatioEnabled: Boolean) {
        this.aspectRatioEnabled = aspectRatioEnabled
        requestLayout()
    }

    fun getDominantMeasurement(): Int {
        return dominantMeasurement
    }

    fun setDominantMeasurement(dominantMeasurement: Int) {
        require(!(dominantMeasurement != MEASUREMENT_HEIGHT && dominantMeasurement != MEASUREMENT_WIDTH)) { "Invalid measurement type." }
        this.dominantMeasurement = dominantMeasurement
        requestLayout()
    }

    companion object {
        const val MEASUREMENT_WIDTH = 0
        const val MEASUREMENT_HEIGHT = 1
        private const val DEFAULT_ASPECT_RATIO = 1f
        private const val DEFAULT_ASPECT_RATIO_ENABLED = false
        private const val DEFAULT_DOMINANT_MEASUREMENT = MEASUREMENT_WIDTH
    }

    init {
        loadStateFromAttrs(attrs)
    }
}