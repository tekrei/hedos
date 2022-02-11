package hedos.ga.mutation;

import hedos.ga.GeneticAlgorithm;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;

public class OnlyImprovingSystematicMutation extends Mutation {

    @Override
    void mutate(Chromosome chromosome) {
        int[] tempDeger = chromosome.getGenes().clone();

        if (GAParameters.getInstance().nextFloat() < GAParameters
                .getInstance().getMutationProbability()) {
            boolean devam = true;

            for (int i = 0; (i < tempDeger.length) && devam; i++) {
                for (int j = i + 1; (j < tempDeger.length) && devam; j++) {
                    int temp = tempDeger[i];
                    tempDeger[i] = tempDeger[j];
                    tempDeger[i] = temp;

                    if (GeneticAlgorithm.calculateCost(tempDeger) < chromosome.getCost()) {
                        chromosome.setGenes(tempDeger, GeneticAlgorithm.calculateCost(tempDeger));
                        devam = false;
                    }
                }
            }
        }
    }
}
