package net.tekrei.hedos.ga.crossover;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.utilities.Chromosome;

public class SinglePointCrossover extends Crossover {
    @Override
    void match(Chromosome firstParent, Chromosome secondParent) {
        int cutPoint = firstParent.getGenes().length / 2;

        int[] firstParentGenes = firstParent.getGenes();
        int[] secondParentGenes = secondParent.getGenes();

        int[] child = matchGenes(firstParentGenes, secondParentGenes, cutPoint);
        child = fixGenes(child);
        firstParent.setGenes(child, GeneticAlgorithm.calculateCost(child));
        child = matchGenes(secondParentGenes, firstParentGenes, cutPoint);
        child = fixGenes(child);
        secondParent.setGenes(child, GeneticAlgorithm.calculateCost(child));
    }

    private int[] matchGenes(int[] firstParent, int[] secondParent, int cutPoint) {
        int[] esleme = new int[firstParent.length];

        for (int i = 0; i < firstParent.length; i++) {
            if (i < cutPoint) {
                esleme[i] = firstParent[i];
            } else {
                esleme[i] = secondParent[i];
            }
        }

        return esleme;
    }
}
