package hedos.utility;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import hedos.ga.crossover.*;
import hedos.ga.data.*;
import hedos.ga.mutation.*;
import hedos.ga.selection.*;

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
        bind(TargetGenerator.class).in(Scopes.SINGLETON);

        // Bind CostCalculator to its implementation
        bind(CostCalculator.class).to(TSPCostCalculator.class).in(Scopes.SINGLETON);

        // Configure Crossover MapBinder for CrossoverFactory
        MapBinder<GAParameters.CrossoverType, Crossover> crossoverBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.CrossoverType.class, Crossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.SINGLE_POINT).to(SinglePointCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.TWO_POINT).to(TwoPointCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.UNIFORM).to(UniformCrossover.class);
        crossoverBinder.addBinding(GAParameters.CrossoverType.ORDERED).to(OrderedCrossover.class);

        // Configure Mutation MapBinder for MutationFactory
        MapBinder<GAParameters.MutationType, Mutation> mutationBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.MutationType.class, Mutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.RANDOM).to(RandomMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.ONLY_IMPROVING_RANDOM).to(OnlyImprovingRandomMutation.class);
        mutationBinder.addBinding(GAParameters.MutationType.ONLY_IMPROVING_SYSTEMATIC).to(OnlyImprovingSystematicMutation.class);

        // Configure Selection MapBinder for SelectionFactory
        MapBinder<GAParameters.SelectionType, Selection> selectionBinder = 
            MapBinder.newMapBinder(binder(), GAParameters.SelectionType.class, Selection.class);
        selectionBinder.addBinding(GAParameters.SelectionType.TOURNAMENT).to(TournamentSelection.class);
        selectionBinder.addBinding(GAParameters.SelectionType.ROULETTE_WHEEL).to(RouletteWheelSelection.class);
    }
}