package hedos.ga.data;

import hedos.utility.Settings;
import hedos.utility.MessageKeys;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.random.RandomGenerator;

@Singleton
public class GAParameters {

    /**
     * Java 21 Scoped Value for passing parameters through evaluation scopes.
     */
    public static final ScopedValue<GAParameters> CURRENT = ScopedValue.newInstance();

    public enum CrossoverType {
        SINGLE_POINT(MessageKeys.CROSSOVER_SINGLE_POINT),
        TWO_POINT(MessageKeys.CROSSOVER_TWO_POINT),
        UNIFORM(MessageKeys.CROSSOVER_UNIFORM),
        ORDERED(MessageKeys.CROSSOVER_ORDERED),
        VECTORIZED_UNIFORM(MessageKeys.CROSSOVER_VECTORIZED_UNIFORM), // Already defined
        PMX(MessageKeys.CROSSOVER_PMX),
        CYCLE(MessageKeys.CROSSOVER_CYCLE),
        ERX(MessageKeys.CROSSOVER_ERX);

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
        ONLY_IMPROVING_SYSTEMATIC(MessageKeys.MUTATION_ONLY_IMPROVING_SYSTEMATIC),
        VECTORIZED_SCRAMBLE(MessageKeys.MUTATION_VECTORIZED_SCRAMBLE), // Already defined
        INVERSION(MessageKeys.MUTATION_INVERSION),
        DISPLACEMENT(MessageKeys.MUTATION_DISPLACEMENT);

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
        ROULETTE_WHEEL(MessageKeys.SELECTION_ROULETTE_WHEEL),
        SUS(MessageKeys.SELECTION_SUS);

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

    public enum StagnationType {
        SIMPLE(MessageKeys.STAGNATION_SIMPLE),
        ANNEALING(MessageKeys.STAGNATION_ANNEALING);

        private final String nameKey;
        StagnationType(String nameKey) { this.nameKey = nameKey; }
        public String getNameKey() { return nameKey; }
        public static StagnationType fromKey(String key) {
            for (StagnationType t : values()) if (t.nameKey.equals(key)) return t;
            return SIMPLE;
        }
    }

    public enum ElitismType {
        NONE(MessageKeys.ELITISM_NONE),
        DEFAULT(MessageKeys.ELITISM_DEFAULT);

        private final String nameKey;
        ElitismType(String nameKey) { this.nameKey = nameKey; }
        public String getNameKey() { return nameKey; }
        public static ElitismType fromKey(String key) {
            for (ElitismType t : values()) if (t.nameKey.equals(key)) return t;
            return NONE;
        }
    }

    public enum LocalOptimizationType {
        NONE(MessageKeys.LOCAL_OPT_NONE),
        TWO_OPT(MessageKeys.LOCAL_OPT_TWO_OPT),
        BEST_TWO_OPT(MessageKeys.LOCAL_OPT_BEST_TWO_OPT),
        THREE_OPT(MessageKeys.LOCAL_OPT_THREE_OPT),
        BEST_THREE_OPT(MessageKeys.LOCAL_OPT_BEST_THREE_OPT),
        LIMITED_THREE_OPT(MessageKeys.LOCAL_OPT_LIMITED_THREE_OPT),
        PARTITIONED_2_OPT(MessageKeys.LOCAL_OPT_PARTITIONED_2_OPT),
        PARTITIONED_3_OPT(MessageKeys.LOCAL_OPT_PARTITIONED_3_OPT),
        LIN_KERNIGHAN(MessageKeys.LOCAL_OPT_LIN_KERNIGHAN),
        MULTI_START_LIN_KERNIGHAN(MessageKeys.LOCAL_OPT_MULTI_START_LK);

        private final String nameKey;
        LocalOptimizationType(String nameKey) { this.nameKey = nameKey; }
        public String getNameKey() { return nameKey; }
        public static LocalOptimizationType fromKey(String key) {
            for (LocalOptimizationType t : values()) if (t.nameKey.equals(key)) return t;
            return NONE;
        }
    }

    private final Settings settings;
    private final RandomGenerator generator;
    private int generationCount;
    private int populationSize;
    private float mutationProbability;
    private float crossoverProbability;
    private MutationType mutationType;
    private CrossoverType crossoverType;
    private StagnationType stagnationType;
    private ElitismType elitismType;
    private SelectionType selectionType;
    private LocalOptimizationType localOptimizationType;
    private int tournamentSize;
    private int neighborhoodSize;
    private float turnPenaltyFactor;
    private long evaluationTimeout; // Timeout in milliseconds

