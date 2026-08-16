package faststt;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Scanner;

/**
 * Interactive Live Microphone Demo for FastSTT.
 * Captures real voice input from the default microphone and transcribes it using Whisper.
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   FastSTT — Live Microphone STT Demo   ");
        System.out.println("=========================================");

        String modelType = "tiny";
        String language = "auto";
        String elevenLabsKey = System.getenv("ELEVENLABS_API_KEY");
        File inputWavFile = null;
        boolean forceDummy = false;

        if (args.length > 0) {
            for (String arg : args) {
                if (arg.startsWith("--whisper=")) {
                    modelType = arg.substring(10);
                } else if (arg.startsWith("--elevenlabs=")) {
                    elevenLabsKey = arg.substring(13);
                } else if (arg.startsWith("--key=")) {
                    elevenLabsKey = arg.substring(6);
                } else if (arg.startsWith("--lang=")) {
                    language = arg.substring(7);
                } else if (arg.equalsIgnoreCase("--de") || arg.equalsIgnoreCase("-de")) {
                    language = "de";
                } else if (arg.equalsIgnoreCase("--en") || arg.equalsIgnoreCase("-en")) {
                    language = "en";
                } else if (arg.contains("--dummy")) {
                    forceDummy = true;
                } else if (arg.endsWith(".wav") || new File(arg).isFile()) {
                    inputWavFile = new File(arg);
                }
            }
        }

        FastSTT stt;
        if (elevenLabsKey != null && !elevenLabsKey.isEmpty()) {
            System.out.println("Engine:   ElevenLabs Scribe Cloud API");
            System.out.println("Language: " + language);
            System.out.println("\n[1/3] Initializing ElevenLabs STT engine...");
            stt = FastSTT.createElevenLabs(elevenLabsKey, "scribe_v1", language);
            System.out.println("   ✓ Engine initialized successfully");
        } else {
            System.out.println("Engine:   Local Whisper (" + modelType + ")");
            System.out.println("Language: " + language);

            String modelFileName = "models/ggml-" + modelType + ".bin";
            File modelFile = new File(modelFileName);
            if (!modelFile.exists()) {
                File projectRoot = new File("..\\..");
                modelFile = new File(projectRoot, modelFileName);
            }

            if (!modelFile.exists()) {
                System.err.println("\n[ERROR] Model file not found: " + modelFileName);
                System.err.println("Please run run-installer.bat first to download the model.");
                return;
            }

            System.out.println("\n[1/3] Initializing Whisper engine...");
            WhisperSTTImpl whisperImpl = new WhisperSTTImpl(modelFile.getAbsolutePath());
            whisperImpl.setLanguage(language);
            stt = whisperImpl;
            System.out.println("   ✓ Engine initialized successfully");
        }

        // 3. Audio File Benchmark Mode OR Live Microphone Loop
        if (inputWavFile != null && inputWavFile.exists()) {
            System.out.println("\n[2/3] Audio File Benchmark Mode Active.");
            System.out.println("📁 Input File: " + inputWavFile.getAbsolutePath());
            System.out.println("📊 Size:       " + (inputWavFile.length() / 1024) + " KB");
            System.out.println("\n⌛ Transcribing audio file (" + inputWavFile.getName() + ")...");

            long startTime = System.currentTimeMillis();
            String resultText = stt.transcribe(inputWavFile);
            long durationMs = System.currentTimeMillis() - startTime;

            System.out.println("\n=========================================");
            System.out.println("📝 Transcription:");
            System.out.println(resultText.isEmpty() ? "(No speech detected)" : resultText);
            System.out.println("-----------------------------------------");
            System.out.println("⏱️  Processing Time: " + durationMs + " ms");
            System.out.println("=========================================");
        } else if (!forceDummy && isMicrophoneAvailable()) {
            System.out.println("[2/3] Live Microphone Audio Mode active.");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n-----------------------------------------");
                System.out.println("👉 Press [ENTER] to start recording your voice (or type 'q' + ENTER to exit)...");
                String input = scanner.nextLine();
                if (input.trim().equalsIgnoreCase("q")) {
                    break;
                }

                System.out.println("\n🔴 RECORDING... (Press [ENTER] to stop)\n");

                byte[] audioData = recordAudioFromMicrophoneWithLivePreview(stt, scanner);

                if (audioData != null && audioData.length > 0 && !(stt instanceof ElevenLabsSTTImpl)) {
                    String finalFullText = stt.transcribe(audioData);
                    if (finalFullText != null && !finalFullText.trim().isEmpty()) {
                        System.out.println("\n-----------------------------------------");
                        System.out.println("📝 Complete Text: " + finalFullText.trim());
                    }
                }
                System.out.println("\n=========================================");
                System.out.println("✓ Session finished.");
                System.out.println("=========================================");
            }
        } else {
            System.out.println("\n[2/3] Fallback mode (Dummy Audio)...");
            byte[] dummyAudio = new byte[16000 * 2]; // 1s silence
            String result = stt.transcribe(dummyAudio);
            System.out.println("   Result: " + result);
        }

        // 4. Cleanup
        System.out.println("\n[3/3] Closing engine...");
        stt.close();
        System.out.println("   ✓ Resources released");
        System.out.println("\n=== Demo Complete! ===");
    }

    private static boolean isMicrophoneAvailable() {
        try {
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] recordAudioFromMicrophoneWithLivePreview(FastSTT stt, Scanner scanner) {
        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false); // 16kHz 16-bit Mono
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final boolean[] recording = {true};

            // Audio Capture Thread
            Thread captureThread = new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (recording[0]) {
                    int count = line.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        synchronized (out) {
                            out.write(buffer, 0, count);
                        }
                    }
                }
            });
            captureThread.start();

            // Continuous Cumulative Word-by-Word Live Transcript Thread
            final StringBuilder accumulatedTranscript = new StringBuilder();
            final int[] lastPrintedIndex = {0};

            Thread previewThread = new Thread(() -> {
                while (recording[0]) {
                    try {
                        Thread.sleep(250); // Refresh every 250ms
                        if (!recording[0]) break;

                        byte[] currentPcm;
                        synchronized (out) {
                            currentPcm = out.toByteArray();
                        }

                        if (currentPcm.length >= 8000) { // At least 250ms audio
                            String text = stt.transcribe(currentPcm);
                            if (text != null && !text.trim().isEmpty()) {
                                String cleanText = text.trim()
                                        .replace("[BLANK_AUDIO]", "")
                                        .replaceAll("\\s+", " ")
                                        .trim();

                                if (cleanText.length() > lastPrintedIndex[0]) {
                                    // Print new incoming words persistently to terminal!
                                    String newPart = cleanText.substring(lastPrintedIndex[0]);
                                    System.out.print(newPart);
                                    System.out.flush();
                                    lastPrintedIndex[0] = cleanText.length();
                                    accumulatedTranscript.setLength(0);
                                    accumulatedTranscript.append(cleanText);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            previewThread.start();

            // Wait for user to hit ENTER to stop recording
            scanner.nextLine();

            recording[0] = false;
            try {
                line.stop();
                line.close();
            } catch (Throwable ignored) {
            }

            try {
                captureThread.join(500);
            } catch (Exception ignored) {
            }
            try {
                previewThread.join(500);
            } catch (Exception ignored) {
            }

            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static String computeDelta(String oldText, String newText) {
        if (oldText == null || oldText.isEmpty()) {
            return newText;
        }
        if (newText == null || newText.isEmpty()) {
            return "";
        }
        if (newText.startsWith(oldText)) {
            return newText.substring(oldText.length());
        }

        // Match longest common prefix
        int commonLength = 0;
        int minLen = Math.min(oldText.length(), newText.length());
        while (commonLength < minLen && oldText.charAt(commonLength) == newText.charAt(commonLength)) {
            commonLength++;
        }

        if (commonLength > 0 && newText.length() > commonLength) {
            return newText.substring(commonLength);
        }

        // Word-based match fallback
        String[] oldWords = oldText.split("\\s+");
        String[] newWords = newText.split("\\s+");

        int matchCount = 0;
        for (int i = 0; i < Math.min(oldWords.length, newWords.length); i++) {
            if (oldWords[i].equalsIgnoreCase(newWords[i])) {
                matchCount++;
            } else {
                break;
            }
        }

        if (matchCount > 0 && newWords.length > matchCount) {
            StringBuilder sb = new StringBuilder();
            for (int i = matchCount; i < newWords.length; i++) {
                if (sb.length() > 0 || !newText.startsWith(" ")) sb.append(" ");
                sb.append(newWords[i]);
            }
            return sb.toString();
        }

        return " " + newText;
    }
}

