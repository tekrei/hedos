package hedos.ga.selection;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;
import java.util.Random;

public class TournamentSelection extends Selection {
    private final Random random = new Random();

    @Override
    public Chromosome select(Chromosome[] population, int tournamentSize) {
        Chromosome best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Chromosome contestant = population[random.nextInt(population.length)];
            if (best == null || contestant.cost() < best.cost()) {
                best = contestant;
            }
        }
        return best;
    }

    @Override
    public String getNameKey() {
        return MessageKeys.SELECTION_TOURNAMENT;
    }
}