/*
 * tekrei tarafindan Feb 10, 2006 tarihinde yaratilmistir.
 */
package net.tekrei.hedos.ga;

import net.tekrei.hedos.ga.crossover.Crossover;
import net.tekrei.hedos.ga.mutation.Mutation;
import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.GAParameters;
import net.tekrei.hedos.ga.utilities.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class GeneticAlgorithm {
    private static ArrayList<Point> TARGETS;
    private Chromosome[] population;
    private Chromosome best;

    public static float calculateCost(int[] genes) {
        float cost = 0.0f;

        for (int i = 1; i < genes.length; i++) {
            cost += TARGETS.get(genes[i]).distance(TARGETS.get(genes[i - 1]));
        }

        return cost;
    }

    public Chromosome run(ArrayList<Point> _targets) {
        TARGETS = _targets;
        best = null;
        initPopulation();

        int generation = 0;
        Crossover caprazlayici = GAParameters.getCrossover();
        Mutation mutator = GAParameters.getMutation();

        while (generation < GAParameters.getInstance().getNesilSayisi()) {
            //long bas = System.currentTimeMillis();
            generation++;
            if (caprazlayici != null) {
                population = caprazlayici.crossover(population);
            }
            if (mutator != null) {
                population = mutator.mutate(population);
            }
            Arrays.sort(population);
            elitism();
            //System.out.println(generation + "\t" + best.toString() + "\t" + (System.currentTimeMillis() - bas));
        }
        return best;
    }

    private int[] randomGenes() {
        int[] genes = new int[TARGETS.size()];

        for (int i = 0; i < genes.length; i++) {
            genes[i] = -1;
        }

        Random generator = new Random();

        for (int i = 0; i < genes.length; i++) {
            int uretilen = generator.nextInt(genes.length);

            while (contains(genes, uretilen)) {
                uretilen = generator.nextInt(genes.length);
            }

            genes[i] = uretilen;
        }
        return genes;
    }

    private boolean contains(int[] array, int gene) {
        return IntStream.of(array).anyMatch(x -> x == gene);
    }

    private void elitism() {
        if (best == null) {
            best = new Chromosome(population[0].getGenes(), population[0].getCost());
        } else if (GAParameters.getInstance().isElitism()) {
            population[population.length - 1].setGenes(best.getGenes(), best.getCost());
        }

        if (best.getCost() > population[0].getCost()) {
            best.setGenes(population[0].getGenes().clone(), population[0].getCost());
        }
    }

    private void initPopulation() {
        population = new Chromosome[GAParameters.getInstance().getToplumBuyuklugu()];

        for (int i = 0; i < population.length; i++) {
            int[] genes = randomGenes();
            population[i] = new Chromosome(genes, calculateCost(genes));
        }
    }
}
