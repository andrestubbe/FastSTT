# FastSTT 0.1.3 [ALPHA] — Ultra-Fast Native Speech-to-Text for Java

[![Status](https://img.shields.io/badge/status-0.1.3-brightgreen.svg)](https://github.com/andrestubbe/FastSTT/releases/tag/0.1.3)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.3-green.svg)](https://jitpack.io/#andrestubbe/FastSTT)

**⚡ A high-performance native speech-to-text module for the FastJava ecosystem. Ultra-low latency via JNI-based
Whisper.cpp and real-time Cloud streaming.**

**FastSTT** provides professional-grade speech recognition with minimal latency. It unified local high-performance
processing (Whisper) with lightning-fast cloud backends (Deepgram/OpenAI) under a single Java API.

[![FastKeyboard Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Table of Contents

- [Features](#features)
- [Performance Benchmarks](#performance-benchmarks)
- [Installation](#installation)
- [Try the Installer](#try-the-installer)
- [License](#license)

---

## Features

- **🎙️ Local Whisper**: Native C++ integration via whisper.cpp for 100% offline privacy.
- **⚡ Zero-Copy Shared Memory IPC**: Direct audio reading from **[FastSharedMemory](https://github.com/andrestubbe/FastSharedMemory)** pointers (`transcribeFromMemoryAddress`), cutting IPC audio transfer latency to **3.4 microseconds** (25,000x faster than disk).
- **🚀 AVX2 SIMD Vector Acceleration**: Instant native float conversion of PCM16 audio buffers.
- **⚡ Cloud Streaming**: Real-time WebSocket integration with Deepgram and OpenAI.
- **🛠️ Integrated Installer**: Built-in downloader for GGML models (Tiny, Base, Small).

---

## Performance Benchmarks

| Audio Handoff Mode | Latency / Overhead | Transcribe Execution Time |
|:---|:---:|:---:|
| **Disk WAV File IPC (`createTempFile`)** | ~20,000,000 ns (20.0 ms) | ~400–800 ms |
| **FastSTT Zero-Copy IPC (`FastSharedMemory`)** | **3,400 ns (0.0034 ms)** | **108 ms** |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
   <dependency>
       <groupId>com.github.andrestubbe</groupId>
       <artifactId>FastSTT</artifactId>
       <version>0.1.3</version>
   </dependency>
   <dependency>
       <groupId>com.github.andrestubbe</groupId>
       <artifactId>FastSharedMemory</artifactId>
       <version>0.1.2</version>
   </dependency>
   <dependency>
       <groupId>com.github.andrestubbe</groupId>
       <artifactId>FastPointer</artifactId>
       <version>0.1.1</version>
   </dependency>
   <dependency>
       <groupId>com.github.andrestubbe</groupId>
       <artifactId>FastCore</artifactId>
       <version>0.1.0</version>
   </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastSTT:0.1.3'
    implementation 'com.github.andrestubbe:FastSharedMemory:0.1.2'
    implementation 'com.github.andrestubbe:FastPointer:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 🎙️ **[FastSTT-0.1.3.jar](https://github.com/andrestubbe/FastSTT/releases/download/0.1.3/FastSTT-0.1.3.jar)** (Native Speech-to-Text Engine)
2. ⚡ **[FastSharedMemory-0.1.2.jar](https://github.com/andrestubbe/FastSharedMemory/releases/download/0.1.2/FastSharedMemory-0.1.2.jar)** (Zero-Copy IPC)
3. 📌 **[FastPointer-0.1.1.jar](https://github.com/andrestubbe/FastPointer/releases/download/0.1.1/FastPointer-0.1.1.jar)** (Native Pointer Arithmetic)
4. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native JNI Loader)
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.andrestubbe:faststt:0.1.1'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[faststt-0.1.1.jar](https://github.com/andrestubbe/FastSTT/releases/download/0.1.1/faststt-0.1.1.jar)** (The
   Core Library)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (
   The Mandatory Native Loader)

---

## Try the Installer

FastSTT comes with a built-in installer to help you download and manage Whisper models.

1. Clone this repository.
2. Run `run-installer.bat`.
3. Choose **Option 1** to download a Whisper model (e.g., `base.bin`).

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native Library Loader for Java
- [FastAudioCapture](https://github.com/andrestubbe/FastAudioCapture) — High-Performance Native Audio Capture for Java
- [FastAudioPlayer](https://github.com/andrestubbe/FastAudioPlayer) — Native Windows WASAPI Audio Playback for Java
- [FastTTS](https://github.com/andrestubbe/FastTTS) — High-Performance Native Windows TTS API for Java
- [FastWakeWord](https://github.com/andrestubbe/FastWakeWord)

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*



