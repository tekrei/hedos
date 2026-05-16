package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.CostCalculator;
import hedos.ga.data.GAParameters;
import com.google.inject.Inject;
import hedos.utility.MessageKeys;

public class OnlyImprovingSystematicMutation extends Mutation {
    private final CostCalculator calculator;

    @Inject
    public OnlyImprovingSystematicMutation(CostCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    Chromosome mutate(Chromosome chromosome, GAParameters params) {
        int[] tempGenes = chromosome.genes().clone();
        int size = tempGenes.length;
        float originalCost = chromosome.cost();

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                // Swap
                int geneA = tempGenes[i];
                tempGenes[i] = tempGenes[j];
                tempGenes[j] = geneA;

                float newCost = calculator.calculateCost(tempGenes);
                if (newCost < originalCost) {
                    return new Chromosome(tempGenes, newCost);
                }
                
                // Revert swap efficiently
                tempGenes[j] = tempGenes[i];
                tempGenes[i] = geneA;
            }
        }
        return chromosome;
    }

    @Override
    public String getNameKey() {
        return MessageKeys.MUTATION_ONLY_IMPROVING_SYSTEMATIC;
    }
}
