package hedos.ga.lso;

import com.google.inject.Singleton;
import java.util.stream.IntStream;
import java.util.Optional;

/**
 * First Improvement 3-opt optimization.
 * Removes three edges and applies the first gain-positive reconnection found in parallel.
 */
@Singleton
public class FirstThreeOptOptimization extends LocalSearchOptimizer {

    private record Move(float gain, int i, int j, int k, int type) {
        static final Move NONE = new Move(-1, -1, -1, -1, -1);
    }

    @Override
    public void optimize(int[] genes, float[] distanceMatrix, int[][] neighborLists, int n) {
        int[] pos = new int[n];
        for (int p = 0; p < n; p++) pos[genes[p]] = p;

        boolean improvement = true;
        while (improvement) {
            // Use findAny() on parallel stream to implement First Improvement
            Optional<Move> firstMove = IntStream.range(1, genes.length - 4)
                    .parallel()
                    .mapToObj(i -> findFirstMoveForI(i, genes, pos, distanceMatrix, neighborLists, n))
                    .filter(m -> m.gain > 0.001f)
                    .findAny();

            if (firstMove.isPresent()) {
                applyMove(genes, pos, firstMove.get());
                improvement = true;
            } else {
                improvement = false;
            }
        }
    }

    private Move findFirstMoveForI(int i, int[] genes, int[] pos, float[] dist, int[][] neighborLists, int n) {
        int cityA = genes[i - 1];
        for (int j = i + 2; j < genes.length - 2; j++) {
            int cityC = genes[j - 1];
            for (int cityF : neighborLists[cityC]) {
                int k = pos[cityF];
                if (k >= j + 2 && k < genes.length - 1) {
                    int a = cityA, b = genes[i], c = cityC, d = genes[j], e = genes[k - 1], f = cityF;
                    float d0 = dist[a * n + b] + dist[c * n + d] + dist[e * n + f];

                    float g1 = d0 - (dist[a * n + c] + dist[b * n + d] + dist[e * n + f]);
                    if (g1 > 0.001f) return new Move(g1, i, j, k, 1);

                    float g2 = d0 - (dist[a * n + b] + dist[c * n + e] + dist[d * n + f]);
                    if (g2 > 0.001f) return new Move(g2, i, j, k, 2);

                    float g3 = d0 - (dist[a * n + d] + dist[e * n + b] + dist[c * n + f]);
                    if (g3 > 0.001f) return new Move(g3, i, j, k, 3);
                }
            }
        }
        return Move.NONE;
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
        return "GA.LocalOpt.3Opt";
    }
}
