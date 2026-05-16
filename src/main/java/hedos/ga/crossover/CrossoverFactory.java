package hedos.ga.crossover;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters.CrossoverType;
import java.util.Map;

@Singleton
public class CrossoverFactory {
    private final Map<CrossoverType, Crossover> operators;

    @Inject
    public CrossoverFactory(Map<CrossoverType, Crossover> operators) {
        this.operators = operators;
    }

    public Crossover get(CrossoverType type) {
        return operators.getOrDefault(type, operators.get(CrossoverType.SINGLE_POINT));
    }
}