package hedos.ga;

import hedos.ga.cost.CostCalculator;
import hedos.ga.cost.TSPCostCalculator;
import hedos.ga.crossover.Crossover;
import hedos.ga.crossover.CrossoverFactory;
import hedos.ga.data.Chromosome;
import hedos.ga.data.ChromosomeFactory;
import hedos.ga.data.GAParameters;
import hedos.ga.data.Point;
import hedos.ga.elitism.ElitismHandler;
import hedos.ga.elitism.ElitismHandlerFactory;
import hedos.ga.mutation.Mutation;
import hedos.ga.mutation.MutationFactory;
import hedos.ga.lso.LocalSearchFactory;
import hedos.ga.lso.LSORuntime;
import hedos.ga.lso.LocalSearchOptimizer;
import hedos.ga.selection.Selection;
import hedos.ga.selection.SelectionFactory;
import hedos.ga.stagnation.StagnationHandler;
import hedos.ga.stagnation.StagnationHandlerFactory;

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
    private final StagnationHandlerFactory stagnationFactory;
    private final ElitismHandlerFactory elitismFactory;

    public static final ScopedValue<ProgressListener> PROGRESS_LISTENER = ScopedValue.newInstance();

    private CostCalculator calculator;
    private MemorySegment populationSegment; // Off-heap storage for all genes
    private volatile boolean cancelled = false;

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
                            PopulationEvaluator evaluator,
                            StagnationHandlerFactory stagnationFactory,
                            ElitismHandlerFactory elitismFactory) {
        this.gaParameters = gaParameters;
        this.crossoverFactory = crossoverFactory;
        this.mutationFactory = mutationFactory;
        this.selectionFactory = selectionFactory;
        this.chromosomeFactory = chromosomeFactory;
        this.localSearchFactory = localSearchFactory;
        this.evaluator = evaluator;
        this.stagnationFactory = stagnationFactory;
        this.elitismFactory = elitismFactory;
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
        
        StagnationHandler stagnationHandler = stagnationFactory.get(gaParameters.getStagnationType());
        ElitismHandler elitismHandler = elitismFactory.get(gaParameters.getElitismType());
        stagnationHandler.reset();

        long startTime = System.currentTimeMillis();
        int generation = 0;
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
                final int currentGen = generation;
                final Chromosome[] currentPop = population;
                try {
                    population = ScopedValue.where(GAParameters.CURRENT, gaParameters).call(() -> {
                        // Use Config to name the scope for JFR / Debugging
                        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll(), 
                                cfg -> cfg.withName("GA-Selection-Gen-" + currentGen))) {
                            var tasks = new ArrayList<StructuredTaskScope.Subtask<Chromosome>>();
                            int tSize = gaParameters.getTournamentSize();
                            for (int i = 0; i < currentPop.length; i++) {
                                tasks.add(scope.fork(() -> selectionOperator.select(currentPop, tSize)));
                            }
                            scope.join();
                            Chromosome[] nextPop = new Chromosome[currentPop.length];
                            for (int i = 0; i < nextPop.length; i++) {
                                var t = tasks.get(i);
                                nextPop[i] = t.state() == StructuredTaskScope.Subtask.State.SUCCESS ? t.get() : currentPop[i];
                            }
                            return nextPop;
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

            boolean neighborhoodIncreased = stagnationHandler.checkStagnation(
                    population[0], best, calculator, targets.size());

            best = elitismHandler.applyElitism(population, best, gaParameters.isElitism());
            
            long genDuration = (System.nanoTime() - genStartTime) / 1_000_000;

            if (PROGRESS_LISTENER.isBound()) {
                PROGRESS_LISTENER.get().onProgress(generation, gaParameters.getGenerationCount(), best.cost(), genDuration, lsDuration, currentLsoKey, neighborhoodIncreased);
            }
        }
        logger.info("Genetic Algorithm finished {} generations in {} ms", generation, (System.currentTimeMillis() - startTime));
        return best;
    }

    private void initPopulation() {
        population = new Chromosome[gaParameters.getPopulationSize()];
        int n = targets.size();
        // Allocate off-heap segment for the entire population's genes
        this.populationSegment = Arena.ofAuto().allocate(
                (long) population.length * n * ValueLayout.JAVA_INT.byteSize(), 64);

        for (int i = 0; i < population.length; i++) {
            int[] genes = chromosomeFactory.createRandomGenes(n);
            // Copy to off-heap for future crossover/mutation operations to use directly
            MemorySegment.copy(MemorySegment.ofArray(genes), 0, populationSegment, (long) i * n * ValueLayout.JAVA_INT.byteSize(), (long) n * ValueLayout.JAVA_INT.byteSize());
            population[i] = new Chromosome(genes, calculator.calculateCost(genes), calculator.calculateTurnCost(genes));
        }
    }
}
