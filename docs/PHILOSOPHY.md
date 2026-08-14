# FastSTT Design Philosophy

`FastSTT` is engineered around three core speech recognition principles:

1. **Zero Garbage Collection Audio Streaming**: Audio buffers are processed off-heap to guarantee smooth sub-100ms transcription latencies.
2. **AVX2 Vector Audio Preprocessing**: Leverages 256-bit SIMD registers to convert 16-bit PCM samples into 32-bit normalized floats at multi-gigabyte per second speeds.
3. **Dual Offline & Cloud Architecture**: Local Whisper C++ engine for zero-cost offline processing + ElevenLabs cloud Scribe for ultra-high accuracy.
