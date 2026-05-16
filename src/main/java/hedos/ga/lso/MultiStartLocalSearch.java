package hedos.ga.lso;

import hedos.ga.data.ChromosomeFactory;
import hedos.ga.lso.LocalSearchOptimizer;

import java.util.concurrent.StructuredTaskScope;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import com.google.inject.Inject;

/**
 * Executes multiple local search instances in parallel from random starting points.
 */
public class MultiStartLocalSearch extends LocalSearchOptimizer {
    private final LocalSearchOptimizer delegate;
    private final ChromosomeFactory factory;
    private final LSORuntime runtime;
    private static final int STARTS = 4; // Number of parallel starts

    @Inject
    public MultiStartLocalSearch(LinKernighanOptimization delegate, ChromosomeFactory factory, LSORuntime runtime) {
        this.delegate = delegate;
        this.factory = factory;
        this.runtime = runtime;
    }

    @Override
    public void optimize(int[] genes, int[][] neighborLists, int n) {
        MemorySegment dist = runtime.getDistanceMatrix();
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
            var tasks = new java.util.ArrayList<StructuredTaskScope.Subtask<int[]>>();
            
            for (int i = 0; i < STARTS; i++) {
                final int index = i;
                tasks.add(scope.fork(() -> {
                    // Deep clone to ensure total thread isolation
                    int[] candidate = (index == 0) ? genes.clone() : factory.createRandomGenes(n);
                    delegate.optimize(candidate, neighborLists, n);
                    return candidate;
                }));
            }
            scope.join();

            // Pick best result
            float bestCost = Float.MAX_VALUE;
            for (var task : tasks) {
                if (task.state() != StructuredTaskScope.Subtask.State.SUCCESS) {
                    continue;
                }
                int[] result = task.get();
                float cost = calculatePathCost(result, dist, n);
                if (cost < bestCost) {
                    bestCost = cost;
                    System.arraycopy(result, 0, genes, 0, n);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private float calculatePathCost(int[] genes, MemorySegment dist, int n) {
        float cost = 0;
        for (int i = 0; i < n - 1; i++) {
            cost += dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) genes[i] * n + genes[i + 1]);
        }
        return cost;
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.MultiStartLK"; }
}