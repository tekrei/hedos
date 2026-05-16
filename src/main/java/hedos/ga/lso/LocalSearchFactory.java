package hedos.ga.lso; // File should be moved to src/main/java/hedos/ga/lso/

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters;
import java.util.Map;

@Singleton
public class LocalSearchFactory {
    private final Map<GAParameters.LocalOptimizationType, LocalSearchOptimizer> strategies;

    @Inject
    public LocalSearchFactory(Map<GAParameters.LocalOptimizationType, LocalSearchOptimizer> strategies) {
        this.strategies = strategies;
    }
    public LocalSearchOptimizer get(GAParameters.LocalOptimizationType type) { return strategies.get(type); }
}