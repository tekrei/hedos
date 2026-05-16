package hedos.utility;

import hedos.ga.data.Point;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class Settings {
    private Map<String, Object> config = new HashMap<>();
    private final ObjectMapper mapper = YAMLMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .build();

    private Point startPoint = null;
    private List<Point> targets = null;
    private final EventBus eventBus;

    @Inject
    public Settings(EventBus eventBus) {
        this.eventBus = eventBus;
        // Load default settings from classpath if available
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("settings.yaml")) {
            if (input != null) {
                config = mapper.readValue(input, new TypeReference<Map<String, Object>>() {});
            }
        } catch (IOException ex) {
            // Fallback for missing default settings
        }

        // Decoupled loading: listen for requests from any component
        this.eventBus.subscribe(EventBus.LoadSettingsRequest.class, req -> load(req.file()));
    }

    public boolean load(File file) {
        try (InputStream input = new FileInputStream(file)) {
            config.clear();
            config = mapper.readValue(input, new TypeReference<Map<String, Object>>() {});
            // Invalidate cached objects when settings change
            startPoint = null;
            targets = null;
            
            eventBus.publish(new EventBus.SettingsChangedEvent());
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void save(File file) {
        try {
            mapper.writeValue(file, config);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getString(String key) {
        Object value = config.get(key);
        return value != null ? value.toString() : "";
    }

    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number n) return n.floatValue();
        try {
            return Float.parseFloat(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean b) return b;
        if (value != null) return Boolean.parseBoolean(value.toString());
        return defaultValue;
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public void setString(String key, String value) {
        set(key, value);
    }

    public Point getStartPoint() {
        if (startPoint == null) {
            Object obj = config.get(MessageKeys.SETTING_START_POINT);
            try {
                startPoint = mapper.convertValue(obj, Point.class);
            } catch (IllegalArgumentException e) {
                startPoint = new Point(0, 0, 0);
            }
        }
        return startPoint;
    }

    public List<Point> getTargets() {
        if (targets == null) {
            targets = new ArrayList<>();
            Object obj = config.get("targets");
            List<Point> loaded = mapper.convertValue(obj, new TypeReference<List<Point>>() {});
            if (loaded != null) targets.addAll(loaded);
        }
        return targets;
    }
}