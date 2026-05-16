package hedos.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hedos.utility.EventBus;
import hedos.utility.MessageKeys;
import hedos.utility.Messages;

public class GenerateRandomTargets extends JDialog {
    private static final long serialVersionUID = 1L;
    private final Random generator = new Random(System.currentTimeMillis());
    private JTextField txtTargetCount = null;
    private JButton btnGenerate = null;
    private final EventBus eventBus;
    private final Messages messages;

    public GenerateRandomTargets(Frame owner, EventBus eventBus, Messages messages) {
        super(owner, messages.getString(MessageKeys.GENERATOR_TITLE), true);
        this.eventBus = eventBus;
        this.messages = messages;
        initialize();
    }

    private void initialize() {
        JLabel jLabel = new JLabel();
        jLabel.setText(messages.getString(MessageKeys.GENERATOR_TARGET_COUNT));
        this.getContentPane().setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(360, 100));
        this.getContentPane().add(jLabel, java.awt.BorderLayout.WEST);
        this.getContentPane().add(getTxtTargetCount(),
                java.awt.BorderLayout.CENTER);
        this.getContentPane().add(getBtnGenerate(), java.awt.BorderLayout.SOUTH);
    }

    private JTextField getTxtTargetCount() {
        if (txtTargetCount == null) {
            txtTargetCount = new JTextField();
        }

        return txtTargetCount;
    }

    private JButton getBtnGenerate() {
        if (btnGenerate == null) {
            btnGenerate = new JButton();
            btnGenerate.setText(messages.getString(MessageKeys.GENERATOR_BTN_GENERATE));
            btnGenerate.addActionListener(e -> generate());
        }

        return btnGenerate;
    }

    private void generate() {
        try {
            int targetCount = Integer.parseInt(txtTargetCount.getText());
            
            // Create the structured data for YAML
            Map<String, Object> config = new LinkedHashMap<>();
            
            // Set a default Start Point
            Map<String, Double> startPoint = new HashMap<>();
            startPoint.put("x", 0.0);
            startPoint.put("y", 0.0);
            startPoint.put("z", 0.0);
            config.put(MessageKeys.SETTING_START_POINT, startPoint);

            // Generate random targets
            List<Map<String, Integer>> targetList = new ArrayList<>();
            for (int i = 0; i < targetCount; i++) {
                Map<String, Integer> pt = new HashMap<>();
                pt.put("x", generator.nextInt(100));
                pt.put("y", generator.nextInt(100));
                pt.put("z", generator.nextInt(100));
                targetList.add(pt);
            }
            config.put("targets", targetList);

            // Write using Jackson for proper YAML formatting
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            File outputFile = new File("targets_" + targetCount + ".yaml");
            mapper.writeValue(outputFile, config);
            
            // Decoupled trigger: publish a request instead of calling settings directly
            eventBus.publish(new EventBus.LoadSettingsRequest(outputFile));

            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_SUCCESS) + outputFile.getAbsolutePath());
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_ERROR) + e.getMessage(), messages.getString(MessageKeys.GENERATOR_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }
} // @jve:decl-index=0:visual-constraint="10,10"
