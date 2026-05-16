package hedos.ga;

import com.google.inject.Singleton;

import hedos.ga.cost.CostCalculator;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;

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
                if (subtask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                    failures.add(subtask.exception());
                }
                return true;
            }
            @Override
            public List<Throwable> result() { return failures; }
        }

        // Use Scoped Values to make params available to all virtual threads in this scope
        ScopedValue.where(GAParameters.CURRENT, params).run(() -> {
            try (var scope = StructuredTaskScope.open(new EvaluationJoiner(),
                    cfg -> cfg.withTimeout(Duration.ofMillis(params.getEvaluationTimeout())))) {
                for (int i = 0; i < population.length; i += BATCH_SIZE) {
                    final int start = i;
                    final int end = Math.min(i + BATCH_SIZE, population.length);
                    
                    scope.fork(() -> {
                        for (int j = start; j < end; j++) {
                            Chromosome chromosome = population[j];
                            if (!chromosome.isEvaluated()) {
                                int[] genes = chromosome.genes();
                                chromosome.setCost(calculator.calculateCost(genes));
                                chromosome.setTurnCost(calculator.calculateTurnCost(genes));
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
            System.err.println("Evaluation error: " + t.getMessage());
        }
        throw new RuntimeException("Multiple errors during population evaluation");
    }
}