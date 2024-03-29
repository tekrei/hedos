package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;

public abstract class Crossover {

    public Chromosome[] crossover(Chromosome[] population) {
        // SonucKromozom(N) V SonucKromozom[N+1] = Chromosome(N) CAPRAZ
        // Chromosome(N+1)
        for (int i = 0; i < population.length; i = i + 2) {
            try {
                if (GAParameters.getInstance().caprazla()) {
                    match(population[i], population[i + 1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return population;
    }

    abstract void match(Chromosome firstParent, Chromosome secondParent);

    int[] fixGenes(int[] genes) {
        int size = genes.length;
        boolean[] exists = new boolean[size];

        for (int i = 0; i < size; i++) {
            exists[i] = false;
        }

        for (int i = 0; i < size; i++) {
            if (exists[genes[i]]) {
                genes[i] = bringFirst(exists);
            }

            exists[genes[i]] = true;
        }

        return genes;
    }

    private int bringFirst(boolean[] geneStatus) {
        for (int i = 0; i < geneStatus.length; i++) {
            if (!geneStatus[i]) {
                return i;
            }
        }

        return -1;
    }
}
