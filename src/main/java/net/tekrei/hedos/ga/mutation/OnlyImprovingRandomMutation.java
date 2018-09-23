package net.tekrei.hedos.ga.mutation;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.data.Chromosome;
import net.tekrei.hedos.ga.data.GAParameters;

public class OnlyImprovingRandomMutation extends Mutation {

    @Override
    void mutate(Chromosome degisecek) {
        int[] tempDeger = degisecek.getGenes().clone();

        if (GAParameters.getInstance().nextFloat() < GAParameters
                .getInstance().getMutationProbability()) {
            int first = GAParameters.getInstance().nextInt(tempDeger.length);
            int second = GAParameters.getInstance().nextInt(tempDeger.length);

            int temp = tempDeger[first];
            tempDeger[first] = tempDeger[second];
            tempDeger[second] = temp;
        }
        if (GeneticAlgorithm.calculateCost(tempDeger) < degisecek.getCost()) {
            degisecek.setGenes(tempDeger, GeneticAlgorithm.calculateCost(tempDeger));
        }
    }
}
