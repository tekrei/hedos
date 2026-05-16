package hedos.ga.lso;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.StructuredTaskScope;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simplified Lin-Kernighan heuristic implementation.
 */
@Singleton
public class LinKernighanOptimization extends LocalSearchOptimizer {
    private static final VectorSpecies<Integer> I_SPECIES = IntVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Float> F_SPECIES = FloatVector.SPECIES_PREFERRED;
    private final LSORuntime runtime;

    @Inject
    public LinKernighanOptimization(LSORuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void optimize(int[] genes, int[][] neighborLists, int n) {
        class FirstSuccessJoiner implements StructuredTaskScope.Joiner<Integer, Integer> {
            private final AtomicInteger result = new AtomicInteger(-1);
            @Override
            public boolean onComplete(StructuredTaskScope.Subtask<? extends Integer> subtask) {
                if (subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                    result.compareAndSet(-1, subtask.get());
                    return false; // Found one, stop others
                }
                return true;
            }
            @Override
            public Integer result() { return result.get(); }
        }

        MemorySegment distanceMatrix = runtime.getDistanceMatrix();
        int[] pos = new int[n]; // Move to local variable to ensure thread-safety in Singleton
        for (int i = 0; i < n; i++) pos[genes[i]] = i;

        boolean improvement = true;
        int batchSize = 50; // Batching reduces fork overhead
        int iterations = 0;
        int maxIterations = n * 2;
        
        while (improvement && iterations++ < maxIterations) {
            improvement = false;
            try (var scope = StructuredTaskScope.open(new FirstSuccessJoiner())) {
                for (int i = 0; i < n; i += batchSize) {
                    final int start = i;
                    final int end = Math.min(i + batchSize, n);
                    scope.fork(() -> {
                        for (int idx = start; idx < end; idx++) {
                            if (checkLKImprovement(genes, idx, distanceMatrix, neighborLists, n, pos)) {
                                return idx;
                            }
                        }
                        return -1;
                    });
                }
                int foundIdx = scope.join();
                if (foundIdx != -1) {
                    improvement = applyLKMove(genes, foundIdx, distanceMatrix, neighborLists, n, pos);
                }
            } catch (Exception ignored) {
                // No more improvements found
            }
        }
    }

    private boolean checkLKImprovement(int[] genes, int t1Idx, MemorySegment dist, int[][] neighborLists, int n, int[] pos) {
        int t1 = genes[t1Idx];
        int t2Idx = (t1Idx + 1) % n;
        int t2 = genes[t2Idx];
        float distT1T2 = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t1 * n + t2);

        int[] neighbors = neighborLists[t2];
        float[] distsT1T3 = new float[I_SPECIES.length()];
        float[] distsT3T4 = new float[I_SPECIES.length()];
        float[] distsT2T4 = new float[I_SPECIES.length()];

        for (int i = 0; i < neighbors.length; i += I_SPECIES.length()) {
            var mask = I_SPECIES.indexInRange(i, neighbors.length);

            // Hybrid load into local arrays
            for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                if (mask.laneIsSet(lane)) {
                    int t3 = neighbors[i + lane];
                    // Neutralize degenerate moves
                    if (t3 == t1 || t3 == t2) {
                        distsT1T3[lane] = distsT3T4[lane] = distsT2T4[lane] = 1e9f;
                        continue;
                    }
                    int t3Idx = pos[t3];
                    int t4Idx = (t3Idx + 1) % n;
                    int t4 = genes[t4Idx];

                    distsT1T3[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t1 * n + t3);
                    distsT3T4[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t3 * n + t4);
                    distsT2T4[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t2 * n + t4);
                    
                    // Enforce basic LK gain criterion: dist(t1,t2) - dist(t1,t3) MUST be positive
                    if (distT1T2 - distsT1T3[lane] <= 0) {
                        distsT1T3[lane] = distsT3T4[lane] = distsT2T4[lane] = 1e9f;
                    }
                } else {
                    // Neutralize inactive lanes
                    distsT1T3[lane] = distsT3T4[lane] = distsT2T4[lane] = 1e9f;
                }
            }

            var vT1T3 = FloatVector.fromArray(F_SPECIES, distsT1T3, 0, mask.cast(F_SPECIES));
            var vT3T4 = FloatVector.fromArray(F_SPECIES, distsT3T4, 0, mask.cast(F_SPECIES));
            var vT2T4 = FloatVector.fromArray(F_SPECIES, distsT2T4, 0, mask.cast(F_SPECIES));

            // Gain criterion: [dist(t1,t2) + dist(t3,t4)] - [dist(t1,t3) + dist(t2,t4)] > 0
            var gains = vT3T4.add(distT1T2).sub(vT1T3).sub(vT2T4);

            // Check if any lane found an improvement (with epsilon)
            if (gains.compare(VectorOperators.GT, 0.001f, mask.cast(F_SPECIES)).anyTrue()) {
                return true;
            }
        }
        return false;
    }

    private boolean applyLKMove(int[] genes, int t1Idx, MemorySegment dist, int[][] neighborLists, int n, int[] posMap) {
        int t1 = genes[t1Idx];
        int t2Idx = (t1Idx + 1) % n;
        int t2 = genes[t2Idx];
        
        float distT1T2 = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t1 * n + t2);
        
        for (int t3 : neighborLists[t2]) {
            // Synchronize with SIMD logic: Check dist(t1,t2) - dist(t1,t3) first
            float intermediateGain = distT1T2 - dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t1 * n + t3);
            if (intermediateGain <= 0) continue;

            int t3Idx = posMap[t3];
            int t4Idx = (t3Idx + 1) % n;
            int t4 = genes[t4Idx];
            
            // Total Gain calculation must match SIMD: [dist(t1,t2) + dist(t3,t4)] - [dist(t1,t3) + dist(t2,t4)]
            float totalGain = (distT1T2 + dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t3 * n + t4)) - 
                              (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t1 * n + t3) + 
                               dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) t2 * n + t4));

            if (totalGain > 0.001f) {
                if (t1Idx < t3Idx) {
                    reverseWithMap(genes, t1Idx + 1, t3Idx, posMap);
                } else {
                    reverseWithMap(genes, t3Idx + 1, t1Idx, posMap);
                }
                return true;
            }
        }
        return false;
    }

    private void reverseWithMap(int[] genes, int start, int end, int[] posMap) {
        reverse(genes, start, end);
        // Update the position map for the reversed segment
        for (int i = start; i <= end; i++) {
            posMap[genes[i]] = i;
        }
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.LinKernighan"; }
}