package hedos.ga.mutation;

import com.google.inject.Inject;

import hedos.ga.data.Chromosome;
import hedos.ga.data.ChromosomeFactory;
import hedos.ga.data.GAParameters;
import hedos.utility.MessageKeys;

public class VectorizedScrambleMutation extends Mutation {
    private final ChromosomeFactory factory;

    @Inject
    public VectorizedScrambleMutation(ChromosomeFactory factory) {
        this.factory = factory;
    }

    @Override
    Chromosome mutate(Chromosome chromosome, GAParameters params) {
        // Use the vectorized scramble logic from the factory
        factory.vectorizedMutate(chromosome.genes(), params.getMutationProbability());
        chromosome.setEvaluated(false); // Invalidate cost after mutation
        return chromosome;
    }

    @Override
    public String getNameKey() {
        return MessageKeys.MUTATION_VECTORIZED_SCRAMBLE;
    }
}