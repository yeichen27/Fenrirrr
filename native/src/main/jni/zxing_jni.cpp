#include "ReadBarcode.h"
#include "QREncodeResult.h"
#include "QREncoder.h"
#include "QRErrorCorrectionLevel.h"
#include "Utf.h"

#include <android/bitmap.h>
#include <android/log.h>
#include <chrono>
#include <exception>
#include <stdexcept>
#include <string>
#include "libyuv/convert_argb.h"
#include "libyuv/rotate_argb.h"
#include "libyuv/convert.h"
#include "libyuv/planar_functions.h"
#include "fenrir_native.h"

using namespace ZXing;
using namespace std::string_literals;

#define PACKAGE "dev/ragnarok/fenrir/module/ZXingWrapper$"

static libyuv::RotationMode get_rotation_mode(int rotation) {
    libyuv::RotationMode mode = libyuv::kRotate0;
    switch ((rotation + 360) % 360) {
        case 0:
            mode = libyuv::kRotate0;
            break;
        case 90:
            mode = libyuv::kRotate90;
            break;
        case 180:
            mode = libyuv::kRotate180;
            break;
        case 270:
            mode = libyuv::kRotate270;
            break;
        default:
            break;
    }
    return mode;
}

static uint8_t *
doRotateYBuffer(uint8_t *src_ptr, size_t src_size, int pixel_stride, int &width, int &height,
                int &left, int &top, int rotation,
                bool flip) {
    libyuv::RotationMode mode = get_rotation_mode(rotation);
    bool flip_wh = (mode == libyuv::kRotate90 || mode == libyuv::kRotate270);
    int rotated_stride = flip_wh ? height * pixel_stride : width * pixel_stride;
    auto *resultBuf = new uint8_t[src_size];
    if (mode != libyuv::kRotate0) {
        if (libyuv::RotatePlane(src_ptr, width * pixel_stride, resultBuf, rotated_stride, width,
                                height, mode)) {
            delete[] resultBuf;
            return nullptr;
        }
        if (flip_wh) {
            std::swap(width, height);
            std::swap(left, top);
        }
    }
    if (flip) {
        auto *tmpBuf = resultBuf;
        resultBuf = new uint8_t[src_size];
        libyuv::MirrorPlane(tmpBuf, rotated_stride, resultBuf, rotated_stride, width, height);
        delete[] tmpBuf;
    }
    return resultBuf;
}

static const char *JavaBarcodeFormatName(const BarcodeFormat &format) {
    // These have to be the names of the enum constants in the kotlin code.
    switch (format) {
        case BarcodeFormat::None:
            return "NONE";
        case BarcodeFormat::Aztec:
            return "AZTEC";
        case BarcodeFormat::Codabar:
            return "CODABAR";
        case BarcodeFormat::Code39:
            return "CODE_39";
        case BarcodeFormat::Code93:
            return "CODE_93";
        case BarcodeFormat::Code128:
            return "CODE_128";
        case BarcodeFormat::DataMatrix:
            return "DATA_MATRIX";
        case BarcodeFormat::EAN8:
            return "EAN_8";
        case BarcodeFormat::EAN13:
            return "EAN_13";
        case BarcodeFormat::ITF:
            return "ITF";
        case BarcodeFormat::MaxiCode:
            return "MAXICODE";
        case BarcodeFormat::PDF417:
            return "PDF_417";
        case BarcodeFormat::QRCode:
            return "QR_CODE";
        case BarcodeFormat::MicroQRCode:
            return "MICRO_QR_CODE";
        case BarcodeFormat::RMQRCode:
            return "RMQR_CODE";
        case BarcodeFormat::DataBar:
            return "DATA_BAR";
        case BarcodeFormat::DataBarExp:
            return "DATA_BAR_EXPANDED";
        case BarcodeFormat::DataBarLtd:
            return "DATA_BAR_LIMITED";
        case BarcodeFormat::DXFilmEdge:
            return "DX_FILM_EDGE";
        case BarcodeFormat::UPCA:
            return "UPC_A";
        case BarcodeFormat::UPCE:
            return "UPC_E";
        default:
            throw std::invalid_argument("Invalid BarcodeFormat");
    }
}

