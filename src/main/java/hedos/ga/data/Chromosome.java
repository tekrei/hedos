package hedos.ga.data;

import java.util.Arrays;

public class Chromosome implements Comparable<Chromosome> {
    private final int[] genes;
    private float cost;
    private float turnCost;
    private boolean evaluated;

    public Chromosome(int[] genes) {
        this.genes = genes;
        this.cost = Float.MAX_VALUE;
        this.evaluated = false;
    }

    public Chromosome(int[] genes, float cost) {
        this(genes, cost, 0);
    }

    public Chromosome(int[] genes, float cost, float turnCost) {
        this.genes = genes;
        this.cost = cost;
        this.turnCost = turnCost;
        this.evaluated = true;
    }

    public int[] genes() { return genes; }
    public float cost() { return cost; }
    public float turnCost() { return turnCost; }
    public boolean isEvaluated() { return evaluated; }

    public void setCost(float cost) {
        this.cost = cost;
        this.evaluated = true;
    }

    public void setTurnCost(float turnCost) {
        this.turnCost = turnCost;
    }

    @Override
    public String toString() {
        return String.format("[Cost: %.2f, TurnCost: %.2f] %s", cost, turnCost, Arrays.toString(genes));
    }
    @Override
    public int compareTo(Chromosome o) {
        int cmp = Float.compare(this.cost, o.cost());
        if (cmp != 0) return cmp;
        int turnCmp = Float.compare(this.turnCost, o.turnCost());
        if (turnCmp != 0) return turnCmp;
        return Arrays.compare(this.genes, o.genes());
    }
}
