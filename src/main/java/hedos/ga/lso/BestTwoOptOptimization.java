package hedos.ga.lso;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;

/**
 * Best Improvement 2-opt optimization.
 * Scans all possible swaps and selects the one that provides the maximum gain.
 */
@Singleton
public class BestTwoOptOptimization extends LocalSearchOptimizer {
    private static final VectorSpecies<Integer> I_SPECIES = IntVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Float> F_SPECIES = FloatVector.SPECIES_PREFERRED;
    private final LSORuntime runtime;

    @Inject
    public BestTwoOptOptimization(LSORuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void optimize(int[] genes, int[][] neighborLists, int n) {
        MemorySegment distanceMatrix = runtime.getDistanceMatrix();
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            float bestGain = 0;
            int bestI = -1;
            int bestJ = -1;

            float[] distsAC = new float[I_SPECIES.length()];
            float[] distsBD = new float[I_SPECIES.length()];
            float[] distsCD = new float[I_SPECIES.length()];

            for (int i = 1; i < genes.length - 2; i++) {
                int a = genes[i - 1];
                int b = genes[i];
                float distAB = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b);

                for (int j = i + 1; j < genes.length - 1; j += I_SPECIES.length()) {
                    var mask = I_SPECIES.indexInRange(j, genes.length - 1);
                    
                    // Scalar load row into local array to bypass gather-load signature variations
                    for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                        if (mask.laneIsSet(lane)) {
                            int c = genes[j + lane];
                            int d = genes[j + lane + 1];
                            distsAC[lane] = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + c);
                            distsBD[lane] = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) b * n + d);
                            distsCD[lane] = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + d);
                        }
                    }

                    var vDistAC = FloatVector.fromArray(F_SPECIES, distsAC, 0, mask.cast(F_SPECIES));
                    var vDistBD = FloatVector.fromArray(F_SPECIES, distsBD, 0, mask.cast(F_SPECIES));
                    var vDistCD = FloatVector.fromArray(F_SPECIES, distsCD, 0, mask.cast(F_SPECIES));

                    var gains = vDistCD.add(distAB).sub(vDistAC.add(vDistBD));
                    float maxLaneGain = gains.reduceLanes(VectorOperators.MAX, mask.cast(F_SPECIES));

                    if (maxLaneGain > bestGain) {
                        for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                            if (mask.laneIsSet(lane) && gains.lane(lane) > bestGain) {
                                bestGain = gains.lane(lane);
                                bestI = i;
                                bestJ = j + lane;
                                improvement = true;
                            }
                        }
                    }
                }
            }

            if (improvement) {
                reverse(genes, bestI, bestJ);
            }
        }
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.Best2Opt"; }
}