/*
 * tekrei tarafindan Feb 10, 2006 tarihinde yaratilmistir.
 */
package net.tekrei.hedos.ga;

import net.tekrei.hedos.ga.crossover.Crossover;
import net.tekrei.hedos.ga.mutation.Mutation;
import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.GAParameters;
import net.tekrei.hedos.ga.utilities.QubbleSortAlgorithm;

public class GeneticAlgorithm {
    private Chromosome[] population;

    private Chromosome elite;

    public Chromosome isle() {
        initPopulation();

        int generation = 0;
        Crossover caprazlayici = GAParameters.getCrossover();
        Mutation mutator = GAParameters.getMutation();

        while (generation < GAParameters.getInstance().getNesilSayisi()) {
            long bas = System.currentTimeMillis();
            generation++;

            sort();

            if (GAParameters.getInstance().isElitism()) {
                elitism();
            }

            if (caprazlayici != null) {
                population = caprazlayici.crossover(population);
            }

            if (mutator != null) {
                population = mutator.mutate(population);
            }

            System.out.println(generation + "\t" + enIyi().toString() + "\t"
                    + (System.currentTimeMillis() - bas));
        }

        return enIyi();
    }

    private Chromosome enIyi() {
        int[] bestGenes = bestChromosome(population).getGenes();
        if (elite != null && elite.getCost() < Chromosome.calculateCost(bestGenes)) {
            bestGenes = elite.getGenes();
        }
        Chromosome k = new Chromosome();
        k.setGenes(bestGenes);
        return k;
    }

    private void elitism() {
        if (elite != null) {
            population[population.length - 1].setGenes(elite.getGenes());
        } else {
            elite = new Chromosome();
            elite.setGenes(population[0].getGenes());
        }

        sort();

        if (elite.getCost() > population[0].getCost()) {
            elite.setGenes(population[0].getGenes());
        }
    }

    private void sort() {
        population = QubbleSortAlgorithm.getInstance().sort(population);
    }

    private void initPopulation() {
        population = new Chromosome[GAParameters.getInstance().getToplumBuyuklugu()];

        for (int i = 0; i < population.length; i++) {
            population[i] = new Chromosome();
        }
    }

    private Chromosome bestChromosome(Chromosome[] toplum) {
        int bestIndex = 0;

        for (int i = 0; i < toplum.length; i++) {
            if (toplum[i].getCost() < toplum[bestIndex].getCost()) {
                bestIndex = i;
            }
        }

        return toplum[bestIndex];
    }
}