static const char *JavaContentTypeName(const ContentType &contentType) {
    // These have to be the names of the enum constants in the kotlin code.
    switch (contentType) {
        case ContentType::Text:
            return "TEXT";
        case ContentType::Binary:
            return "BINARY";
        case ContentType::Mixed:
            return "MIXED";
        case ContentType::GS1:
            return "GS1";
        case ContentType::ISO15434:
            return "ISO15434";
        case ContentType::UnknownECI:
            return "UNKNOWN_ECI";
        default:
            throw std::invalid_argument("Invalid contentType");
    }
}

static const char *JavaErrorTypeName(const Error::Type &errorType) {
    // These have to be the names of the enum constants in the kotlin code.
    switch (errorType) {
        case Error::Type::Format:
            return "FORMAT";
        case Error::Type::Checksum:
            return "CHECKSUM";
        case Error::Type::Unsupported:
            return "UNSUPPORTED";
        default:
            throw std::invalid_argument("Invalid errorType");
    }
}

inline constexpr auto hash(const std::string &sv) {
    unsigned int hash = 5381;
    for (unsigned char c: sv)
        hash = ((hash << 5) + hash) ^ c;
    return hash;
}

inline constexpr auto operator ""_h(const char *str, size_t len) { return hash({str, len}); }

static EanAddOnSymbol EanAddOnSymbolFromString(const std::string &name) {
    switch (hash(name)) {
        case "IGNORE"_h :
            return EanAddOnSymbol::Ignore;
        case "READ"_h :
            return EanAddOnSymbol::Read;
        case "REQUIRE"_h :
            return EanAddOnSymbol::Require;
        default:
            throw std::invalid_argument("Invalid eanAddOnSymbol name");
    }
}

static Binarizer BinarizerFromString(const std::string &name) {
    switch (hash(name)) {
        case "LOCAL_AVERAGE"_h :
            return Binarizer::LocalAverage;
        case "GLOBAL_HISTOGRAM"_h :
            return Binarizer::GlobalHistogram;
        case "FIXED_THRESHOLD"_h :
            return Binarizer::FixedThreshold;
        case "BOOL_CAST"_h :
            return Binarizer::BoolCast;
        default:
            throw std::invalid_argument("Invalid binarizer name");
    }
}

static TextMode TextModeFromString(const std::string &name) {
    switch (hash(name)) {
        case "PLAIN"_h :
            return TextMode::Plain;
        case "ECI"_h :
            return TextMode::ECI;
        case "HRI"_h :
            return TextMode::HRI;
        case "ESCAPED"_h :
            return TextMode::Escaped;
        case "HEX"_h :
            return TextMode::Hex;
        case "HEXECI"_h :
            return TextMode::HexECI;
        default:
            throw std::invalid_argument("Invalid textMode name");
    }
}

