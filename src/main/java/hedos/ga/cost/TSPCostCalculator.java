package hedos.ga.cost;

import java.util.List;
import java.util.Arrays;
import hedos.ga.data.Point;
import hedos.ga.data.GAParameters;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSPCostCalculator implements CostCalculator {
    private static final Logger logger = LoggerFactory.getLogger(TSPCostCalculator.class);
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private List<Point> targets;
    private MemorySegment distanceMatrixSegment;
    private float[] targetXs, targetYs, targetZs;
    private int[][] neighborLists;
    private final GAParameters gaParams;

    @Inject
    public TSPCostCalculator(GAParameters gaParams) {
        this.gaParams = gaParams;
    }

    public void init(List<Point> targets) {
        this.targets = targets;
        int n = targets.size();
        
        // Optimize for SIMD by aligning to 64 bytes (AVX-512 compatible)
        this.distanceMatrixSegment = Arena.ofAuto().allocate((long) n * n * Float.BYTES, 64);
        
        this.targetXs = new float[n];
        this.targetYs = new float[n];
        this.targetZs = new float[n];

        for (int i = 0; i < n; i++) {
            Point p = targets.get(i);
            targetXs[i] = p.x();
            targetYs[i] = p.y();
            targetZs[i] = p.z();
        }

        long start = System.nanoTime();
        precomputeDistances(n);
        initNeighbors();
        double duration = (System.nanoTime() - start) / 1_000_000.0;
        logger.info("Distance matrix and neighbor lists ({}x{}) precomputation took {} ms", n, n, duration);
    }

    public void initNeighbors() {
        if (targets != null) {
            precomputeNeighbors(targets.size(), gaParams.getNeighborhoodSize());
        }
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

                dists.intoMemorySegment(distanceMatrixSegment, (long) (i * n + j) * Float.BYTES, ByteOrder.nativeOrder(), mask);
            }
        });
    }

    @Override
    public float calculateCost(int[] genes) {
        return computeActualCost(genes);
    }

    private float computeActualCost(int[] genes) {
        float totalDistance = 0.0f;
        int n = targets.size();
        for (int i = 0; i < genes.length - 1; i++) {
            totalDistance += distanceMatrixSegment.getAtIndex(ValueLayout.JAVA_FLOAT, (long) genes[i] * n + genes[i + 1]);
        }

        // Include sharp turns in the primary cost calculation with a penalty
        float turnPenalty = calculateTurnCost(genes);
        
        // Favor Scoped Parameters if available, otherwise use injected instance
        float penaltyFactor = GAParameters.CURRENT.isBound() ? 
            GAParameters.CURRENT.get().getTurnPenaltyFactor() : gaParams.getTurnPenaltyFactor();
            
        return totalDistance + (turnPenalty * penaltyFactor);
    }

    private void precomputeNeighbors(int n, int k) {
        int limit = Math.min(n - 1, k);
        neighborLists = new int[n][limit];
        IntStream.range(0, n).parallel().forEach(i -> {
            final int current = i;
            final long rowOffset = (long) current * n;
            neighborLists[i] = IntStream.range(0, n)
                    .filter(j -> j != current)
                    .boxed()
                    .sorted(Comparator.comparingDouble(j -> distanceMatrixSegment.getAtIndex(ValueLayout.JAVA_FLOAT, rowOffset + j)))
                    .limit(limit)
                    .mapToInt(Integer::intValue)
                    .toArray();
        });
    }

    @Override
    public float calculateTurnCost(int[] genes) {
        return computeActualTurnCost(genes);
    }

    private float computeActualTurnCost(int[] genes) {
        float totalAngle = 0;
        int n = genes.length;
        if (n < 3) return 0;

        for (int i = 0; i < n - 2; i += SPECIES.length()) {
            var mask = SPECIES.indexInRange(i, n - 2);
            // Gather coordinates for points p1, p2, p3
            var p1x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i, mask);
            var p1y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i, mask);
            var p1z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i, mask);

            var p2x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i + 1, mask);
            var p2y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i + 1, mask);
            var p2z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i + 1, mask);

            var p3x = FloatVector.fromArray(SPECIES, targetXs, 0, genes, i + 2, mask);
            var p3y = FloatVector.fromArray(SPECIES, targetYs, 0, genes, i + 2, mask);
            var p3z = FloatVector.fromArray(SPECIES, targetZs, 0, genes, i + 2, mask);

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
            var validMask = mask.and(mag1.compare(VectorOperators.GT, 0.0f)).and(mag2.compare(VectorOperators.GT, 0.0f));
            var cosTheta = dot.div(mag1.mul(mag2))
                    .lanewise(VectorOperators.MIN, 1.0f)
                    .lanewise(VectorOperators.MAX, -1.0f);

            var angles = cosTheta.lanewise(VectorOperators.ACOS);
            totalAngle += angles.reduceLanes(VectorOperators.ADD, validMask);
        }
        return totalAngle;
    }

    public MemorySegment getDistanceMatrix() {
        return distanceMatrixSegment;
    }

    public int[][] getNeighborLists() {
        return neighborLists;
    }
}