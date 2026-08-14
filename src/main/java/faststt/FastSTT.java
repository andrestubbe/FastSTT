package faststt;

/**
 * High-level interface for Speech-to-Text operations.
 */
public interface FastSTT {
    
    /**
     * Creates a local Whisper Speech-to-Text engine.
     */
    static FastSTT createLocalWhisper(String modelPath) {
        return new WhisperSTTImpl(modelPath);
    }

    /**
     * Creates an ElevenLabs Speech-to-Text (Scribe) engine.
     */
    static FastSTT createElevenLabs(String apiKey) {
        return new ElevenLabsSTTImpl(apiKey);
    }

    /**
     * Creates an ElevenLabs Speech-to-Text (Scribe) engine with model & language parameters.
     */
    static FastSTT createElevenLabs(String apiKey, String modelId, String language) {
        return new ElevenLabsSTTImpl(apiKey, modelId, language);
    }

    /**
     * Transcribes a single PCM audio buffer.
     * @param pcmAudio 16kHz, 16-bit mono PCM data.
     * @return Transcribed text.
     */
    String transcribe(byte[] pcmAudio);

    /**
     * Transcribes a WAV audio file directly.
     * @param wavFile Target .wav file.
     * @return Transcribed text.
     */
    String transcribe(java.io.File wavFile);

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

