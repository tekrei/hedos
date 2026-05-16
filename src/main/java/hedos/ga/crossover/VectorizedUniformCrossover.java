package hedos.ga.crossover;

import com.google.inject.Inject;
import hedos.ga.ChromosomeFactory;
import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;

import java.util.random.RandomGenerator;

public class VectorizedUniformCrossover extends Crossover {
    private final ChromosomeFactory factory;
    private final RandomGenerator random;

    @Inject
    public VectorizedUniformCrossover(ChromosomeFactory factory) {
        this.factory = factory;
        this.random = factory.getRandomGenerator();
    }

    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int n = p1.genes().length;
        boolean[] mask = new boolean[n];
        for (int i = 0; i < n; i++) {
            mask[i] = random.nextBoolean();
        }

        // Offspring 1
        int[] child1Genes = factory.vectorizedBlend(p1.genes(), p2.genes(), mask);
        // Offspring 2 (inverse mask)
        for (int i = 0; i < n; i++) mask[i] = !mask[i];
        int[] child2Genes = factory.vectorizedBlend(p1.genes(), p2.genes(), mask);

        return new Chromosome[]{new Chromosome(child1Genes), new Chromosome(child2Genes)};
    }

    @Override
    public String getNameKey() {
        return MessageKeys.CROSSOVER_VECTORIZED_UNIFORM;
    }
}