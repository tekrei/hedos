package hedos.utility;

/**
 * Centralized constants for settings and localization keys.
 */
public final class MessageKeys {
    private MessageKeys() {}

    // --- Settings File Keys (settings.properties) ---
    public static final String SETTING_START_POINT = "StartPoint";
    public static final String SETTING_TARGET_COUNT = "TargetCount";
    public static final String SETTING_TARGET_PREFIX = "Target";
    public static final String SETTING_COORD_SEPARATOR = "CoordinateSeparator";
    
    public static final String PARAM_GEN_COUNT = "generationCount";
    public static final String PARAM_POP_SIZE = "populationSize";
    public static final String PARAM_MUT_PROB = "mutationProbability";
    public static final String PARAM_ELITISM = "elitism";
    public static final String PARAM_CROSS_PROB = "crossoverProbability";
    public static final String PARAM_MUT_TYPE = "mutationType";
    public static final String PARAM_CROSS_TYPE = "crossoverType";
    public static final String PARAM_SEL_TYPE = "selectionType";
    public static final String PARAM_STAGNATION_TYPE = "stagnationType";
    public static final String PARAM_ELITISM_TYPE = "elitismType";
    public static final String PARAM_TOUR_SIZE = "tournamentSize";
    public static final String PARAM_TURN_PENALTY = "turnPenaltyFactor";
    public static final String PARAM_EVAL_TIMEOUT = "evaluationTimeout";

    // --- Localization Keys (messages.properties) ---
    public static final String HEDOS_FRAME_FILE = "HedosFrame.File";
    public static final String HEDOS_FRAME_SAVE_SETTINGS = "HedosFrame.SaveSettings";
    public static final String HEDOS_FRAME_LOAD_SETTINGS = "HedosFrame.LoadSettings";
    public static final String HEDOS_FRAME_SAVE_SETTINGS_AS = "HedosFrame.SaveSettingsAs";
    public static final String HEDOS_FRAME_SAVE_RESULTS = "HedosFrame.SaveResults";
    public static final String HEDOS_FRAME_SAVE_CHART = "HedosFrame.SaveChart";
    public static final String HEDOS_FRAME_EXIT = "HedosFrame.Exit";
    public static final String HEDOS_FRAME_SETTINGS = "HedosFrame.Settings";
    public static final String HEDOS_FRAME_MULTIPLE_TEST = "HedosFrame.MultipleTest";
    public static final String HEDOS_FRAME_MANAGE_TARGETS = "HedosFrame.ManageTargets";
    public static final String HEDOS_FRAME_GENERATE_TARGETS = "HedosFrame.GenerateTargets";
    public static final String HEDOS_FRAME_LANGUAGE = "HedosFrame.Language";

    public static final String STATUS_BAR_READY = "StatusBar.Ready";
    public static final String STATUS_BAR_GENERATION = "StatusBar.Generation";
    public static final String STATUS_BAR_BEST_FITNESS = "StatusBar.BestFitness";

    public static final String LOG_CALC_START = "Log.CalcStart";
    public static final String LOG_CALC_COMPLETE = "Log.CalcComplete";
    public static final String LOG_TRIALS_START = "Log.TrialsStart";
    public static final String LOG_TRIAL_START = "Log.TrialStart";
    public static final String LOG_TRIAL_FINISH = "Log.TrialFinish";
    public static final String LOG_TRIALS_COMPLETE = "Log.TrialsComplete";
    public static final String LOG_INVALID_COUNT = "Log.InvalidCount";

    public static final String SIDE_PANEL_GA_SETTINGS = "SidePanel.GASettings";
    public static final String SIDE_PANEL_POPULATION_SIZE = "SidePanel.PopulationSize";
    public static final String SIDE_PANEL_GENERATION_COUNT = "SidePanel.GenerationCount";
    public static final String SIDE_PANEL_CROSSOVER_PROBABILITY = "SidePanel.CrossoverProbability";
    public static final String SIDE_PANEL_MUTATION_PROBABILITY = "SidePanel.MutationProbability";
    public static final String SIDE_PANEL_TURN_PENALTY = "SidePanel.TurnPenalty";
    public static final String SIDE_PANEL_TOURNAMENT_SIZE = "SidePanel.TournamentSize";
    public static final String SIDE_PANEL_STAGNATION_STRATEGY = "SidePanel.StagnationStrategy";
    public static final String SIDE_PANEL_ELITISM_STRATEGY = "SidePanel.ElitismStrategy";
    public static final String SIDE_PANEL_ELITISM = "SidePanel.Elitism";
    public static final String SIDE_PANEL_LOCAL_OPTIMIZATION = "SidePanel.LocalOptimization";
    public static final String SIDE_PANEL_MUTATION_TYPE = "SidePanel.MutationType";
    public static final String SIDE_PANEL_SELECTION_TYPE = "SidePanel.SelectionType";
    public static final String SIDE_PANEL_CROSSOVER_TYPE = "SidePanel.CrossoverType";
    public static final String SIDE_PANEL_ACTIONS = "SidePanel.Actions";
    public static final String SIDE_PANEL_SOLVE = "SidePanel.Solve";
    public static final String SIDE_PANEL_CLEAR_SOLUTION = "SidePanel.ClearSolution";
    public static final String SIDE_PANEL_TRAVEL = "SidePanel.Travel";
    public static final String SIDE_PANEL_RESET_DEFAULTS = "SidePanel.ResetDefaults";
    public static final String SIDE_PANEL_SOLVE_TOOLTIP = "SidePanel.Solve.Tooltip";
    public static final String SIDE_PANEL_TRAVEL_TOOLTIP = "SidePanel.Travel.Tooltip";
    public static final String SIDE_PANEL_CLEAR_SOLUTION_TOOLTIP = "SidePanel.ClearSolution.Tooltip";
    public static final String HEDOS_FRAME_MULTIPLE_TEST_TOOLTIP = "HedosFrame.MultipleTest.Tooltip";

