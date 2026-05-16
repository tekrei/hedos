package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;

public class TwoPointCrossover extends Crossover {
    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int cp1 = p1.genes().length / 3;
        int cp2 = 2 * cp1;

        int[] c1Genes = fixGenes(matchGenes(p1.genes(), p2.genes(), cp1, cp2));
        int[] c2Genes = fixGenes(matchGenes(p2.genes(), p1.genes(), cp1, cp2));

        return new Chromosome[] {
            new Chromosome(c1Genes),
            new Chromosome(c2Genes)
        };
    }

    @Override
    public String getNameKey() {
        return MessageKeys.CROSSOVER_TWO_POINT;
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
