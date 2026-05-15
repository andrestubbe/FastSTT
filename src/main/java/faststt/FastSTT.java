package faststt;

/**
 * High-level interface for Speech-to-Text operations.
 */
public interface FastSTT {
    
    /**
     * Transcribes a single PCM audio buffer.
     * @param pcmAudio 16kHz, 16-bit mono PCM data.
     * @return Transcribed text.
     */
    String transcribe(byte[] pcmAudio);

    /**
     * Starts a real-time streaming session.
     * @param listener Callback for partial and final results.
     */
    void startStreaming(FastSTTListener listener);

    /**
     * Stops the current streaming session.
     */
    void stopStreaming();

    /**
     * Releases native resources.
     */
    void close();
}
