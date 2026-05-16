package hedos.ga.selection;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;
import java.util.Random;

public class RouletteWheelSelection extends Selection {
    private final Random random = new Random();

    @Override
    public Chromosome select(Chromosome[] population, int tournamentSize) {
        double totalFitness = 0;
        double maxFitness = 0;
        
        for (Chromosome c : population) {
            if (c.cost() > maxFitness) maxFitness = c.cost();
        }

        for (Chromosome c : population) {
            totalFitness += (maxFitness - c.cost() + 0.0001);
        }

        double value = random.nextDouble() * totalFitness;
        double sum = 0;
        for (Chromosome c : population) {
            sum += (maxFitness - c.cost() + 0.0001);
            if (sum >= value) return c;
        }
        return population[population.length - 1];
    }

    @Override
    public String getNameKey() {
        return MessageKeys.SELECTION_ROULETTE_WHEEL;
    }
}