#include <stdint.h>
#include <iostream>
#include <jni.h>
#include <string>
#include <vector>

// Whisper handle (placeholder for whisper_context*)
typedef void *whisper_handle;

extern "C" {

JNIEXPORT jlong JNICALL Java_faststt_WhisperSTTImpl_initializeNative(
    JNIEnv *env, jobject obj, jstring modelPath) {
  const char *path = env->GetStringUTFChars(modelPath, nullptr);

  std::cout << "[Native] Initializing Whisper model from: " << path
            << std::endl;

  // TODO: Integrate whisper_init_from_file(path)
  // For now, we return a dummy handle
  whisper_handle handle = (void *)(uintptr_t)0xDEADBEEF;

  env->ReleaseStringUTFChars(modelPath, path);
  return (jlong)handle;
}

JNIEXPORT jstring JNICALL Java_faststt_WhisperSTTImpl_transcribeNative(
    JNIEnv *env, jobject obj, jlong handle, jbyteArray pcmAudio) {
  jsize len = env->GetArrayLength(pcmAudio);
  jbyte *pcmData = env->GetByteArrayElements(pcmAudio, nullptr);

  std::cout << "[Native] Transcribing " << len << " bytes of audio..."
            << std::endl;

  // TODO: Convert PCM bytes to float vector and call whisper_full()

  env->ReleaseByteArrayElements(pcmAudio, pcmData, JNI_ABORT);

  return env->NewStringUTF("Transcription successful (Native Placeholder)");
}

JNIEXPORT void JNICALL Java_faststt_WhisperSTTImpl_closeNative(JNIEnv *env,
                                                               jobject obj,
                                                               jlong handle) {
  std::cout << "[Native] Closing Whisper context." << std::endl;
  // TODO: whisper_free((whisper_context*)handle);
}
}
