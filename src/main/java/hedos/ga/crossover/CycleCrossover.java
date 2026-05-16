package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CycleCrossover extends Crossover {
    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        int[] genes1 = p1.genes();
        int[] genes2 = p2.genes();
        int size = genes1.length;

        int[] off1 = new int[size];
        int[] off2 = new int[size];
        Arrays.fill(off1, -1);
        Arrays.fill(off2, -1);

        List<Integer> cycle = new ArrayList<>();
        int startIdx = 0;
        
        while (cycle.size() < size) {
            int idx = -1;
            for (int i = 0; i < size; i++) {
                if (!cycle.contains(i)) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) break;

            List<Integer> currCycle = new ArrayList<>();
            int firstVal = genes1[idx];
            int currVal = -1;
            while (currVal != firstVal) {
                currCycle.add(idx);
                currVal = genes2[idx];
                for (int j = 0; j < size; j++) {
                    if (genes1[j] == currVal) {
                        idx = j;
                        break;
                    }
                }
            }
            cycle.addAll(currCycle);
            for (int i : currCycle) {
                off1[i] = (startIdx % 2 == 0) ? genes1[i] : genes2[i];
                off2[i] = (startIdx % 2 == 0) ? genes2[i] : genes1[i];
            }
            startIdx++;
        }
        return new Chromosome[]{new Chromosome(off1), new Chromosome(off2)};
    }

    @Override
    public String getNameKey() { return "GA.Crossover.Cycle"; }
}