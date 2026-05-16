package hedos.ga.lso;

import com.google.inject.Singleton;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.stream.IntStream;

/**
 * Best Improvement 3-opt optimization.
 * Scans all possible triplet edge removals and applies the move that results in the maximum gain.
 */
@Singleton
public class BestThreeOptOptimization extends LocalSearchOptimizer {

    private record Move(float gain, int i, int j, int k, int type) {
        static final Move NONE = new Move(-1, -1, -1, -1, -1);
    }

    @Override
    public void optimize(int[] genes, MemorySegment distanceMatrix, int[][] neighborLists, int n) {
        int[] pos = new int[n];
        for (int p = 0; p < n; p++) pos[genes[p]] = p;

        boolean improvement = true;
        while (improvement) {
            // Parallel search for the best move across all possible outer loop 'i' indices
            Move bestMove = IntStream.range(1, genes.length - 4)
                    .parallel()
                    .mapToObj(i -> findBestMoveForI(i, genes, pos, distanceMatrix, neighborLists, n))
                    .reduce(Move.NONE, (m1, m2) -> m1.gain > m2.gain ? m1 : m2);

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
        Move bestForI = Move.NONE;

        for (int j = i + 2; j < genes.length - 2; j++) {
            int cityC = genes[j - 1];
            for (int cityF : neighborLists[cityC]) {
                int k = pos[cityF];
                if (k >= j + 2 && k < genes.length - 1) {
                    int a = cityA, b = genes[i], c = cityC, d = genes[j], e = genes[k - 1], f = cityF;
                    float d0 = dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b) + 
                               dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + d) + 
                               dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) e * n + f);

                    float g1 = d0 - (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + c) + 
                                     dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) b * n + d) + 
                                     dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) e * n + f));
                    if (g1 > bestForI.gain) bestForI = new Move(g1, i, j, k, 1);

                    float g2 = d0 - (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + b) + 
                                     dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + e) + 
                                     dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) d * n + f));
                    if (g2 > bestForI.gain) bestForI = new Move(g2, i, j, k, 2);

                    float g3 = d0 - (dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) a * n + d) + 
                                     dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) e * n + b) + 
                                     dist.getAtIndex(ValueLayout.JAVA_FLOAT, (long) c * n + f));
                    if (g3 > bestForI.gain) bestForI = new Move(g3, i, j, k, 3);
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