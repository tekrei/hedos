package hedos.ga.lso;

import com.google.inject.Singleton;
import hedos.ga.data.GAParameters;
import java.util.stream.IntStream;

/**
 * Simplified Lin-Kernighan heuristic implementation.
 * Uses a variable-depth search based on the gain criterion to find tour improvements.
 */
@Singleton
public class LinKernighanOptimization extends LocalSearchOptimizer {
    private int[] pos;

    @Override
    public void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n) {
        pos = new int[n];
        for (int i = 0; i < n; i++) pos[genes[i]] = i;

        boolean improvement = true;
        while (improvement) {
            final boolean[] found = {false};
            // Parallelize starting points
            IntStream.range(0, n).parallel().forEach(i -> {
                if (found[0]) return;
                if (checkLKImprovement(genes, i, distanceMatrix, neighborLists, n)) {
                    synchronized (genes) {
                        if (!found[0]) {
                            applyLKMove(genes, i, distanceMatrix, neighborLists, n, pos);
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

            int t3Idx = pos[t3];
            int t4Idx = (t3Idx + 1) % n;
            int t4 = genes[t4Idx];
            
            if (gain + dist[t3 * n + t4] - dist[t4 * n + t1] > 0.001f) return true;
        }
        return false;
    }

    private void applyLKMove(int[] genes, int t1Idx, float[] dist, int[][] neighborLists, int n, int[] posMap) {
        int t1 = genes[t1Idx];
        int t2Idx = (t1Idx + 1) % n;
        int t2 = genes[t2Idx];
        
        float initialGain = dist[t1 * n + t2];
        
        for (int t3 : neighborLists[t2]) {
            float gain = initialGain - dist[t2 * n + t3];
            if (gain <= 0) continue;

            int t3Idx = posMap[t3];
            int t4Idx = (t3Idx + 1) % n;
            int t4 = genes[t4Idx];
            
            if (gain + dist[t3 * n + t4] - dist[t4 * n + t1] > 0.001f) {
                // Perform a 2-opt swap (basic LK move)
                if (t1Idx < t3Idx) {
                    reverseWithMap(genes, t1Idx + 1, t3Idx, posMap);
                } else {
                    reverseWithMap(genes, t3Idx + 1, t1Idx, posMap);
                }
                return;
            }
        }
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