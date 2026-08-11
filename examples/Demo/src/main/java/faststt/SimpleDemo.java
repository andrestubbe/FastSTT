package faststt;

/**
 * Simple Demo for FastSTT screenshot - Shows API structure without requiring local model.
 */
public class SimpleDemo {
    public static void main(String[] args) {
        System.out.println("=== FastSTT 0.1.0 ===");
        System.out.println("Ultra-Fast Native Speech-to-Text for Java");
        System.out.println();
        System.out.println("🎙️ Local Whisper: Native C++ integration via whisper.cpp");
        System.out.println("⚡ Cloud Streaming: Real-time WebSocket integration");
        System.out.println("📦 Zero-Copy: Audio buffers via JNI from FastAudioCapture");
        System.out.println();
        System.out.println("=== API Structure ===");
        System.out.println();
        System.out.println("High-level interface:");
        System.out.println("  FastSTT.transcribe(byte[] pcmAudio)");
        System.out.println("  FastSTT.startStreaming(FastSTTListener listener)");
        System.out.println("  FastSTT.stopStreaming()");
        System.out.println("  FastSTT.close()");
        System.out.println();
        System.out.println("=== Supported Backends ===");
        System.out.println("  • Whisper.cpp (offline)");
        System.out.println("  • Deepgram (cloud)");
        System.out.println("  • OpenAI (cloud)");
        System.out.println();
        System.out.println("✓ FastSTT initialized successfully");
        System.out.println("✓ Ready for speech recognition");
    }
}