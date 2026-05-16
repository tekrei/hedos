package hedos.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.random.RandomGenerator;

/**
 * Responsible for generating random target datasets in YAML format.
 */
@Singleton
public class TargetGenerator {
    private final ObjectMapper mapper = new YAMLMapper();
    private final RandomGenerator generator = RandomGenerator.getDefault();
    private final EventBus eventBus;

    @Inject
    public TargetGenerator(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Generates random targets and saves them to a YAML file, then triggers a settings load request.
     */
    public void generateRandomTargetsFile(int targetCount, File outputFile) throws IOException {
        Map<String, Object> configMap = new LinkedHashMap<>();

        // Set a default Start Point
        Map<String, Double> startPoint = new HashMap<>();
        startPoint.put("x", 0.0);
        startPoint.put("y", 0.0);
        startPoint.put("z", 0.0);
        configMap.put(MessageKeys.SETTING_START_POINT, startPoint);

        // Generate random targets
        List<Map<String, Integer>> targetList = new ArrayList<>();
        for (int i = 0; i < targetCount; i++) {
            Map<String, Integer> pt = new HashMap<>();
            pt.put("x", generator.nextInt(100));
            pt.put("y", generator.nextInt(100));
            pt.put("z", generator.nextInt(100));
            targetList.add(pt);
        }
        configMap.put("targets", targetList);

        mapper.writeValue(outputFile, configMap);
        // Request the system to load the newly generated file
        eventBus.publish(new EventBus.LoadSettingsRequest(outputFile));
    }
}