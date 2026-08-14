package faststt.demo;

import faststt.FastSTT;

public class Demo {
    public static void main(String[] args) {
        System.out.println("--- FastSTT 0.1.2 Speech-to-Text Demo ---");
        try {
            FastSTT stt = FastSTT.createLocalWhisper("models/ggml-base.bin");
            byte[] dummyPcm = new byte[32000]; // 1 second of 16kHz 16-bit PCM audio
            String text = stt.transcribe(dummyPcm);
            System.out.println("Result: " + text);
            stt.close();
            System.out.println("✔ FastSTT demo completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
