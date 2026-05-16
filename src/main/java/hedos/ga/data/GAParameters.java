package hedos.ga.data;

import hedos.utility.Settings;
import hedos.utility.MessageKeys;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Random;

@Singleton
public class GAParameters {

    public enum CrossoverType {
        SINGLE_POINT(MessageKeys.CROSSOVER_SINGLE_POINT),
        TWO_POINT(MessageKeys.CROSSOVER_TWO_POINT),
        UNIFORM(MessageKeys.CROSSOVER_UNIFORM),
        ORDERED(MessageKeys.CROSSOVER_ORDERED);

        private final String nameKey;
        CrossoverType(String nameKey) { this.nameKey = nameKey; }
        public String getNameKey() { return nameKey; }
        public static CrossoverType fromKey(String key) {
            for (CrossoverType t : values()) {
                if (t.nameKey.equals(key)) return t;
            }
            return SINGLE_POINT;
        }
    }

    public enum MutationType {
        RANDOM(MessageKeys.MUTATION_RANDOM),
        ONLY_IMPROVING_RANDOM(MessageKeys.MUTATION_ONLY_IMPROVING_RANDOM),
        ONLY_IMPROVING_SYSTEMATIC(MessageKeys.MUTATION_ONLY_IMPROVING_SYSTEMATIC);

        private final String nameKey;
        MutationType(String nameKey) { this.nameKey = nameKey; }
        public String getNameKey() { return nameKey; }
        public static MutationType fromKey(String key) {
            for (MutationType t : values()) {
                if (t.nameKey.equals(key)) return t;
            }
            return RANDOM;
        }
    }

    public enum SelectionType {
        TOURNAMENT(MessageKeys.SELECTION_TOURNAMENT),
        ROULETTE_WHEEL(MessageKeys.SELECTION_ROULETTE_WHEEL);

        private final String nameKey;
        SelectionType(String nameKey) { this.nameKey = nameKey; }
        public String getNameKey() { return nameKey; }

        public static SelectionType fromKey(String key) {
            for (SelectionType t : values()) {
                if (t.nameKey.equals(key)) return t;
            }
            return TOURNAMENT;
        }
    }

    private final Settings settings;
    private final Random generator;
    private int generationCount;
    private int populationSize;
    private float mutationProbability;
    private boolean elitism;
    private float crossoverProbability;
    private MutationType mutationType;
    private CrossoverType crossoverType;
    private SelectionType selectionType;
    private int tournamentSize;
    private float turnPenaltyFactor;
    private long evaluationTimeout; // Timeout in milliseconds

    @Inject
    public GAParameters(Settings settings) {
        this.settings = settings;
        generator = new Random();
        load(settings);
    }

    private void load(Settings settings) {
        this.generationCount = settings.getInt(MessageKeys.PARAM_GEN_COUNT, 100);
        this.populationSize = settings.getInt(MessageKeys.PARAM_POP_SIZE, 50);
        this.mutationProbability = settings.getFloat(MessageKeys.PARAM_MUT_PROB, 0.05f);
        this.elitism = settings.getBoolean(MessageKeys.PARAM_ELITISM, false);
        this.crossoverProbability = settings.getFloat(MessageKeys.PARAM_CROSS_PROB, 0.8f);

        this.mutationType = MutationType.fromKey(settings.getString(MessageKeys.PARAM_MUT_TYPE));
        this.crossoverType = CrossoverType.fromKey(settings.getString(MessageKeys.PARAM_CROSS_TYPE));
        this.selectionType = SelectionType.fromKey(settings.getString(MessageKeys.PARAM_SEL_TYPE));

        this.tournamentSize = settings.getInt(MessageKeys.PARAM_TOUR_SIZE, 3);
        this.turnPenaltyFactor = settings.getFloat(MessageKeys.PARAM_TURN_PENALTY, 50.0f);
        this.evaluationTimeout = settings.getInt("evaluationTimeout", 5000);
    }

    public void saveToSettings() {
        settings.set(MessageKeys.PARAM_GEN_COUNT, generationCount);
        settings.set(MessageKeys.PARAM_POP_SIZE, populationSize);
        settings.set(MessageKeys.PARAM_MUT_PROB, mutationProbability);
        settings.set(MessageKeys.PARAM_ELITISM, elitism);
        settings.set(MessageKeys.PARAM_CROSS_PROB, crossoverProbability);
        settings.set(MessageKeys.PARAM_MUT_TYPE, mutationType.getNameKey());
        settings.set(MessageKeys.PARAM_CROSS_TYPE, crossoverType.getNameKey());
        settings.set(MessageKeys.PARAM_SEL_TYPE, selectionType.getNameKey());
        settings.set(MessageKeys.PARAM_TOUR_SIZE, tournamentSize);
        settings.set(MessageKeys.PARAM_TURN_PENALTY, turnPenaltyFactor);
        settings.set("evaluationTimeout", evaluationTimeout);
    }

    public void resetToDefaults() {
        load(this.settings);
    }

    // --- Getters and Setters ---

    public int getGenerationCount() { return generationCount; }
    public void setGenerationCount(int generationCount) { this.generationCount = generationCount; }

    public int getPopulationSize() { return populationSize; }
    public void setPopulationSize(int populationSize) { this.populationSize = populationSize; }

    public float getMutationProbability() { return mutationProbability; }
    public void setMutationProbability(float mutationProbability) { this.mutationProbability = mutationProbability; }

    public boolean isElitism() { return elitism; }
    public void setElitism(boolean elitism) { this.elitism = elitism; }

    public float getCrossoverProbability() { return crossoverProbability; }
    public void setCrossoverProbability(float crossoverProbability) { this.crossoverProbability = crossoverProbability; }

    public MutationType getMutationType() { return mutationType; }
    public void setMutationType(MutationType mutationType) { this.mutationType = mutationType; }

    public CrossoverType getCrossoverType() { return crossoverType; }
    public void setCrossoverType(CrossoverType crossoverType) { this.crossoverType = crossoverType; }

    public SelectionType getSelectionType() { return selectionType; }
    public void setSelectionType(SelectionType selectionType) { this.selectionType = selectionType; }

    public int getTournamentSize() { return tournamentSize; }
    public void setTournamentSize(int tournamentSize) { this.tournamentSize = tournamentSize; }

    public float getTurnPenaltyFactor() { return turnPenaltyFactor; }
    public void setTurnPenaltyFactor(float factor) { this.turnPenaltyFactor = factor; }

    public long getEvaluationTimeout() { return evaluationTimeout; }
    public void setEvaluationTimeout(long timeout) { this.evaluationTimeout = timeout; }

    // --- Logic Methods ---

    public boolean shouldCrossover() {
        return nextFloat() < crossoverProbability;
    }

    public boolean shouldMutate() {
        return nextFloat() < mutationProbability;
    }

    public float nextFloat() {
        return generator.nextFloat();
    }

    public int nextInt(int bound) {
        return generator.nextInt(bound);
    }
}
