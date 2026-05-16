package hedos.ga.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class BatchSizeBenchmark {

    @Param({"1000", "5000"})
    private int populationSize;

    @Param({"1", "10", "100", "500", "1000"})
    private int batchSize;

    private int[] data;

    @Setup
    public void setup() {
        data = IntStream.range(0, populationSize).toArray();
    }

    @Benchmark
    public void testBatchThroughput() throws Exception {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
            for (int i = 0; i < populationSize; i += batchSize) {
                final int start = i;
                final int end = Math.min(i + batchSize, populationSize);
                scope.fork(() -> {
                    double result = 0;
                    for (int j = start; j < end; j++) {
                        result += simulateWork(data[j]);
                    }
                    return result;
                });
            }
            scope.join();
        }
    }

    private double simulateWork(int val) {
        // Minimal work representing a distance calculation
        return Math.sqrt(val * val + (val + 1) * (val + 1));
    }
}