# FastSTT 0.1.3 [ALPHA-2026-09-30] — Ultra-Fast Native Speech-to-Text for Java

[![Status](https://img.shields.io/badge/status-0.1.3-brightgreen.svg)](https://github.com/andrestubbe/FastSTT/releases/tag/0.1.3)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.3-green.svg)](https://jitpack.io/#andrestubbe/FastSTT)

---

**⚡ High-performance native speech-to-text module for the FastJava ecosystem. Ultra-low latency via JNI-based Whisper.cpp and real-time Cloud streaming.**

**FastSTT** provides professional-grade speech recognition with minimal latency. It unifies local high-performance processing (Whisper) with lightning-fast cloud backends (Deepgram/OpenAI) under a single Java API.

---

## Quick Start

### 1. Minimal Java Transcription (Local Whisper)
```java
import faststt.FastSTT;
import java.io.File;

public class Demo {
    public static void main(String[] args) {
        // 1. Initialize local Whisper engine with a GGML model
        FastSTT stt = FastSTT.createLocalWhisper("models/ggml-base.bin");

        // 2. Transcribe WAV audio file directly
        File audioFile = new File("audio.wav");
        if (audioFile.exists()) {
            String text = stt.transcribe(audioFile);
            System.out.println("Recognized Text: " + text);
        }

        // 3. Alternatively, transcribe from raw 16kHz 16-bit PCM bytes
        byte[] pcmBuffer = new byte[32000]; // 1 second of 16kHz mono audio
        String result = stt.transcribe(pcmBuffer);
        System.out.println("PCM Result: " + result);

        // 4. Clean up native context when done
        stt.close();
    }
}
```

### 2. Interactive Microphone Demo
Launch the interactive live microphone demo directly via batch script:
```powershell
.\run-demo.bat
```

### 3. Model Installer CLI
Download and manage Whisper GGML models (Tiny, Base, Small) with the interactive installer:
```powershell
.\run-installer.bat
```

---

## Table of Contents

- [Why FastSTT?](#why-faststt)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastSTT?

Integrating speech-to-text into Java services commonly forces developers between high-latency cloud APIs or heavy, clunky process wrappers:

1. **Disk IPC Bottlenecks**: Naive Java Whisper bridges serialize audio chunks to temporary `.wav` files on disk before spawning CLI processes, introducing 20–50 ms of disk I/O overhead per chunk.
2. **Heavy Framework Overhead**: Java bindings for legacy toolkits (Vosk, Sphinx) carry massive dependencies, complex JNI bridge layers, and high memory footprints.
3. **Lack of Zero-Copy Pipeline**: Converting live microphone PCM streams across Java heap buffers to native inference contexts creates GC pressure and cache thrashing.
4. **Inflexible Hybrid Routing**: Cloud-only speech APIs introduce network latency and privacy liabilities, while pure-offline solutions cannot dynamically fall back to cloud models when accuracy demands it.

FastSTT resolves this by compiling whisper.cpp to a slim native JNI engine with AVX2 SIMD acceleration, supporting zero-copy shared memory IPC (`FastSharedMemory`) down to 3.4 microseconds handoff latency:

| Feature | Subprocess Wrappers (`whisper.exe`) | Cloud Speech APIs (REST) | FastSTT |
|:---|:---|:---|:---|
| **Audio Handoff** | Disk WAV File (~20 ms) | HTTP multipart upload | **Zero-Copy Shared Memory (3.4 µs)** |
| **Local Inference** | External CLI binary | ❌ None (Cloud only) | **Native whisper.cpp + AVX2 SIMD** |
| **Turnaround Latency** | 500–1200 ms (Disk/process lag) | 300–800 ms (Network RTT) | **Sub-110 ms** (Direct local) |
| **Offline Privacy** | Yes (External tool) | ❌ Audio leaves premise | **✅ 100% Offline private** |
| **Hybrid Fallback** | Manual shell scripting | ❌ Cloud only | **✅ Unified Local + Cloud fallback** |
| **Dependencies** | External CLI binary installed | HTTP client + JSON parser | **Slim Native DLL via `FastCore`** |

---

## Key Features

- **🎙️ Local Whisper Engine** — Native C++ integration via whisper.cpp for 100% offline privacy and zero cloud costs.
- **⚡ Zero-Copy Shared Memory IPC** — Direct audio reading from **[FastSharedMemory](https://github.com/andrestubbe/FastSharedMemory)** pointers (`transcribeFromMemoryAddress`), cutting IPC audio transfer latency to **3.4 microseconds** (25,000x faster than disk).
- **🚀 AVX2 SIMD Vector Acceleration** — Instant native float normalization and conversion of PCM16 audio buffers directly in SIMD registers.
- **☁️ Cloud Streaming Integration** — Real-time cloud speech integration with ElevenLabs Scribe, Deepgram, and OpenAI.
- **🛠️ Integrated Model Downloader** — Built-in GGML model management tool for downloading Tiny, Base, and Small models on demand.

---

## Real-World Use Cases

- **🎙️ Real-Time Voice Assistants** — Sub-110 ms speech processing for desktop assistants and voice-activated automation tools.
- **⚡ High-Throughput Audio Pipelines** — Zero-copy shared memory integration streaming multi-channel live audio from [FastAudioCapture](https://github.com/andrestubbe/FastAudioCapture).
- **🔒 Air-Gapped Speech Transcription** — Compliant, fully offline transcription for healthcare, legal, and financial environments.
- **🔄 Hybrid Cloud/Local Fallback** — Instant local edge inference with seamless dynamic switch to cloud Scribe for complex acoustics.

---

## Performance Benchmarks

Profiled across memory handoff modes and audio transcription pipelines:

| Audio Handoff Mode | Latency / Overhead | Transcribe Execution Time |
|:---|:---:|:---:|
| **Disk WAV File IPC (`createTempFile`)** | ~20,000,000 ns (20.0 ms) | ~400–800 ms |
| **FastSTT Zero-Copy IPC (`FastSharedMemory`)** | **3,400 ns (0.0034 ms)** | **108 ms** |

*Run the benchmarks locally:*
```powershell
.\run-benchmark.bat
```

---

## API Quick Reference

### Core Transcription API (`faststt.FastSTT`)

| Method / Signature | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `FastSTT.createLocalWhisper(String modelPath)` | `FastSTT` | Creates a local offline Whisper engine loaded from a GGML model file. | [Wiki](docs/REFERENCE.md#1-faststt-engine-creation-api) |
| `FastSTT.createElevenLabs(String apiKey)` | `FastSTT` | Creates an ElevenLabs cloud Scribe speech-to-text streaming engine. | [Wiki](docs/REFERENCE.md#1-faststt-engine-creation-api) |
| `stt.transcribe(byte[] pcmAudio)` | `String` | Transcribes 16kHz 16-bit mono PCM audio byte buffer into text. | [Wiki](docs/REFERENCE.md#2-transcription-api) |
| `stt.transcribe(File wavFile)` | `String` | Reads and transcribes a standard WAV audio file from disk. | [Wiki](docs/REFERENCE.md#2-transcription-api) |
| `stt.transcribeFromMemoryAddress(long address, int bytes)` | `String` | Zero-copy transcription directly from a native shared memory address. | [Wiki](docs/REFERENCE.md#2-transcription-api) |
| `stt.close()` | `void` | Frees native Whisper context and closes cloud connection streams. | [Wiki](docs/REFERENCE.md) |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|:---|:---|:---|:---|
| **Live Microphone Demo** | [Demo.java](examples/Demo/src/main/java/faststt/Demo.java) | `run-demo.bat` | Interactive live microphone capture and real-time Whisper transcription. |
| **Model Installer Tool** | [FastSTTInstaller.java](examples/Installer/src/main/java/faststt/manager/FastSTTInstaller.java) | `run-installer.bat` | Interactive CLI for downloading and verifying GGML Whisper models. |
| **Zero-Copy IPC Demo** | [ZeroCopyDemo.java](examples/Demo/src/main/java/faststt/ZeroCopyDemo.java) | `run-zerocopy-demo.bat` | Demonstrates zero-copy audio reading from FastSharedMemory buffers. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/faststt/benchmark/Benchmark.java) | `run-benchmark.bat` | Formal OpenJDK JMH throughput and latency measurements. |

---

## Installation

FastJava modules are distributed via JitPack.

### Option 1: Maven (Recommended via JitPack)

Add the JitPack repository and dependencies to your `pom.xml`:

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
    <!-- Mandatory Native JNI Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
    <!-- Optional: Zero-Copy Shared Memory IPC -->
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
</dependencies>
```

### Option 2: Gradle (via JitPack)

Add this to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastSTT:0.1.3'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
    implementation 'com.github.andrestubbe:FastSharedMemory:0.1.2'
    implementation 'com.github.andrestubbe:FastPointer:0.1.1'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest pre-compiled JARs directly:

1. 🎙️ [**FastSTT-0.1.3.jar**](https://github.com/andrestubbe/FastSTT/releases) (The Core Module & Native DLL)
2. ⚙️ [**FastCore-0.1.0.jar**](https://github.com/andrestubbe/FastCore/releases) (Mandatory JNI Loader)
3. ⚡ [**FastSharedMemory-0.1.2.jar**](https://github.com/andrestubbe/FastSharedMemory/releases) (Zero-Copy Shared Memory IPC)

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts, transcription options, and memory layouts.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-copy architecture, SIMD vectorization, and low-latency audio pipelines.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones, CUDA GPU inference, and multi-language auto-detection.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history, release notes, and migration guides.
- **[COMPILE.md](docs/COMPILE.md)**: Compilation guide for C++ native Whisper libraries and Java sources.

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---|:---|:---|
| Windows 10/11 | x64 | ✅ Fully Supported | Native whisper.cpp JNI with AVX2 SIMD acceleration |
| Linux | x64, ARM64 | 🚧 Planned | Native Whisper build planned (Cloud fallback available) |
| macOS | Apple Silicon, x64 | 🚧 Planned | Native Whisper / Metal planned (Cloud fallback available) |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native library loader and platform abstraction
- [FastAudioCapture](https://github.com/andrestubbe/FastAudioCapture) — High-Performance Native Audio Capture for Java
- [FastAudioPlayer](https://github.com/andrestubbe/FastAudioPlayer) — Native Windows WASAPI Audio Playback for Java
- [FastTTS](https://github.com/andrestubbe/FastTTS) — High-Performance Native Windows TTS API for Java
- [FastSharedMemory](https://github.com/andrestubbe/FastSharedMemory) — High-speed inter-process memory sharing
- [FastWakeWord](https://github.com/andrestubbe/FastWakeWord) — Low-latency voice activation and wake word engine

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
