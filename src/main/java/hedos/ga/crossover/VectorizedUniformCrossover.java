package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import java.util.Random;

public class VectorizedUniformCrossover extends Crossover {
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
    private final Random random = new Random();

    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int[] g1 = p1.genes();
        int[] g2 = p2.genes();
        int n = g1.length;
        int[] off1 = new int[n];
        int[] off2 = new int[n];

        for (int i = 0; i < n; i += SPECIES.length()) {
            var mask = SPECIES.indexInRange(i, n);
            
            // Directly generate a random bit mask from a long to avoid array allocation
            var vMask = VectorMask.fromLong(SPECIES, random.nextLong()).and(mask);

            var v1 = IntVector.fromArray(SPECIES, g1, i, mask);
            var v2 = IntVector.fromArray(SPECIES, g2, i, mask);

            // Blend genes based on the random mask
            v1.blend(v2, vMask).intoArray(off1, i, mask);
            v2.blend(v1, vMask).intoArray(off2, i, mask);
        }

        return new Chromosome[]{
            new Chromosome(fixGenes(off1)),
            new Chromosome(fixGenes(off2))
        };
    }

    @Override
    public String getNameKey() { return "GA.Crossover.VectorizedUniform"; }
}