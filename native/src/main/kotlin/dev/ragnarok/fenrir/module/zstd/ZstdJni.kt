/*
 * Copyright (C) 2025 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ragnarok.fenrir.module.zstd

import dev.ragnarok.fenrir.module.zstd.okio.ZstdCompressSink
import dev.ragnarok.fenrir.module.zstd.okio.ZstdDecompressSource
import okio.Sink
import okio.Source
import okio.buffer

object ZstdJni {
    internal val jniZstdPointer: Long by lazy {
        createJniZstd()
    }

    // From ZSTD_EndDirective in zstd.h.
    const val ZSTD_e_continue = 0
    const val ZSTD_e_flush = 1
    const val ZSTD_e_end = 2

    const val ZSTD_c_compressionLevel = 100
    const val ZSTD_c_checksumFlag = 201

    fun getErrorName(code: Long): String? = jniGetErrorName(code)

    /** Returns a new compressor. The caller must close it. */
    fun zstdCompressor(): ZstdCompressor = JniZstdCompressor()

    /** Returns a new decompressor. The caller must close it. */
    fun zstdDecompressor(): ZstdDecompressor = JniZstdDecompressor()

    /** Returns a [Sink] that compresses its data with Zstandard before forwarding to this. */
    fun Sink.zstdCompress(): Sink = ZstdCompressSink(this.buffer(), zstdCompressor())

    /** Returns a [Source] that decompresses its data with Zstandard after reading from this. */
    fun Source.zstdDecompress(): Source = ZstdDecompressSource(this.buffer(), zstdDecompressor())

    internal val emptyByteArray = ByteArray(0)

    private external fun createJniZstd(): Long
    private external fun jniGetErrorName(code: Long): String?
}