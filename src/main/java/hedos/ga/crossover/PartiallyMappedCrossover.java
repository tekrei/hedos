package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import java.util.Arrays;
import java.util.Random;

public class PartiallyMappedCrossover extends Crossover {
    private final Random random = new Random();

    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int size = p1.genes().length;
        int cp1 = random.nextInt(size);
        int cp2 = random.nextInt(size);
        int start = Math.min(cp1, cp2);
        int end = Math.max(cp1, cp2);

        int[] c1 = pmx(p1.genes(), p2.genes(), start, end);
        int[] c2 = pmx(p2.genes(), p1.genes(), start, end);

        return new Chromosome[]{new Chromosome(c1), new Chromosome(c2)};
    }

    private int[] pmx(int[] parent1, int[] parent2, int start, int end) {
        int[] offspring = new int[parent1.length];
        Arrays.fill(offspring, -1);
        System.arraycopy(parent1, start, offspring, start, end - start + 1);

        for (int i = start; i <= end; i++) {
            int gene = parent2[i];
            if (!contains(offspring, gene)) {
                int pos = i;
                while (offspring[pos] != -1) {
                    int val = offspring[pos];
                    for (int j = 0; j < parent2.length; j++) {
                        if (parent2[j] == val) { pos = j; break; }
                    }
                }
                offspring[pos] = gene;
            }
        }

        for (int i = 0; i < parent1.length; i++) {
            if (offspring[i] == -1) offspring[i] = parent2[i];
        }
        return offspring;
    }

    private boolean contains(int[] arr, int val) {
        for (int x : arr) if (x == val) return true;
        return false;
    }

    @Override
    public String getNameKey() { return "GA.Crossover.PMX"; }
}