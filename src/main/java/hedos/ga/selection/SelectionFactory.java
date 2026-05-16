package hedos.ga.selection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters.SelectionType;
import java.util.Map;

@Singleton
public class SelectionFactory {
    private final Map<SelectionType, Selection> operators;

    @Inject
    public SelectionFactory(Map<SelectionType, Selection> operators) {
        this.operators = operators;
    }

    public Selection get(SelectionType type) {
        return operators.getOrDefault(type, operators.get(SelectionType.TOURNAMENT));
    }
}