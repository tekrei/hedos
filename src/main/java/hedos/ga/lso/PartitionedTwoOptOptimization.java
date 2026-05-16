package hedos.ga.lso;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.stream.IntStream;

/**
 * Partitioned 2-opt optimization.
 * Divides the tour into disjoint segments and optimizes them in parallel.
 */
public class PartitionedTwoOptOptimization extends LocalSearchOptimizer {

    @Override
    public void optimize(int[] genes, MemorySegment distanceMatrix, int[][] neighborLists, int n) {
        int processors = Runtime.getRuntime().availableProcessors();
        int segmentSize = (genes.length - 2) / processors;
        
        if (segmentSize < 10) {
            // Fallback to standard sequential 2-opt for small tours
            new TwoOptOptimization().optimize(genes, distanceMatrix, neighborLists, n);
            return;
        }

        IntStream.range(0, processors).parallel().forEach(p -> {
            int start = 1 + (p * segmentSize);
            int end = (p == processors - 1) ? genes.length - 2 : start + segmentSize;

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
        });
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.Partitioned2Opt"; }
}