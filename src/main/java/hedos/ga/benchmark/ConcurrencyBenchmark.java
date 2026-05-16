package hedos.ga.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class ConcurrencyBenchmark {

    private static final ThreadLocal<String> THREAD_LOCAL = ThreadLocal.withInitial(() -> "Context");
    private static final ScopedValue<String> SCOPED_VALUE = ScopedValue.newInstance();

    @Benchmark
    public String testThreadLocal() {
        return THREAD_LOCAL.get();
    }

    @Benchmark
    public String testScopedValue() {
        // Note: In a real app, this would be bound higher up the call stack
        return ScopedValue.where(SCOPED_VALUE, "Context").call(SCOPED_VALUE::get);
    }

    @Benchmark
    public String testBoundScopedValue() {
        // Simulates performance once already inside a bound scope
        return ScopedValue.where(SCOPED_VALUE, "Context").call(this::justGet);
    }

    private String justGet() {
        return SCOPED_VALUE.get();
    }
}