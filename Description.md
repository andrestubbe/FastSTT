# FastSTT (Speech-to-Text)

## 1. Vision & Kernidee
**FastSTT** ist das universelle "Ohr" des Agenten. Genauso wie `FastTTS` verschiedene Sprech-Engines vereint, fasst FastSTT verschiedene Spracherkennungs-Engines unter einer sauberen, Streaming-fähigen Java-API zusammen.

**Warum nicht *nur* lokales Whisper?**
Lokales Whisper (via `whisper.cpp`) ist fantastisch für Privatsphäre und Kostenkontrolle. Aber wenn es auf extrem geringe Latenz ankommt (Echtzeit-Diktat), schlagen spezialisierte Cloud-Anbieter wie **Deepgram** oder die **OpenAI API** die lokale CPU-Version manchmal um Längen. FastSTT lässt dir die Wahl.

**Die Unified Architecture:**
- **Lokal (whisper.cpp):** Nutzt C++ für maximale Performance. FastJava lädt das Whisper-Modell in den RAM und füttert es via JNI direkt mit Audiodaten aus `FastAudioCapture`. Beschleunigung über AVX2 (CPU) oder Vulkan/CUDA (GPU).
- **Online (Deepgram / OpenAI):** Nutzt WebSockets für echtes Echtzeit-Streaming. Während du sprichst, korrigiert die Cloud den Text im Millisekundentakt.

## 2. Java High-Level API

```java
public interface FastSTT {
    
    // Initialisiert die Engine
    static FastSTT createLocalWhisper(String modelPath) { return new WhisperSTTImpl(modelPath); }
    static FastSTT createDeepgram(String apiKey) { return new DeepgramSTTImpl(apiKey); }

    // Einmalige Transkription eines Audio-Puffers (z.B. Sprachnachricht)
    String transcribe(byte[] pcmAudio);

    // Echtzeit-Streaming (Voice Assistant Modus)
    void startStreaming(FastSTTListener listener);
    void stopStreaming();
}

public interface FastSTTListener {
    // Wird gefeuert, wenn ein Wort erkannt wurde, der Satz aber noch nicht fertig ist
    void onPartialResult(String text);
    
    // Wird gefeuert, wenn eine Sprachpause erkannt wurde (Satz beendet)
    void onFinalResult(String text);
}
```

## 3. Architektur-Details (Local vs Cloud)

**Lokales Whisper Backend (JNI):**
1. Anstatt Python-Wrapper zu bemühen, bindet FastJava direkt die `whisper.dll` ein.
2. Der `FastAudioPlayer` (Capture) liefert einen kontinuierlichen Strom an PCM-Audio (16kHz, 16-bit).
3. Dieser Puffer wird direkt via Shared Memory / JNI an den C++ Thread übergeben. 
4. VAD (Voice Activity Detection) läuft in C++, um Stille herauszufiltern und CPU-Zeit zu sparen. Sobald jemand spricht, wird Whisper aktiv.

**Cloud Backend (WebSockets):**
1. Der Audio-Stream wird direkt über einen asynchronen WebSocket an Deepgram/OpenAI gestreamt.
2. Über denselben WebSocket kommen sofort die `onPartialResult` Strings zurück, die der Java-Listener auswertet.

## 4. Agent-Kit (KI-Integration)
Das ist das wichtigste Modul für "Voice Assistant" Features.

**Workflow für Agenten (Wake-Word & Command):**
1. **Wake-Word:** Ein winziges, extrem leichtgewichtiges lokales Modell horcht 24/7 auf ein Wake-Word (z.B. "Hey Butler"). 
2. **Streaming:** Sobald das Wake-Word fällt, startet `FastSTT` den Echtzeit-Stream.
3. **Intent-Erkennung:** Während der User noch spricht (`onPartialResult`), kann der Agent bereits Kontext suchen (FastVectorDB) oder Tools vorbereiten (FastMCP).
4. Sobald `onFinalResult` eintrifft, schlägt der Agent zu und antwortet via `FastTTS`.
