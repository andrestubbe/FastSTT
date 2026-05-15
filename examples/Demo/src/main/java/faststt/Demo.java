package faststt;

import java.io.File;

/**
 * Basic Demo for FastSTT to verify JNI connectivity.
 */
public class Demo {
    public static void main(String[] args) {
        System.out.println("=== FastSTT Native Bridge Demo ===");

        // 1. Check if model exists
        File modelFile = new File("models/ggml-tiny.bin");
        if (!modelFile.exists()) {
            System.err.println("[ERROR] Model not found: models/ggml-tiny.bin");
            System.err.println("Please run run-installer.bat first to download the model.");
            return;
        }

        // 2. Initialize Engine
        System.out.println("\n[1/3] Initializing Whisper engine...");
        WhisperSTTImpl stt = new WhisperSTTImpl(modelFile.getAbsolutePath());
        System.out.println("   ✓ Native handle created");

        // 3. Test Transcription
        System.out.println("\n[2/3] Testing transcription (dummy audio)...");
        byte[] dummyAudio = new byte[16000]; // 1 second of silence
        String result = stt.transcribe(dummyAudio);
        System.out.println("   Result: " + result);

        // 4. Cleanup
        System.out.println("\n[3/3] Closing engine...");
        stt.close();
        System.out.println("   ✓ Resources released");

        System.out.println("\n=== Demo Complete! ===");
    }
}
