package hedos.ga.stagnation;

import hedos.ga.cost.CostCalculator;
import hedos.ga.data.Chromosome;

/**
 * Strategy interface for handling genetic algorithm stagnation and neighborhood adaptation.
 */
public interface StagnationHandler {
    /**
     * Checks for stagnation and adapts parameters if necessary.
     * @return true if the neighborhood size was increased.
     */
    boolean checkStagnation(Chromosome currentBest, Chromosome globalBest, CostCalculator calculator, int targetLimit);

    /** Resets internal counters for a new GA run. */
    void reset();
}