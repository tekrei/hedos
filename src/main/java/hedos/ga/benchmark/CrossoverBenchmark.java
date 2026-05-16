package hedos.ga.benchmark;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the performance difference between scalar and SIMD-accelerated 
 * uniform crossover operations.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class CrossoverBenchmark {
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

    @Param({"500", "2000", "5000"})
    private int n;

    private int[] g1, g2, off1, off2;
    private Random random;

    @Setup
    public void setup() {
        g1 = new int[n];
        g2 = new int[n];
        off1 = new int[n];
        off2 = new int[n];
        random = new Random(42);
        for (int i = 0; i < n; i++) {
            g1[i] = i;
            g2[i] = n - i - 1;
        }
    }

    @Benchmark
    public void scalarUniformCrossover() {
        for (int i = 0; i < n; i++) {
            if (random.nextBoolean()) {
                off1[i] = g1[i];
                off2[i] = g2[i];
            } else {
                off1[i] = g2[i];
                off2[i] = g1[i];
            }
        }
    }

    @Benchmark
    public void vectorizedUniformCrossover() {
        for (int i = 0; i < n; i += SPECIES.length()) {
            var mask = SPECIES.indexInRange(i, n);
            
            // Use the optimized bit-pattern mask generation
            var vMask = VectorMask.fromLong(SPECIES, random.nextLong()).and(mask);

            var v1 = IntVector.fromArray(SPECIES, g1, i, mask);
            var v2 = IntVector.fromArray(SPECIES, g2, i, mask);

            // Blend genes based on the random mask
            v1.blend(v2, vMask).intoArray(off1, i, mask);
            v2.blend(v1, vMask).intoArray(off2, i, mask);
        }
    }
}
