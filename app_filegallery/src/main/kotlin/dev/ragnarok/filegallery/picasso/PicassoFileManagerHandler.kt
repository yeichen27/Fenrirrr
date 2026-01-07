package dev.ragnarok.filegallery.picasso

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso3.BitmapSafeResize
import com.squareup.picasso3.BitmapUtils
import com.squareup.picasso3.Picasso
import com.squareup.picasso3.Request
import com.squareup.picasso3.RequestHandler
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.StringHash
import dev.ragnarok.fenrir.module.animation.AnimatedFileFrame
import dev.ragnarok.filegallery.Constants
import dev.ragnarok.filegallery.Includes
import dev.ragnarok.filegallery.fragment.filemanager.FileManagerFragment.Companion.isExtension
import dev.ragnarok.filegallery.nonNullNoEmpty
import dev.ragnarok.filegallery.settings.Settings
import dev.ragnarok.filegallery.util.CoverSafeResize
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.InputStream
import java.nio.ByteBuffer

class PicassoFileManagerHandler(val context: Context) : RequestHandler() {
    companion object {
        fun toSha1(str: String): String {
            return StringHash.calculateSha1(str)
        }
    }

    override fun canHandleRequest(data: Request): Boolean {
        return data.uri?.path.nonNullNoEmpty() && data.uri?.lastPathSegment.nonNullNoEmpty() && "thumb_file" == data.uri?.scheme
    }

    private fun getMetadataAudioThumbnail(uri: Uri): Bitmap? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(Includes.provideApplicationContext(), uri)
            val cover = mediaMetadataRetriever.embeddedPicture ?: return null
            BitmapFactory.decodeStream(ByteArrayInputStream(cover))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Throws(FileNotFoundException::class)
    private fun getSource(file: File): InputStream {
        return FileInputStream(file)
    }

    private val filter: FilenameFilter = FilenameFilter { dir, filename ->
        val sel = File(dir, filename)
        var ret = !sel.isHidden && sel.canRead() && !sel.isDirectory
        if (ret) {
            ret = false
            for (i in Settings.get().main().photoExt) {
                if (sel.extension.contains(i, true)) {
                    ret = true
                    break
                }
            }
            if (!ret) {
                for (i in Settings.get().main().audioExt) {
                    if (sel.extension.contains(i, true)) {
                        ret = true
                        break
                    }
                }
            }
            if (!ret) {
                for (i in Settings.get().main().videoExt) {
                    if (sel.extension.contains(i, true)) {
                        ret = true
                        break
                    }
                }
            }
        }
        ret
    }

    private class ItemModificationComparator : Comparator<File> {
        override fun compare(lhs: File, rhs: File): Int {
            return rhs.lastModified().compareTo(lhs.lastModified())
        }
    }

