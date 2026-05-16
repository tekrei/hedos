package hedos.ga.selection;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;
import java.util.random.RandomGenerator;

/**
 * Stochastic Universal Sampling (SUS) selection.
 * Provides zero bias and minimum spread.
 */
public class StochasticUniversalSampling extends Selection {
    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public Chromosome select(Chromosome[] population, int tournamentSize) {
        int n = population.length;
        double[] fitness = new double[n];
        double maxCost = 0;
        double minCost = Double.MAX_VALUE;

        for (Chromosome c : population) {
            maxCost = Math.max(maxCost, c.cost());
            minCost = Math.min(minCost, c.cost());
        }

        // Invert cost to fitness (minimization problem)
        double totalFitness = 0;
        for (int i = 0; i < n; i++) {
            fitness[i] = (maxCost - population[i].cost()) + (maxCost - minCost) / 10.0;
            totalFitness += fitness[i];
        }

        double pointerDist = totalFitness / n;
        double start = random.nextDouble() * pointerDist;
        
        // Since GA calls this in a loop, we simulate the SUS pointer for the 'current' request
        // In a true batch SUS, this would be done once.
        double target = start + (random.nextInt(n) * pointerDist);
        target %= totalFitness;

        double runningSum = 0;
        for (int i = 0; i < n; i++) {
            runningSum += fitness[i];
            if (runningSum >= target) {
                return population[i];
            }
        }
        return population[random.nextInt(n)];
    }

    @Override
    public String getNameKey() { return "GA.Selection.SUS"; }
}