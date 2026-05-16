package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public class DisplacementMutation extends Mutation {
    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    Chromosome mutate(Chromosome chromosome, GAParameters params) {
        int[] genes = chromosome.genes();
        int n = genes.length;
        
        int i = random.nextInt(n);
        int j = random.nextInt(n);
        int start = Math.min(i, j);
        int end = Math.max(i, j);

        List<Integer> subTour = new ArrayList<>();
        List<Integer> remainder = new ArrayList<>();
        
        for (int k = 0; k < n; k++) {
            if (k >= start && k <= end) subTour.add(genes[k]);
            else remainder.add(genes[k]);
        }

        int insertPos = random.nextInt(Math.max(1, remainder.size()));
        remainder.addAll(insertPos, subTour);

        for (int k = 0; k < n; k++) genes[k] = remainder.get(k);
        
        chromosome.setEvaluated(false);
        return chromosome;
    }

    @Override
    public String getNameKey() { return "GA.Mutation.Displacement"; }
}