static CharacterSet CharacterSetFromString(const std::string &name) {
    switch (hash(name)) {
        case "UNKNOWN"_h :
            return CharacterSet::Unknown;
        case "ASCII"_h :
            return CharacterSet::ASCII;
        case "ISO8859_1"_h :
            return CharacterSet::ISO8859_1;
        case "ISO8859_2"_h :
            return CharacterSet::ISO8859_2;
        case "ISO8859_3"_h :
            return CharacterSet::ISO8859_3;
        case "ISO8859_4"_h :
            return CharacterSet::ISO8859_4;
        case "ISO8859_5"_h :
            return CharacterSet::ISO8859_5;
        case "ISO8859_6"_h :
            return CharacterSet::ISO8859_6;
        case "ISO8859_7"_h :
            return CharacterSet::ISO8859_7;
        case "ISO8859_8"_h :
            return CharacterSet::ISO8859_8;
        case "ISO8859_9"_h :
            return CharacterSet::ISO8859_9;
        case "ISO8859_10"_h :
            return CharacterSet::ISO8859_10;
        case "ISO8859_11"_h :
            return CharacterSet::ISO8859_11;
        case "ISO8859_13"_h :
            return CharacterSet::ISO8859_13;
        case "ISO8859_14"_h :
            return CharacterSet::ISO8859_14;
        case "ISO8859_15"_h :
            return CharacterSet::ISO8859_15;
        case "ISO8859_16"_h :
            return CharacterSet::ISO8859_16;
        case "CP437"_h :
            return CharacterSet::Cp437;
        case "CP1250"_h :
            return CharacterSet::Cp1250;
        case "CP1251"_h :
            return CharacterSet::Cp1251;
        case "CP1252"_h :
            return CharacterSet::Cp1252;
        case "CP1256"_h :
            return CharacterSet::Cp1256;
        case "SHIFT_JIS"_h :
            return CharacterSet::Shift_JIS;
        case "BIG5"_h :
            return CharacterSet::Big5;
        case "GB2312"_h :
            return CharacterSet::GB2312;
        case "GB18030"_h :
            return CharacterSet::GB18030;
        case "EUC_JP"_h :
            return CharacterSet::EUC_JP;
        case "EUC_KR"_h :
            return CharacterSet::EUC_KR;
        case "UTF16BE"_h :
            return CharacterSet::UTF16BE;
        case "UTF8"_h :
            return CharacterSet::UTF8;
        case "UTF16LE"_h :
            return CharacterSet::UTF16LE;
        case "UTF32BE"_h :
            return CharacterSet::UTF32BE;
        case "UTF32LE"_h :
            return CharacterSet::UTF32LE;
        case "BINARY"_h :
            return CharacterSet::BINARY;
        default:
            throw std::invalid_argument("Invalid encoding name");
    }
}

static QRCode::ErrorCorrectionLevel ErrorCorrectionLevelFromString(const std::string &name) {
    switch (hash(name)) {
        case "LOW"_h :
            return QRCode::ErrorCorrectionLevel::Low;
        case "MEDIUM"_h :
            return QRCode::ErrorCorrectionLevel::Medium;
        case "QUALITY"_h :
            return QRCode::ErrorCorrectionLevel::Quality;
        case "HIGH"_h :
            return QRCode::ErrorCorrectionLevel::High;
        default:
            throw std::invalid_argument("Invalid errorCorrectionLevel name");
    }
}

static jstring ThrowJavaException(JNIEnv *env, const char *message) {
    //	if (env->ExceptionCheck())
    //		return 0;
    jclass cls = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(cls, message);
    return nullptr;
}

jstring C2JString(JNIEnv *env, const std::string &str) {
    return env->NewStringUTF(str.c_str());
}

std::string J2CString(JNIEnv *env, jstring str) {
    // Buffer size must be in bytes.
    const jsize size = env->GetStringUTFLength(str);
    std::string res(size, 0);

    // Translates 'len' number of Unicode characters into modified
    // UTF-8 encoding and place the result in the given buffer.
    const jsize len = env->GetStringLength(str);
    env->GetStringUTFRegion(str, 0, len, res.data());

    return res;
}

static jobject NewPosition(JNIEnv *env, const Position &position) {
    jclass clsPosition = env->FindClass(PACKAGE "Position");
    jclass clsPoint = env->FindClass("android/graphics/Point");
    jmethodID midPointInit = env->GetMethodID(clsPoint, "<init>", "(II)V");
    auto NewPoint = [&](const PointI &point) {
        return env->NewObject(clsPoint, midPointInit, point.x, point.y);
    };
    jmethodID midPositionInit = env->GetMethodID(
            clsPosition, "<init>",
            "(Landroid/graphics/Point;"
            "Landroid/graphics/Point;"
            "Landroid/graphics/Point;"
            "Landroid/graphics/Point;"
            "D)V");
    return env->NewObject(
            clsPosition, midPositionInit,
            NewPoint(position[0]),
            NewPoint(position[1]),
            NewPoint(position[2]),
            NewPoint(position[3]),
            position.orientation());
}

