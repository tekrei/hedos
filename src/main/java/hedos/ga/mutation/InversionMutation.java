package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import java.util.Random;

public class InversionMutation extends Mutation {
    private final Random random = new Random();

    @Override
    Chromosome mutate(Chromosome chromosome, GAParameters params) {
        int[] genes = chromosome.genes();
        int size = genes.length;
        int i = random.nextInt(size);
        int j = random.nextInt(size);
        
        int start = Math.min(i, j);
        int end = Math.max(i, j);

        while (start < end) {
            int temp = genes[start];
            genes[start] = genes[end];
            genes[end] = temp;
            start++; end--;
        }
        chromosome.setEvaluated(false);
        return chromosome;
    }

    @Override
    public String getNameKey() { return "GA.Mutation.Inversion"; }
}