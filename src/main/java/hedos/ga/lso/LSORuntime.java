package hedos.ga.lso;

import com.google.inject.Singleton;
import hedos.ga.cost.CostCalculator;
import java.lang.foreign.MemorySegment;
import java.time.Duration;

/**
 * Java 25 Scoped Values for Local Search Optimizer runtime context.
 */
@Singleton
public class LSORuntime {
    public static final ScopedValue<MemorySegment> DISTANCE_MATRIX = ScopedValue.newInstance();
    public static final ScopedValue<Integer> TOUR_SIZE = ScopedValue.newInstance();
    public static final ScopedValue<Duration> EVALUATION_TIMEOUT = ScopedValue.newInstance();
    public static final ScopedValue<CostCalculator> CALCULATOR = ScopedValue.newInstance();

    public MemorySegment getDistanceMatrix() {
        if (!DISTANCE_MATRIX.isBound()) {
            throw new IllegalStateException("Distance Matrix not bound in current scope.");
        }
        return DISTANCE_MATRIX.get();
    }

    public int getTourSize() {
        return TOUR_SIZE.get();
    }

    public CostCalculator getCalculator() {
        return CALCULATOR.get();
    }
}