package faststt;

/**
 * Listener for real-time STT results.
 */
public interface FastSTTListener {
    /**
     * Fired when a word or phrase is detected, but the sentence is not yet finished.
     */
    void onPartialResult(String text);
    
    /**
     * Fired when a sentence is completed (silence detected).
     */
    void onFinalResult(String text);

    /**
     * Fired in case of an error during streaming.
     */
    void onError(String message);
}
