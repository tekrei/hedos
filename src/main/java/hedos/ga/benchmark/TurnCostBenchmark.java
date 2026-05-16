package hedos.ga.benchmark;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the performance difference between scalar and SIMD-accelerated 
 * turn cost (sharp turn penalty) calculations.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class TurnCostBenchmark {
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    @Param({"500", "2000", "5000"})
    private int n;

    private int[] genes;
    private float[] targetXs, targetYs, targetZs;

    @Setup
    public void setup() {
        Random random = new Random(42);
        genes = new int[n];
        targetXs = new float[n];
        targetYs = new float[n];
        targetZs = new float[n];

        for (int i = 0; i < n; i++) {
            genes[i] = i;
            targetXs[i] = random.nextFloat() * 100;
            targetYs[i] = random.nextFloat() * 100;
            targetZs[i] = random.nextFloat() * 100;
        }
    }

    @Benchmark
    public float scalarTurnCost() {
        float totalAngle = 0;
        if (n < 3) return 0;

        for (int i = 0; i < n - 2; i++) {
            int p1 = genes[i];
            int p2 = genes[i + 1];
            int p3 = genes[i + 2];

            float v1x = targetXs[p2] - targetXs[p1];
            float v1y = targetYs[p2] - targetYs[p1];
            float v1z = targetZs[p2] - targetZs[p1];

            float v2x = targetXs[p3] - targetXs[p2];
            float v2y = targetYs[p3] - targetYs[p2];
            float v2z = targetZs[p3] - targetZs[p2];

            float dot = v1x * v2x + v1y * v2y + v1z * v2z;
            float mag1 = (float) Math.sqrt(v1x * v1x + v1y * v1y + v1z * v1z);
            float mag2 = (float) Math.sqrt(v2x * v2x + v2y * v2y + v2z * v2z);

            if (mag1 > 0 && mag2 > 0) {
                float cosTheta = Math.max(-1f, Math.min(1f, dot / (mag1 * mag2)));
                totalAngle += (float) Math.acos(cosTheta);
            }
        }
        return totalAngle;
    }

    @Benchmark
    public float vectorizedTurnCost() {
        float totalAngle = 0;
        if (n < 3) return 0;

        for (int i = 0; i < n - 2; i += SPECIES.length()) {
            var mask = SPECIES.indexInRange(i, n - 2);
            
            var p1x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i, mask);
            var p1y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i, mask);
            var p1z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i, mask);

            var p2x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i + 1, mask);
            var p2y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i + 1, mask);
            var p2z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i + 1, mask);

            var p3x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i + 2, mask);
            var p3y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i + 2, mask);
            var p3z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i + 2, mask);

            var v1x = p2x.sub(p1x);
            var v1y = p2y.sub(p1y);
            var v1z = p2z.sub(p1z);

            var v2x = p3x.sub(p2x);
            var v2y = p3y.sub(p2y);
            var v2z = p3z.sub(p2z);

            var dot = v1x.mul(v2x).add(v1y.mul(v2y)).add(v1z.mul(v2z));
            var mag1 = v1x.mul(v1x).add(v1y.mul(v1y)).add(v1z.mul(v1z)).sqrt();
            var mag2 = v2x.mul(v2x).add(v2y.mul(v2y)).add(v2z.mul(v2z)).sqrt();

            var validMask = mask.and(mag1.compare(VectorOperators.GT, 0.0f)).and(mag2.compare(VectorOperators.GT, 0.0f));
            var cosTheta = dot.div(mag1.mul(mag2))
                    .lanewise(VectorOperators.MIN, 1.0f)
                    .lanewise(VectorOperators.MAX, -1.0f);

            var angles = cosTheta.lanewise(VectorOperators.ACOS);
            totalAngle += angles.reduceLanes(VectorOperators.ADD, validMask);
        }
        return totalAngle;
    }
}
