package hedos.ga.lso;

import com.google.inject.Singleton;
import java.util.stream.IntStream;

/**
 * Simplified Lin-Kernighan heuristic implementation.
 * Uses a variable-depth search based on the gain criterion to find tour improvements.
 */
@Singleton
public class LinKernighanOptimization extends LocalSearchOptimizer {

    @Override
    public void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n) {
        boolean improvement = true;
        while (improvement) {
            final boolean[] found = {false};
            // Parallelize starting points for the improvement search
            IntStream.range(0, n).parallel().forEach(i -> {
                if (found[0]) return;
                if (checkLKImprovement(genes, i, distanceMatrix, neighborLists, n)) {
                    synchronized (genes) {
                        if (!found[0]) {
                            applyLKMove(genes, i, distanceMatrix, neighborLists, n);
                            found[0] = true;
                        }
                    }
                }
            });
            improvement = found[0];
        }
    }

    private boolean checkLKImprovement(int[] genes, int t1Idx, float[] dist, int[][] neighborLists, int n) {
        int t1 = genes[t1Idx];
        int t2Idx = (t1Idx + 1) % n;
        int t2 = genes[t2Idx];
        float initialGain = dist[t1 * n + t2];

        for (int t3 : neighborLists[t2]) {
            float gain = initialGain - dist[t2 * n + t3];
            if (gain <= 0) continue;

            int t3Idx = findIndex(genes, t3);
            int t4Idx = (t3Idx + 1) % n;
            int t4 = genes[t4Idx];
            
            if (gain + dist[t3 * n + t4] - dist[t4 * n + t1] > 0.001f) return true;
        }
        return false;
    }

    private void applyLKMove(int[] genes, int t1Idx, float[] dist, int[][] neighborLists, int n) {
        int t1 = genes[t1Idx];
        int t2Idx = (t1Idx + 1) % n;
        int t2 = genes[t2Idx];
        
        float initialGain = dist[t1 * n + t2];
        
        for (int t3 : neighborLists[t2]) {
            float gain = initialGain - dist[t2 * n + t3];
            if (gain <= 0) continue;

            int t3Idx = findIndex(genes, t3);
            int t4Idx = (t3Idx + 1) % n;
            int t4 = genes[t4Idx];
            
            if (gain + dist[t3 * n + t4] - dist[t4 * n + t1] > 0.001f) {
                // Perform a 2-opt swap (basic LK move)
                if (t1Idx < t3Idx) {
                    reverse(genes, t1Idx + 1, t3Idx);
                } else {
                    reverse(genes, t3Idx + 1, t1Idx);
                }
                return;
            }
        }
    }

    private int findIndex(int[] genes, int value) {
        for (int i = 0; i < genes.length; i++) {
            if (genes[i] == value) return i;
        }
        return -1;
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.LinKernighan"; }
}