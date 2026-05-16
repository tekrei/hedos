package hedos.ga.elitism;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import hedos.ga.data.GAParameters;
import java.util.Map;

/**
 * Factory for retrieving elitism strategies based on user selection.
 */
@Singleton
public class ElitismHandlerFactory {
    private final Map<GAParameters.ElitismType, Provider<ElitismHandler>> providers;

    @Inject
    public ElitismHandlerFactory(Map<GAParameters.ElitismType, Provider<ElitismHandler>> providers) {
        this.providers = providers;
    }

    public ElitismHandler get(GAParameters.ElitismType type) {
        Provider<ElitismHandler> provider = providers.get(type);
        if (provider == null) {
            // Fallback to default if type is null or binding is missing
            provider = providers.get(GAParameters.ElitismType.DEFAULT);
        }
        return provider.get();
    }
}