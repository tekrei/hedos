package hedos.ga;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import hedos.ga.cost.CostCalculator;
import hedos.ga.cost.TSPCostCalculator;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import hedos.ga.data.Point;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Consumer;

/**
 * Service for orchestrating Genetic Algorithm execution.
 * Decouples SwingWorker management and background task execution from the UI.
 */
@Singleton
public class GeneticAlgorithmService {
    private final Provider<GeneticAlgorithm> gaProvider;
    private final Provider<CostCalculator> calculatorProvider;
    private final GAParameters gaParameters;

    public record ProgressUpdate(int current, int total, float bestCost, long duration, long lsDuration, String lsoKey, boolean neighborhoodIncreased) {}
    public record TestUpdate(int trial, int totalTrials, float cost, boolean finished) {}

    @Inject
    public GeneticAlgorithmService(Provider<GeneticAlgorithm> gaProvider, 
                                   Provider<CostCalculator> calculatorProvider, 
                                   GAParameters gaParameters) {
        this.gaProvider = gaProvider;
        this.calculatorProvider = calculatorProvider;
        this.gaParameters = gaParameters;
    }

    public void calculate(List<Point> targets, Consumer<ProgressUpdate> progressConsumer, Consumer<Chromosome> onDone) {
        CostCalculator calculator = calculatorProvider.get();
        if (calculator instanceof TSPCostCalculator tspCalc) {
            tspCalc.init(targets);
        }
        
        GeneticAlgorithm ga = gaProvider.get();
        ga.setCalculator(calculator);

        new SwingWorker<Chromosome, ProgressUpdate>() {
            @Override
            protected Chromosome doInBackground() {
                ga.setProgressListener((current, total, bestCost, duration, lsDuration, lsoKey, neighborhoodIncreased) -> 
                    publish(new ProgressUpdate(current, total, bestCost, duration, lsDuration, lsoKey, neighborhoodIncreased)));
                return ga.run(targets);
            }

            @Override
            protected void process(List<ProgressUpdate> chunks) {
                chunks.forEach(progressConsumer);
            }

            @Override
            protected void done() {
                try {
                    onDone.accept(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void runMultipleTests(List<Point> targets, int trialCount, Consumer<TestUpdate> eventConsumer, Runnable onComplete) {
        new SwingWorker<Void, TestUpdate>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
                    for (int i = 0; i < trialCount; i++) {
                        final int trialIndex = i + 1;
                        publish(new TestUpdate(trialIndex, trialCount, 0, false));
                        scope.fork(() -> {
                            CostCalculator localCalc = calculatorProvider.get();
                            if (localCalc instanceof TSPCostCalculator tspCalc) {
                                tspCalc.init(targets);
                            }
                            GeneticAlgorithm ga = gaProvider.get();
                            ga.setCalculator(localCalc);
                            Chromosome best = ga.run(targets);
                            publish(new TestUpdate(trialIndex, trialCount, best.cost(), true));
                            return best;
                        });
                    }
                    scope.join();
                }
                return null;
            }

            @Override
            protected void process(List<TestUpdate> chunks) {
                chunks.forEach(eventConsumer);
            }

            @Override
            protected void done() { onComplete.run(); }
        }.execute();
    }
}