static jbyteArray NewByteArray(JNIEnv *env, const std::vector<uint8_t> &byteArray) {
    auto size = static_cast<jsize>(byteArray.size());
    jbyteArray res = env->NewByteArray(size);
    env->SetByteArrayRegion(res, 0, size, reinterpret_cast<const jbyte *>(byteArray.data()));
    return res;
}

static jbyteArray NewByteArray(JNIEnv *env, const uint8_t *byteArray, size_t bufSize) {
    auto size = static_cast<jsize>(bufSize);
    jbyteArray res = env->NewByteArray(bufSize);
    env->SetByteArrayRegion(res, 0, bufSize, reinterpret_cast<const jbyte *>(byteArray));
    return res;
}

static jobject NewEnum(JNIEnv *env, const char *value, const char *type) {
    auto className = PACKAGE ""s + type;
    jclass cls = env->FindClass(className.c_str());
    jfieldID fidCT = env->GetStaticFieldID(cls, value, ("L" + className + ";").c_str());
    return env->GetStaticObjectField(cls, fidCT);
}

static jobject NewError(JNIEnv *env, const Error &error) {
    jclass cls = env->FindClass(PACKAGE "Error");
    jmethodID midInit = env->GetMethodID(cls, "<init>",
                                         "(L" PACKAGE "ErrorType;" "Ljava/lang/String;)V");
    return env->NewObject(cls, midInit, NewEnum(env, JavaErrorTypeName(error.type()), "ErrorType"),
                          C2JString(env, error.msg()));
}

static jobject NewReaderResult(JNIEnv *env, const Barcode &result) {
    jclass cls = env->FindClass(PACKAGE "ReaderResult");
    jmethodID midInit = env->GetMethodID(
            cls, "<init>",
            "(L" PACKAGE "Format;"
            "Ljava/lang/String;"
            "L" PACKAGE "ContentType;"
            "L" PACKAGE "Position;"
            "I"
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            "I"
            "I"
            "Ljava/lang/String;"
            "Z"
            "I"
            "L" PACKAGE "Error;"
            ")V");
    bool valid = result.isValid();
    return env->NewObject(cls, midInit,
                          NewEnum(env, JavaBarcodeFormatName(result.format()), "Format"),
                          valid ? C2JString(env, result.text()) : nullptr,
                          NewEnum(env, JavaContentTypeName(result.contentType()), "ContentType"),
                          NewPosition(env, result.position()),
                          result.orientation(),
                          valid ? C2JString(env, result.ecLevel()) : nullptr,
                          valid ? C2JString(env, result.symbologyIdentifier()) : nullptr,
                          result.sequenceSize(),
                          result.sequenceIndex(),
                          valid ? C2JString(env, result.sequenceId()) : nullptr,
                          result.readerInit(),
                          result.lineCount(),
                          result.error() ? NewError(env, result.error()) : nullptr
    );
}

static jobject newQRWriterResult(JNIEnv *env, const Matrix<uint8_t> &result) {
    jclass cls = env->FindClass(PACKAGE "ByteMatrix");
    jmethodID midInit = env->GetMethodID(
            cls, "<init>",
            "(I"
            "I"
            "[B"
            ")V");
    return env->NewObject(cls, midInit,
                          result.width(),
                          result.height(),
                          NewByteArray(env, result.data(), result.size())
    );
}

static jobject Read(JNIEnv *env, jobject thiz, ImageView image, const ReaderOptions &opts) {
    try {
        //auto startTime = std::chrono::high_resolution_clock::now();
        auto barcodes = ReadBarcodes(image, opts);
        //auto duration = std::chrono::high_resolution_clock::now() - startTime;
        //LOGD("time: %4d ms\n", (int)std::chrono::duration_cast<std::chrono::milliseconds>(duration).count());

        jclass clsList = env->FindClass("java/util/ArrayList");
        jobject objList = env->NewObject(clsList, env->GetMethodID(clsList, "<init>", "()V"));
        if (!barcodes.empty()) {
            jmethodID midAdd = env->GetMethodID(clsList, "add", "(Ljava/lang/Object;)Z");
            for (const auto &barcode: barcodes)
                env->CallBooleanMethod(objList, midAdd, NewReaderResult(env, barcode));
        }
        return objList;
    } catch (const std::exception &e) {
        return ThrowJavaException(env, e.what());
    } catch (...) {
        return ThrowJavaException(env, "Unknown exception");
    }
}

