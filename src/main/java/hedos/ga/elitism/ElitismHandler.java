package hedos.ga.elitism;

import hedos.ga.data.Chromosome;

/**
 * Strategy for maintaining elite individuals across generations.
 */
public interface ElitismHandler {
    /**
     * Evaluates the population and applies elitism logic.
     * 
     * @param population Current sorted population.
     * @param globalBest The best individual found across all generations.
     * @param enabled Whether elitism is enabled in parameters.
     * @return The updated global best.
     */
    Chromosome applyElitism(Chromosome[] population, Chromosome globalBest, boolean enabled);
}