    private fun getExifRotation(orientation: Int) =
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }

    private fun getExifTranslation(orientation: Int) =
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL, ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE, ExifInterface.ORIENTATION_TRANSVERSE -> -1

            else -> 1
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeImageSource(imageSource: ImageDecoder.Source): Bitmap {
        return ImageDecoder.decodeBitmap(imageSource) { imageDecoder, imageInfo, _ ->
            imageDecoder.allocator = when {
                BitmapSafeResize.isHardwareRendering() == 1 -> {
                    imageDecoder.isMutableRequired = true
                    ImageDecoder.ALLOCATOR_DEFAULT
                }

                BitmapSafeResize.isHardwareRendering() == 2 -> {
                    imageDecoder.isMutableRequired = false
                    ImageDecoder.ALLOCATOR_HARDWARE
                }

                else -> {
                    imageDecoder.isMutableRequired = true
                    ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }
            val resWidth = imageInfo.size.width
            val resHeight = imageInfo.size.height
            CoverSafeResize.checkSizeOfBitmapP(resWidth, resHeight, object :
                CoverSafeResize.ResizeBitmap {
                override fun doResize(resizedWidth: Int, resizedHeight: Int) {
                    imageDecoder.setTargetSize(resizedWidth, resizedHeight)
                }
            })
        }
    }

    private fun work(requestFile: File, dir: File, request: Request, callback: Callback) {
        if (dir.exists()) {
            if (dir.length() <= 0) {
                callback.onError(Throwable("Cache file empty"))
                return
            }
            try {
                val s = getSource(dir)
                val bs = BitmapUtils.decodeStream(
                    s.source(),
                    request
                )
                s.close()
                callback.onSuccess(
                    Result.Bitmap(
                        bs, Picasso.LoadedFrom.DISK
                    )
                )
            } catch (e: Exception) {
                callback.onError(e)
            }
            return
        }
        when {
            isExtension(requestFile.absolutePath, Settings.get().main().audioExt) -> {
                var target = getMetadataAudioThumbnail(requestFile.toUri())
                if (target == null) {
                    dir.createNewFile()
                    callback.onError(Throwable("Thumb work error"))
                    return
                } else {
                    target = CoverSafeResize.checkBitmap(target)
                    val fOutputStream = FileOutputStream(dir)
                    target.compress(
                        Bitmap.CompressFormat.JPEG,
                        95,
                        fOutputStream
                    )

                    fOutputStream.flush()
                    fOutputStream.close()
                }
                callback.onSuccess(Result.Bitmap(target, Picasso.LoadedFrom.DISK))
                return
            }

            isExtension(
                requestFile.absolutePath,
                Settings.get().main().videoExt
            ) -> {
                var target =
                    if (FenrirNative.isNativeLoaded) AnimatedFileFrame.getThumbnail(requestFile) else null
                if (target == null) {
                    dir.createNewFile()
                    callback.onError(Throwable("Thumb work error"))
                    return
                } else {
                    target = CoverSafeResize.checkBitmap(target)
                    val fOutputStream = FileOutputStream(dir)
                    target.compress(
                        Bitmap.CompressFormat.JPEG,
                        95,
                        fOutputStream
                    )

                    fOutputStream.flush()
                    fOutputStream.close()
                }
                callback.onSuccess(Result.Bitmap(target, Picasso.LoadedFrom.DISK))
                return
            }

            isExtension(requestFile.absolutePath, Settings.get().main().photoExt) -> {
                val s = getSource(requestFile)
                var target: Bitmap
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val exceptionCatchingSource =
                            BitmapUtils.ExceptionCatchingSource(s.source())
                        val bufferedSource = exceptionCatchingSource.buffer()
                        val imageSource =
                            ImageDecoder.createSource(ByteBuffer.wrap(bufferedSource.readByteArray()))
                        target = decodeImageSource(imageSource)
                        exceptionCatchingSource.throwIfCaught()
                    } else {
                        target = CoverSafeResize.checkBitmap(
                            BitmapUtils.decodeStream(
                                s.source(),
                                request
                            )
                        )
                    }
                    s.close()
                } catch (_: Exception) {
                    dir.createNewFile()
                    callback.onError(Throwable("Thumb work error"))
                    return
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    try {
                        val exifOrientation = getExifOrientation(requestFile)
                        val exifRotation = getExifRotation(exifOrientation)
                        val exifTranslation = getExifTranslation(exifOrientation)
                        if (exifRotation != 0 || exifTranslation != 1) {
                            val matrix = Matrix()
                            if (exifRotation != 0) {
                                matrix.preRotate(exifRotation.toFloat())
                            }
                            if (exifTranslation != 1) {
                                matrix.postScale(exifTranslation.toFloat(), 1f)
                            }
                            /*
                            if (exifRotation == 90 || exifRotation == 270) {
                                val tmpHeight = target.height
                                target.height = target.width
                                target.width = tmpHeight
                            }
                             */
                            target = Bitmap.createBitmap(
                                target, 0, 0,
                                target.width, target.height, matrix, true
                            )
                        }
                    } catch (e: Exception) {
                        if (Constants.IS_DEBUG) {
                            e.printStackTrace()
                        }
                    }
                }
                val fOutputStream = FileOutputStream(dir)
                target.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    fOutputStream
                )

                fOutputStream.flush()
                fOutputStream.close()
                callback.onSuccess(Result.Bitmap(target, Picasso.LoadedFrom.DISK))
                return
            }

            else -> callback.onError(Throwable("Thumb not handle"))
        }
    }

    private fun prepareDirectory(requestFile: File, request: Request, callback: Callback): Boolean {
        if (!requestFile.isDirectory) {
            return false
        }

        val dir = File(
            PicassoInstance.getCoversPath(context),
            "thumb_" + toSha1(requestFile.absolutePath + requestFile.lastModified()) + ".jpg"
        )
        if (dir.exists()) {
            if (dir.length() <= 0) {
                callback.onError(Throwable("Cache file empty"))
                return true
            }
            try {
                val s = getSource(dir)
                callback.onSuccess(
                    Result.Bitmap(
                        BitmapUtils.decodeStream(
                            s.source(),
                            request
                        ), Picasso.LoadedFrom.DISK
                    )
                )
                s.close()
            } catch (e: Exception) {
                callback.onError(e)
            }
            return true
        }

        val fList = requestFile.listFiles(filter)
        val dst = if (fList != null && fList.isNotEmpty()) {
            fList.sortedWith(ItemModificationComparator())[0]
        } else {
            null
        }
        if (dst == null) {
            callback.onError(Throwable("Thumb not handle"))
        } else {
            work(dst, dir, request, callback)
        }
        return true

    }

    private fun getExifOrientation(requestFile: File): Int {
        return ExifInterface(requestFile.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }

    override fun load(picasso: Picasso, request: Request, callback: Callback) {
        val requestFile = checkNotNull(request.uri) { "request.uri == null" }.toString()
            .replace("thumb_", "").toUri().toFile()
        if (prepareDirectory(requestFile, request, callback)) {
            return
        }
        val dir = File(
            PicassoInstance.getCoversPath(context),
            "thumb_" + toSha1(requestFile.absolutePath + requestFile.lastModified()) + ".jpg"
        )
        work(requestFile, dir, request, callback)
    }
}
