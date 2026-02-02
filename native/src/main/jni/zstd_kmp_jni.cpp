#include <jni.h>
#include <zstd.h>

class JniZstd {
public:
    JniZstd(JNIEnv *env, jclass zstdCompressorClass, jclass zstdDecompressorClass);

    jfieldID zstdCompressorOutputBytesProcessed;
    jfieldID zstdCompressorInputBytesProcessed;
    jfieldID zstdDecompressorOutputBytesProcessed;
    jfieldID zstdDecompressorInputBytesProcessed;
};

JniZstd::JniZstd(JNIEnv *env, jclass zstdCompressorClass, jclass zstdDecompressorClass)
        : zstdCompressorOutputBytesProcessed(
        env->GetFieldID(zstdCompressorClass, "outputBytesProcessed", "I")),
          zstdCompressorInputBytesProcessed(
                  env->GetFieldID(zstdCompressorClass, "inputBytesProcessed", "I")),
          zstdDecompressorOutputBytesProcessed(
                  env->GetFieldID(zstdDecompressorClass, "outputBytesProcessed", "I")),
          zstdDecompressorInputBytesProcessed(
                  env->GetFieldID(zstdDecompressorClass, "inputBytesProcessed", "I")) {
}

extern "C" JNIEXPORT jstring JNICALL
Java_dev_ragnarok_fenrir_module_zstd_ZstdJni_jniGetErrorName(JNIEnv *env, jobject type,
                                                             jlong code) {
    auto codeSizeT = static_cast<size_t>(code);
    if (!ZSTD_isError(codeSizeT)) return nullptr;
    auto errorString = ZSTD_getErrorName(codeSizeT);
    return env->NewStringUTF(errorString);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_zstd_ZstdJni_createJniZstd(JNIEnv *env, jobject type) {
    auto zstdCompressorClass = env->FindClass("dev/ragnarok/fenrir/module/zstd/ZstdCompressor");
    auto zstdDecompressorClass = env->FindClass("dev/ragnarok/fenrir/module/zstd/ZstdDecompressor");
    auto jniZstd = new JniZstd(env, zstdCompressorClass, zstdDecompressorClass);
    return reinterpret_cast<jlong>(jniZstd);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdCompressor_createZstdCompressor(JNIEnv *env,
                                                                            jobject type) {
    ZSTD_CCtx *cctx = ZSTD_createCCtx(); // Could be NULL.
    return reinterpret_cast<jlong>(cctx);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdCompressor_setParameter(JNIEnv *env, jobject type,
                                                                    jlong cctxPointer, jint param,
                                                                    jint value) {
    auto cctx = reinterpret_cast<ZSTD_CCtx *>(cctxPointer);
    return (jlong) ZSTD_CCtx_setParameter(cctx, static_cast<ZSTD_cParameter>(param), value);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdCompressor_compressStream2(JNIEnv *env, jobject type,
                                                                       jlong jniZstdPointer,
                                                                       jlong cctxPointer,
                                                                       jbyteArray outputByteArray,
                                                                       jint outputEnd,
                                                                       jint outputStart,
                                                                       jbyteArray inputByteArray,
                                                                       jint inputEnd,
                                                                       jint inputStart, jint mode) {
    auto jniZstd = reinterpret_cast<JniZstd *>(jniZstdPointer);
    auto cctx = reinterpret_cast<ZSTD_CCtx *>(cctxPointer);

    auto inputByteArrayElements = env->GetByteArrayElements(inputByteArray, nullptr);
    ZSTD_inBuffer zstdInput = {inputByteArrayElements, static_cast<size_t>(inputEnd),
                               static_cast<size_t>(inputStart)};

    auto outputByteArrayElements = env->GetByteArrayElements(outputByteArray, nullptr);
    ZSTD_outBuffer zstdOutput = {outputByteArrayElements, static_cast<size_t>(outputEnd),
                                 static_cast<size_t>(outputStart)};

    size_t result;
    if (inputByteArrayElements != nullptr && outputByteArrayElements != nullptr) {
        result = ZSTD_compressStream2(cctx, &zstdOutput, &zstdInput,
                                      static_cast<ZSTD_EndDirective>(mode));
    } else {
        result = -ZSTD_error_GENERIC;
    }

    env->SetIntField(type, jniZstd->zstdCompressorOutputBytesProcessed,
                     static_cast<jint>(zstdOutput.pos) - outputStart);
    env->SetIntField(type, jniZstd->zstdCompressorInputBytesProcessed,
                     static_cast<jint>(zstdInput.pos) - inputStart);

    env->ReleaseByteArrayElements(inputByteArray, inputByteArrayElements, JNI_ABORT);
    env->ReleaseByteArrayElements(outputByteArray, outputByteArrayElements, 0);

    return (jlong) result;
}

extern "C" JNIEXPORT void JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdCompressor_close(JNIEnv *env, jobject type,
                                                             jlong cctxPointer) {
    auto cctx = reinterpret_cast<ZSTD_CCtx *>(cctxPointer);
    ZSTD_freeCCtx(cctx);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdDecompressor_createZstdDecompressor(JNIEnv *env,
                                                                                jobject type) {
    ZSTD_DCtx *dctx = ZSTD_createDCtx(); // Could be NULL.
    return reinterpret_cast<jlong>(dctx);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdDecompressor_decompressStream(JNIEnv *env, jobject type,
                                                                          jlong jniZstdPointer,
                                                                          jlong dctxPointer,
                                                                          jbyteArray outputByteArray,
                                                                          jint outputEnd,
                                                                          jint outputStart,
                                                                          jbyteArray inputByteArray,
                                                                          jint inputEnd,
                                                                          jint inputStart) {
    auto jniZstd = reinterpret_cast<JniZstd *>(jniZstdPointer);
    auto dctx = reinterpret_cast<ZSTD_DCtx *>(dctxPointer);

    auto inputByteArrayElements = env->GetByteArrayElements(inputByteArray, nullptr);
    ZSTD_inBuffer zstdInput = {inputByteArrayElements, static_cast<size_t>(inputEnd),
                               static_cast<size_t>(inputStart)};

    auto outputByteArrayElements = env->GetByteArrayElements(outputByteArray, nullptr);
    ZSTD_outBuffer zstdOutput = {outputByteArrayElements, static_cast<size_t>(outputEnd),
                                 static_cast<size_t>(outputStart)};

    size_t result;
    if (inputByteArrayElements != nullptr && outputByteArrayElements != nullptr) {
        result = ZSTD_decompressStream(dctx, &zstdOutput, &zstdInput);
    } else {
        result = -ZSTD_error_GENERIC;
    }

    env->SetIntField(type, jniZstd->zstdDecompressorOutputBytesProcessed,
                     static_cast<jint>(zstdOutput.pos) - outputStart);
    env->SetIntField(type, jniZstd->zstdDecompressorInputBytesProcessed,
                     static_cast<jint>(zstdInput.pos) - inputStart);

    env->ReleaseByteArrayElements(inputByteArray, inputByteArrayElements, JNI_ABORT);
    env->ReleaseByteArrayElements(outputByteArray, outputByteArrayElements, 0);

    return (jlong) result;
}

extern "C" JNIEXPORT void JNICALL
Java_dev_ragnarok_fenrir_module_zstd_JniZstdDecompressor_close(JNIEnv *env, jobject type,
                                                               jlong dctxPointer) {
    auto dctx = reinterpret_cast<ZSTD_DCtx *>(dctxPointer);
    ZSTD_freeDCtx(dctx);
}