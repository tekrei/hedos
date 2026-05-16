package hedos.ga.lso;

import com.google.inject.Inject;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.StructuredTaskScope;

/**
 * Partitioned 2-opt optimization.
 * Divides the tour into disjoint segments and optimizes them in parallel.
 */
public class PartitionedTwoOptOptimization extends LocalSearchOptimizer {
    private final LSORuntime runtime;
    private final TwoOptOptimization sequentialLso;

    @Inject
    public PartitionedTwoOptOptimization(LSORuntime runtime, TwoOptOptimization sequentialLso) {
        this.runtime = runtime;
        this.sequentialLso = sequentialLso;
    }

    @Override
    public void optimize(int[] genes, int[][] neighborLists, int n) {
        MemorySegment distanceMatrix = runtime.getDistanceMatrix();
        int processors = Runtime.getRuntime().availableProcessors();
        int segmentSize = (genes.length - 2) / processors;
        
        if (segmentSize < 10) {
            // Fallback to standard sequential 2-opt for small tours
            sequentialLso.optimize(genes, neighborLists, n);
            return;
        }

        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
            for (int p = 0; p < processors; p++) {
                final int procIdx = p;
                scope.fork(() -> {
                    int start = 1 + (procIdx * segmentSize);
                    int end = (procIdx == processors - 1) ? genes.length - 2 : start + segmentSize;

                    for (int i = start; i < end - 1; i++) {
                        for (int j = i + 1; j < end; j++) {
                            float d0 = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) genes[i - 1] * n + genes[i]) + 
                                       distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) genes[j] * n + genes[j + 1]);
                            float d1 = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) genes[i - 1] * n + genes[j]) + 
                                       distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) genes[i] * n + genes[j + 1]);
                            
                            if (d1 < d0) {
                                reverse(genes, i, j);
                            }
                        }
                    }
                    return null;
                });
            }
            scope.join();
        } catch (Exception e) {
            throw new RuntimeException("Partitioned 2-Opt failed", e);
        }
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.Partitioned2Opt"; }
}