package hedos.utility;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A simple publish-subscribe event bus to decouple application components.
 */
@Singleton
public class EventBus {
    private final Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();

    public record SettingsChangedEvent() {}
    public record LoadSettingsRequest(java.io.File file) {}
    public record LocaleChangedEvent(String newLocale) {}

    public synchronized <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(obj -> listener.accept(eventType.cast(obj)));
    }

    public synchronized void publish(Object event) {
        List<Consumer<Object>> targets = listeners.get(event.getClass());
        if (targets != null) {
            for (Consumer<Object> listener : new ArrayList<>(targets)) {
                listener.accept(event);
            }
        }
    }
}