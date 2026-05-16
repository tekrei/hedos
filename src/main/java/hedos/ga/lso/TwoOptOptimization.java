package hedos.ga.lso; // File should be moved to src/main/java/hedos/ga/lso/

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;

@Singleton
public class TwoOptOptimization extends LocalSearchOptimizer {
    private static final VectorSpecies<Integer> I_SPECIES = IntVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Float> F_SPECIES = FloatVector.SPECIES_PREFERRED;
    private final LSORuntime runtime;

    @Inject
    public TwoOptOptimization(LSORuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void optimize(int[] genes, int[][] neighborLists, int n) {
        MemorySegment distanceMatrix = runtime.getDistanceMatrix();
        boolean improvement = true;
        boolean[] dlb = new boolean[n];
        while (improvement) {
            improvement = false;
            for (int i = 1; i < genes.length - 2; i++) {
                if (dlb[genes[i]]) continue;
                boolean cityImproved = false;
                int a = genes[i - 1];
                int b = genes[i];
                float distAB = distanceMatrix.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b);

                float[] distsAC = new float[I_SPECIES.length()];
                float[] distsBD = new float[I_SPECIES.length()];
                float[] distsCD = new float[I_SPECIES.length()];

                for (int j = i + 1; j < genes.length - 1; j += I_SPECIES.length()) {
                    var mask = I_SPECIES.indexInRange(j, genes.length - 1);
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
                    if (gains.compare(VectorOperators.GT, 0.0f, mask.cast(F_SPECIES)).anyTrue()) {
                        for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                            if (mask.laneIsSet(lane) && gains.lane(lane) > 0) {
                                reverse(genes, i, j + lane);
                                improvement = cityImproved = true;
                                dlb[genes[i-1]] = dlb[genes[i]] = dlb[genes[j+lane]] = dlb[genes[j+lane+1]] = false;
                                break;
                            }
                        }
                    }
                    if (cityImproved) break;
                }
                if (!cityImproved) dlb[genes[i]] = true;
            }
        }
    }

    @Override
    public String getNameKey() { return "GA.LocalOpt.2Opt"; }
}