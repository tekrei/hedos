package hedos.ga.stagnation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters;
import java.util.Map;

/**
 * Factory for retrieving stagnation handling strategies based on user selection.
 */
@Singleton
public class StagnationHandlerFactory {
    private final Map<GAParameters.StagnationType, Provider<StagnationHandler>> providers;

    @Inject
    public StagnationHandlerFactory(Map<GAParameters.StagnationType, Provider<StagnationHandler>> providers) {
        this.providers = providers;
    }

    public StagnationHandler get(GAParameters.StagnationType type) {
        Provider<StagnationHandler> provider = providers.get(type);
        if (provider == null) {
            // Fallback to SIMPLE if type is null or binding is missing
            provider = providers.get(GAParameters.StagnationType.SIMPLE);
        }
        return provider.get();
    }
}