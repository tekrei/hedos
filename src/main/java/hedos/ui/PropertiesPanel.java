package hedos.ui;

import hedos.HedosFrame;
import hedos.ga.data.GAParameters;
import hedos.utility.Messages;
import hedos.utility.MessageKeys;
import javax.swing.border.TitledBorder;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.swing.*;
import java.awt.*;

public class PropertiesPanel extends JPanel {
    private final Provider<HedosFrame> frameProvider;
    private final Messages messages;
    private final GAParameters gaParameters;

    private JSpinner generationCountSpinner = null;
    private JSpinner populationSizeSpinner = null;
    private JSlider mutationProbabilitySlider = null;
    private JSlider crossoverProbabilitySlider = null;
    private JSlider turnPenaltySlider = null;
    private JLabel mutationValueLabel = new JLabel("0.00");
    private JLabel crossoverValueLabel = new JLabel("0.00");
    private JLabel turnPenaltyValueLabel = new JLabel("0.00");
    private JSpinner tournamentSizeSpinner = null;
    private JCheckBox elitismCheckBox;
    private JComboBox<GAParameters.CrossoverType> crossoverTypeCombo = null;
    private JComboBox<GAParameters.MutationType> mutationTypeCombo = null;
    private JComboBox<GAParameters.SelectionType> selectionTypeCombo = null;
    private JButton btnTravel = null;
    private JButton btnCalculate = null;
    private JButton btnClearSolution = null;
    private JButton btnResetDefaults = null;
    private JButton btnMultipleCalculate = null;
    private JPanel gaSettingsPanel;
    private JPanel actionButtonPanel;

    @Inject
    public PropertiesPanel(Provider<HedosFrame> frameProvider, Messages messages, GAParameters gaParams) {
        super();
        this.frameProvider = frameProvider;
        this.messages = messages;
        this.gaParameters = gaParams;
    }

