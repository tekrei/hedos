package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;

public class UniformCrossover extends Crossover {

    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int[] p1Genes = p1.genes();
        int[] p2Genes = p2.genes();

        int[] c1 = new int[p1Genes.length];
        int[] c2 = new int[p1Genes.length];

        for (int i = 0; i < p1Genes.length; i += 2) {
            if (i + 1 < p1Genes.length) { // Process pairs
                c1[i] = p1Genes[i];
                c2[i] = p2Genes[i];
                c1[i + 1] = p2Genes[i + 1];
                c2[i + 1] = p1Genes[i + 1];
            } else { // Handle the last element if length is odd
                c1[i] = p1Genes[i];
                c2[i] = p2Genes[i];
            }
        }
        
        c1 = fixGenes(c1);
        c2 = fixGenes(c2);

        return new Chromosome[] {
            new Chromosome(c1),
            new Chromosome(c2)
        };
    }

    @Override
    public String getNameKey() {
        return MessageKeys.CROSSOVER_UNIFORM;
    }
}
