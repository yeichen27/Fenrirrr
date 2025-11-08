package dev.ragnarok.filegallery.picasso.transforms

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.Shader
import android.os.Build
import androidx.core.graphics.createBitmap

object ImageHelper {
    fun getRoundedBitmap(workBitmap: Bitmap?): Bitmap? {
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

    fun getEllipseBitmap(workBitmap: Bitmap?, angle: Float): Bitmap? {
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
