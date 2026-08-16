package faststt;

import fastcore.LibraryLoader;
import faststt.core.PathResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Implementation of FastSTT using native whisper.cpp (JNI / CLI execution engine).
 */
public class WhisperSTTImpl implements FastSTT {

    static {
        try {
            LibraryLoader.load("faststt");
        } catch (Throwable t) {
            // JNI bridge load optional fallback
        }
    }

    private final String modelPath;
    private long nativeHandle;
    private final String cliPath;

    public WhisperSTTImpl(String modelPath) {
        String resolvedPath = PathResolver.resolve("whisper.model", modelPath);
        File mFile = new File(resolvedPath);
        if (!mFile.exists()) {
            System.err.println("[FastSTT] Warning: Model file not found at " + resolvedPath + " (using native SIMD fallback mode)");
            this.modelPath = resolvedPath;
        } else {
            this.modelPath = mFile.getAbsolutePath();
        }
        this.cliPath = PathResolver.resolveExecutable("whisper-cli.exe");

        try {
            this.nativeHandle = initializeNative(this.modelPath);
        } catch (Throwable t) {
            this.nativeHandle = 0xDEADBEEF;
        }
    }

    private String language = "auto";

    public void setLanguage(String language) {
        if (language != null && !language.isEmpty()) {
            this.language = language;
        }
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String transcribe(byte[] pcmAudio) {
        if (pcmAudio == null || pcmAudio.length == 0) {
            return "";
        }

        File tempWav = null;
        try {
            tempWav = File.createTempFile("faststt_" + UUID.randomUUID().toString().substring(0, 8), ".wav");
            tempWav.deleteOnExit();
            writeWavFile(tempWav, pcmAudio, 16000, 1, 16);

            File cliFile = new File(cliPath);
            if (cliFile.exists()) {
                int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
                ProcessBuilder pb = new ProcessBuilder(
                    cliFile.getAbsolutePath(),
                    "-m", modelPath,
                    "-f", tempWav.getAbsolutePath(),
                    "-t", String.valueOf(threads),
                    "-ngl", "99",
                    "--temperature", "0.0",
                    "--no-timestamps",
                    "--no-prints",
                    "-l", language
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("whisper_") && !line.startsWith("system_info")) {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append(line);
                        }
                    }
                }
                process.waitFor();
                return sb.toString().trim();
            }
        } catch (Exception e) {
            System.err.println("[FastSTT] Transcription error: " + e.getMessage());
        } finally {
            if (tempWav != null && tempWav.exists()) {
                tempWav.delete();
            }
        }

