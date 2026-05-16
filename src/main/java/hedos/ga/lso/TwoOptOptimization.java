package hedos.ga.lso; // File should be moved to src/main/java/hedos/ga/lso/

public class TwoOptOptimization extends LocalSearchOptimizer {
    @Override
    public void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n) {
        boolean improvement = true;
        boolean[] dlb = new boolean[n];
        while (improvement) {
            improvement = false;
            for (int i = 1; i < genes.length - 2; i++) {
                if (dlb[genes[i]]) continue;
                boolean cityImproved = false;
                for (int j = i + 1; j < genes.length - 1; j++) {
                    float currentDist = distanceMatrix[genes[i-1]*n + genes[i]] + distanceMatrix[genes[j]*n + genes[j+1]];
                    float newDist = distanceMatrix[genes[i-1]*n + genes[j]] + distanceMatrix[genes[i]*n + genes[j+1]];
                    if (newDist < currentDist) {
                        reverse(genes, i, j);
                        improvement = true;
                        cityImproved = true;
                        dlb[genes[i-1]] = dlb[genes[i]] = dlb[genes[j]] = dlb[genes[j+1]] = false;
                    }
                }
                if (!cityImproved) dlb[genes[i]] = true;
            }
        }
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.2Opt"; }
}