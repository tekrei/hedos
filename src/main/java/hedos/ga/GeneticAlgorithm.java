package hedos.ga;

import hedos.ga.crossover.Crossover;
import hedos.ga.crossover.CrossoverFactory;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import hedos.ga.data.CostCalculator;
import hedos.ga.data.Point;
import hedos.ga.mutation.Mutation;
import hedos.ga.mutation.MutationFactory;
import hedos.ga.selection.Selection;
import hedos.ga.selection.SelectionFactory;

import java.util.*;
import java.util.stream.IntStream;
import com.google.inject.Inject;

public class GeneticAlgorithm {
    private List<Point> targets;
    private Chromosome[] population;
    private Chromosome best;
    private final GAParameters gaParameters;
    private final CrossoverFactory crossoverFactory;
    private final MutationFactory mutationFactory;
    private final SelectionFactory selectionFactory;
    private CostCalculator calculator;
    private ProgressListener progressListener;
    private volatile boolean cancelled = false;

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int current, int total, float bestCost);
    }

    @Inject
    public GeneticAlgorithm(GAParameters gaParameters, 
                            CrossoverFactory crossoverFactory,
                            MutationFactory mutationFactory,
                            SelectionFactory selectionFactory) {
        this.gaParameters = gaParameters;
        this.crossoverFactory = crossoverFactory;
        this.mutationFactory = mutationFactory;
        this.selectionFactory = selectionFactory;
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

        int generation = 0;
        Crossover crossoverOperator = crossoverFactory.get(gaParameters.getCrossoverType());
        Mutation mutator = mutationFactory.get(gaParameters.getMutationType());
        Selection selectionOperator = selectionFactory.get(gaParameters.getSelectionType());

        while (generation < gaParameters.getGenerationCount()) {
            if (cancelled || Thread.currentThread().isInterrupted()) {
                break;
            }
            generation++;

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

            // Refresh cost and sharp turns for the new generation
            Arrays.stream(population).parallel().forEach(chromosome -> {
                if (!chromosome.isEvaluated()) {
                    int[] genes = chromosome.genes();
                    chromosome.setCost(calculator.calculateCost(genes));
                    chromosome.setTurnCost(calculator.calculateTurnCost(genes));
                }
            });

            Arrays.sort(population);
            elitism();
            if (progressListener != null) {
                progressListener.onProgress(generation, gaParameters.getGenerationCount(), best.cost());
            }
        }
        return best;
    }

    private int[] randomGenes() {
        List<Integer> indices = new ArrayList<>(
                IntStream.range(0, targets.size()).boxed().toList()
        );
        Collections.shuffle(indices);
        return indices.stream().mapToInt(i -> i).toArray();
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
            int[] genes = randomGenes();
            population[i] = new Chromosome(genes, calculator.calculateCost(genes), calculator.calculateTurnCost(genes));
        }
    }

    public float calculateCost(int[] genes) {
        return calculator.calculateCost(genes);
    }

    public float calculateTurnCost(int[] genes) {
        return calculator.calculateTurnCost(genes);
    }
}
