package hedos.ui;

import com.google.inject.Singleton;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * Decouples JMH benchmark execution from the UI layer.
 */
@Singleton
public class BenchmarkService {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkService.class);
    private static final String BENCHMARK_TITLE = "JMH Benchmark Results";

    public void runAllBenchmarks(Consumer<String> statusConsumer, Consumer<String> resultsConsumer) {
        new Thread(() -> {
            statusConsumer.accept("Starting JMH Benchmarks... Please wait.");
            File tempFile = null;

            try {
                // Create a temporary file to hold the results
                tempFile = File.createTempFile("hedos-benchmark-", ".txt");

                // include matches the package pattern
                Options opt = new OptionsBuilder()
                        .include("hedos.ga.benchmark.*")
                        .forks(1)
                        .jvmArgs("--add-modules=jdk.incubator.vector", "--enable-preview")
                        .warmupIterations(3)
                        .measurementIterations(5)
                        .resultFormat(ResultFormatType.TEXT)
                        .result(tempFile.getAbsolutePath()) // Requires a String path
                        .build();

                // Run the benchmarks and capture the results
                new Runner(opt).run();
                
                // Read the generated text file
                String resultsText = Files.readString(tempFile.toPath());
                resultsConsumer.accept(resultsText);

                statusConsumer.accept("Benchmarks completed successfully.");
            } catch (Exception e) {
                logger.error("JMH Benchmark execution failed", e);
                statusConsumer.accept("Benchmark failed: " + e.getMessage());
                resultsConsumer.accept("Benchmark execution failed: " + e.getMessage());
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }).start();
    }
}