static bool GetBooleanField(JNIEnv *env, jclass cls, jobject opts, const char *name) {
    return env->GetBooleanField(opts, env->GetFieldID(cls, name, "Z"));
}

static int GetIntField(JNIEnv *env, jclass cls, jobject opts, const char *name) {
    return env->GetIntField(opts, env->GetFieldID(cls, name, "I"));
}

static std::string
GetEnumField(JNIEnv *env, jclass cls, jobject opts, const char *name, const char *type) {
    auto className = PACKAGE ""s + type;
    jmethodID midName = env->GetMethodID(env->FindClass(className.c_str()), "name",
                                         "()Ljava/lang/String;");
    jobject objField = env->GetObjectField(opts, env->GetFieldID(cls, name,
                                                                 ("L"s + className + ";").c_str()));
    return J2CString(env, reinterpret_cast<jstring>(env->CallObjectMethod(objField, midName)));
}

static BarcodeFormats GetFormats(JNIEnv *env, jclass clsOptions, jobject opts) {
    jobject objField = env->GetObjectField(opts, env->GetFieldID(clsOptions, "formats",
                                                                 "Ljava/util/Set;"));
    jmethodID midToArray = env->GetMethodID(env->FindClass("java/util/Set"), "toArray",
                                            "()[Ljava/lang/Object;");
    auto objArray = reinterpret_cast<jobjectArray>(env->CallObjectMethod(objField, midToArray));
    if (!objArray) {
        return {};
    }

    jmethodID midName = env->GetMethodID(env->FindClass(PACKAGE "Format"), "name",
                                         "()Ljava/lang/String;");
    std::vector<BarcodeFormat> ret;
    for (int i = 0, size = env->GetArrayLength(objArray); i < size; ++i) {
        auto objName = reinterpret_cast<jstring>(env->CallObjectMethod(
                env->GetObjectArrayElement(objArray, i), midName));
        ret.push_back(BarcodeFormatFromString(J2CString(env, objName)));
    }
    return ret;
}

static ReaderOptions CreateReaderOptions(JNIEnv *env, jobject opts) {
    jclass cls = env->GetObjectClass(opts);
    return ReaderOptions()
            .formats(GetFormats(env, cls, opts))
            .tryHarder(GetBooleanField(env, cls, opts, "tryHarder"))
            .tryRotate(GetBooleanField(env, cls, opts, "tryRotate"))
            .tryInvert(GetBooleanField(env, cls, opts, "tryInvert"))
            .tryDownscale(GetBooleanField(env, cls, opts, "tryDownscale"))
            .isPure(GetBooleanField(env, cls, opts, "isPure"))
            .binarizer(BinarizerFromString(GetEnumField(env, cls, opts, "binarizer", "Binarizer")))
            .downscaleThreshold(GetIntField(env, cls, opts, "downscaleThreshold"))
            .downscaleFactor(GetIntField(env, cls, opts, "downscaleFactor"))
            .minLineCount(GetIntField(env, cls, opts, "minLineCount"))
            .maxNumberOfSymbols(GetIntField(env, cls, opts, "maxNumberOfSymbols"))
            .tryCode39ExtendedMode(GetBooleanField(env, cls, opts, "tryCode39ExtendedMode"))
            .returnErrors(GetBooleanField(env, cls, opts, "returnErrors"))
            .eanAddOnSymbol(EanAddOnSymbolFromString(
                    GetEnumField(env, cls, opts, "eanAddOnSymbol", "EanAddOnSymbol")))
            .textMode(TextModeFromString(GetEnumField(env, cls, opts, "textMode", "TextMode")));
}

