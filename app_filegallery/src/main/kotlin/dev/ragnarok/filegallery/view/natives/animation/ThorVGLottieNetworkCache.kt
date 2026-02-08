package dev.ragnarok.filegallery.view.natives.animation

import android.content.Context
import android.util.Log
import dev.ragnarok.filegallery.Constants
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.isActive
import okio.BufferedSource
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class ThorVGLottieNetworkCache(context: Context) {
    private val appContext = context.applicationContext

    fun fetch(url: String): File? {
        return synchronized(sync) {
            val cachedFile = File(parentDir(appContext), filenameForUrl(url, false))
            if (!cachedFile.exists()) {
                null
            } else {
                if (Constants.IS_DEBUG) {
                    Log.d(
                        "ThorVGLottieNetworkCache",
                        "Cache hit for $url at ${cachedFile.absolutePath}"
                    )
                }
                cachedFile
            }
        }
    }

    fun isCachedFile(url: String): Boolean {
        synchronized(sync) {
            return File(parentDir(appContext), filenameForUrl(url, false)).exists()
        }
    }

    suspend fun writeTempCacheFile(url: String, source: BufferedSource): Boolean {
        val file = File(parentDir(appContext), filenameForUrl(url, true))
        val newFile: File
        synchronized(sync) {
            newFile = File(parentDir(appContext), filenameForUrl(url, false))
            if (newFile.exists()) {
                return true
            }
        }
        try {
            file.sink().buffer().use { output ->
                output.writeAll(source)
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }

        if (!isActive()) {
            file.delete()
            return false
        }
        synchronized(sync) {
            return if (newFile.exists()) {
                file.delete()
                true
            } else {
                val rs = file.renameTo(newFile)
                if (!rs) {
                    file.delete()
                }
                rs
            }
        }
    }

    companion object {
        private val counter = AtomicInteger(0)
        private val sync = Any()
        private const val EXTENSION = ".json"
        internal fun filenameForUrl(url: String, isTemp: Boolean) =
            "lottie_cache_" + url.replace(
                "\\W+".toRegex(),
                ""
            ) + if (isTemp) "$EXTENSION." + counter.addAndGet(1)
                .toString() + ".temp" else EXTENSION

        internal fun parentDir(context: Context): File {
            val file = File(context.cacheDir, "lottie_cache")
            if (file.isFile) {
                file.delete()
            }
            if (!file.exists()) {
                file.mkdirs()
            }
            return file
        }
    }
}
