package net.tekrei.hedos.ga.crossover;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.utilities.Chromosome;

public class TwoPointCrossover extends Crossover {
    @Override
    void match(Chromosome firstParent, Chromosome secondParent) {
        // 10 elemanli bir dizide 1. kesme 3, ikinci kesme 6 da olacak
        int cp1 = firstParent.getGenes().length / 3;
        int cp2 = 2 * cp1;

        // Kesme noktasina gore bu kromozomlari esleyelim
        int[] firstParentGenes = firstParent.getGenes();

        int[] secondParentGenes = secondParent.getGenes();

        int[] child = matchGenes(firstParentGenes, secondParentGenes, cp1, cp2);
        child = fixGenes(child);

        firstParent.setGenes(child, GeneticAlgorithm.calculateCost(child));

        child = matchGenes(secondParentGenes, firstParentGenes, cp1, cp2);
        child = fixGenes(child);

        secondParent.setGenes(child, GeneticAlgorithm.calculateCost(child));
    }

    private int[] matchGenes(int[] p1, int[] p2, int c1, int c2) {
        int size = p1.length;
        int[] match = new int[size];

        for (int i = 0; i < size; i++) {
            if (i < c1) {
                match[i] = p1[i];
            } else if (i < c2) {
                match[i] = p2[i];
            } else {
                match[i] = p1[i];
            }
        }
        return match;
    }
}