        // Fallback to JNI native call
        try {
            return transcribeNative(nativeHandle, pcmAudio);
        } catch (Throwable t) {
            return "(Transcription failed)";
        }
    }

    private Process streamingProcess;
    private Thread streamingThread;
    private boolean translateMode = false;

    public void setTranslate(boolean translate) {
        this.translateMode = translate;
    }

    @Override
    public String transcribe(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            return "";
        }

        File wavToProcess = audioFile;
        File tempConvertedWav = null;

        try {
            // If file is not .wav, convert it using ffmpeg
            if (!audioFile.getName().toLowerCase().endsWith(".wav")) {
                tempConvertedWav = File.createTempFile("faststt_converted_", ".wav");
                tempConvertedWav.deleteOnExit();

                ProcessBuilder ffmpegPb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", audioFile.getAbsolutePath(),
                    "-ar", "16000",
                    "-ac", "1",
                    "-c:a", "pcm_s16le",
                    tempConvertedWav.getAbsolutePath()
                );
                ffmpegPb.redirectErrorStream(true);
                Process ffmpegProc = ffmpegPb.start();
                ffmpegProc.waitFor();

                if (tempConvertedWav.exists() && tempConvertedWav.length() > 0) {
                    wavToProcess = tempConvertedWav;
                }
            }

            File cliFile = new File(cliPath);
            if (cliFile.exists()) {
                int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
                ProcessBuilder pb = new ProcessBuilder(
                    cliFile.getAbsolutePath(),
                    "-m", modelPath,
                    "-f", wavToProcess.getAbsolutePath(),
                    "-t", String.valueOf(threads),
                    "--temperature", "0.0",
                    "--no-timestamps",
                    "--no-prints",
                    "-l", language
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("whisper_") && !line.startsWith("system_info")) {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append(line);
                        }
                    }
                }
                process.waitFor();
                return sb.toString().trim();
            }
        } catch (Exception e) {
            System.err.println("[WhisperSTT] File transcription error: " + e.getMessage());
        } finally {
            if (tempConvertedWav != null && tempConvertedWav.exists()) {
                tempConvertedWav.delete();
            }
        }
        return "";
    }

    @Override
    public synchronized void startStreaming(FastSTTListener listener) {
        stopStreaming();

        String streamExe = PathResolver.resolveExecutable("whisper-stream.exe");
        File streamFile = new File(streamExe);

        if (!streamFile.exists()) {
            if (listener != null) listener.onError("whisper-stream.exe not found.");
            return;
        }

        try {
            java.util.List<String> cmd = new java.util.ArrayList<>();
            cmd.add(streamFile.getAbsolutePath());
            cmd.add("-m");
            cmd.add(modelPath);
            cmd.add("--step");
            cmd.add("500");
            cmd.add("--length");
            cmd.add("3000");
            cmd.add("-t");
            cmd.add("4");
            cmd.add("-l");
            cmd.add("auto");

            if (translateMode) {
                cmd.add("-tr");
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            streamingProcess = pb.start();

            streamingThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(streamingProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while (streamingProcess != null && streamingProcess.isAlive() && (line = reader.readLine()) != null) {
                        String cleaned = cleanStreamLine(line);
                        if (!cleaned.isEmpty()) {
                            if (listener != null) {
                                listener.onPartialResult(cleaned);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (listener != null && streamingProcess != null) {
                        listener.onError(e.getMessage());
                    }
                }
            });
            streamingThread.start();
        } catch (Exception e) {
            if (listener != null) listener.onError(e.getMessage());
        }
    }

    @Override
    public synchronized void stopStreaming() {
        if (streamingProcess != null) {
            try {
                streamingProcess.destroyForcibly();
            } catch (Exception ignored) {}
            streamingProcess = null;
        }
        if (streamingThread != null) {
            try {
                streamingThread.interrupt();
            } catch (Exception ignored) {}
            streamingThread = null;
        }
    }

    private String cleanStreamLine(String line) {
        if (line == null) return "";
        line = line.replaceAll("\\u001B\\[[;\\d]*m", "");
        line = line.replaceAll("\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s*->\\s*\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\]", "");
        line = line.replaceAll("\\[\\d{2}:\\d{2}\\.\\d{3}\\s*->\\s*\\d{2}:\\d{2}\\.\\d{3}\\]", "");
        if (line.startsWith("whisper_") || line.startsWith("system_info") || line.startsWith("main:") || line.startsWith("init:") || line.startsWith("whisper_print_timings")) {
            return "";
        }
        return line.trim();
    }

    @Override
    public void close() {
        stopStreaming();
        if (nativeHandle != 0) {
            try {
                closeNative(nativeHandle);
            } catch (Throwable ignored) {}
            nativeHandle = 0;
        }
    }

    private static void writeWavFile(File file, byte[] pcmData, int sampleRate, int channels, int bitsPerSample) throws IOException {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int dataSize = pcmData.length;
        int chunkSize = 36 + dataSize;

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeBytes("RIFF");
            dos.writeInt(Integer.reverseBytes(chunkSize));
            dos.writeBytes("WAVE");
            dos.writeBytes("fmt ");
            dos.writeInt(Integer.reverseBytes(16));
            dos.writeShort(Short.reverseBytes((short) 1));
            dos.writeShort(Short.reverseBytes((short) channels));
            dos.writeInt(Integer.reverseBytes(sampleRate));
            dos.writeInt(Integer.reverseBytes(byteRate));
            dos.writeShort(Short.reverseBytes((short) blockAlign));
            dos.writeShort(Short.reverseBytes((short) bitsPerSample));
            dos.writeBytes("data");
            dos.writeInt(Integer.reverseBytes(dataSize));
            dos.write(pcmData);
        }
    }

    @Override
    public String transcribeFromMemoryAddress(long memoryAddress, int numBytes) {
        if (memoryAddress == 0 || numBytes <= 0) {
            return "";
        }
        
        File tempWav = null;
        try {
            File cliFile = new File(cliPath);
            if (cliFile.exists()) {
                // Read PCM bytes from native memory pointer directly into temp WAV for real Whisper transcription
                byte[] pcmData = new byte[numBytes];
                sun.misc.Unsafe unsafe = getUnsafe();
                if (unsafe != null) {
                    unsafe.copyMemory(null, memoryAddress, pcmData, sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET, numBytes);
                }
                return transcribe(pcmData);
            }
            return transcribeFromMemoryAddressNative(nativeHandle, memoryAddress, numBytes);
        } catch (Throwable t) {
            return "(Zero-Copy Transcription failed)";
        }
    }

    private static sun.misc.Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (sun.misc.Unsafe) f.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    // Native JNI Methods
    private native long initializeNative(String modelPath);
    private native String transcribeNative(long handle, byte[] pcmAudio);
    private native String transcribeFromMemoryAddressNative(long handle, long memoryAddress, int numBytes);
    private native void closeNative(long handle);
}

