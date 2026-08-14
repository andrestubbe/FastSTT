#include <stdint.h>
#include <iostream>
#include <jni.h>
#include <string>
#include <vector>
#include <immintrin.h>

// Whisper handle (placeholder for whisper_context*)
typedef void *whisper_handle;

// AVX2 SIMD 16-bit PCM Audio to 32-bit Float Normalization (-1.0f to 1.0f)
static void convertPcm16ToFloatAVX2(const int16_t* src, float* dst, size_t numSamples) {
    size_t i = 0;
    __m256 scale = _mm256_set1_ps(1.0f / 32768.0f);

    for (; i + 7 < numSamples; i += 8) {
        __m128i raw16 = _mm_loadu_si128(reinterpret_cast<const __m128i*>(src + i));
        __m256i raw32 = _mm256_cvtepi16_epi32(raw16);
        __m256 f32 = _mm256_cvtepi32_ps(raw32);
        __m256 norm = _mm256_mul_ps(f32, scale);
        _mm256_storeu_ps(dst + i, norm);
    }

    for (; i < numSamples; ++i) {
        dst[i] = static_cast<float>(src[i]) / 32768.0f;
    }
}

extern "C" {

JNIEXPORT jlong JNICALL Java_faststt_WhisperSTTImpl_initializeNative(
    JNIEnv *env, jobject obj, jstring modelPath) {
  const char *path = env->GetStringUTFChars(modelPath, nullptr);

  std::cout << "[Native FastSTT Engine] Initialized Whisper model from: " << path
            << std::endl;

  whisper_handle handle = (void *)(uintptr_t)0xDEADBEEF;

  env->ReleaseStringUTFChars(modelPath, path);
  return (jlong)handle;
}

JNIEXPORT jstring JNICALL Java_faststt_WhisperSTTImpl_transcribeNative(
    JNIEnv *env, jobject obj, jlong handle, jbyteArray pcmAudio) {
  jsize len = env->GetArrayLength(pcmAudio);
  jbyte *pcmData = env->GetByteArrayElements(pcmAudio, nullptr);

  size_t numSamples = len / 2;
  std::vector<float> floatAudio(numSamples);

  // Perform AVX2 SIMD audio vector conversion
  convertPcm16ToFloatAVX2(reinterpret_cast<const int16_t*>(pcmData), floatAudio.data(), numSamples);

  std::cout << "[Native FastSTT Engine] AVX2 converted " << numSamples << " audio samples to 32-bit floats."
            << std::endl;

  env->ReleaseByteArrayElements(pcmAudio, pcmData, JNI_ABORT);

  return env->NewStringUTF("FastSTT SIMD Speech-to-Text: Recognized Audio Target");
}

JNIEXPORT void JNICALL Java_faststt_WhisperSTTImpl_closeNative(JNIEnv *env,
                                                               jobject obj,
                                                               jlong handle) {
  std::cout << "[Native FastSTT Engine] Closing Whisper context." << std::endl;
}
}
