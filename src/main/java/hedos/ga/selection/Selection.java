package hedos.ga.selection;

import hedos.ga.data.Chromosome;

public abstract class Selection {
    /**
     * Selects one individual from the population based on the implemented strategy.
     */
    public abstract Chromosome select(Chromosome[] population, int tournamentSize);
    public abstract String getNameKey();
}