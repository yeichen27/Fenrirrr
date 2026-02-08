package dev.ragnarok.fenrir.view.natives.animation

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.isActive
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class AnimationNetworkCache(context: Context) {
    private val appContext = context.applicationContext

    fun fetch(url: String): File? {
        return synchronized(syncUrl) {
            val cachedFile = File(parentUrlDir(appContext), filenameForUrl(url, false))
            if (!cachedFile.exists()) {
                null
            } else {
                if (Constants.IS_DEBUG) {
                    Log.d(
                        "AnimationNetworkCache",
                        "Cache hit for $url at ${cachedFile.absolutePath}"
                    )
                }
                cachedFile
            }
        }
    }

    fun fetch(@RawRes res: Int): File? {
        return synchronized(syncRes) {
            val cachedFile = File(parentResDir(appContext), filenameForRes(res, false))
            if (!cachedFile.exists()) {
                null
            } else {
                if (Constants.IS_DEBUG) {
                    Log.d(
                        "AnimationNetworkCache",
                        "Cache hit for $res at ${cachedFile.absolutePath}"
                    )
                }
                cachedFile
            }
        }
    }

    fun isCachedFile(url: String): Boolean {
        synchronized(syncUrl) {
            return File(parentUrlDir(appContext), filenameForUrl(url, false)).exists()
        }
    }

    fun isCachedRes(@RawRes res: Int): Boolean {
        synchronized(syncRes) {
            return File(parentResDir(appContext), filenameForRes(res, false)).exists()
        }
    }

    suspend fun writeTempCacheFile(url: String, source: BufferedSource): Boolean {
        val file = File(parentUrlDir(appContext), filenameForUrl(url, true))
        val newFile: File
        synchronized(syncUrl) {
            newFile = File(parentUrlDir(appContext), filenameForUrl(url, false))
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
        synchronized(syncUrl) {
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

    suspend fun writeTempCacheFile(@RawRes rawRes: Int): Boolean {
        val file = File(parentResDir(appContext), filenameForRes(rawRes, true))
        val newFile: File
        synchronized(syncRes) {
            newFile = File(parentResDir(appContext), filenameForRes(rawRes, false))
            if (newFile.exists()) {
                return true
            }
        }
        try {
            appContext.resources.openRawResource(rawRes).use {
                file.sink().buffer().use { output ->
                    output.writeAll(it.source())
                }
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }

        if (!isActive()) {
            file.delete()
            return false
        }
        synchronized(syncRes) {
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
        private val counterUrl = AtomicInteger(0)
        private val counterRes = AtomicInteger(0)
        private val syncUrl = Any()
        private val syncRes = Any()
        private const val EXTENSION = ".video"
        internal fun filenameForUrl(url: String, isTemp: Boolean) =
            "video_cache_" + url.replace(
                "\\W+".toRegex(),
                ""
            ) + if (isTemp) "$EXTENSION." + counterUrl.addAndGet(1)
                .toString() + ".temp" else EXTENSION

        internal fun filenameForRes(@RawRes res: Int, isTemp: Boolean) =
            "video_res_cache_$res" + if (isTemp) "$EXTENSION." + counterRes.addAndGet(1)
                .toString() + ".temp" else EXTENSION

        internal fun parentUrlDir(context: Context): File {
            val file = File(context.cacheDir, "video_network_cache")
            if (file.isFile) {
                file.delete()
            }
            if (!file.exists()) {
                file.mkdirs()
            }
            return file
        }

        internal fun parentResDir(context: Context): File {
            val file = File(context.cacheDir, "video_resource_cache")
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
