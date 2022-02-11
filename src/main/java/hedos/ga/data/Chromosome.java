package hedos.ga.data;

import java.util.Arrays;

public class Chromosome implements Comparable<Chromosome> {
    private int[] genes;
    private float cost;

    public Chromosome(int[] _genes, float _cost) {
        genes = _genes;
        cost = _cost;
    }

    public int[] getGenes() {
        return genes;
    }

    public void setGenes(int[] _genes, float _cost) {
        this.genes = _genes;
        this.cost = _cost;
    }

    public float getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", getCost(), Arrays.toString(genes));
    }

    public void swap(int first, int second) {
        int temp = genes[first];
        genes[first] = genes[second];
        genes[second] = temp;
    }

    @Override
    public int compareTo(Chromosome o) {
        if (o != null) {
            return (int) (getCost() - o.getCost());
        }
        return -1;
    }
}
