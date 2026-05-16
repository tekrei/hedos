package hedos.ga.mutation;

import hedos.ga.cost.CostCalculator;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import com.google.inject.Inject;
import hedos.utility.MessageKeys;

public class OnlyImprovingRandomMutation extends Mutation {
    private final CostCalculator calculator;

    @Inject
    public OnlyImprovingRandomMutation(CostCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    Chromosome mutate(Chromosome toMutate, GAParameters params) {
        int[] tempGenes = toMutate.genes().clone();

        int first = params.nextInt(tempGenes.length);
        int second = params.nextInt(tempGenes.length);

        int temp = tempGenes[first];
        tempGenes[first] = tempGenes[second];
        tempGenes[second] = temp;

        float newCost = calculator.calculateCost(tempGenes);
        if (newCost < toMutate.cost()) {
            return new Chromosome(tempGenes, newCost);
        }
        return toMutate;
    }

    @Override
    public String getNameKey() {
        return MessageKeys.MUTATION_ONLY_IMPROVING_RANDOM;
    }
}
