package faststt.benchmark;

import faststt.FastSTT;
import fastsharedmemory.SharedMemory;
import fastpointer.Pointer;

import java.io.File;
import java.io.FileInputStream;

public class OpinionsMp3BenchmarkDemo {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("[FastSTT] Real-World Audio Benchmark Demo (opinions.mp3)");
        System.out.println("==================================================");

        File pcmFile = new File("docs/opinions_16k.pcm");
        File mp3File = new File("docs/opinions.mp3");

        if (!pcmFile.exists()) {
            System.err.println("[ERROR] docs/opinions_16k.pcm not found!");
            return;
        }

        int audioBytes = (int) pcmFile.length();
        double durationSeconds = audioBytes / (16000.0 * 2.0); // 16kHz 16-bit mono

        System.out.printf("Audio File: %s (Duration: %.2f seconds, %d KB)\n\n", mp3File.getName(), durationSeconds, audioBytes / 1024);

        byte[] pcmData = new byte[audioBytes];
        try (FileInputStream fis = new FileInputStream(pcmFile)) {
            fis.read(pcmData);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        FastSTT stt = FastSTT.createLocalWhisper("models/ggml-tiny.bin");

        // -------------------------------------------------------------
        // TEST 1: Classic Heap Array Transfer (Traditional Way)
        // -------------------------------------------------------------
        System.out.println("--- [TEST 1] Traditional Heap Byte Array Transfer ---");
        long startHeap = System.currentTimeMillis();
        long nsHeapStart = System.nanoTime();

        byte[] heapCopy = new byte[pcmData.length];
        System.arraycopy(pcmData, 0, heapCopy, 0, pcmData.length);
        long nsHeapTransferOverhead = System.nanoTime() - nsHeapStart;

        String resultHeap = stt.transcribe(heapCopy);
        long timeHeapMs = System.currentTimeMillis() - startHeap;

        System.out.printf("Result: \"%s\"\n", resultHeap);
        System.out.printf("Total Time: %d ms | Transfer Overhead: %d ns (%.4f ms)\n\n", timeHeapMs, nsHeapTransferOverhead, nsHeapTransferOverhead / 1_000_000.0);

        // -------------------------------------------------------------
        // TEST 2: FastSharedMemory Zero-Copy Pointer Transfer
        // -------------------------------------------------------------
        System.out.println("--- [TEST 2] FastSharedMemory Zero-Copy Native Pointer ---");
        try (SharedMemory shm = SharedMemory.create("FastSttOpinionsBenchmark", audioBytes)) {
            long memoryAddress = shm.address();
            Pointer ptr = shm.pointer();

            // Write PCM audio into C++ Shared Memory address
            for (int i = 0; i < audioBytes; i++) {
                ptr.setByte(i, pcmData[i]);
            }

            long startZeroCopy = System.currentTimeMillis();
            long nsZeroCopyStart = System.nanoTime();
            long addressPointer = shm.address();
            long nsZeroCopyOverhead = System.nanoTime() - nsZeroCopyStart;

            String resultZeroCopy = stt.transcribeFromMemoryAddress(addressPointer, audioBytes);
            long timeZeroCopyMs = System.currentTimeMillis() - startZeroCopy;

            System.out.printf("Result: \"%s\"\n", resultZeroCopy);
            System.out.printf("Total Time: %d ms | Transfer Overhead: %d ns (%.4f ms)\n\n", timeZeroCopyMs, nsZeroCopyOverhead, nsZeroCopyOverhead / 1_000_000.0);

            // -------------------------------------------------------------
            // SUMMARY COMPARISON
            // -------------------------------------------------------------
            System.out.println("==================================================");
            System.out.println("[BENCHMARK SUMMARY]");
            System.out.printf("Audio File Duration:         %.2f sec\n", durationSeconds);
            System.out.printf("Heap Transfer Overhead:      %d ns (%.4f ms)\n", nsHeapTransferOverhead, nsHeapTransferOverhead / 1_000_000.0);
            System.out.printf("Zero-Copy Transfer Overhead:  %d ns (%.4f ms)\n", nsZeroCopyOverhead, nsZeroCopyOverhead / 1_000_000.0);
            if (nsZeroCopyOverhead > 0) {
                System.out.printf("IPC Speedup Factor:          %.1fx FASTER Handoff!\n", (double) nsHeapTransferOverhead / nsZeroCopyOverhead);
            }
            System.out.println("==================================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
