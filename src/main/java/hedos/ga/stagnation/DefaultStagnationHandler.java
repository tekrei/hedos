package hedos.ga.stagnation;

import com.google.inject.Inject;
import hedos.ga.cost.CostCalculator;
import hedos.ga.cost.TSPCostCalculator;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;

public class DefaultStagnationHandler implements StagnationHandler {
    private final GAParameters gaParameters;
    private int stagnationCount = 0;
    private static final int STAGNATION_THRESHOLD = 50;
    private static final int NEIGHBORHOOD_INCREMENT = 5;

    @Inject
    public DefaultStagnationHandler(GAParameters gaParameters) {
        this.gaParameters = gaParameters;
    }

    @Override
    public boolean checkStagnation(Chromosome currentBest, Chromosome globalBest, CostCalculator calculator, int targetLimit) {
        if (globalBest != null && currentBest.cost() < globalBest.cost()) {
            stagnationCount = 0;
        } else {
            stagnationCount++;
        }

        if (stagnationCount >= STAGNATION_THRESHOLD) {
            int newSize = gaParameters.getNeighborhoodSize() + NEIGHBORHOOD_INCREMENT;
            if (newSize < targetLimit) {
                gaParameters.setNeighborhoodSize(newSize);
                if (calculator instanceof TSPCostCalculator tsp) {
                    tsp.initNeighbors();
                }
                stagnationCount = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        this.stagnationCount = 0;
    }
}