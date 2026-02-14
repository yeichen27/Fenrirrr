package dev.ragnarok.fenrir.module

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.Keep
import java.nio.ByteBuffer

@Keep
class ZXingWrapper {
    // Enumerates barcode formats known to this package.
    // Note that this has to be kept synchronized with native (C++/JNI) side.
    enum class Format {
        NONE, AZTEC, CODABAR, CODE_39, CODE_93, CODE_128, DATA_BAR, DATA_BAR_EXPANDED, DATA_BAR_LIMITED,
        DATA_MATRIX, DX_FILM_EDGE, EAN_8, EAN_13, ITF, MAXICODE, PDF_417, QR_CODE, MICRO_QR_CODE, RMQR_CODE, UPC_A, UPC_E
    }

    enum class ContentType {
        TEXT, BINARY, MIXED, GS1, ISO15434, UNKNOWN_ECI
    }

    enum class Binarizer {
        LOCAL_AVERAGE, GLOBAL_HISTOGRAM, FIXED_THRESHOLD, BOOL_CAST
    }

    enum class EanAddOnSymbol {
        IGNORE, READ, REQUIRE
    }

    enum class TextMode {
        PLAIN, ECI, HRI, HEX, ESCAPED
    }

    enum class ErrorType {
        FORMAT, CHECKSUM, UNSUPPORTED
    }

    enum class ErrorCorrectionLevel {
        LOW, MEDIUM, QUALITY, HIGH
    }

    enum class CharacterSet {
        UNKNOWN,
        ASCII, ISO8859_1, ISO8859_2, ISO8859_3, ISO8859_4, ISO8859_5, ISO8859_6, ISO8859_7, ISO8859_8, ISO8859_9,
        ISO8859_10, ISO8859_11, ISO8859_13, ISO8859_14, ISO8859_15, ISO8859_16, CP437, CP1250, CP1251, CP1252,
        CP1256, SHIFT_JIS, BIG5, GB2312, GB18030, EUC_JP, EUC_KR, UTF16BE, UTF8, UTF16LE, UTF32BE, UTF32LE, BINARY
    }

    data class ReaderOptions(
        var formats: Set<Format> = setOf(),
        var tryHarder: Boolean = false,
        var tryRotate: Boolean = false,
        var tryInvert: Boolean = false,
        var tryDownscale: Boolean = false,
        var isPure: Boolean = false,
        var binarizer: Binarizer = Binarizer.LOCAL_AVERAGE,
        var downscaleFactor: Int = 3,
        var downscaleThreshold: Int = 500,
        var minLineCount: Int = 2,
        var maxNumberOfSymbols: Int = 0xff,
        var returnErrors: Boolean = false,
        var eanAddOnSymbol: EanAddOnSymbol = EanAddOnSymbol.IGNORE,
        var textMode: TextMode = TextMode.HRI
    )

    data class QRWriterOptions(
        var encoding: CharacterSet = CharacterSet.UNKNOWN,
        var errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.LOW,
        var version: Int = 0,
        var useGs1Format: Boolean = false,
        var maskPattern: Int = -1
    )

    data class Error(
        val type: ErrorType,
        val message: String
    )

    data class Position(
        val topLeft: Point,
        val topRight: Point,
        val bottomRight: Point,
        val bottomLeft: Point,
        val orientation: Double
    )

    data class ReaderResult(
        val format: Format,
        val text: String?,
        val contentType: ContentType,
        val position: Position,
        val orientation: Int,
        val ecLevel: String?,
        val symbologyIdentifier: String?,
        val sequenceSize: Int,
        val sequenceIndex: Int,
        val sequenceId: String?,
        val readerInit: Boolean,
        val lineCount: Int,
        val error: Error?
    )

    data class ByteMatrix(
        val width: Int,
        val height: Int,
        val data: ByteArray
    ) {
        operator fun get(x: Int, y: Int): Byte {
            return data[y * width + x]
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ByteMatrix

            if (width != other.width) return false
            if (height != other.height) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = width
            result = 31 * result + height
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    fun generateQR(
        content: String,
        width: Int,
        height: Int
    ): ByteMatrix? {
        return generateQRNative(content, width, height, qrWriterOptions)
    }

    fun readYBuffer(
        yBuffer: ByteBuffer,
        pixelStride: Int,
        cropRect: Rect = Rect(),
        width: Int,
        height: Int,
        rotation: Int = 0,
        flip: Boolean = false
    ): List<ReaderResult>? {
        return readYBufferNative(
            yBuffer,
            pixelStride,
            cropRect.left,
            cropRect.top,
            width, height,
            cropRect.width(),
            cropRect.height(),
            rotation,
            flip,
            readerOptions
        )
    }

    fun readBitmap(
        bitmap: Bitmap, cropRect: Rect = Rect(), rotation: Int = 0
    ): List<ReaderResult> {
        return readBitmapNative(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
            rotation,
            readerOptions
        )
    }

    private external fun generateQRNative(
        content: String,
        width: Int,
        height: Int,
        options: QRWriterOptions
    ): ByteMatrix?

    private external fun readYBufferNative(
        yBuffer: ByteBuffer,
        pixelStride: Int,
        left: Int,
        top: Int,
        origWidth: Int,
        origHeight: Int,
        width: Int,
        height: Int,
        rotation: Int,
        flip: Boolean,
        options: ReaderOptions
    ): List<ReaderResult>?

    private external fun readBitmapNative(
        bitmap: Bitmap,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        rotation: Int,
        options: ReaderOptions
    ): List<ReaderResult>

    var readerOptions: ReaderOptions = ReaderOptions()
    var qrWriterOptions: QRWriterOptions = QRWriterOptions()
}