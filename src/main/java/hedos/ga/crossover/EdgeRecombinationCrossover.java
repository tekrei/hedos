package hedos.ga.crossover;

import hedos.ga.data.Chromosome;
import java.util.*;
import java.util.random.RandomGenerator;

/**
 * Edge Recombination Crossover (ERX).
 * Highly effective for TSP as it preserves edges from parents.
 */
public class EdgeRecombinationCrossover extends Crossover {
    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    Chromosome[] reproduce(Chromosome p1, Chromosome p2) {
        return new Chromosome[]{
            new Chromosome(erx(p1.genes(), p2.genes())),
            new Chromosome(erx(p2.genes(), p1.genes()))
        };
    }

    private int[] erx(int[] p1, int[] p2) {
        int n = p1.length;
        Map<Integer, Set<Integer>> edgeTable = new HashMap<>();
        
        // Build Edge Table
        for (int i = 0; i < n; i++) {
            edgeTable.put(i, new HashSet<>());
        }
        
        buildEdges(edgeTable, p1);
        buildEdges(edgeTable, p2);

        int[] child = new int[n];
        boolean[] visited = new boolean[n];
        int current = p1[0];

        for (int i = 0; i < n; i++) {
            child[i] = current;
            visited[current] = true;
            removeFromTable(edgeTable, current);

            Set<Integer> neighbors = edgeTable.get(current);
            int next;
            if (!neighbors.isEmpty()) {
                next = findBestNeighbor(neighbors, edgeTable);
            } else {
                next = findRandomUnvisited(visited);
            }
            current = next;
        }
        return child;
    }

    private void buildEdges(Map<Integer, Set<Integer>> table, int[] parent) {
        int n = parent.length;
        for (int i = 0; i < n; i++) {
            int prev = parent[(i - 1 + n) % n];
            int next = parent[(i + 1) % n];
            table.get(parent[i]).add(prev);
            table.get(parent[i]).add(next);
        }
    }

    private void removeFromTable(Map<Integer, Set<Integer>> table, int city) {
        for (Set<Integer> neighbors : table.values()) {
            neighbors.remove(city);
        }
    }

    private int findBestNeighbor(Set<Integer> neighbors, Map<Integer, Set<Integer>> table) {
        int best = -1;
        int minSize = Integer.MAX_VALUE;
        
        List<Integer> candidates = new ArrayList<>(neighbors);
        Collections.shuffle(candidates); // Tie-breaker

        for (int neighbor : candidates) {
            int size = table.get(neighbor).size();
            if (size < minSize) {
                minSize = size;
                best = neighbor;
            }
        }
        return best;
    }

    private int findRandomUnvisited(boolean[] visited) {
        List<Integer> unvisited = new ArrayList<>();
        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) unvisited.add(i);
        }
        if (unvisited.isEmpty()) return 0;
        return unvisited.get(random.nextInt(unvisited.size()));
    }

    @Override
    public String getNameKey() { return "GA.Crossover.ERX"; }
}