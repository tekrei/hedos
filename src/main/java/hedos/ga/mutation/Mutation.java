package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Mutation {
    private static final Logger logger = LoggerFactory.getLogger(Mutation.class);

    public Chromosome[] mutate(Chromosome[] population, GAParameters params) {
        for (int i = 0; i < population.length; i++) {
            try {
                if (params.shouldMutate()) {
                    population[i] = mutate(population[i], params);
                }
            } catch (Exception e) {
                logger.error("Mutation operation failed at index {}: {}", i, e.getMessage(), e);
            }
        }

        return population;
    }

    abstract Chromosome mutate(Chromosome chromosome, GAParameters params);

    public abstract String getNameKey();
}