    @Inject
    public GAParameters(Settings settings) {
        this.settings = settings;
        generator = RandomGenerator.getDefault();
        load(settings);
    }

    private void load(Settings settings) {
        this.generationCount = settings.getInt(MessageKeys.PARAM_GEN_COUNT, 100);
        this.populationSize = settings.getInt(MessageKeys.PARAM_POP_SIZE, 50);
        this.mutationProbability = settings.getFloat(MessageKeys.PARAM_MUT_PROB, 0.05f);
        this.crossoverProbability = settings.getFloat(MessageKeys.PARAM_CROSS_PROB, 0.8f);

        this.mutationType = MutationType.fromKey(settings.getString(MessageKeys.PARAM_MUT_TYPE));
        this.crossoverType = CrossoverType.fromKey(settings.getString(MessageKeys.PARAM_CROSS_TYPE));
        this.selectionType = SelectionType.fromKey(settings.getString(MessageKeys.PARAM_SEL_TYPE));
        this.stagnationType = StagnationType.fromKey(settings.getString(MessageKeys.PARAM_STAGNATION_TYPE));
        this.elitismType = ElitismType.fromKey(settings.getString(MessageKeys.PARAM_ELITISM_TYPE));
        this.localOptimizationType = LocalOptimizationType.fromKey(settings.getString("localOptimizationType"));

        this.neighborhoodSize = settings.getInt("neighborhoodSize", 20);
        this.tournamentSize = settings.getInt(MessageKeys.PARAM_TOUR_SIZE, 3);
        this.turnPenaltyFactor = settings.getFloat(MessageKeys.PARAM_TURN_PENALTY, 50.0f);
        this.evaluationTimeout = settings.getInt(MessageKeys.PARAM_EVAL_TIMEOUT, 5000);
    }

    public void saveToSettings() {
        settings.set(MessageKeys.PARAM_GEN_COUNT, generationCount);
        settings.set(MessageKeys.PARAM_POP_SIZE, populationSize);
        settings.set(MessageKeys.PARAM_MUT_PROB, mutationProbability);
        settings.set(MessageKeys.PARAM_CROSS_PROB, crossoverProbability);
        settings.set("localOptimizationType", localOptimizationType.getNameKey());
        settings.set(MessageKeys.PARAM_MUT_TYPE, mutationType.getNameKey());
        settings.set(MessageKeys.PARAM_CROSS_TYPE, crossoverType.getNameKey());
        settings.set(MessageKeys.PARAM_SEL_TYPE, selectionType.getNameKey());
        settings.set(MessageKeys.PARAM_STAGNATION_TYPE, stagnationType.getNameKey());
        settings.set(MessageKeys.PARAM_ELITISM_TYPE, elitismType.getNameKey());
        settings.set("neighborhoodSize", neighborhoodSize);
        settings.set(MessageKeys.PARAM_TOUR_SIZE, tournamentSize);
        settings.set(MessageKeys.PARAM_TURN_PENALTY, turnPenaltyFactor);
        settings.set(MessageKeys.PARAM_EVAL_TIMEOUT, evaluationTimeout);
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

    public boolean isElitism() { return elitismType != ElitismType.NONE; }

    public float getCrossoverProbability() { return crossoverProbability; }
    public void setCrossoverProbability(float crossoverProbability) { this.crossoverProbability = crossoverProbability; }

    public MutationType getMutationType() { return mutationType; }
    public void setMutationType(MutationType mutationType) { this.mutationType = mutationType; }

    public CrossoverType getCrossoverType() { return crossoverType; }
    public void setCrossoverType(CrossoverType crossoverType) { this.crossoverType = crossoverType; }

    public SelectionType getSelectionType() { return selectionType; }
    public void setSelectionType(SelectionType selectionType) { this.selectionType = selectionType; }

    public StagnationType getStagnationType() { return stagnationType; }
    public void setStagnationType(StagnationType stagnationType) { this.stagnationType = stagnationType; }

    public ElitismType getElitismType() { return elitismType; }
    public void setElitismType(ElitismType elitismType) { this.elitismType = elitismType; }

    public LocalOptimizationType getLocalOptimizationType() { return localOptimizationType; }
    public void setLocalOptimizationType(LocalOptimizationType type) { this.localOptimizationType = type; }

    public int getTournamentSize() { return tournamentSize; }
    public void setTournamentSize(int tournamentSize) { this.tournamentSize = tournamentSize; }

    public int getNeighborhoodSize() { return neighborhoodSize; }
    public void setNeighborhoodSize(int size) { this.neighborhoodSize = size; }

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
