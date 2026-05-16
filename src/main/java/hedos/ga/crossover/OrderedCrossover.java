package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import hedos.utility.MessageKeys;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class OrderedCrossover extends Crossover {
    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        return new Chromosome[] {
            createChild(p1, p2),
            createChild(p2, p1)
        };
    }

    private Chromosome createChild(Chromosome p1, Chromosome p2) {
        int size = p1.genes().length;
        int[] childGenes = new int[size];
        if (size < 2) {
            return new Chromosome(p1.genes().clone());
        }

        Arrays.fill(childGenes, -1);

        int start = ThreadLocalRandom.current().nextInt(size);
        int end = ThreadLocalRandom.current().nextInt(size);

        int cut1 = Math.min(start, end);
        int cut2 = Math.max(start, end);

        // Copy segment from P1
        for (int i = cut1; i <= cut2; i++) {
            childGenes[i] = p1.genes()[i];
        }

        // Fill remaining from P2
        int currentPos = (cut2 + 1) % size;
        for (int i = 0; i < size; i++) {
            int p2Pos = (cut2 + 1 + i) % size;
            int gene = p2.genes()[p2Pos];

            if (!contains(childGenes, gene)) {
                childGenes[currentPos] = gene;
                currentPos = (currentPos + 1) % size;
            }
        }

        return new Chromosome(childGenes);
    }

    @Override
    public String getNameKey() {
        return MessageKeys.CROSSOVER_ORDERED;
    }

    private boolean contains(int[] array, int gene) {
        for (int x : array) {
            if (x == gene) {
                return true;
            }
        }
        return false;
    }
}