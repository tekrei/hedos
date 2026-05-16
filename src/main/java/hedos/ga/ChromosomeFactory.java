package hedos.ga;

import com.google.inject.Singleton;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Factory for creating and repairing Chromosome gene sequences.
 */
@Singleton
public class ChromosomeFactory {
    private final RandomGenerator randomGenerator = RandomGeneratorFactory.getDefault().create();

    public int[] createRandomGenes(int n) {
        int[] genes = new int[n];
        for (int i = 0; i < n; i++) genes[i] = i;
        
        // Optimized Fisher-Yates shuffle
        for (int i = n - 1; i > 0; i--) {
            int j = randomGenerator.nextInt(i + 1);
            int temp = genes[i];
            genes[i] = genes[j];
            genes[j] = temp;
        }
        return genes;
    }

    public void repairGenes(int[] genes) {
        int n = genes.length;
        boolean[] used = new boolean[n];
        int[] duplicatePositions = new int[n];
        int dupCount = 0;

        for (int i = 0; i < n; i++) {
            int val = genes[i];
            if (val >= 0 && val < n && !used[val]) {
                used[val] = true;
            } else {
                duplicatePositions[dupCount++] = i;
            }
        }

        int missingVal = 0;
        for (int i = 0; i < dupCount; i++) {
            while (used[missingVal]) missingVal++;
            genes[duplicatePositions[i]] = missingVal;
            used[missingVal] = true;
        }
    }

    /**
     * Performs a vectorized uniform crossover (blend) between two parents.
     */
    public int[] vectorizedBlend(int[] parent1, int[] parent2, boolean[] mask) {
        VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
        int n = parent1.length;
        int[] child = new int[n];
        
        for (int i = 0; i < n; i += SPECIES.length()) {
            var m = SPECIES.indexInRange(i, n);
            var v1 = IntVector.fromArray(SPECIES, parent1, i, m);
            var v2 = IntVector.fromArray(SPECIES, parent2, i, m);
            
            VectorMask<Integer> blendMask;
            if (i + SPECIES.length() <= n) {
                blendMask = SPECIES.loadMask(mask, i);
            } else {
                boolean[] tailBits = new boolean[SPECIES.length()];
                System.arraycopy(mask, i, tailBits, 0, n - i);
                blendMask = SPECIES.loadMask(tailBits, 0);
            }
            
            v1.blend(v2, blendMask).intoArray(child, i, m);
        }
        
        repairGenes(child);
        return child;
    }

    public RandomGenerator getRandomGenerator() { return randomGenerator; }
}