package hedos.ui;

import javax.swing.*;
import java.awt.BorderLayout; // Explicit import
import java.awt.Frame; // Explicit import
import java.io.File;
import java.io.IOException; // Explicit import
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import hedos.HedosFrame;

import hedos.utility.MessageKeys;
import hedos.utility.Messages;
import hedos.utility.TargetGenerator;

public class GenerateRandomTargetsDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField txtTargetCount = null;
    private JButton btnGenerate = null;
    private final Messages messages;
    private final TargetGenerator targetGenerator;

    public interface Factory {
        GenerateRandomTargetsDialog create(HedosFrame owner);
    }

    @AssistedInject
    public GenerateRandomTargetsDialog(@Assisted HedosFrame owner, Messages messages, TargetGenerator targetGenerator) {
        super(owner, messages.getString(MessageKeys.GENERATOR_TITLE), true);
        this.messages = messages;
        this.targetGenerator = targetGenerator;
        initialize();
    }

    private void initialize() {
        JLabel jLabel = new JLabel();
        jLabel.setText(messages.getString(MessageKeys.GENERATOR_TARGET_COUNT));
        getContentPane().setLayout(new BorderLayout());
        setSize(new java.awt.Dimension(360, 100));
        getContentPane().add(jLabel, BorderLayout.WEST);
        getContentPane().add(getTxtTargetCount(), BorderLayout.CENTER);
        getContentPane().add(getBtnGenerate(), BorderLayout.SOUTH);
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
            btnGenerate.addActionListener(e -> generate()); // Use lambda for action listener
        }
        return btnGenerate;
    }

    private void generate() {
        try {
            int targetCount = Integer.parseInt(txtTargetCount.getText());
            if (targetCount <= 0) {
                JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_INVALID_COUNT), messages.getString(MessageKeys.GENERATOR_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
                return;
            }
            File outputFile = new File("targets_" + targetCount + ".yaml");

            // Use the specialized generator
            targetGenerator.generateRandomTargetsFile(targetCount, outputFile);

            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_SUCCESS) + outputFile.getAbsolutePath());
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_INVALID_COUNT), messages.getString(MessageKeys.GENERATOR_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_ERROR) + e.getMessage(), messages.getString(MessageKeys.GENERATOR_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.GENERATOR_ERROR) + e.getMessage(), messages.getString(MessageKeys.GENERATOR_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }
}