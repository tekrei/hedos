package net.tekrei.hedos.ga.utilities;

import java.util.Random;
import java.util.stream.IntStream;

public class Chromosome {
    private int[] genes;
    private float cost;

    public Chromosome() {
        initialize(GAParameters.getInstance().getHedefSayi());
        randomize();
        calculateCost();
    }

    public static float calculateCost(int[] genes) {
        float cost = 0.0f;

        for (int i = 1; i < genes.length; i++) {
            cost += GAParameters.getInstance().getDistance(genes[i],
                    genes[i - 1]);
        }

        return cost;
    }

    private void initialize(int size) {
        genes = new int[size];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = -1;
        }

    }

    public int[] getGenes() {
        return genes;
    }

    public void setGenes(int[] genes) {
        this.genes = genes;
        calculateCost();
    }

    private boolean contains(int[] array, int gene) {
        return IntStream.of(array).anyMatch(x -> x == gene);
    }

    private void randomize() {
        Random generator = new Random();

        for (int i = 0; i < genes.length; i++) {
            int uretilen = generator.nextInt(genes.length);

            while (contains(genes, uretilen)) {
                uretilen = generator.nextInt(genes.length);
            }

            genes[i] = uretilen;
        }
    }

    public float getCost() {
        return cost;
    }

    private void calculateCost() {
        cost = 0f;

        for (int i = 1; i < genes.length; i++) {
            cost += GAParameters.getInstance().getDistance(genes[i],
                    genes[i - 1]);
        }

    }

    @Override
    public String toString() {
        return String.format("%s", getCost());
    }

    public void swap(int first, int second) {
        int temp = genes[first];
        genes[first] = genes[second];
        genes[second] = temp;
    }
}
