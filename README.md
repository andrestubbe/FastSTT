# FastSTT — Ultra-Fast Native Speech-to-Text for Java [v0.1.0]

**A high-performance native speech-to-text module for the FastJava ecosystem. Ultra-low latency via JNI-based Whisper.cpp and real-time Cloud streaming.**

[![Status](https://img.shields.io/badge/status-v0.1.0--alpha-orange.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

**FastSTT** provides professional-grade speech recognition with minimal latency. It unified local high-performance processing (Whisper) with lightning-fast cloud backends (Deepgram/OpenAI) under a single Java API.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Try the Installer](#try-the-installer)
- [License](#license)

## Features
- **🎙️ Local Whisper**: Native C++ integration via whisper.cpp for 100% offline privacy.
- **⚡ Cloud Streaming**: Real-time WebSocket integration with Deepgram and OpenAI.
- **📦 Zero-Copy**: Audio buffers are passed directly via JNI from FastAudioCapture.
- **🛠️ Integrated Installer**: Built-in downloader for GGML models (Tiny, Base, Small).


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
    <!-- FastSTT Library -->
    <dependency>
        <groupId>io.github.andrestubbe</groupId>
        <artifactId>faststt</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.andrestubbe:faststt:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v1.0.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1.  📦 **[faststt-v0.1.0.jar](https://github.com/andrestubbe/faststt/releases)** (The Core Library)
2.  ⚙️ **[fastcore-v1.0.0.jar](https://github.com/andrestubbe/FastCore/releases)** (The Mandatory Native Loader)

> [!IMPORTANT]
> Both JARs must be in your classpath for the native JNI calls to function correctly.

## Try the Installer

FastSTT comes with a built-in installer to help you download and manage Whisper models.

1.  Clone this repository.
2.  Run `run-installer.bat`.
3.  Choose **Option 1** to download a Whisper model (e.g., `base.bin`).

---

## License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
