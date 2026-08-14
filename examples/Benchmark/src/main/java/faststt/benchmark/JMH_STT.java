package faststt.benchmark;

import faststt.FastSTT;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JMH_STT {

    private FastSTT stt;
    private byte[] audioBuffer;

    @Setup
    public void setup() {
        stt = FastSTT.createLocalWhisper("models/ggml-base.bin");
        audioBuffer = new byte[32000];
    }

    @Benchmark
    public String benchmarkTranscribe() {
        return stt.transcribe(audioBuffer);
    }
}
