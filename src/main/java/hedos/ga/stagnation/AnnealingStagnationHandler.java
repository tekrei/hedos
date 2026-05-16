package hedos.ga.stagnation;

import com.google.inject.Inject;
import hedos.ga.cost.CostCalculator;
import hedos.ga.cost.TSPCostCalculator;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;

/**
 * Advanced Stagnation Handler using Simulated Annealing logic.
 * Instead of immediate increments, it builds up "heat" while stagnant.
 */
public class AnnealingStagnationHandler implements StagnationHandler {
    private final GAParameters gaParameters;
    private int stagnationCount = 0;
    private float temperature = 1.0f;

    @Inject
    public AnnealingStagnationHandler(GAParameters gaParameters) {
        this.gaParameters = gaParameters;
    }

    @Override
    public boolean checkStagnation(Chromosome currentBest, Chromosome globalBest, CostCalculator calculator, int targetLimit) {
        if (globalBest != null && currentBest.cost() < globalBest.cost()) {
            stagnationCount = 0;
            temperature = Math.max(1.0f, temperature * 0.95f); // Cool down
            return false;
        }

        stagnationCount++;
        // The "heat" rises as we stay stagnant
        if (stagnationCount > 25) {
            temperature *= 1.1f; 
            int increment = (int) (5 * temperature);
            int newSize = gaParameters.getNeighborhoodSize() + increment;
            
            if (newSize < targetLimit) {
                gaParameters.setNeighborhoodSize(newSize);
                if (calculator instanceof TSPCostCalculator tsp) tsp.initNeighbors();
                stagnationCount = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() { stagnationCount = 0; temperature = 1.0f; }
}