# FastSTT v0.1.0 [ALPHA] — Ultra-Fast Native Speech-to-Text for Java

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastSTT/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

**⚡ A high-performance native speech-to-text module for the FastJava ecosystem. Ultra-low latency via JNI-based
Whisper.cpp and real-time Cloud streaming.**

**FastSTT** provides professional-grade speech recognition with minimal latency. It unified local high-performance
processing (Whisper) with lightning-fast cloud backends (Deepgram/OpenAI) under a single Java API.

[![FastKeyboard Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Try the Installer](#try-the-installer)
- [License](#license)

---

## Features

- **🎙️ Local Whisper**: Native C++ integration via whisper.cpp for 100% offline privacy.
- **⚡ Cloud Streaming**: Real-time WebSocket integration with Deepgram and OpenAI.
- **📦 Zero-Copy**: Audio buffers are passed directly via JNI from FastAudioCapture.
- **🛠️ Integrated Installer**: Built-in downloader for GGML models (Tiny, Base, Small).

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
       <artifactId>faststt</artifactId>
       <version>v0.1.0</version>
   </dependency>
   <dependency>
       <groupId>com.github.andrestubbe</groupId>
       <artifactId>fastcore</artifactId>
       <version>v0.1.0</version>
   </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.andrestubbe:faststt:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[faststt-v0.1.0.jar](https://github.com/andrestubbe/FastSTT/releases/download/v0.1.0/faststt-v0.1.0.jar)** (The
   Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-v0.1.0.jar)** (
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



