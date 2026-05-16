package hedos.ga.benchmark;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class LSOBenchmark {
    private static final VectorSpecies<Integer> I_SPECIES = IntVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Float> F_SPECIES = FloatVector.SPECIES_PREFERRED;

    @Param({"500", "2000"})
    private int n;

    private int[] genes;
    private MemorySegment distanceMatrix;
    private float[] distsAC, distsBD, distsCD;

    @Setup
    public void setup() {
        Random random = new Random(42);
        genes = new int[n];
        for (int i = 0; i < n; i++) genes[i] = i;
        
        distanceMatrix = Arena.ofAuto().allocate((long) n * n * Float.BYTES);
        for (int i = 0; i < n * n; i++) {
            distanceMatrix.setAtIndex(ValueLayout.JAVA_FLOAT, i, random.nextFloat());
        }

        distsAC = new float[I_SPECIES.length()];
        distsBD = new float[I_SPECIES.length()];
        distsCD = new float[I_SPECIES.length()];
    }

    @Benchmark
    public float scalar2OptGain() {
        float bestGain = 0;
        int a = genes[0], b = genes[1];
        float distAB = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b);

        for (int j = 2; j < n - 1; j++) {
            int c = genes[j], d = genes[j + 1];
            float currentDist = distAB + distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + d);
            float newDist = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + c) + 
                           distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) b * n + d);
            float gain = currentDist - newDist;
            if (gain > bestGain) bestGain = gain;
        }
        return bestGain;
    }

    @Benchmark
    public float vectorizedHybrid2OptGain() {
        float bestGain = 0;
        int a = genes[0], b = genes[1];
        float distAB = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b);

        for (int j = 2; j < n - 1; j += I_SPECIES.length()) {
            var mask = I_SPECIES.indexInRange(j, n - 1);
            
            for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                if (mask.laneIsSet(lane)) {
                    int c = genes[j + lane], d = genes[j + lane + 1];
                    distsAC[lane] = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + c);
                    distsBD[lane] = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) b * n + d);
                    distsCD[lane] = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + d);
                }
            }

            var vDistAC = FloatVector.fromArray(F_SPECIES, distsAC, 0, mask.cast(F_SPECIES));
            var vDistBD = FloatVector.fromArray(F_SPECIES, distsBD, 0, mask.cast(F_SPECIES));
            var vDistCD = FloatVector.fromArray(F_SPECIES, distsCD, 0, mask.cast(F_SPECIES));

            var gains = vDistCD.add(distAB).sub(vDistAC.add(vDistBD));
            float maxLaneGain = gains.reduceLanes(VectorOperators.MAX, mask.cast(F_SPECIES));
            if (maxLaneGain > bestGain) bestGain = maxLaneGain;
        }
        return bestGain;
    }
}