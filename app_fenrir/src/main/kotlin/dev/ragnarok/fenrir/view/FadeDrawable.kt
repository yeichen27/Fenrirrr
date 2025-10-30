package dev.ragnarok.fenrir.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import androidx.core.graphics.withRotation
import com.squareup.picasso3.BuildConfig
import com.squareup.picasso3.Picasso.LoadedFrom
import com.squareup.picasso3.Picasso.LoadedFrom.MEMORY
import com.squareup.picasso3.Rotatable
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class FadeDrawable(
    context: Context,
    bitmap: Bitmap,
    loadedFrom: LoadedFrom,
    noFade: Boolean = false
) : BitmapDrawable(context.resources, bitmap), Rotatable {
    private var startTimeMillis: Long = 0
    private var animating = false
    private var _alpha = 0xFF
    private var rotate = 0f

    init {
        val fade = loadedFrom != MEMORY && !noFade
        if (fade) {
            animating = true
            startTimeMillis = SystemClock.uptimeMillis()
        }
    }

    private fun evaluate(fraction: Float, startValue: Float, endValue: Float): Float {
        // convert to linear
        val startValueL = startValue.toDouble().pow(2.2).toFloat()
        val endValueL = endValue.toDouble().pow(2.2).toFloat()

        // compute the interpolated in linear space
        var r = startValueL + fraction * (endValueL - startValueL)
        r = r.toDouble().pow(1.0 / 2.2).toFloat()
        return r
    }

    private fun applyRotationAndDraw(canvas: Canvas) {
        if (rotate > 0f && intrinsicWidth > 0 && intrinsicHeight > 0) {
            canvas.withRotation(rotate, intrinsicWidth / 2f, intrinsicHeight / 2f) {
                val cosR = cos(Math.toRadians(rotate.toDouble())).toFloat()
                val sinR = sin(Math.toRadians(rotate.toDouble())).toFloat()

                val ssx = evaluate(
                    abs(cosR),
                    intrinsicHeight.toFloat(), intrinsicWidth.toFloat()
                ) / intrinsicWidth.toFloat()

                val ssy = evaluate(
                    abs(sinR),
                    intrinsicHeight.toFloat(), intrinsicWidth.toFloat()
                ) / intrinsicHeight.toFloat()

                scale(
                    if (intrinsicWidth > intrinsicHeight) ssx else ssy,
                    if (intrinsicHeight > intrinsicWidth) ssy else ssx,
                    intrinsicWidth / 2f, intrinsicHeight / 2f
                )

                super.draw(this)
            }
        } else {
            super.draw(canvas)
        }
    }

    override fun draw(canvas: Canvas) {
        if (!animating) {
            try {
                applyRotationAndDraw(canvas)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
            }
        } else {
            val normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION
            if (normalized >= 1f) {
                animating = false
                try {
                    applyRotationAndDraw(canvas)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace()
                    }
                }
            } else {
                val partialAlpha = (_alpha * normalized).toInt()
                super.setAlpha(partialAlpha)
                try {
                    applyRotationAndDraw(canvas)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace()
                    }
                }
                super.setAlpha(_alpha)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        this._alpha = alpha
        super.setAlpha(alpha)
    }

    companion object {
        private const val FADE_DURATION = 700f // ms
    }

    override fun rotate(degrees: Float) {
        rotate = degrees
        invalidateSelf()
    }

    override fun getRotation(): Float {
        return rotate
    }
}