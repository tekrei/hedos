package hedos.ui;

import hedos.HedosFrame;
import hedos.ga.data.Point;
import hedos.utility.Messages;
import hedos.utility.MessageKeys;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

public class TargetManagementDialog extends JDialog {
    private final HedosFrame frame;
    private final Messages messages;
    private final JTextField xField = new JTextField(5);
    private final JTextField yField = new JTextField(5);
    private final JTextField zField = new JTextField(5);
    private final DefaultListModel<Point> listModel = new DefaultListModel<>();
    private final JList<Point> targetList = new JList<>(listModel);

    public TargetManagementDialog(HedosFrame frame, Messages messages) {
        super(frame, messages.getString(MessageKeys.SOL_PANEL_TARGET_MANAGEMENT), true);
        this.frame = frame;
        this.messages = messages;
        init();
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel(messages.getString(MessageKeys.TARGET_MGMT_X))); inputPanel.add(xField);
        inputPanel.add(new JLabel(messages.getString(MessageKeys.TARGET_MGMT_Y))); inputPanel.add(yField);
        inputPanel.add(new JLabel(messages.getString(MessageKeys.TARGET_MGMT_Z))); inputPanel.add(zField);
        
        JButton addButton = new JButton(messages.getString(MessageKeys.SOL_PANEL_ADD), FontIcon.of(FontAwesomeSolid.PLUS, 16));
        addButton.addActionListener(e -> validateAndAdd());
        inputPanel.add(addButton);

        // List Panel
        refreshList();
        JScrollPane scrollPane = new JScrollPane(targetList);
        
        JPanel sideButtons = new JPanel(new GridLayout(0, 1, 5, 5));
        JButton deleteButton = new JButton(messages.getString(MessageKeys.SOL_PANEL_DELETE), FontIcon.of(FontAwesomeSolid.TRASH_ALT, 16));
        deleteButton.addActionListener(e -> {
            Point selected = targetList.getSelectedValue();
            if (selected != null) {
                frame.deleteTarget(selected);
                refreshList();
            }
        });
        sideButtons.add(deleteButton);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(sideButtons, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(frame);
    }

    private void validateAndAdd() {
        String xStr = xField.getText().trim();
        String yStr = yField.getText().trim();
        String zStr = zField.getText().trim();

        if (xStr.isEmpty() || yStr.isEmpty() || zStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.TARGET_MGMT_VALIDATION_ERROR_MSG), messages.getString(MessageKeys.TARGET_MGMT_VALIDATION_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            float x = Float.parseFloat(xStr);
            float y = Float.parseFloat(yStr);
            float z = Float.parseFloat(zStr);

            frame.addTarget(new Point(x, y, z));
            xField.setText("");
            yField.setText("");
            zField.setText("");
            refreshList();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.TARGET_MGMT_INPUT_ERROR_MSG), messages.getString(MessageKeys.TARGET_MGMT_INPUT_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshList() {
        listModel.clear();
        for (Point p : frame.getTargets()) {
            listModel.addElement(p);
        }
    }
}