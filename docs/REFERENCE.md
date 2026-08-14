# FastSTT API Reference Manual

`FastSTT` provides native C++ AVX2 audio preprocessing, local Whisper GGML model execution, and ElevenLabs cloud Scribe speech recognition.

---

## 1. FastSTT Engine Creation API

### `createLocalWhisper`
```java
public static FastSTT createLocalWhisper(String modelPath)
```
Creates a local Whisper Speech-to-Text engine using a GGML model file.

---

### `createElevenLabs`
```java
public static FastSTT createElevenLabs(String apiKey)
```
Creates an ElevenLabs cloud Scribe speech-to-text streaming engine.

---

## 2. Transcription API

### `transcribe`
```java
public String transcribe(byte[] pcmAudio)
public String transcribe(File wavFile)
```
Transcribes 16kHz 16-bit mono PCM audio buffers or WAV audio files directly.
