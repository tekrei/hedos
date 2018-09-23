package net.tekrei.hedos.ga.mutation;

import net.tekrei.hedos.ga.data.Chromosome;
import net.tekrei.hedos.ga.data.GAParameters;

public class RandomMutation extends Mutation {
    @Override
    void mutate(Chromosome chromosome) {
        int geneCount = chromosome.getGenes().length;

        if (GAParameters.getInstance().nextFloat() < GAParameters
                .getInstance().getMutationProbability()) {
            int first = GAParameters.getInstance().nextInt(geneCount);
            int second = GAParameters.getInstance().nextInt(geneCount);

            chromosome.swap(first, second);
        }
    }
}
