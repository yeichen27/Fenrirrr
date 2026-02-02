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

abstract class ZstdCompressor : AutoCloseable {
    /** The number of bytes consumed on the most recent call to [compressStream2]. */
    @JvmField
    var inputBytesProcessed: Int = -1

    /** The number of bytes produced on the most recent call to [compressStream2]. */
    @JvmField
    var outputBytesProcessed: Int = -1

    /** @param param one of [ZstdJni.ZSTD_c_compressionLevel] or [ZstdJni.ZSTD_c_checksumFlag]. */
    abstract fun setParameter(param: Int, value: Int): Long

    /** @param mode one of [ZstdJni.ZSTD_e_continue], [ZstdJni.ZSTD_e_flush], or [ZstdJni.ZSTD_e_end]. */
    abstract fun compressStream2(
        outputByteArray: ByteArray,
        outputEnd: Int,
        outputStart: Int,
        inputByteArray: ByteArray,
        inputEnd: Int,
        inputStart: Int,
        mode: Int,
    ): Long
}
