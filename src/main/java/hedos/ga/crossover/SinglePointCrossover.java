package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;

public class SinglePointCrossover extends Crossover {
    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int cutPoint = p1.genes().length / 2;

        int[] c1Genes = fixGenes(matchGenes(p1.genes(), p2.genes(), cutPoint));
        int[] c2Genes = fixGenes(matchGenes(p2.genes(), p1.genes(), cutPoint));

        return new Chromosome[] {
            new Chromosome(c1Genes),
            new Chromosome(c2Genes)
        };
    }

    @Override
    public String getNameKey() {
        return MessageKeys.CROSSOVER_SINGLE_POINT;
    }

    private int[] matchGenes(int[] firstParent, int[] secondParent, int cutPoint) {
        int[] offspringGenes = new int[firstParent.length];

        for (int i = 0; i < firstParent.length; i++) {
            if (i < cutPoint) {
                offspringGenes[i] = firstParent[i];
            } else {
                offspringGenes[i] = secondParent[i];
            }
        }

        return offspringGenes;
    }
}
