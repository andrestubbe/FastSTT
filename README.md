# FastSTT 0.1.2 [ALPHA-2026-08] — High-Performance Native Speech-to-Text for Java

[![Status](https://img.shields.io/badge/status-0.1.2-brightgreen.svg)](https://github.com/andrestubbe/FastSTT/releases/tag/0.1.2)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.2-green.svg)](https://jitpack.io/#andrestubbe/FastSTT)

---

**⚡ Hardware SIMD-accelerated native Whisper Speech-to-Text and ElevenLabs real-time audio transcription engine for Java.**

`FastSTT` provides native C++ AVX2 vector audio preprocessing and Whisper C++ bindings for Java applications, enabling low-latency real-time voice recognition with zero Garbage Collection pressure.

![Showcase](docs/screenshot.png)

---

## Quick Start — Example

```java
import faststt.FastSTT;

public class Demo {
    public static void main(String[] args) {
        // 1. Create native SIMD Whisper Speech-to-Text engine
        FastSTT stt = FastSTT.createLocalWhisper("models/ggml-base.bin");

        // 2. Transcribe 16kHz 16-bit mono PCM audio buffer
        byte[] pcmAudio = new byte[32000]; // 1 second audio
        String text = stt.transcribe(pcmAudio);

        System.out.println("Transcribed Text: " + text);
    }
}
```

---

## Table of Contents

- [Why FastSTT?](#why-faststt)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Reference](#api-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastSTT?

Standard Java audio processing libraries struggle with real-time speech recognition due to slow scalar float conversions, excessive heap allocations, and JNI transfer overhead. FastSTT solves this by:

- **AVX2 SIMD Audio Vector Preprocessing** — Uses 256-bit SIMD registers to convert 16-bit PCM audio samples into 32-bit normalized floats at multi-gigabyte per second speeds.
- **Native Whisper C++ Engine** — Direct native bindings to Whisper.cpp with GPGPU/AVX2 execution.
- **ElevenLabs Scribe Real-Time Cloud Integration** — Low-latency WebSocket streaming for cloud AI transcription.

---

## Key Features

* **⚡ AVX2 Audio Normalization** — Hardware-accelerated 16-bit PCM to 32-bit float vector conversion.
* **🎙️ Local Whisper AI Engine** — Offline speech recognition with support for GGML models (tiny, base, small, medium, large).
* **🌐 Cloud ElevenLabs Scribe API** — Real-time streaming transcription using ElevenLabs cloud infrastructure.
* **🔄 Zero-GC Off-Heap Buffers** — Direct memory buffer transcription operating outside JVM Garbage Collection limits.
* **⚡ Full FastJava Interoperability** — Seamlessly integrates with **[FastAudioProcess](https://github.com/andrestubbe/FastAudioProcess)** and **[FastAudioCapture](https://github.com/andrestubbe/FastAudioCapture)**.

---

## Real-World Use Cases

- 🎙️ **Voice Command & Control**: Real-time local speech-to-text input in desktop applications and gaming GUIs.
- 💬 **Live Subtitle Generation**: Stream live microphone audio to transcription pipelines with sub-100ms latency.
- 📞 **Call Center & Meeting Summarization**: Transcribe call audio recordings directly from file or stream.
- 🤖 **Voice AI Assistants**: Combine with **[FastTTS](https://github.com/andrestubbe/FastTTS)** for conversational voice agents.

---

## Performance Benchmarks

In the official [JMH Benchmark](examples/Benchmark), `FastSTT` measured audio vector normalization and transcription throughput:

```text
Benchmark                     Mode  Cnt  Score   Error  Units
JMH_STT.benchmarkTranscribe  thrpt    2  8,792          ops/s
```

> **8,792 Ops / sec**: `FastSTT` executes audio vector conversions and transcription scheduling at **8,792 operations per second** with **zero JVM Garbage Collection allocations**.

---

## API Reference

### Core Classes

#### `FastSTT` — Speech-to-Text Factory & Interface

- `createLocalWhisper(modelPath)` — Create native Whisper AI engine using GGML model file.
- `createElevenLabs(apiKey)` — Create cloud ElevenLabs Scribe real-time speech engine.
- `transcribe(pcmAudio)` — Transcribe 16kHz 16-bit mono PCM audio byte array.
- `transcribe(wavFile)` — Transcribe WAV audio file directly from disk.
- `startStreaming(listener)` — Begin low-latency real-time audio streaming transcription.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the complete dependency stack to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastSTT Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastSTT</artifactId>
        <version>0.1.2</version>
    </dependency>

    <!-- FastSIMD Hardware Vector Acceleration Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastSIMD</artifactId>
        <version>0.1.3</version>
    </dependency>

    <!-- FastMemory Aligned Allocator -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastMemory</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastPointer Address Wrapper -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastPointer</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastAudioProcess Audio Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAudioProcess</artifactId>
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

---

## Documentation

- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.
- **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
- **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and routing logic.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Off-heap zero-GC memory philosophy.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🔄 Planned |
| macOS | 🔄 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastAudioProcess](https://github.com/andrestubbe/FastAudioProcess) — Hardware-accelerated audio processing engine
- [FastTTS](https://github.com/andrestubbe/FastTTS) — Native Text-to-Speech synthesis engine
- [FastSIMD](https://github.com/andrestubbe/FastSIMD) — Hardware SIMD acceleration engine

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. ⚡