extern "C" JNIEXPORT jobject JNICALL
Java_dev_ragnarok_fenrir_module_ZXingWrapper_readYBufferNative(
        JNIEnv *env, jobject thiz, jobject yBuffer, jint pixelStride,
        jint left, jint top, jint origWidth, jint origHeight, jint width, jint height,
        jint rotation, jboolean flip, jobject options) {
    bool needClearPixels = false;
    auto *pixels = static_cast<uint8_t *>(env->GetDirectBufferAddress(yBuffer));
    size_t src_size = env->GetDirectBufferCapacity(yBuffer);
    if ((rotation != 0 || flip) && pixels) {
        pixels = doRotateYBuffer(pixels, src_size, pixelStride, origWidth, origHeight, left, top,
                                 rotation, flip);
        needClearPixels = true;
    }
    if (!pixels) {
        return nullptr;
    }

    auto image = ImageView{pixels, origWidth, origHeight, ImageFormat::Lum, origWidth * pixelStride,
                           pixelStride}
            .cropped(left, top, width, height);
    auto ret = Read(env, thiz, image, CreateReaderOptions(env, options));
    if (needClearPixels) {
        delete[] pixels;
    }
    return ret;
}

extern "C" JNIEXPORT jobject JNICALL
Java_dev_ragnarok_fenrir_module_ZXingWrapper_generateQRNative(JNIEnv *env, jobject thiz,
                                                              jstring content,
                                                              jint width, jint height,
                                                              jobject options) {
    jclass cls = env->GetObjectClass(options);
    try {
        auto code = Encode(FromUtf8(J2CString(env, content)), ErrorCorrectionLevelFromString(
                                   GetEnumField(env, cls, options, "errorCorrectionLevel", "ErrorCorrectionLevel")),
                           CharacterSetFromString(
                                   GetEnumField(env, cls, options, "encoding", "CharacterSet")),
                           GetIntField(env, cls, options, "version"),
                           GetBooleanField(env, cls, options, "useGs1Format"),
                           GetIntField(env, cls, options, "maskPattern"));
        if (code.matrix.empty()) {
            return nullptr;
        }
        return newQRWriterResult(env, ToMatrix<uint8_t>(code.matrix, 1, 0));
    } catch (const std::exception &e) {
        return ThrowJavaException(env, e.what());
    } catch (...) {
        return ThrowJavaException(env, "Unknown exception");
    }
    return nullptr;
}

class LockedPixels {
public:
    JNIEnv *env;
    jobject bitmap;
    void *pixels = nullptr;

    LockedPixels(JNIEnv *env, jobject bitmap) : env(env), bitmap(bitmap) {
        if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESUT_SUCCESS)
            pixels = nullptr;
    }

    operator const uint8_t *() const { return static_cast<const uint8_t *>(pixels); }

    ~LockedPixels() {
        if (pixels)
            AndroidBitmap_unlockPixels(env, bitmap);
    }
};

extern "C" JNIEXPORT jobject JNICALL
Java_dev_ragnarok_fenrir_module_ZXingWrapper_readBitmapNative(
        JNIEnv *env, jobject thiz, jobject bitmap,
        jint left, jint top, jint width, jint height, jint rotation, jobject options) {
    AndroidBitmapInfo bmInfo;
    AndroidBitmap_getInfo(env, bitmap, &bmInfo);

    ImageFormat fmt = ImageFormat::None;
    switch (bmInfo.format) {
        case ANDROID_BITMAP_FORMAT_A_8: {
            fmt = ImageFormat::Lum;
            break;
        }
        case ANDROID_BITMAP_FORMAT_RGBA_8888: {
            fmt = ImageFormat::RGBA;
            break;
        }
        default:
            return ThrowJavaException(env, "Unsupported image format in AndroidBitmap");
    }

    auto pixels = LockedPixels(env, bitmap);

    if (!pixels) {
        return ThrowJavaException(env, "Failed to lock/read AndroidBitmap data");
    }

    auto image =
            ImageView{pixels, (int) bmInfo.width, (int) bmInfo.height, fmt, (int) bmInfo.stride}
                    .cropped(left, top, width, height)
                    .rotated(rotation);

    return Read(env, thiz, image, CreateReaderOptions(env, options));
}