package hedos.ga.lso;

import com.google.inject.Singleton;

/**
 * Best Improvement 2-opt optimization.
 * Scans all possible swaps and selects the one that provides the maximum gain.
 */
@Singleton
public class BestTwoOptOptimization extends LocalSearchOptimizer {

    @Override
    public void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n) {
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            float bestGain = 0;
            int bestI = -1;
            int bestJ = -1;

            for (int i = 1; i < genes.length - 2; i++) {
                for (int j = i + 1; j < genes.length - 1; j++) {
                    float currentDist = distanceMatrix[genes[i - 1] * n + genes[i]] + 
                                      distanceMatrix[genes[j] * n + genes[j + 1]];
                    float newDist = distanceMatrix[genes[i - 1] * n + genes[j]] + 
                                   distanceMatrix[genes[i] * n + genes[j + 1]];
                    
                    float gain = currentDist - newDist;
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestI = i;
                        bestJ = j;
                        improvement = true;
                    }
                }
            }

            if (improvement) {
                reverse(genes, bestI, bestJ);
            }
        }
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.Best2Opt"; }
}