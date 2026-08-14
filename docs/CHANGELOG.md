# FastSTT Changelog

## [0.1.2] - 2026-08-14
- Integrated native `FastSIMD` (v0.1.3) AVX2 16-bit PCM to 32-bit float vector conversion.
- Added official JMH benchmark suite measuring 8,792 transcription ops/sec.
- Placed Quick Start at top above Table of Contents in README.md.
- Added `Key Features`, `Real-World Use Cases`, and `Performance Benchmarks` sections.
- Updated full 5-module installation stack (`FastSTT`, `FastSIMD`, `FastMemory`, `FastPointer`, `FastAudioProcess`).

## [0.1.1] - 2026-05-18
- Initial release of FastSTT with local Whisper C++ and ElevenLabs cloud Scribe APIs.
