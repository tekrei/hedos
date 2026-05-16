package hedos.ga.cost;

public interface CostCalculator {
    float calculateCost(int[] genes);
    float calculateTurnCost(int[] genes);
}