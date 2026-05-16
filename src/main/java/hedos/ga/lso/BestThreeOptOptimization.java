package hedos.ga.lso;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;
import java.util.ArrayList;
import java.util.concurrent.StructuredTaskScope;

/**
 * Best Improvement 3-opt optimization.
 * Scans all possible triplet edge removals and applies the move that results in the maximum gain.
 */
@Singleton
public class BestThreeOptOptimization extends LocalSearchOptimizer {
    private static final VectorSpecies<Integer> I_SPECIES = IntVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Float> F_SPECIES = FloatVector.SPECIES_PREFERRED;
    private final LSORuntime runtime;

    @Inject
    public BestThreeOptOptimization(LSORuntime runtime) {
        this.runtime = runtime;
    }

    private record Move(float gain, int i, int j, int k, int type) {
        static final Move NONE = new Move(-1, -1, -1, -1, -1);
    }

    @Override
    public void optimize(int[] genes, int[][] neighborLists, int n) {
        MemorySegment distanceMatrix = runtime.getDistanceMatrix();
        int[] pos = new int[n];
        for (int p = 0; p < n; p++) pos[genes[p]] = p;

        boolean improvement = true;
        int batchSize = 25; // Batching O(N) forks into O(N/batchSize)
        while (improvement) {
            Move bestMove;
            try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
                var tasks = new ArrayList<StructuredTaskScope.Subtask<Move>>();
                for (int i = 1; i < genes.length - 4; i += batchSize) {
                    final int start = i;
                    final int end = Math.min(i + batchSize, genes.length - 4);
                    tasks.add(scope.fork(() -> {
                        Move localBest = Move.NONE;
                        for (int idx = start; idx < end; idx++) {
                            Move m = findBestMoveForI(idx, genes, pos, distanceMatrix, neighborLists, n);
                            if (m.gain > localBest.gain) localBest = m;
                        }
                        return localBest;
                    }));
                }
                scope.join();
                bestMove = tasks.stream().map(StructuredTaskScope.Subtask::get)
                        .reduce(Move.NONE, (m1, m2) -> m1.gain > m2.gain ? m1 : m2);
            } catch (Exception e) { throw new RuntimeException("3-Opt Parallel Search Failed", e); }

            if (bestMove.gain > 0.001f) {
                applyMove(genes, pos, bestMove);
                improvement = true;
            } else {
                improvement = false;
            }
        }
    }

    private Move findBestMoveForI(int i, int[] genes, int[] pos, MemorySegment dist, int[][] neighborLists, int n) {
        int cityA = genes[i - 1];
        int cityB = genes[i];
        float distAB = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityA * n + cityB);
        Move bestForI = Move.NONE;

        float[] distsAC = new float[I_SPECIES.length()];
        float[] distsBD = new float[I_SPECIES.length()];
        float[] distsCE = new float[I_SPECIES.length()];
        float[] distsDF = new float[I_SPECIES.length()];
        float[] distsAD = new float[I_SPECIES.length()];
        float[] distsEB = new float[I_SPECIES.length()];
        float[] distsCF = new float[I_SPECIES.length()];
        float[] distsEF = new float[I_SPECIES.length()];

        for (int j = i + 2; j < genes.length - 2; j++) {
            int cityC = genes[j - 1];
            int cityD = genes[j];
            float distCD = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityC * n + cityD);
            int[] neighbors = neighborLists[cityC];

            for (int m = 0; m < neighbors.length; m += I_SPECIES.length()) {
                var mask = I_SPECIES.indexInRange(m, neighbors.length);
                for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                    if (mask.laneIsSet(lane)) {
                        int cityF = neighbors[m + lane];
                        int k = pos[cityF];
                        if (k >= j + 2 && k < genes.length - 1) {
                            int cityE = genes[k - 1];
                            distsAC[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityA * n + cityC);
                            distsBD[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityB * n + cityD);
                            distsEF[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityE * n + cityF);
                            distsCE[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityC * n + cityE);
                            distsDF[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityD * n + cityF);
                            distsAD[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityA * n + cityD);
                            distsEB[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityE * n + cityB);
                            distsCF[lane] = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) cityC * n + cityF);
                        } else {
                            distsAC[lane] = distsBD[lane] = distsEF[lane] = distsCE[lane] = distsDF[lane] = distsAD[lane] = distsEB[lane] = distsCF[lane] = 1e9f;
                        }
                    }
                }

                var vAC = FloatVector.fromArray(F_SPECIES, distsAC, 0, mask.cast(F_SPECIES));
                var vBD = FloatVector.fromArray(F_SPECIES, distsBD, 0, mask.cast(F_SPECIES));
                var vEF = FloatVector.fromArray(F_SPECIES, distsEF, 0, mask.cast(F_SPECIES));
                var vCE = FloatVector.fromArray(F_SPECIES, distsCE, 0, mask.cast(F_SPECIES));
                var vDF = FloatVector.fromArray(F_SPECIES, distsDF, 0, mask.cast(F_SPECIES));
                var vAD = FloatVector.fromArray(F_SPECIES, distsAD, 0, mask.cast(F_SPECIES));
                var vEB = FloatVector.fromArray(F_SPECIES, distsEB, 0, mask.cast(F_SPECIES));
                var vCF = FloatVector.fromArray(F_SPECIES, distsCF, 0, mask.cast(F_SPECIES));

                var d0 = vEF.add(distAB + distCD);
                var g1 = d0.sub(vAC.add(vBD).add(vEF));
                var g2 = d0.sub(FloatVector.broadcast(F_SPECIES, distAB).add(vCE).add(vDF));
                var g3 = d0.sub(vAD.add(vEB).add(vCF));

                float maxG = g1.lanewise(VectorOperators.MAX, g2).lanewise(VectorOperators.MAX, g3)
                              .reduceLanes(VectorOperators.MAX, mask.cast(F_SPECIES));

                if (maxG > bestForI.gain) {
                    for (int lane = 0; lane < I_SPECIES.length(); lane++) {
                        if (mask.laneIsSet(lane)) {
                            int cityF = neighbors[m + lane];
                            int k = pos[cityF];
                            if (k >= j + 2 && k < genes.length - 1) {
                                if (g1.lane(lane) > bestForI.gain) bestForI = new Move(g1.lane(lane), i, j, k, 1);
                                if (g2.lane(lane) > bestForI.gain) bestForI = new Move(g2.lane(lane), i, j, k, 2);
                                if (g3.lane(lane) > bestForI.gain) bestForI = new Move(g3.lane(lane), i, j, k, 3);
                            }
                        }
                    }
                }
            }
        }
        return bestForI;
    }

    private void applyMove(int[] genes, int[] pos, Move move) {
        switch (move.type) {
            case 1 -> reverseWithMap(genes, pos, move.i, move.j - 1);
            case 2 -> reverseWithMap(genes, pos, move.j, move.k - 1);
            case 3 -> swapSegmentsWithMap(genes, pos, move.i, move.j, move.k);
        }
    }

    private void reverseWithMap(int[] genes, int[] pos, int start, int end) {
        reverse(genes, start, end);
        for (int p = start; p <= end; p++) {
            pos[genes[p]] = p;
        }
    }

    private void swapSegmentsWithMap(int[] genes, int[] pos, int i, int j, int k) {
        int[] segmentB = new int[j - i];
        System.arraycopy(genes, i, segmentB, 0, j - i);
        int[] segmentC = new int[k - j];
        System.arraycopy(genes, j, segmentC, 0, k - j);

        System.arraycopy(segmentC, 0, genes, i, segmentC.length);
        System.arraycopy(segmentB, 0, genes, i + segmentC.length, segmentB.length);

        for (int p = i; p <= k; p++) {
            pos[genes[p]] = p;
        }
    }

    @Override
    public String getNameKey() {
        return "GA.LocalOpt.Best3Opt";
    }
}