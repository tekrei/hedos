package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SIMD-accelerated Scramble Mutation utilizing VectorShuffle.
 * For segments matching the CPU vector width, indices are permuted in parallel.
 */
public class VectorizedScrambleMutation extends Mutation {
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

    @Override
    public Chromosome mutate(Chromosome chromosome, GAParameters params) {
        scramble(chromosome.genes());
        chromosome.setEvaluated(false);
        return chromosome;
    }

    private void scramble(int[] genes) {
        int n = genes.length;
        if (n < SPECIES.length()) return;
        var random = ThreadLocalRandom.current();

        // Pick a random starting point that fits a full vector lane
        int start = random.nextInt(n - SPECIES.length() + 1);

        // Load the segment into a vector
        IntVector v = IntVector.fromArray(SPECIES, genes, start);

        // Generate a random permutation for the vector lanes
        int[] indices = new int[SPECIES.length()];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        
        // Fisher-Yates shuffle for the indices
        for (int i = indices.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;
        }

        // Apply the shuffle using SIMD rearrange
        VectorShuffle<Integer> shuffle = VectorShuffle.fromArray(SPECIES, indices, 0);
        v.rearrange(shuffle).intoArray(genes, start);
    }

    @Override
    public String getNameKey() { return "GA.Mutation.VectorScramble"; }
}