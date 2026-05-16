package hedos.ga.mutation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters.MutationType;
import java.util.Map;

@Singleton
public class MutationFactory {
    private final Map<MutationType, Mutation> operators;

    @Inject
    public MutationFactory(Map<MutationType, Mutation> operators) {
        this.operators = operators;
    }

    public Mutation get(MutationType type) {
        return operators.getOrDefault(type, operators.get(MutationType.RANDOM));
    }
}