package hedos.ga.data;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import com.google.inject.Inject;

public class TSPCostCalculator implements CostCalculator {
    private List<Point> targets;
    private float[] distanceMatrix;
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
        costCache.clear();
        turnCache.clear();
        precomputeDistances(n);
    }

    private void precomputeDistances(int n) {
        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = i; j < n; j++) {
                Point p1 = targets.get(i);
                Point p2 = targets.get(j);
                float dx = p2.x() - p1.x();
                float dy = p2.y() - p1.y();
                float dz = p2.z() - p1.z();
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                
                distanceMatrix[i * n + j] = dist;
                distanceMatrix[j * n + i] = dist;
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
        if (genes.length < 3) return 0;

        for (int i = 0; i < genes.length - 2; i++) {
            Point p1 = targets.get(genes[i]);
            Point p2 = targets.get(genes[i + 1]);
            Point p3 = targets.get(genes[i + 2]);

            float v1x = p2.x() - p1.x();
            float v1y = p2.y() - p1.y();
            float v1z = p2.z() - p1.z();

            float v2x = p3.x() - p2.x();
            float v2y = p3.y() - p2.y();
            float v2z = p3.z() - p2.z();

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