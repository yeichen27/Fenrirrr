package dev.ragnarok.fenrir.picasso.transforms.stroke

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap

object ImageWithStrokeHelper {
    fun getRoundedBitmap(
        @ColorInt strokeFirst: Int,
        workBitmap: Bitmap?
    ): Bitmap? {
        workBitmap ?: return null
        val bitmapWidth = workBitmap.width
        val bitmapHeight = workBitmap.height
        val newSize = bitmapWidth.coerceAtMost(bitmapHeight)
        val moveX: Int = (bitmapWidth - newSize) / 2
        val moveY: Int = (bitmapHeight - newSize) / 2
        val isHardware =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && workBitmap.config == Bitmap.Config.HARDWARE

        var output: Bitmap? = null
        val canvas: Canvas
        var obj: Picture? = null
        if (isHardware) {
            obj = Picture()
            canvas = obj.beginRecording(newSize, newSize)
        } else {
            output = createBitmap(
                newSize,
                newSize,
                Bitmap.Config.ARGB_8888
            )
            canvas = Canvas(output)
        }
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.shader = BitmapShader(workBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val useTransformation = moveX != 0 || moveY != 0
        if (useTransformation) {
            canvas.save()
            val matrix = Matrix()
            matrix.preTranslate(-moveX.toFloat(), -moveY.toFloat())
            canvas.setMatrix(matrix)
        }
        canvas.drawOval(
            moveX.toFloat(),
            moveY.toFloat(),
            (newSize + moveX).toFloat(),
            (newSize + moveY).toFloat(),
            paint
        )

        paint.style = Paint.Style.STROKE
        var rdd = 0.066f * newSize
        paint.strokeWidth = rdd
        paint.shader = null
        paint.color = Color.TRANSPARENT
        paint.alpha = 0
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawOval(
            moveX + rdd / 2,
            moveY + rdd / 2,
            (newSize + moveX - rdd / 2),
            (newSize + moveY - rdd / 2),
            paint
        )

        rdd = 0.040f * newSize
        paint.strokeWidth = rdd
        paint.color = strokeFirst
        paint.alpha = 255
        paint.xfermode = null
        canvas.drawOval(
            moveX + rdd / 2,
            moveY + rdd / 2,
            (newSize + moveX - rdd / 2),
            (newSize + moveY - rdd / 2),
            paint
        )
        if (useTransformation) {
            canvas.restore()
        }
        workBitmap.recycle()
        if (isHardware && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            obj?.endRecording()
            output =
                obj?.let { Bitmap.createBitmap(it, it.width, it.height, Bitmap.Config.HARDWARE) }
        }
        return output
    }

    fun getEllipseBitmap(
        @ColorInt strokeFirst: Int,
        workBitmap: Bitmap?,
        angle: Float
    ): Bitmap? {
        workBitmap ?: return null
        val bitmapWidth = workBitmap.width
        val bitmapHeight = workBitmap.height
        val newSize = bitmapWidth.coerceAtMost(bitmapHeight)
        val moveX: Int = (bitmapWidth - newSize) / 2
        val moveY: Int = (bitmapHeight - newSize) / 2
        val isHardware =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && workBitmap.config == Bitmap.Config.HARDWARE

        var output: Bitmap? = null
        val canvas: Canvas
        var obj: Picture? = null
        if (isHardware) {
            obj = Picture()
            canvas = obj.beginRecording(newSize, newSize)
        } else {
            output = createBitmap(
                newSize,
                newSize,
                Bitmap.Config.ARGB_8888
            )
            canvas = Canvas(output)
        }
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.shader = BitmapShader(workBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val useTransformation = moveX != 0 || moveY != 0
        if (useTransformation) {
            canvas.save()
            val matrix = Matrix()
            matrix.preTranslate(-moveX.toFloat(), -moveY.toFloat())
            canvas.setMatrix(matrix)
        }
        canvas.drawRoundRect(
            moveX.toFloat(),
            moveY.toFloat(),
            (newSize + moveX).toFloat(),
            (newSize + moveY).toFloat(),
            newSize * angle,
            newSize * angle,
            paint
        )
        paint.style = Paint.Style.STROKE
        var rdd = 0.066f * newSize
        paint.strokeWidth = rdd
        paint.shader = null
        paint.color = Color.TRANSPARENT
        paint.alpha = 0
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(
            moveX + rdd / 2,
            moveY + rdd / 2,
            (newSize + moveX - rdd / 2),
            (newSize + moveY - rdd / 2),
            newSize * angle,
            newSize * angle,
            paint
        )

        rdd = 0.040f * newSize
        paint.strokeWidth = rdd
        paint.color = strokeFirst
        paint.alpha = 255
        paint.xfermode = null
        canvas.drawRoundRect(
            moveX + rdd / 2,
            moveY + rdd / 2,
            (newSize + moveX - rdd / 2),
            (newSize + moveY - rdd / 2),
            newSize * angle,
            newSize * angle,
            paint
        )
        if (useTransformation) {
            canvas.restore()
        }
        workBitmap.recycle()
        if (isHardware && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            obj?.endRecording()
            output =
                obj?.let { Bitmap.createBitmap(it, it.width, it.height, Bitmap.Config.HARDWARE) }
        }
        return output
    }
}
