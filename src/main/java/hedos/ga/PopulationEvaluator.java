package hedos.ga;

import com.google.inject.Singleton;

import hedos.ga.cost.CostCalculator;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import hedos.ga.lso.LSORuntime;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;

@Singleton
public class PopulationEvaluator {

    private static final int BATCH_SIZE = 100;

    public void evaluate(Chromosome[] population, CostCalculator calculator, GAParameters params) {
        class EvaluationJoiner implements StructuredTaskScope.Joiner<Void, List<Throwable>> {
            private final List<Throwable> failures = new CopyOnWriteArrayList<>();
            @Override
            public boolean onComplete(StructuredTaskScope.Subtask<? extends Void> subtask) {
                // Ignore InterruptedException - these are expected if the scope times out
                if (subtask.state() == StructuredTaskScope.Subtask.State.FAILED && 
                    !(subtask.exception() instanceof InterruptedException)) {
                    failures.add(subtask.exception());
                }
                return true;
            }
            @Override
            public List<Throwable> result() { return failures; }
        }

        // Use Scoped Values to make params available to all virtual threads in this scope
        // Note: CALCULATOR is usually already bound by the GA or Service layer.
        ScopedValue.where(GAParameters.CURRENT, params).run(() -> {
            try (var scope = StructuredTaskScope.open(new EvaluationJoiner(),
                    cfg -> cfg.withName("Population-Evaluator")
                              .withTimeout(Duration.ofMillis(params.getEvaluationTimeout())))) {
                for (int i = 0; i < population.length; i += BATCH_SIZE) {
                    final int start = i;
                    final int end = Math.min(i + BATCH_SIZE, population.length);
                    
                    scope.fork(() -> {
                        // Retrieve from ScopedValue if bound (for LSO consistency), 
                        // otherwise fallback to the method parameter
                        CostCalculator localCalc = LSORuntime.CALCULATOR.isBound() ? 
                            LSORuntime.CALCULATOR.get() : calculator;
                        for (int j = start; j < end; j++) {
                            Chromosome chromosome = population[j];
                            if (!chromosome.isEvaluated()) {
                                int[] genes = chromosome.genes();
                                chromosome.setCost(localCalc.calculateCost(genes));
                                chromosome.setTurnCost(localCalc.calculateTurnCost(genes));
                            }
                        }
                        return null;
                    });
                }
                
                List<Throwable> failures = scope.join();
                if (!failures.isEmpty()) {
                    handleFailures(failures);
                }
            } catch (Exception e) {
                throw new RuntimeException("Population evaluation failed", e);
            }
        });
    }

    private void handleFailures(List<Throwable> failures) {
        for (Throwable t : failures) {
            t.printStackTrace();
        }
    }
}