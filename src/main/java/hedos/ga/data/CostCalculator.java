package hedos.ga.data;

public interface CostCalculator {
    float calculateCost(int[] genes);
    float calculateTurnCost(int[] genes);
}