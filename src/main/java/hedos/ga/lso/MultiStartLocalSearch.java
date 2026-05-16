package hedos.ga.lso;

import hedos.ga.data.ChromosomeFactory;
import hedos.ga.lso.LocalSearchOptimizer;

import java.util.concurrent.StructuredTaskScope;
import com.google.inject.Inject;

/**
 * Executes multiple local search instances in parallel from random starting points.
 */
public class MultiStartLocalSearch extends LocalSearchOptimizer {
    private final LocalSearchOptimizer delegate;
    private final ChromosomeFactory factory;
    private static final int STARTS = 4; // Number of parallel starts

    @Inject
    public MultiStartLocalSearch(LinKernighanOptimization delegate, ChromosomeFactory factory) {
        this.delegate = delegate;
        this.factory = factory;
    }

    @Override
    public void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var tasks = new java.util.ArrayList<java.util.concurrent.StructuredTaskScope.Subtask<int[]>>();
            
            for (int i = 0; i < STARTS; i++) {
                tasks.add(scope.fork(() -> {
                    int[] localGenes = (tasks.size() == 0) ? genes.clone() : factory.createRandomGenes(n);
                    delegate.optimize(localGenes, distanceMatrix, neighborLists, n);
                    return localGenes;
                }));
            }
            scope.join().throwIfFailed();

            // Pick best result
            float bestCost = Float.MAX_VALUE;
            for (var task : tasks) {
                int[] result = task.get();
                float cost = calculatePathCost(result, distanceMatrix, n);
                if (cost < bestCost) {
                    bestCost = cost;
                    System.arraycopy(result, 0, genes, 0, n);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private float calculatePathCost(int[] genes, float[] dist, int n) {
        float cost = 0;
        for (int i = 0; i < n - 1; i++) cost += dist[genes[i] * n + genes[i+1]];
        return cost;
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.MultiStartLK"; }
}