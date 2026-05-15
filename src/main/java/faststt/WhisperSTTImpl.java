package faststt;

import fastcore.LibraryLoader;
import faststt.core.PathResolver;
import java.io.File;

/**
 * Implementation of FastSTT using native whisper.cpp (JNI).
 */
public class WhisperSTTImpl implements FastSTT {

    static {
        LibraryLoader.load("faststt");
    }

    private long nativeHandle;

    public WhisperSTTImpl(String modelPath) {
        String resolvedPath = PathResolver.resolve("whisper.model", modelPath);
        if (!new File(resolvedPath).exists()) {
            throw new RuntimeException("Whisper model not found at: " + resolvedPath);
        }
        this.nativeHandle = initializeNative(resolvedPath);
    }

    @Override
    public String transcribe(byte[] pcmAudio) {
        return transcribeNative(nativeHandle, pcmAudio);
    }

    @Override
    public void startStreaming(FastSTTListener listener) {
        // Implementation for real-time streaming using FastAudioCapture
    }

    @Override
    public void stopStreaming() {
        // Stop streaming logic
    }

    @Override
    public void close() {
        if (nativeHandle != 0) {
            closeNative(nativeHandle);
            nativeHandle = 0;
        }
    }

    // Native Methods
    private native long initializeNative(String modelPath);
    private native String transcribeNative(long handle, byte[] pcmAudio);
    private native void closeNative(long handle);
}