    public static final String GENERATOR_TITLE = "Generator.Title";
    public static final String GENERATOR_TARGET_COUNT = "Generator.TargetCount";
    public static final String GENERATOR_BTN_GENERATE = "Generator.BtnGenerate";
    public static final String GENERATOR_SUCCESS = "Generator.Success";
    public static final String GENERATOR_ERROR = "Generator.Error";
    public static final String GENERATOR_ERROR_TITLE = "Generator.ErrorTitle";
    public static final String GENERATOR_INVALID_COUNT = "Generator.InvalidCount";

    public static final String SOL_PANEL_TARGET_MANAGEMENT = "SolPanel.TargetManagement";
    public static final String SOL_PANEL_ADD = "SolPanel.Add";
    public static final String SOL_PANEL_DELETE = "SolPanel.Delete";
    public static final String SIDE_PANEL_CROSSOVER_TOOLTIP = "SidePanel.CrossoverProbability.Tooltip";
    public static final String SIDE_PANEL_MUTATION_TOOLTIP = "SidePanel.MutationProbability.Tooltip";
    public static final String SIDE_PANEL_TURN_PENALTY_TOOLTIP = "SidePanel.TurnPenalty.Tooltip";

    public static final String TARGET_MGMT_X = "TargetMgmt.X";
    public static final String TARGET_MGMT_Y = "TargetMgmt.Y";
    public static final String TARGET_MGMT_Z = "TargetMgmt.Z";
    public static final String TARGET_MGMT_VALIDATION_ERROR_MSG = "TargetMgmt.ValidationError.Msg";
    public static final String TARGET_MGMT_VALIDATION_ERROR_TITLE = "TargetMgmt.ValidationError.Title";
    public static final String TARGET_MGMT_INPUT_ERROR_MSG = "TargetMgmt.InputError.Msg";
    public static final String TARGET_MGMT_INPUT_ERROR_TITLE = "TargetMgmt.InputError.Title";

    public static final String DIALOG_NO_TARGETS_MSG = "Dialog.NoTargets.Msg";
    public static final String DIALOG_NO_TARGETS_TITLE = "Dialog.NoTargets.Title";

    public static final String CROSSOVER_SINGLE_POINT = "GA.Crossover.SinglePoint";
    public static final String CROSSOVER_TWO_POINT = "GA.Crossover.TwoPoint";
    public static final String CROSSOVER_UNIFORM = "GA.Crossover.Uniform";
    public static final String CROSSOVER_ORDERED = "GA.Crossover.Ordered";
    public static final String CROSSOVER_VECTORIZED_UNIFORM = "GA.Crossover.VectorizedUniform";
    public static final String CROSSOVER_PMX = "GA.Crossover.PMX";
    public static final String CROSSOVER_CYCLE = "GA.Crossover.Cycle";
    public static final String CROSSOVER_ERX = "GA.Crossover.ERX";
    public static final String MUTATION_RANDOM = "GA.Mutation.Random";
    public static final String MUTATION_ONLY_IMPROVING_RANDOM = "GA.Mutation.OnlyImprovingRandom";
    public static final String MUTATION_ONLY_IMPROVING_SYSTEMATIC = "GA.Mutation.OnlyImprovingSystematic";
    public static final String MUTATION_VECTORIZED_SCRAMBLE = "GA.Mutation.VectorizedScramble";
    public static final String MUTATION_INVERSION = "GA.Mutation.Inversion";
    public static final String MUTATION_DISPLACEMENT = "GA.Mutation.Displacement";

    public static final String STAGNATION_SIMPLE = "GA.Stagnation.Simple";
    public static final String STAGNATION_ANNEALING = "GA.Stagnation.Annealing";
    public static final String ELITISM_NONE = "GA.Elitism.None";
    public static final String ELITISM_DEFAULT = "GA.Elitism.Default";

    public static final String SELECTION_TOURNAMENT = "GA.Selection.Tournament";
    public static final String SELECTION_ROULETTE_WHEEL = "GA.Selection.RouletteWheel";
    public static final String SELECTION_SUS = "GA.Selection.SUS";

    public static final String LOCAL_OPT_NONE = "GA.LocalOpt.None";
    public static final String LOCAL_OPT_TWO_OPT = "GA.LocalOpt.2Opt";
    public static final String LOCAL_OPT_BEST_TWO_OPT = "GA.LocalOpt.Best2Opt";
    public static final String LOCAL_OPT_THREE_OPT = "GA.LocalOpt.3Opt";
    public static final String LOCAL_OPT_BEST_THREE_OPT = "GA.LocalOpt.Best3Opt";
    public static final String LOCAL_OPT_LIMITED_THREE_OPT = "GA.LocalOpt.Limited3Opt";
    public static final String LOCAL_OPT_PARTITIONED_2_OPT = "GA.LocalOpt.Partitioned2Opt";
    public static final String LOCAL_OPT_PARTITIONED_3_OPT = "GA.LocalOpt.Partitioned3Opt";
    public static final String LOCAL_OPT_LIN_KERNIGHAN = "GA.LocalOpt.LinKernighan";
    public static final String LOCAL_OPT_MULTI_START_LK = "GA.LocalOpt.MultiStartLK";

    // Tooltips for PropertiesPanel
    public static final String SIDE_PANEL_CROSSOVER_PROBABILITY_TOOLTIP = "SidePanel.CrossoverProbability.Tooltip";
    public static final String SIDE_PANEL_MUTATION_PROBABILITY_TOOLTIP = "SidePanel.MutationProbability.Tooltip";
}