package hedos.utility;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

import hedos.ga.GeneticAlgorithmService;
import hedos.ga.PopulationEvaluator;
import hedos.ga.cost.CostCalculator;
import hedos.ga.cost.TSPCostCalculator;
import hedos.ga.lso.*;
import hedos.utility.PersistenceService;
import hedos.ga.crossover.*;
import hedos.ga.data.*;
import hedos.ga.mutation.*;
import hedos.ga.selection.*;
import hedos.ui.BenchmarkService;
import hedos.ui.GenerateRandomTargetsDialog;
import hedos.ui.HedosMenu;
import hedos.ui.TargetManagementDialog;

/**
 * Configures Guice bindings for the HeDoS application.
 */
public class HedosModule extends AbstractModule {
    @Override
    protected void configure() {
        // Global Singletons
        bind(EventBus.class).in(Scopes.SINGLETON);
        bind(Messages.class).in(Scopes.SINGLETON);
        bind(Settings.class).in(Scopes.SINGLETON);
        bind(GAParameters.class).in(Scopes.SINGLETON);
        bind(LocalSearchFactory.class).in(Scopes.SINGLETON);
        bind(TargetGenerator.class).in(Scopes.SINGLETON);
        bind(ChromosomeFactory.class).in(Scopes.SINGLETON);
        bind(PopulationEvaluator.class).in(Scopes.SINGLETON);
        bind(PersistenceService.class).in(Scopes.SINGLETON);
        bind(GeneticAlgorithmService.class).in(Scopes.SINGLETON);
        bind(HedosMenu.class).in(Scopes.SINGLETON);
        bind(BenchmarkService.class).in(Scopes.SINGLETON);

        // Bind CostCalculator to its implementation
        bind(CostCalculator.class).to(TSPCostCalculator.class).in(Scopes.SINGLETON);

        // Configure Crossover MapBinder for CrossoverFactory
        MapBinder<GAParameters.CrossoverType, Crossover> crossoverBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.CrossoverType.class, Crossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.SINGLE_POINT).to(SinglePointCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.TWO_POINT).to(TwoPointCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.UNIFORM).to(UniformCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.ORDERED).to(OrderedCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.VECTORIZED_UNIFORM).to(VectorizedUniformCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.ERX).to(EdgeRecombinationCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.PMX).to(PartiallyMappedCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.CYCLE).to(CycleCrossover.class);

        // Local Search MapBinder
        MapBinder<GAParameters.LocalOptimizationType, LocalSearchOptimizer> lsoBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.LocalOptimizationType.class, LocalSearchOptimizer.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.TWO_OPT).to(TwoOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.BEST_TWO_OPT).to(BestTwoOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.THREE_OPT).to(FirstThreeOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.BEST_THREE_OPT).to(BestThreeOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.LIMITED_THREE_OPT).to(FirstThreeOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.PARTITIONED_2_OPT).to(PartitionedTwoOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.PARTITIONED_3_OPT).to(PartitionedThreeOptOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.LIN_KERNIGHAN).to(LinKernighanOptimization.class);
        lsoBinder.addBinding(GAParameters.LocalOptimizationType.MULTI_START_LIN_KERNIGHAN).to(MultiStartLocalSearch.class);

        // Configure Mutation MapBinder for MutationFactory
        MapBinder<GAParameters.MutationType, Mutation> mutationBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.MutationType.class, Mutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.RANDOM).to(RandomMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.ONLY_IMPROVING_RANDOM).to(OnlyImprovingRandomMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.ONLY_IMPROVING_SYSTEMATIC).to(OnlyImprovingSystematicMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.VECTORIZED_SCRAMBLE).to(VectorizedScrambleMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.INVERSION).to(InversionMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.DISPLACEMENT).to(DisplacementMutation.class);

        // Configure Selection MapBinder for SelectionFactory
        MapBinder<GAParameters.SelectionType, Selection> selectionBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.SelectionType.class, Selection.class);
        selectionBinder.addBinding(GAParameters.SelectionType.TOURNAMENT).to(TournamentSelection.class);
        selectionBinder.addBinding(GAParameters.SelectionType.ROULETTE_WHEEL).to(RouletteWheelSelection.class);
        selectionBinder.addBinding(GAParameters.SelectionType.SUS).to(StochasticUniversalSampling.class);


        // Assisted Injection Factories
        install(new FactoryModuleBuilder().build(TargetManagementDialog.Factory.class));
        install(new FactoryModuleBuilder().build(GenerateRandomTargetsDialog.Factory.class));
    }
}