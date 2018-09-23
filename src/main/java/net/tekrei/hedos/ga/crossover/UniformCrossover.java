package net.tekrei.hedos.ga.crossover;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.utilities.Chromosome;

public class UniformCrossover extends Crossover {

    @Override
    void match(Chromosome p1, Chromosome p2) {
        int[] p1Genes = p1.getGenes();
        int[] p2Genes = p2.getGenes();

        int[] c1 = new int[p1Genes.length];
        int[] c2 = new int[p1Genes.length];

        for (int i = 0; i < p1Genes.length; i += 2) {
            c1[i] = p1Genes[i];
            c2[i] = p2Genes[i];
            c1[i + 1] = p2Genes[i + 1];
            c2[i + 1] = p1Genes[i + 1];
        }
        c1 = fixGenes(c1);
        c2 = fixGenes(c2);

        p1.setGenes(c1, GeneticAlgorithm.calculateCost(c1));
        p2.setGenes(c2, GeneticAlgorithm.calculateCost(c2));
    }
}
