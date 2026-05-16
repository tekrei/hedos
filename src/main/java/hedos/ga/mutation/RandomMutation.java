package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import hedos.utility.MessageKeys;

import java.util.Random;

public class RandomMutation extends Mutation {
    private final Random random = new Random();

    @Override
    Chromosome mutate(Chromosome original, GAParameters params) {
        int[] newGenes = original.genes().clone();
        if (newGenes.length < 2) {
            return original;
        }

        int index1 = random.nextInt(newGenes.length);
        int index2 = random.nextInt(newGenes.length);

        int temp = newGenes[index1];
        newGenes[index1] = newGenes[index2];
        newGenes[index2] = temp;

        return new Chromosome(newGenes);
    }

    @Override
    public String getNameKey() {
        return MessageKeys.MUTATION_RANDOM;
    }
}