    @Inject
    public void initialize() {
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1. GA Settings Panel
        gaSettingsPanel = new JPanel(new GridBagLayout());
        setupSectionPanel(gaSettingsPanel, messages.getString(MessageKeys.SIDE_PANEL_GA_SETTINGS));

        int currentGaGridY = 0;
        currentGaGridY = addLabeledFieldToPanel(messages.getString(MessageKeys.SIDE_PANEL_POPULATION_SIZE), getPopulationSizeSpinner(), gaSettingsPanel, currentGaGridY);
        currentGaGridY = addLabeledFieldToPanel(messages.getString(MessageKeys.SIDE_PANEL_GENERATION_COUNT), getGenerationCountSpinner(), gaSettingsPanel, currentGaGridY);
        currentGaGridY = addSliderWithLabel(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_PROBABILITY), getCrossoverProbabilitySlider(), crossoverValueLabel, gaSettingsPanel, currentGaGridY);
        currentGaGridY = addSliderWithLabel(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_PROBABILITY), getMutationProbabilitySlider(), mutationValueLabel, gaSettingsPanel, currentGaGridY);
        currentGaGridY = addSliderWithLabel(messages.getString(MessageKeys.SIDE_PANEL_TURN_PENALTY), getTurnPenaltySlider(), turnPenaltyValueLabel, gaSettingsPanel, currentGaGridY);
        currentGaGridY = addLabeledFieldToPanel(messages.getString(MessageKeys.SIDE_PANEL_TOURNAMENT_SIZE), getTournamentSizeSpinner(), gaSettingsPanel, currentGaGridY);

        GridBagConstraints typeGbc = new GridBagConstraints();
        typeGbc.fill = GridBagConstraints.HORIZONTAL;
        typeGbc.weightx = 1.0;
        typeGbc.gridx = 0;
        typeGbc.gridwidth = 2;

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(5, 5, 5, 5);
        gaSettingsPanel.add(getElitismCheckBox(), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(2, 5, 0, 5);
        gaSettingsPanel.add(new JLabel(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_TYPE)), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(0, 5, 5, 5);
        gaSettingsPanel.add(getMutationTypeCombo(), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(5, 5, 0, 5);
        gaSettingsPanel.add(new JLabel(messages.getString(MessageKeys.SIDE_PANEL_SELECTION_TYPE)), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(0, 5, 5, 5);
        gaSettingsPanel.add(getSelectionTypeCombo(), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(5, 5, 0, 5);
        gaSettingsPanel.add(new JLabel(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_TYPE)), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(0, 5, 5, 5);
        gaSettingsPanel.add(getCrossoverTypeCombo(), typeGbc);

        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(10, 5, 5, 5);
        gaSettingsPanel.add(getBtnResetDefaults(), typeGbc);

        this.add(gaSettingsPanel, gbc);
        gbc.gridy++;

        // 3. Action Buttons Panel
        actionButtonPanel = new JPanel(new GridBagLayout());
        setupSectionPanel(actionButtonPanel, messages.getString(MessageKeys.SIDE_PANEL_ACTIONS));
        GridBagConstraints actionGbc = new GridBagConstraints();
        actionGbc.fill = GridBagConstraints.HORIZONTAL;
        actionGbc.weightx = 1.0;
        actionGbc.gridx = 0;
        actionGbc.gridy = 0;
        actionGbc.insets = new Insets(5, 5, 5, 5);

        actionButtonPanel.add(getBtnCalculate(), actionGbc);
        actionGbc.gridy++;
        actionButtonPanel.add(getBtnClearSolution(), actionGbc);
        actionGbc.gridy++;
        actionButtonPanel.add(getBtnMultipleCalculate(), actionGbc);
        actionGbc.gridy++;
        actionButtonPanel.add(getBtnTravel(), actionGbc);

        this.add(actionButtonPanel, gbc);
        
        // Add a vertical spacer at the end to keep items at the top
        gbc.gridy++;
        gbc.weighty = 1.0;
        this.add(Box.createVerticalGlue(), gbc);
    }

    public void refreshLabels() {
        setupSectionPanel(gaSettingsPanel, messages.getString(MessageKeys.SIDE_PANEL_GA_SETTINGS));
        setupSectionPanel(actionButtonPanel, messages.getString(MessageKeys.SIDE_PANEL_ACTIONS));
        
        btnCalculate.setText(messages.getString(MessageKeys.SIDE_PANEL_SOLVE));
        btnClearSolution.setText(messages.getString(MessageKeys.SIDE_PANEL_CLEAR_SOLUTION));
        btnTravel.setText(messages.getString(MessageKeys.SIDE_PANEL_TRAVEL));
        btnResetDefaults.setText(messages.getString(MessageKeys.SIDE_PANEL_RESET_DEFAULTS));
        btnMultipleCalculate.setText(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST));
        elitismCheckBox.setText(messages.getString(MessageKeys.SIDE_PANEL_ELITISM));

        // Refresh tooltips for sliders
        if (crossoverProbabilitySlider != null) crossoverProbabilitySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_PROBABILITY + ".Tooltip"));
        if (mutationProbabilitySlider != null) mutationProbabilitySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_PROBABILITY + ".Tooltip"));
        if (turnPenaltySlider != null) turnPenaltySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_TURN_PENALTY + ".Tooltip"));
        
        // Force refresh of the renderers in combos
        updateUI();
    }

    // Helper method to add a labeled field pair to a panel
    private int addLabeledFieldToPanel(String labelText, JComponent field, JPanel panel, int currentGridY) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.fill = GridBagConstraints.HORIZONTAL;
        labelGbc.insets = new Insets(4, 5, 4, 5);
        labelGbc.gridwidth = 1;
        labelGbc.weightx = 0.0; // Label takes minimal horizontal space
        labelGbc.gridx = 0;
        labelGbc.gridy = currentGridY;
        panel.add(new JLabel(labelText), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(4, 5, 4, 5);
        fieldGbc.gridwidth = 1;
        fieldGbc.weightx = 1.0; // Field takes remaining horizontal space
        fieldGbc.gridx = 1;
        fieldGbc.gridy = currentGridY;
        panel.add(field, fieldGbc);
        
        return currentGridY + 1; // Return the next available row
    }

    private int addSliderWithLabel(String labelText, JSlider slider, JLabel valueLabel, JPanel panel, int currentGridY) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 5, 0, 5);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = currentGridY;

        // Label Row
        panel.add(new JLabel(labelText), gbc);

        // Slider Row
        JPanel sliderPanel = new JPanel(new BorderLayout(5, 0));
        sliderPanel.add(slider, BorderLayout.CENTER);
        sliderPanel.add(valueLabel, BorderLayout.EAST);
        
        gbc.gridy = currentGridY + 1;
        gbc.insets = new Insets(0, 5, 4, 5);
        panel.add(sliderPanel, gbc);

        return currentGridY + 2;
    }

    private void setupSectionPanel(JPanel panel, String title) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), 
                        title, 
                        TitledBorder.LEFT, 
                        TitledBorder.TOP, 
                        new Font("SansSerif", Font.BOLD, 12)
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private JSpinner getGenerationCountSpinner() {
        if (generationCountSpinner == null) {
            generationCountSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 10));
        }
        return generationCountSpinner;
    }

    private JSpinner getPopulationSizeSpinner() {
        if (populationSizeSpinner == null) {
            populationSizeSpinner = new JSpinner(new SpinnerNumberModel(50, 2, 5000, 2));
        }
        return populationSizeSpinner;
    }

    private JSpinner getTournamentSizeSpinner() {
        if (tournamentSizeSpinner == null) {
            tournamentSizeSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        }
        return tournamentSizeSpinner;
    }

    private JSlider getCrossoverProbabilitySlider() {
        if (crossoverProbabilitySlider == null) {
            crossoverProbabilitySlider = new JSlider(0, 100);
            crossoverProbabilitySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_PROBABILITY + ".Tooltip"));
            crossoverProbabilitySlider.addChangeListener(e ->
                    crossoverValueLabel.setText(String.format("%.2f", crossoverProbabilitySlider.getValue() / 100.0f)));
        }
        return crossoverProbabilitySlider;
    }

    private JSlider getMutationProbabilitySlider() {
        if (mutationProbabilitySlider == null) {
            mutationProbabilitySlider = new JSlider(0, 100);
            mutationProbabilitySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_PROBABILITY + ".Tooltip"));
            mutationProbabilitySlider.addChangeListener(e ->
                    mutationValueLabel.setText(String.format("%.2f", mutationProbabilitySlider.getValue() / 100.0f)));
        }
        return mutationProbabilitySlider;
    }

    private JSlider getTurnPenaltySlider() {
        if (turnPenaltySlider == null) {
            turnPenaltySlider = new JSlider(0, 200);
            turnPenaltySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_TURN_PENALTY + ".Tooltip"));
            turnPenaltySlider.addChangeListener(e ->
                    turnPenaltyValueLabel.setText(String.format("%.1f", (float)turnPenaltySlider.getValue())));
        }
        return turnPenaltySlider;
    }

    private JComboBox<GAParameters.MutationType> getMutationTypeCombo() {
        if (mutationTypeCombo == null) {
            mutationTypeCombo = new JComboBox<>(GAParameters.MutationType.values());
            mutationTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.MutationType type) {
                        setText(messages.getString(type.getNameKey()));
                    }
                    return this;
                }
            });
        }
        return mutationTypeCombo;
    }

    private JComboBox<GAParameters.SelectionType> getSelectionTypeCombo() {
        if (selectionTypeCombo == null) {
            selectionTypeCombo = new JComboBox<>(GAParameters.SelectionType.values());
            selectionTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.SelectionType type) {
                        setText(messages.getString(type.getNameKey()));
                    }
                    return this;
                }
            });
        }
        return selectionTypeCombo;
    }

    private JComboBox<GAParameters.CrossoverType> getCrossoverTypeCombo() {
        if (crossoverTypeCombo == null) {
            crossoverTypeCombo = new JComboBox<>(GAParameters.CrossoverType.values());
            crossoverTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.CrossoverType type) {
                        setText(messages.getString(type.getNameKey()));
                    }
                    return this;
                }
            });
        }
        return crossoverTypeCombo;
    }

    private JCheckBox getElitismCheckBox() {
        if (elitismCheckBox == null) {
            elitismCheckBox = new JCheckBox(messages.getString(MessageKeys.SIDE_PANEL_ELITISM));
        }
        return elitismCheckBox;
    }

    private JButton getBtnTravel() {
        if (btnTravel == null) {
            btnTravel = new JButton(messages.getString(MessageKeys.SIDE_PANEL_TRAVEL));
            btnTravel.setIcon(FontIcon.of(FontAwesomeSolid.MAP_MARKED_ALT, 16));
            btnTravel.setMnemonic('d');
            btnTravel.addActionListener(e -> travelClicked());
        }
        return btnTravel;
    }

    private void travelClicked() {
        frameProvider.get().travel();
    }

    private JButton getBtnCalculate() {
        if (btnCalculate == null) {
            btnCalculate = new JButton(messages.getString(MessageKeys.SIDE_PANEL_SOLVE));
            btnCalculate.setIcon(FontIcon.of(FontAwesomeSolid.CALCULATOR, 16, Color.WHITE));
            btnCalculate.setBackground(new Color(52, 120, 200));
            btnCalculate.setForeground(Color.WHITE);
            btnCalculate.setMnemonic('h');
            btnCalculate.addActionListener(e -> frameProvider.get().calculate());
        }
        return btnCalculate;
    }

    private JButton getBtnClearSolution() {
        if (btnClearSolution == null) {
            btnClearSolution = new JButton(messages.getString(MessageKeys.SIDE_PANEL_CLEAR_SOLUTION));
            btnClearSolution.setIcon(FontIcon.of(FontAwesomeSolid.ERASER, 16));
            btnClearSolution.setMnemonic('l');
            btnClearSolution.addActionListener(e -> frameProvider.get().clearSolution());
        }
        return btnClearSolution;
    }

    private JButton getBtnResetDefaults() {
        if (btnResetDefaults == null) {
            btnResetDefaults = new JButton(messages.getString(MessageKeys.SIDE_PANEL_RESET_DEFAULTS));
            btnResetDefaults.setIcon(FontIcon.of(FontAwesomeSolid.REDO, 16));
            btnResetDefaults.addActionListener(e -> {
                gaParameters.resetToDefaults();
                updatePanel();
            });
        }
        return btnResetDefaults;
    }

    private JButton getBtnMultipleCalculate() {
        if (btnMultipleCalculate == null) {
            btnMultipleCalculate = new JButton(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST));
            btnMultipleCalculate.setIcon(FontIcon.of(FontAwesomeSolid.LAYER_GROUP, 16));
            btnMultipleCalculate.setMnemonic('e');
            btnMultipleCalculate.addActionListener(e -> frameProvider.get().multipleCalculation());
        }
        return btnMultipleCalculate;
    }

    public void updateGPParameter() {
        gaParameters.setCrossoverProbability(crossoverProbabilitySlider.getValue() / 100.0f);
        gaParameters.setMutationProbability(mutationProbabilitySlider.getValue() / 100.0f);
        gaParameters.setTurnPenaltyFactor((float) turnPenaltySlider.getValue());

        gaParameters.setPopulationSize((Integer) populationSizeSpinner.getValue());
        gaParameters.setGenerationCount((Integer) generationCountSpinner.getValue());
        gaParameters.setTournamentSize((Integer) tournamentSizeSpinner.getValue());

        gaParameters.setCrossoverType((GAParameters.CrossoverType) crossoverTypeCombo.getSelectedItem());
        gaParameters.setMutationType((GAParameters.MutationType) mutationTypeCombo.getSelectedItem());
        gaParameters.setSelectionType((GAParameters.SelectionType) selectionTypeCombo.getSelectedItem());
        gaParameters.setElitism(elitismCheckBox.isSelected());
    }

    public void updatePanel() {
        if (generationCountSpinner == null) return;

        generationCountSpinner.setValue(gaParameters.getGenerationCount());
        populationSizeSpinner.setValue(gaParameters.getPopulationSize());

        int mutVal = (int) (gaParameters.getMutationProbability() * 100);
        mutationProbabilitySlider.setValue(mutVal);
        mutationValueLabel.setText(String.format("%.2f", mutVal / 100.0f));

        int crossVal = (int) (gaParameters.getCrossoverProbability() * 100);
        crossoverProbabilitySlider.setValue(crossVal);
        crossoverValueLabel.setText(String.format("%.2f", crossVal / 100.0f));

        turnPenaltySlider.setValue((int) gaParameters.getTurnPenaltyFactor());
        turnPenaltyValueLabel.setText(String.format("%.1f", gaParameters.getTurnPenaltyFactor()));

        tournamentSizeSpinner.setValue(gaParameters.getTournamentSize());

        mutationTypeCombo.setSelectedItem(gaParameters.getMutationType());
        crossoverTypeCombo.setSelectedItem(gaParameters.getCrossoverType());
        selectionTypeCombo.setSelectedItem(gaParameters.getSelectionType());
        elitismCheckBox.setSelected(gaParameters.isElitism());
        this.updateUI();
    }
}