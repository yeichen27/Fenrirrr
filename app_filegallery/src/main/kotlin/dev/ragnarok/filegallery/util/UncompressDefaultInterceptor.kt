package dev.ragnarok.filegallery.util

import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.zstd.ZstdJni.zstdDecompress
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.http.promisesBody
import okio.GzipSource
import okio.buffer

object UncompressDefaultInterceptor : Interceptor {
    private fun uncompress(response: Response): Response {
        if (!response.promisesBody()) {
            return response
        }
        val body = response.body
        val encoding = response.header("Content-Encoding") ?: return response

        val decompressedSource = when {
            encoding.equals("zstd", ignoreCase = true) ->
                body.source().zstdDecompress().buffer()

            encoding.equals("gzip", ignoreCase = true) ->
                GzipSource(body.source()).buffer()

            else -> return response
        }

        return response.newBuilder()
            .addHeader("Compressed-Content-Length", response.header("Content-Length") ?: "-1")
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedSource.asResponseBody(body.contentType(), -1))
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): Response = uncompress(
        chain.proceed(
            chain.request().newBuilder()
                .header(
                    "Accept-Encoding",
                    if (!Utils.isCompressIncomingTraffic || !FenrirNative.isNativeLoaded) "none" else "zstd"
                )
                .build()
        )
    )
}