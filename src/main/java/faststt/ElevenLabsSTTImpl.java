package faststt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

/**
 * ElevenLabs Speech-to-Text (Scribe API) provider implementation for FastSTT.
 * Lightweight, zero-dependency cloud transcription engine.
 */
public class ElevenLabsSTTImpl implements FastSTT {

    private final String apiKey;
    private final String modelId;
    private final String language;
    private final HttpClient httpClient;

    public ElevenLabsSTTImpl(String apiKey) {
        this(apiKey, "scribe_v1", "auto");
    }

    public ElevenLabsSTTImpl(String apiKey, String modelId, String language) {
        this.apiKey = apiKey;
        this.modelId = modelId != null ? modelId : "scribe_v1";
        this.language = language != null ? language : "auto";
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String transcribe(byte[] pcmAudio) {
        if (pcmAudio == null || pcmAudio.length == 0 || apiKey == null || apiKey.isEmpty()) {
            return "";
        }

        try {
            byte[] wavBytes = createWavHeader(pcmAudio, 16000, 1, 16);
            return sendToElevenLabs(wavBytes, "audio.wav");
        } catch (Exception e) {
            System.err.println("[ElevenLabs] Exception: " + e.getMessage());
            return "";
        }
    }

    @Override
    public String transcribe(File wavFile) {
        if (wavFile == null || !wavFile.exists() || apiKey == null || apiKey.isEmpty()) {
            return "";
        }

        try {
            byte[] wavBytes = Files.readAllBytes(wavFile.toPath());
            return sendToElevenLabs(wavBytes, wavFile.getName());
        } catch (Exception e) {
            System.err.println("[ElevenLabs] File transcription exception: " + e.getMessage());
            return "";
        }
    }

    private String sendToElevenLabs(byte[] audioBytes, String filename) throws IOException, InterruptedException {
        String boundary = "----ElevenLabsBoundary" + UUID.randomUUID().toString().substring(0, 8);
        ByteArrayOutputStream body = new ByteArrayOutputStream();

        String lower = filename.toLowerCase();
        String contentType = "audio/wav";
        if (lower.endsWith(".mp3")) contentType = "audio/mpeg";
        else if (lower.endsWith(".m4a") || lower.endsWith(".mp4")) contentType = "audio/mp4";
        else if (lower.endsWith(".ogg")) contentType = "audio/ogg";
        else if (lower.endsWith(".flac")) contentType = "audio/flac";

        // File part
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(audioBytes);
        body.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Model ID part
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"model_id\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(modelId.trim().getBytes(StandardCharsets.UTF_8));
        body.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Language part (if specified)
        if (language != null && !language.equalsIgnoreCase("auto")) {
            body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(("Content-Disposition: form-data; name=\"language_code\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(language.trim().getBytes(StandardCharsets.UTF_8));
            body.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.elevenlabs.io/v1/speech-to-text"))
            .header("xi-api-key", apiKey)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseTextResponse(response.body());
        } else {
            System.err.println("[ElevenLabs] API Error (" + response.statusCode() + "): " + response.body());
            return "";
        }
    }

    private String parseTextResponse(String json) {
        if (json == null) return "";
        int textIdx = json.indexOf("\"text\":");
        if (textIdx != -1) {
            int start = json.indexOf("\"", textIdx + 7);
            if (start != -1) {
                int end = json.indexOf("\"", start + 1);
                if (end != -1) {
                    return json.substring(start + 1, end).replace("\\\"", "\"").replace("\\n", "\n");
                }
            }
        }
        return json;
    }

    private byte[] createWavHeader(byte[] pcmData, int sampleRate, int channels, int bitsPerSample) throws IOException {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int dataSize = pcmData.length;
        int chunkSize = 36 + dataSize;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
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
        dos.flush();
        return baos.toByteArray();
    }

    @Override
    public void startStreaming(FastSTTListener listener) {}

    @Override
    public void stopStreaming() {}

    @Override
    public void close() {}
}
