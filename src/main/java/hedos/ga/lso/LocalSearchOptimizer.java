package hedos.ga.lso; // File should be moved to src/main/java/hedos/ga/lso/

/**
 * Base class for local search heuristics.
 */
public abstract class LocalSearchOptimizer {
    public abstract void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n);
    public abstract String getNameKey();

    protected void reverse(int[] genes, int i, int j) {
        while (i < j) {
            int temp = genes[i];
            genes[i] = genes[j];
            genes[j] = temp;
            i++;
            j--;
        }
    }
}