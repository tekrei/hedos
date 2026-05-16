package hedos.ga.lso;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.stream.IntStream;

/**
 * Partitioned 3-opt optimization.
 * Divides the tour into disjoint segments and optimizes them in parallel.
 * This provides higher concurrency by allowing multiple threads to apply local 
 * improvements simultaneously without waiting for a global reduction.
 */
public class PartitionedThreeOptOptimization extends LocalSearchOptimizer {

    @Override
    public void optimize(int[] genes, MemorySegment distanceMatrix, int[][] neighborLists, int n) {
        int processors = Runtime.getRuntime().availableProcessors();
        // We need segments large enough for 3-opt (at least 12 cities)
        int segmentSize = (genes.length - 2) / processors;
        
        if (segmentSize < 12) {
            // Fallback to sequential best-improvement for small tours
            new BestThreeOptOptimization().optimize(genes, distanceMatrix, neighborLists, n);
            return;
        }

        IntStream.range(0, processors).parallel().forEach(p -> {
            int start = 1 + (p * segmentSize);
            int end = (p == processors - 1) ? genes.length - 1 : start + segmentSize;
            
            boolean segmentImproved = true;
            while (segmentImproved) {
                segmentImproved = false;
                for (int i = start + 1; i < end - 4; i++) {
                    int cityA = genes[i - 1];
                    for (int j = i + 2; j < end - 2; j++) {
                        int cityC = genes[j - 1];
                        // In partitioned mode, we only check neighbors within the same segment 
                        // to maintain thread-safe, lock-free parallel execution.
                        for (int cityF : neighborLists[cityC]) {
                            // Quick search for neighbor index within segment
                            int k = -1;
                            for (int idx = j + 2; idx < end; idx++) {
                                if (genes[idx] == cityF) {
                                    k = idx;
                                    break;
                                }
                            }
                            
                            if (k != -1) {
                                if (tryLocal3OptMove(genes, i, j, k, distanceMatrix, n)) {
                                    segmentImproved = true;
                                    break;
                                }
                            }
                        }
                        if (segmentImproved) break;
                    }
                    if (segmentImproved) break;
                }
            }
        });
    }

    private boolean tryLocal3OptMove(int[] genes, int i, int j, int k, MemorySegment dist, int n) {
        int a = genes[i - 1], b = genes[i];
        int c = genes[j - 1], d = genes[j];
        int e = genes[k - 1], f = genes[k];

        float d0 = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b) + 
                   dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + d) + 
                   dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) e * n + f);

        // Check standard 3-opt reconnection cases
        if (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + c) + 
            dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) b * n + d) + 
            dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) e * n + f) < d0) {
            reverse(genes, i, j - 1);
            return true;
        }
        if (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b) + 
            dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + e) + 
            dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) d * n + f) < d0) {
            reverse(genes, j, k - 1);
            return true;
        }
        if (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + d) + 
            dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) e * n + b) + 
            dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + f) < d0) {
            int[] segmentB = new int[j - i];
            System.arraycopy(genes, i, segmentB, 0, j - i);
            int[] segmentC = new int[k - j];
            System.arraycopy(genes, j, segmentC, 0, k - j);
            System.arraycopy(segmentC, 0, genes, i, segmentC.length);
            System.arraycopy(segmentB, 0, genes, i + segmentC.length, segmentB.length);
            return true;
        }
        return false;
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.Partitioned3Opt"; }
}