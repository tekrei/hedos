package hedos.ga;

import com.google.inject.Singleton;
import hedos.ga.data.Chromosome;
import hedos.ga.data.CostCalculator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

@Singleton
public class PopulationEvaluator {

    private static class EvaluationScope extends StructuredTaskScope<Void> {
        private final List<Throwable> failures = new CopyOnWriteArrayList<>();

        @Override
        protected void handleComplete(Subtask<? extends Void> subtask) {
            if (subtask.state() == Subtask.State.FAILED) {
                failures.add(subtask.exception());
            }
        }
        public List<Throwable> getFailures() { return failures; }
    }

    public void evaluate(Chromosome[] population, CostCalculator calculator, long timeoutMs) {
        try (var scope = new EvaluationScope()) {
            for (Chromosome chromosome : population) {
                if (!chromosome.isEvaluated()) {
                    scope.fork(() -> {
                        int[] genes = chromosome.genes();
                        chromosome.setCost(calculator.calculateCost(genes));
                        chromosome.setTurnCost(calculator.calculateTurnCost(genes));
                        return null;
                    });
                }
            }
            
            scope.joinUntil(Instant.now().plus(Duration.ofMillis(timeoutMs)));
            
            if (!scope.getFailures().isEmpty()) {
                handleFailures(scope.getFailures());
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("Population evaluation timed out after " + timeoutMs + "ms", e);
        } catch (Exception e) {
            throw new RuntimeException("Population evaluation failed", e);
        }
    }

    private void handleFailures(List<Throwable> failures) {
        for (Throwable t : failures) {
            System.err.println("Evaluation error: " + t.getMessage());
        }
        throw new RuntimeException("Multiple errors during population evaluation");
    }
}