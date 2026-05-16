package hedos.ga;

import hedos.ga.cost.CostCalculator;
import hedos.ga.cost.TSPCostCalculator;
import hedos.ga.crossover.Crossover;
import hedos.ga.crossover.CrossoverFactory;
import hedos.ga.data.Chromosome;
import hedos.ga.data.ChromosomeFactory;
import hedos.ga.data.GAParameters;
import hedos.ga.data.Point;
import hedos.ga.mutation.Mutation;
import hedos.ga.mutation.MutationFactory;
import hedos.ga.lso.LocalSearchFactory;
import hedos.ga.lso.LSORuntime;
import hedos.ga.lso.LocalSearchOptimizer;
import hedos.ga.selection.Selection;
import hedos.ga.selection.SelectionFactory;

import java.util.*;
import java.time.Duration;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.StructuredTaskScope;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneticAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);
    private List<Point> targets;
    private Chromosome[] population;
    private Chromosome best;
    private final GAParameters gaParameters;
    private final CrossoverFactory crossoverFactory;
    private final MutationFactory mutationFactory;
    private final SelectionFactory selectionFactory;
    private final ChromosomeFactory chromosomeFactory;
    private final LocalSearchFactory localSearchFactory;
    private final PopulationEvaluator evaluator;

    public static final ScopedValue<ProgressListener> PROGRESS_LISTENER = ScopedValue.newInstance();

    private CostCalculator calculator;
    private MemorySegment populationSegment; // Off-heap storage for all genes
    private volatile boolean cancelled = false;
    private int stagnationCount = 0;

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int current, int total, float bestCost, long durationMs, long lsDurationMs, String lsoKey, boolean neighborhoodIncreased);
    }

    @Inject
    public GeneticAlgorithm(GAParameters gaParameters, 
                            CrossoverFactory crossoverFactory,
                            MutationFactory mutationFactory,
                            SelectionFactory selectionFactory,
                            ChromosomeFactory chromosomeFactory,
                            LocalSearchFactory localSearchFactory,
                            PopulationEvaluator evaluator) {
        this.gaParameters = gaParameters;
        this.crossoverFactory = crossoverFactory;
        this.mutationFactory = mutationFactory;
        this.selectionFactory = selectionFactory;
        this.chromosomeFactory = chromosomeFactory;
        this.localSearchFactory = localSearchFactory;
        this.evaluator = evaluator;
    }

    public void setCalculator(CostCalculator calculator) {
        this.calculator = calculator;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public Chromosome run(List<Point> targets) {
        this.targets = targets;
        best = null;
        initPopulation();
        cancelled = false;

        long startTime = System.currentTimeMillis();
        int generation = 0;
        stagnationCount = 0;
        Crossover crossoverOperator = crossoverFactory.get(gaParameters.getCrossoverType());
        Mutation mutator = mutationFactory.get(gaParameters.getMutationType());
        Selection selectionOperator = selectionFactory.get(gaParameters.getSelectionType());

        while (generation < gaParameters.getGenerationCount()) {
            if (cancelled || Thread.currentThread().isInterrupted()) {
                break;
            }
            generation++;

            long genStartTime = System.nanoTime();
            // Step 1: Selection (Parent Selection / Mating Pool)
            if (selectionOperator != null) {
                try {
                    population = ScopedValue.where(GAParameters.CURRENT, gaParameters).call(() -> {
                        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
                            var tasks = new ArrayList<StructuredTaskScope.Subtask<Chromosome>>();
                            for (int i = 0; i < population.length; i++) {
                                tasks.add(scope.fork(() -> selectionOperator.select(population, gaParameters.getTournamentSize())));
                            }
                            scope.join();
                            return tasks.stream()
                                .map(t -> t.state() == StructuredTaskScope.Subtask.State.SUCCESS ? t.get() : population[tasks.indexOf(t)])
                                .toArray(Chromosome[]::new);
                        }
                    });
                } catch (Exception e) { throw new RuntimeException(e); }
            }

            // Step 2: Variation (Crossover and Mutation)
            if (crossoverOperator != null) {
                population = crossoverOperator.crossover(population, gaParameters);
            }
            if (mutator != null) {
                population = mutator.mutate(population, gaParameters);
            }

            evaluator.evaluate(population, calculator, gaParameters);

            // Hybrid GA: Apply Selected Local Search to the best individual periodically
            long lsDuration = 0;
            String currentLsoKey = "GA.LocalOpt.None";

            if (generation % 5 == 0 && calculator instanceof TSPCostCalculator tsp) {
                long lsStart = System.nanoTime();
                int[] genes = population[0].genes();
                MemorySegment dist = tsp.getDistanceMatrix();
                int n = targets.size();

                LocalSearchOptimizer lso = localSearchFactory.get(gaParameters.getLocalOptimizationType());
                if (lso != null) {
                    currentLsoKey = lso.getNameKey();
                    int[] optimizedGenes = genes.clone();
                    // Use ScopedValue to pass the distance matrix off-heap segment
                    ScopedValue.where(LSORuntime.DISTANCE_MATRIX, dist)
                               .where(LSORuntime.CALCULATOR, calculator)
                               .run(() -> lso.optimize(optimizedGenes, tsp.getNeighborLists(), n));
                    population[0] = new Chromosome(optimizedGenes);
                }
                
                population[0].setEvaluated(false);
                ScopedValue.where(GAParameters.CURRENT, gaParameters)
                           .run(() -> evaluator.evaluate(new Chromosome[]{population[0]}, calculator, gaParameters));
                
                lsDuration = (System.nanoTime() - lsStart) / 1_000_000;
            }

            Arrays.sort(population);

            // Stagnation logic: increase neighborhood size if fitness plateaus
            if (best != null && population[0].cost() < best.cost()) {
                stagnationCount = 0;
            } else {
                stagnationCount++;
            }

            boolean neighborhoodIncreased = false;
            if (stagnationCount >= 50) {
                int newSize = gaParameters.getNeighborhoodSize() + 5;
                if (newSize < targets.size()) {
                    gaParameters.setNeighborhoodSize(newSize);
                    if (calculator instanceof TSPCostCalculator tsp) tsp.initNeighbors();
                    stagnationCount = 0;
                    neighborhoodIncreased = true;
                }
            }

            elitism();
            long genDuration = (System.nanoTime() - genStartTime) / 1_000_000;

            if (PROGRESS_LISTENER.isBound()) {
                PROGRESS_LISTENER.get().onProgress(generation, gaParameters.getGenerationCount(), best.cost(), genDuration, lsDuration, currentLsoKey, neighborhoodIncreased);
            }
        }
        logger.info("Genetic Algorithm finished {} generations in {} ms", generation, (System.currentTimeMillis() - startTime));
        return best;
    }

    private void elitism() {
        if (best == null || population[0].cost() < best.cost()) {
            // Reuse the existing Chromosome object reference
            best = population[0];
        } else if (gaParameters.isElitism()) {
            // Replace the worst individual (last in sorted array) with the current best
            population[population.length - 1] = best;
        }
    }

    private void initPopulation() {
        population = new Chromosome[gaParameters.getPopulationSize()];
        int n = targets.size();
        // Allocate off-heap segment for the entire population's genes
        this.populationSegment = Arena.ofAuto().allocate((long) population.length * n * ValueLayout.JAVA_INT.byteSize());

        for (int i = 0; i < population.length; i++) {
            int[] genes = chromosomeFactory.createRandomGenes(n);
            // Copy to off-heap for future crossover/mutation operations to use directly
            MemorySegment.copy(MemorySegment.ofArray(genes), 0, populationSegment, (long) i * n * ValueLayout.JAVA_INT.byteSize(), (long) n * ValueLayout.JAVA_INT.byteSize());
            population[i] = new Chromosome(genes, calculator.calculateCost(genes), calculator.calculateTurnCost(genes));
        }
    }
}
