package dev.ragnarok.fenrir.module

import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSource
import java.io.IOException
import kotlin.math.min

class FFmpegOkhttp(val pUrl: String, val pHeaders: String?) {
    var pResponse: Response? = null
    var pBody: ResponseBody? = null
    var dataStream: BufferedSource? = null
    var mMimeType: String? = null
    var position: Long = 0
    var pContentSize: Long = Long.MAX_VALUE
    var mSeekable: Boolean = true
    val okhttpClient: OkHttpClient? = okHttpBuilder?.createOkHttpClient()?.build()

    @Throws(IOException::class)
    fun okhttpClose() {
        try {
            pBody?.close()
            pBody = null
            pResponse?.close()
            pResponse = null
            dataStream?.close()
            dataStream = null
            position = 0L
        } catch (_: IOException) {
        }
    }

    fun okhttpGetMime(): String? {
        return mMimeType
    }

    @Throws(IOException::class)
    fun okhttpOpen(avOptions: MutableMap<String, String>?): Int {
        if (okhttpClient == null || isLockNetwork) {
            return -5
        }
        try {
            val httpHeaders = splitHttpHeaders(pHeaders)
            if (mSeekable) {
                httpHeaders["Range"] = "bytes=$position-"
            }
            setOptions(avOptions, httpHeaders)

            val response: Response = okhttpClient.newCall(
                Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
            ).execute()
            pResponse = response
            pBody = response.body
            if (response.code == 416) {
                okhttpClose()
                httpHeaders.remove("Range")
                mSeekable = false
                val response2: Response = okhttpClient.newCall(
                    Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
                ).execute()
                pResponse = response2
                pBody = response2.body
            }
            if (pResponse?.isSuccessful != true) {
                okhttpClose()
                return -5
            }
            mMimeType = pBody?.contentType().toString()
            pContentSize = pBody?.contentLength() ?: 0
            dataStream = pBody?.source()
            if (pContentSize <= 0) {
                pContentSize = Long.MAX_VALUE
            }
            return 0
        } catch (_: IOException) {
            okhttpClose()
            return -5
        }
    }

    @Throws(IOException::class)
    fun okhttpRead(bArr: ByteArray, bufferSize: Int): Int {
        if (isLockNetwork) {
            return -1
        }
        try {
            if (dataStream != null && bufferSize >= 0) {
                val read = dataStream?.read(bArr, 0, min(bufferSize, 8192)) ?: -1
                if (read > 0) {
                    position += read.toLong()
                    return read
                }
                if (read < 0 && pContentSize == Long.MAX_VALUE) {
                    pContentSize = position
                }
                return -1
            }
            return -3
        } catch (_: IOException) {
            return -1
        } catch (_: ArrayIndexOutOfBoundsException) {
            return -1
        } catch (_: IllegalStateException) {
            return -1
        }
    }

    @Throws(IOException::class)
    private fun okhttpSeek(off: Long): Long {
        if (okhttpClient == null || isLockNetwork) {
            return -5
        }
        okhttpClose()
        try {
            val httpHeaders: HashMap<String, String> = splitHttpHeaders(pHeaders)
            httpHeaders["Range"] = "bytes=$off-"
            val response: Response = okhttpClient.newCall(
                Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
            ).execute()
            pResponse = response
            pBody = response.body
            if (response.code == 416) {
                okhttpClose()
                httpHeaders.remove("Range")
                mSeekable = false
                val response2: Response = okhttpClient.newCall(
                    Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
                ).execute()
                pResponse = response2
                pBody = response2.body
            }
            if (pResponse?.isSuccessful == true) {
                dataStream = pBody?.source()
                return off
            }
            okhttpClose()
            return -5L
        } catch (_: IOException) {
            okhttpClose()
            return -5L
        }
    }

    fun setOptions(map: MutableMap<String, String>?, map2: MutableMap<String, String>) {
        if (map == null) {
            return
        }
        if (map.containsKey("seekable")) {
            val str = map["seekable"]
            mSeekable = str != null && str != "-1"
        }
        if (map.containsKey("offset")) {
            var range: String = "bytes=" + map["offset"] + "-"
            if (map.containsKey("end_offset")) {
                range += map["end_offset"]
            }
            map2["Range"] = range
        }
    }

    @Throws(IOException::class)
    fun okhttpSeek(off: Long, whence: Int): Long {
        if (dataStream == null || isLockNetwork) {
            return -5L
        }
        if (whence == 65536) {
            return if (pContentSize == Long.MAX_VALUE) position else pContentSize
        }
        if (whence == 0) {
            position = off
            return okhttpSeek(off)
        }
        if (whence == 1) {
            position += off
            return okhttpSeek(position)
        }
        if (whence != 2) {
            return -4L
        }
        position = pContentSize
        return okhttpSeek(position)
    }

    companion object {
        private var okHttpBuilder: OnFFmpegOkhttpCreate? = null

        @Volatile
        private var isLockNetwork = false
        fun setOnFFmpegOkhttpCreate(client: () -> OkHttpClient.Builder) {
            okHttpBuilder = object : OnFFmpegOkhttpCreate {
                override fun createOkHttpClient(): OkHttpClient.Builder = client()
            }
        }

        fun setLockNetwork(isLockNetwork: Boolean) {
            this.isLockNetwork = isLockNetwork
        }

        fun splitHttpHeaders(str: String?): HashMap<String, String> {
            if (str.isNullOrEmpty()) {
                return HashMap()
            }
            val strArrSplit =
                str.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val map = HashMap<String, String>()
            for (str2 in strArrSplit) {
                val strArrSplit2: Array<String> =
                    str2.split("=".toRegex(), limit = 2).toTypedArray()
                if (strArrSplit2.size >= 2) {
                    map[strArrSplit2[0]] = strArrSplit2[1]
                }
            }
            return map
        }
    }
}

interface OnFFmpegOkhttpCreate {
    fun createOkHttpClient(): OkHttpClient.Builder
}
