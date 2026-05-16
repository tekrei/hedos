package hedos.ga.data;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import com.google.inject.Inject;

public class TSPCostCalculator implements CostCalculator {
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private List<Point> targets;
    private float[] distanceMatrix;
    private float[] targetXs, targetYs, targetZs;
    private final GAParameters gaParams;
    private final Map<GeneSequence, Float> costCache = new ConcurrentHashMap<>();
    private final Map<GeneSequence, Float> turnCache = new ConcurrentHashMap<>();

    private record GeneSequence(int[] genes) {
        @Override
        public boolean equals(Object o) {
            return o instanceof GeneSequence other && Arrays.equals(genes, other.genes);
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(genes);
        }
    }

    @Inject
    public TSPCostCalculator(GAParameters gaParams) {
        this.gaParams = gaParams;
    }

    public void init(List<Point> targets) {
        this.targets = targets;
        int n = targets.size();
        this.distanceMatrix = new float[n * n];
        this.targetXs = new float[n];
        this.targetYs = new float[n];
        this.targetZs = new float[n];

        for (int i = 0; i < n; i++) {
            Point p = targets.get(i);
            targetXs[i] = p.x();
            targetYs[i] = p.y();
            targetZs[i] = p.z();
        }

        costCache.clear();
        turnCache.clear();
        precomputeDistances(n);
    }

    private void precomputeDistances(int n) {
        IntStream.range(0, n).parallel().forEach(i -> {
            float ix = targetXs[i];
            float iy = targetYs[i];
            float iz = targetZs[i];

            // Fully vectorized loop using masking for the tail
            for (int j = 0; j < n; j += SPECIES.length()) {
                var mask = SPECIES.indexInRange(j, n);
                var vx = FloatVector.fromArray(SPECIES, targetXs, j, mask);
                var vy = FloatVector.fromArray(SPECIES, targetYs, j, mask);
                var vz = FloatVector.fromArray(SPECIES, targetZs, j, mask);

                var dx = vx.sub(ix);
                var dy = vy.sub(iy);
                var dz = vz.sub(iz);

                var dists = dx.mul(dx)
                        .add(dy.mul(dy))
                        .add(dz.mul(dz))
                        .sqrt();

                dists.intoArray(distanceMatrix, i * n + j, mask);
            }
        });
    }

    @Override
    public float calculateCost(int[] genes) {
        return costCache.computeIfAbsent(new GeneSequence(genes), k -> computeActualCost(k.genes()));
    }

    private float computeActualCost(int[] genes) {
        float totalDistance = 0.0f;
        int n = targets.size();
        for (int i = 0; i < genes.length - 1; i++) {
            totalDistance += distanceMatrix[genes[i] * n + genes[i + 1]];
        }

        // Include sharp turns in the primary cost calculation with a penalty
        float turnPenalty = calculateTurnCost(genes);
        return totalDistance + (turnPenalty * gaParams.getTurnPenaltyFactor());
    }

    @Override
    public float calculateTurnCost(int[] genes) {
        return turnCache.computeIfAbsent(new GeneSequence(genes), k -> computeActualTurnCost(k.genes()));
    }

    private float computeActualTurnCost(int[] genes) {
        float totalAngle = 0;
        int n = genes.length;
        if (n < 3) return 0;

        int bound = SPECIES.loopBound(n - 2);
        int i = 0;

        for (; i < bound; i += SPECIES.length()) {
            // Gather coordinates for points p1, p2, p3
            var p1x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i);
            var p1y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i);
            var p1z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i);

            var p2x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i + 1);
            var p2y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i + 1);
            var p2z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i + 1);

            var p3x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i + 2);
            var p3y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i + 2);
            var p3z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i + 2);

            // Calculate vectors v1 and v2
            var v1x = p2x.sub(p1x);
            var v1y = p2y.sub(p1y);
            var v1z = p2z.sub(p1z);

            var v2x = p3x.sub(p2x);
            var v2y = p3y.sub(p2y);
            var v2z = p3z.sub(p2z);

            var dot = v1x.mul(v2x).add(v1y.mul(v2y)).add(v1z.mul(v2z));
            var mag1 = v1x.mul(v1x).add(v1y.mul(v1y)).add(v1z.mul(v1z)).sqrt();
            var mag2 = v2x.mul(v2x).add(v2y.mul(v2y)).add(v2z.mul(v2z)).sqrt();

            // Mask to avoid division by zero
            var mask = mag1.compare(VectorOperators.GT, 0.0f).and(mag2.compare(VectorOperators.GT, 0.0f));
            var cosTheta = dot.div(mag1.mul(mag2))
                    .lanewise(VectorOperators.MIN, 1.0f)
                    .lanewise(VectorOperators.MAX, -1.0f);

            var angles = cosTheta.lanewise(VectorOperators.ACOS);
            totalAngle += angles.reduceLanes(VectorOperators.ADD, mask);
        }

        // Scalar tail loop for remaining elements
        for (; i < n - 2; i++) {
            float v1x = targetXs[genes[i + 1]] - targetXs[genes[i]];
            float v1y = targetYs[genes[i + 1]] - targetYs[genes[i]];
            float v1z = targetXs[genes[i + 1]] - targetXs[genes[i]];

            float v2x = targetXs[genes[i + 2]] - targetXs[genes[i + 1]];
            float v2y = targetYs[genes[i + 2]] - targetYs[genes[i + 1]];
            float v2z = targetXs[genes[i + 2]] - targetXs[genes[i + 1]];

            float dot = v1x * v2x + v1y * v2y + v1z * v2z;
            float mag1 = (float) Math.sqrt(v1x * v1x + v1y * v1y + v1z * v1z);
            float mag2 = (float) Math.sqrt(v2x * v2x + v2y * v2y + v2z * v2z);

            if (mag1 > 0 && mag2 > 0) {
                float cosTheta = Math.max(-1f, Math.min(1f, dot / (mag1 * mag2)));
                totalAngle += (float) Math.acos(cosTheta);
            }
        }
        return totalAngle;
    }
}