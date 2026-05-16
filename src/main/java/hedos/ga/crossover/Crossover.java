package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Crossover {
    private static final Logger logger = LoggerFactory.getLogger(Crossover.class);

    public Chromosome[] crossover(Chromosome[] population, GAParameters params) {
        for (int i = 0; i < population.length; i = i + 2) {
            try {
                if (params.shouldCrossover()) {
                    Chromosome[] offspring = reproduce(population[i], population[i + 1]);
                    population[i] = offspring[0];
                    population[i + 1] = offspring[1];
                }
            } catch (Exception e) {
                logger.error("Crossover operation failed: {}", e.getMessage(), e);
            }
        }
        return population;
    }

    abstract Chromosome[] reproduce(Chromosome p1, Chromosome p2);

    public abstract String getNameKey();

    int[] fixGenes(int[] genes) {
        int size = genes.length;
        Set<Integer> presentGenes = new HashSet<>();
        List<Integer> missingGenes = new ArrayList<>();
        int[] fixedGenes = new int[size];

        // First pass: Identify duplicates and mark positions to be filled
        for (int i = 0; i < size; i++) {
            int gene = genes[i];
            // Check if gene is within valid range and not already present
            if (gene >= 0 && gene < size && !presentGenes.contains(gene)) {
                presentGenes.add(gene);
                fixedGenes[i] = gene;
            } else {
                fixedGenes[i] = -1; // Mark as a position that needs a new gene
            }
        }

        // Identify all genes that are missing from the permutation (0 to size-1)
        for (int i = 0; i < size; i++) {
            if (!presentGenes.contains(i)) {
                missingGenes.add(i);
            }
        }

        // Second pass: Fill marked positions with missing genes
        int missingIdx = 0;
        for (int i = 0; i < size; i++) {
            if (fixedGenes[i] == -1) {
                if (missingIdx < missingGenes.size()) {
                    fixedGenes[i] = missingGenes.get(missingIdx++);
                } else {
                    // This scenario indicates a severe problem (more duplicates than missing genes)
                    // For robustness, assign a default or throw an error.
                    // For TSP, it's critical to have a valid permutation.
                    logger.warn("Not enough missing genes to fill duplicate positions. Permutation integrity may be compromised.");
                    fixedGenes[i] = 0; // Fallback, might still be a duplicate
                }
            }
        }
        return fixedGenes;
    }
}
