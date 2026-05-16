package hedos.ga.elitism;

import com.google.inject.Singleton;
import hedos.ga.data.Chromosome;

@Singleton
public class DefaultElitismHandler implements ElitismHandler {
    @Override
    public Chromosome applyElitism(Chromosome[] population, Chromosome globalBest, boolean enabled) {
        Chromosome generationalBest = population[0];

        if (globalBest == null || generationalBest.cost() < globalBest.cost()) {
            // New global best found
            return generationalBest;
        } else if (enabled) {
            // Re-inject the global best into the population if it's falling behind
            population[population.length - 1] = globalBest;
        }
        
        return globalBest;
    }
}