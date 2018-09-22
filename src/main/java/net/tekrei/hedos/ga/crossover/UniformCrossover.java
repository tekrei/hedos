package net.tekrei.hedos.ga.crossover;

import net.tekrei.hedos.ga.utilities.Chromosome;

public class UniformCrossover extends Crossover {

    @Override
    void match(Chromosome baba, Chromosome anne) {
        int[] babaDeger = baba.getGenes();
        int[] anneDeger = anne.getGenes();

        int[] cocuk = new int[babaDeger.length];
        int[] cocuk2 = new int[babaDeger.length];

        for (int i = 0; i < babaDeger.length; i += 2) {
            cocuk[i] = babaDeger[i];
            cocuk2[i] = anneDeger[i];
            cocuk[i + 1] = anneDeger[i + 1];
            cocuk2[i + 1] = babaDeger[i + 1];
        }
        cocuk = fixGenes(cocuk);
        cocuk2 = fixGenes(cocuk2);

        baba.setGenes(cocuk);
        anne.setGenes(cocuk2);
    }
}
