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
import hedos.ga.lso.LocalSearchOptimizer;
import hedos.ga.selection.Selection;
import hedos.ga.selection.SelectionFactory;

import java.util.*;
import java.util.stream.IntStream;
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

    private CostCalculator calculator;
    private ProgressListener progressListener;
    private volatile boolean cancelled = false;

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int current, int total, float bestCost, long durationMs, long lsDurationMs);
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

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
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
                // Parallelizing independent selection operations
                population = IntStream.range(0, population.length)
                        .parallel()
                        .mapToObj(i -> selectionOperator.select(population, gaParameters.getTournamentSize()))
                        .toArray(Chromosome[]::new);
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
            if (generation % 5 == 0 && calculator instanceof TSPCostCalculator tsp) {
                long lsStart = System.nanoTime();
                int[] genes = population[0].genes();
                float[] dist = tsp.getDistanceMatrix();
                int n = targets.size();

                LocalSearchOptimizer lso = localSearchFactory.get(gaParameters.getLocalOptimizationType());
                if (lso != null) {
                    int[] optimizedGenes = genes.clone();
                    lso.optimize(optimizedGenes, dist, tsp.getNeighborLists(), n);
                    population[0] = new Chromosome(optimizedGenes);
                }
                
                population[0].setEvaluated(false);
                evaluator.evaluate(new Chromosome[]{population[0]}, calculator, gaParameters);
                lsDuration = (System.nanoTime() - lsStart) / 1_000_000;
            }

            Arrays.sort(population);
            elitism();
            long genDuration = (System.nanoTime() - genStartTime) / 1_000_000;

            if (progressListener != null) {
                progressListener.onProgress(generation, gaParameters.getGenerationCount(), best.cost(), genDuration, lsDuration);
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

        for (int i = 0; i < population.length; i++) {
            int[] genes = chromosomeFactory.createRandomGenes(targets.size());
            population[i] = new Chromosome(genes, calculator.calculateCost(genes), calculator.calculateTurnCost(genes));
        }
    }
}
