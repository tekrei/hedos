package hedos.ui;

import hedos.HedosFrame;
import hedos.ga.data.GAParameters;
import hedos.utility.EventBus;
import hedos.utility.Messages;
import hedos.utility.MessageKeys;
import javax.swing.border.TitledBorder;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
    private JComboBox<GAParameters.CrossoverType> crossoverTypeCombo = null;
    private JComboBox<GAParameters.MutationType> mutationTypeCombo = null;
    private JComboBox<GAParameters.SelectionType> selectionTypeCombo = null;
    private JComboBox<GAParameters.LocalOptimizationType> localOptTypeCombo = null;
    private JComboBox<GAParameters.StagnationType> stagnationTypeCombo = null;
    private JComboBox<GAParameters.ElitismType> elitismTypeCombo = null;
    private JButton btnTravel = null;
    private JButton btnCalculate = null;
    private JButton btnClearSolution = null;
    private JButton btnResetDefaults = null;
    private JButton btnMultipleCalculate = null;
    private JPanel gaSettingsPanel;
    private JPanel actionButtonPanel;
    private JLabel lblPopulationSize, lblGenerationCount, lblCrossoverProb, lblMutationProb, lblTurnPenalty, lblTournamentSize;
    private JLabel lblElitismStrategy, lblStagnationStrategy, lblMutationType, lblLocalOptimization, lblSelectionType, lblCrossoverType;

    @Inject
    public PropertiesPanel(Provider<HedosFrame> frameProvider, Messages messages, GAParameters gaParams, EventBus eventBus) {
        super();
        this.frameProvider = frameProvider;
        this.messages = messages;
        this.gaParameters = gaParams;
        eventBus.subscribe(EventBus.LocaleChangedEvent.class, e -> refreshLabels());
    }

    @Inject
    public void initialize() {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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
        lblPopulationSize = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_POPULATION_SIZE));
        currentGaGridY = addLabeledFieldToPanel(lblPopulationSize, getPopulationSizeSpinner(), gaSettingsPanel, currentGaGridY);
        lblGenerationCount = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_GENERATION_COUNT));
        currentGaGridY = addLabeledFieldToPanel(lblGenerationCount, getGenerationCountSpinner(), gaSettingsPanel, currentGaGridY);
        lblCrossoverProb = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_PROBABILITY));
        currentGaGridY = addSliderWithLabel(lblCrossoverProb, getCrossoverProbabilitySlider(), crossoverValueLabel, gaSettingsPanel, currentGaGridY);
        lblMutationProb = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_PROBABILITY));
        currentGaGridY = addSliderWithLabel(lblMutationProb, getMutationProbabilitySlider(), mutationValueLabel, gaSettingsPanel, currentGaGridY);
        lblTurnPenalty = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_TURN_PENALTY));
        currentGaGridY = addSliderWithLabel(lblTurnPenalty, getTurnPenaltySlider(), turnPenaltyValueLabel, gaSettingsPanel, currentGaGridY);
        lblTournamentSize = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_TOURNAMENT_SIZE));
        currentGaGridY = addLabeledFieldToPanel(lblTournamentSize, getTournamentSizeSpinner(), gaSettingsPanel, currentGaGridY);

        lblElitismStrategy = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_ELITISM_STRATEGY));
        currentGaGridY = addLabeledFieldToPanel(lblElitismStrategy, getElitismTypeCombo(), gaSettingsPanel, currentGaGridY);
        lblStagnationStrategy = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_STAGNATION_STRATEGY));
        currentGaGridY = addLabeledFieldToPanel(lblStagnationStrategy, getStagnationTypeCombo(), gaSettingsPanel, currentGaGridY);
        lblMutationType = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_TYPE));
        currentGaGridY = addLabeledFieldToPanel(lblMutationType, getMutationTypeCombo(), gaSettingsPanel, currentGaGridY);
        lblLocalOptimization = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_LOCAL_OPTIMIZATION));
        currentGaGridY = addLabeledFieldToPanel(lblLocalOptimization, getLocalOptTypeCombo(), gaSettingsPanel, currentGaGridY);
        lblSelectionType = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_SELECTION_TYPE));
        currentGaGridY = addLabeledFieldToPanel(lblSelectionType, getSelectionTypeCombo(), gaSettingsPanel, currentGaGridY);
        lblCrossoverType = new JLabel(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_TYPE));
        currentGaGridY = addLabeledFieldToPanel(lblCrossoverType, getCrossoverTypeCombo(), gaSettingsPanel, currentGaGridY);

        GridBagConstraints typeGbc = new GridBagConstraints();
        typeGbc.fill = GridBagConstraints.HORIZONTAL;
        typeGbc.weightx = 1.0;
        typeGbc.gridx = 0;
        typeGbc.gridwidth = 2;
        typeGbc.gridy = currentGaGridY++;
        typeGbc.insets = new Insets(5, 5, 2, 5);
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
        actionGbc.insets = new Insets(2, 5, 2, 5);

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
        if (gaSettingsPanel == null) return;
        setupSectionPanel(gaSettingsPanel, messages.getString(MessageKeys.SIDE_PANEL_GA_SETTINGS));
        setupSectionPanel(actionButtonPanel, messages.getString(MessageKeys.SIDE_PANEL_ACTIONS));
        
        btnCalculate.setText(messages.getString(MessageKeys.SIDE_PANEL_SOLVE));
        btnClearSolution.setText(messages.getString(MessageKeys.SIDE_PANEL_CLEAR_SOLUTION));
        btnTravel.setText(messages.getString(MessageKeys.SIDE_PANEL_TRAVEL));
        btnResetDefaults.setText(messages.getString(MessageKeys.SIDE_PANEL_RESET_DEFAULTS));
        btnMultipleCalculate.setText(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST));
        
        btnCalculate.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_SOLVE_TOOLTIP));
        btnTravel.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_TRAVEL_TOOLTIP));
        btnClearSolution.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_CLEAR_SOLUTION_TOOLTIP));
        btnMultipleCalculate.setToolTipText(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST_TOOLTIP));

        lblPopulationSize.setText(messages.getString(MessageKeys.SIDE_PANEL_POPULATION_SIZE));
        lblGenerationCount.setText(messages.getString(MessageKeys.SIDE_PANEL_GENERATION_COUNT));
        lblCrossoverProb.setText(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_PROBABILITY));
        lblMutationProb.setText(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_PROBABILITY));
        lblTurnPenalty.setText(messages.getString(MessageKeys.SIDE_PANEL_TURN_PENALTY));
        lblTournamentSize.setText(messages.getString(MessageKeys.SIDE_PANEL_TOURNAMENT_SIZE));
        lblElitismStrategy.setText(messages.getString(MessageKeys.SIDE_PANEL_ELITISM_STRATEGY));
        lblStagnationStrategy.setText(messages.getString(MessageKeys.SIDE_PANEL_STAGNATION_STRATEGY));
        lblMutationType.setText(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_TYPE));
        lblLocalOptimization.setText(messages.getString(MessageKeys.SIDE_PANEL_LOCAL_OPTIMIZATION));
        lblSelectionType.setText(messages.getString(MessageKeys.SIDE_PANEL_SELECTION_TYPE));
        lblCrossoverType.setText(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_TYPE));

        // Refresh tooltips for sliders
        crossoverProbabilitySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_CROSSOVER_PROBABILITY + ".Tooltip"));
        mutationProbabilitySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_MUTATION_PROBABILITY + ".Tooltip"));
        turnPenaltySlider.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_TURN_PENALTY + ".Tooltip"));
        
        syncAllTooltips();
        updateUI();
    }

    private int addLabeledFieldToPanel(JLabel label, JComponent field, JPanel panel, int currentGridY) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.fill = GridBagConstraints.HORIZONTAL;
        labelGbc.insets = new Insets(1, 5, 1, 5);
        labelGbc.gridwidth = 1;
        labelGbc.weightx = 0.45; 
        labelGbc.gridx = 0;
        labelGbc.gridy = currentGridY;
        panel.add(label, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(1, 5, 1, 5);
        fieldGbc.gridwidth = 1;
        fieldGbc.weightx = 0.55;
        fieldGbc.gridx = 1;
        fieldGbc.gridy = currentGridY;
        panel.add(field, fieldGbc);
        
        return currentGridY + 1; // Return the next available row
    }

    private int addSliderWithLabel(JLabel label, JSlider slider, JLabel valueLabel, JPanel panel, int currentGridY) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.fill = GridBagConstraints.HORIZONTAL;
        labelGbc.insets = new Insets(1, 5, 1, 5);
        labelGbc.gridx = 0;
        labelGbc.gridy = currentGridY;
        labelGbc.weightx = 0.45;
        panel.add(label, labelGbc);

        JPanel sliderPanel = new JPanel(new BorderLayout(5, 0));
        sliderPanel.add(slider, BorderLayout.CENTER);
        sliderPanel.add(valueLabel, BorderLayout.EAST);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(1, 5, 1, 5);
        fieldGbc.gridx = 1;
        fieldGbc.gridy = currentGridY;
        fieldGbc.weightx = 0.55;
        panel.add(sliderPanel, fieldGbc);

        return currentGridY + 1;
    }

    private void setupSectionPanel(JPanel panel, String title) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), 
                        title, 
                        TitledBorder.LEFT, 
                        TitledBorder.TOP
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
            mutationTypeCombo.addActionListener(e -> syncComboTooltip(mutationTypeCombo, (GAParameters.MutationType) mutationTypeCombo.getSelectedItem()));
            mutationTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.MutationType type) {
                        setText(messages.getString(type.getNameKey()));
                        setToolTipText(messages.getString(type.getNameKey() + ".Tooltip"));
                    }
                    return this;
                }
            });
        }
        return mutationTypeCombo;
    }

    private JComboBox<GAParameters.StagnationType> getStagnationTypeCombo() {
        if (stagnationTypeCombo == null) {
            stagnationTypeCombo = new JComboBox<>(GAParameters.StagnationType.values());
            stagnationTypeCombo.addActionListener(e -> syncComboTooltip(stagnationTypeCombo, (GAParameters.StagnationType) stagnationTypeCombo.getSelectedItem()));
            stagnationTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.StagnationType type) {
                        setText(messages.getString(type.getNameKey()));
                        setToolTipText(messages.getString(type.getNameKey() + ".Tooltip"));
                    }
                    return this;
                }
            });
        }
        return stagnationTypeCombo;
    }

    private JComboBox<GAParameters.ElitismType> getElitismTypeCombo() {
        if (elitismTypeCombo == null) {
            elitismTypeCombo = new JComboBox<>(GAParameters.ElitismType.values());
            elitismTypeCombo.addActionListener(e -> syncComboTooltip(elitismTypeCombo, (GAParameters.ElitismType) elitismTypeCombo.getSelectedItem()));
            elitismTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.ElitismType type) {
                        setText(messages.getString(type.getNameKey()));
                        setToolTipText(messages.getString(type.getNameKey() + ".Tooltip"));
                    }
                    return this;
                }
            });
        }
        return elitismTypeCombo;
    }

    private JComboBox<GAParameters.LocalOptimizationType> getLocalOptTypeCombo() {
        if (localOptTypeCombo == null) {
            localOptTypeCombo = new JComboBox<>(GAParameters.LocalOptimizationType.values());
            localOptTypeCombo.addActionListener(e -> syncComboTooltip(localOptTypeCombo, (GAParameters.LocalOptimizationType) localOptTypeCombo.getSelectedItem()));
            localOptTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.LocalOptimizationType type) {
                        setText(messages.getString(type.getNameKey()));
                        setToolTipText(messages.getString(type.getNameKey() + ".Tooltip"));
                    }
                    return this;
                }
            });
        }
        return localOptTypeCombo;
    }

    private JComboBox<GAParameters.SelectionType> getSelectionTypeCombo() {
        if (selectionTypeCombo == null) {
            selectionTypeCombo = new JComboBox<>(GAParameters.SelectionType.values());
            selectionTypeCombo.addActionListener(e -> syncComboTooltip(selectionTypeCombo, (GAParameters.SelectionType) selectionTypeCombo.getSelectedItem()));
            selectionTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.SelectionType type) {
                        setText(messages.getString(type.getNameKey()));
                        setToolTipText(messages.getString(type.getNameKey() + ".Tooltip"));
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
            crossoverTypeCombo.addActionListener(e -> syncComboTooltip(crossoverTypeCombo, (GAParameters.CrossoverType) crossoverTypeCombo.getSelectedItem()));
            crossoverTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GAParameters.CrossoverType type) {
                        setText(messages.getString(type.getNameKey()));
                        setToolTipText(messages.getString(type.getNameKey() + ".Tooltip"));
                    }
                    return this;
                }
            });
        }
        return crossoverTypeCombo;
    }

    private void syncComboTooltip(JComboBox<?> combo, Object selectedValue) {
        if (selectedValue == null) return;
        String nameKey = "";
        if (selectedValue instanceof GAParameters.MutationType t) nameKey = t.getNameKey();
        else if (selectedValue instanceof GAParameters.CrossoverType t) nameKey = t.getNameKey();
        else if (selectedValue instanceof GAParameters.SelectionType t) nameKey = t.getNameKey();
        else if (selectedValue instanceof GAParameters.LocalOptimizationType t) nameKey = t.getNameKey();
        else if (selectedValue instanceof GAParameters.StagnationType t) nameKey = t.getNameKey();
        else if (selectedValue instanceof GAParameters.ElitismType t) nameKey = t.getNameKey();
        
        if (!nameKey.isEmpty()) {
            combo.setToolTipText(messages.getString(nameKey + ".Tooltip"));
        }
    }

    private void syncAllTooltips() {
        syncComboTooltip(mutationTypeCombo, mutationTypeCombo.getSelectedItem());
        syncComboTooltip(crossoverTypeCombo, crossoverTypeCombo.getSelectedItem());
        syncComboTooltip(selectionTypeCombo, selectionTypeCombo.getSelectedItem());
        syncComboTooltip(localOptTypeCombo, localOptTypeCombo.getSelectedItem());
        syncComboTooltip(stagnationTypeCombo, stagnationTypeCombo.getSelectedItem());
        syncComboTooltip(elitismTypeCombo, elitismTypeCombo.getSelectedItem());
    }

    private JButton getBtnTravel() {
        if (btnTravel == null) {
            btnTravel = new JButton(messages.getString(MessageKeys.SIDE_PANEL_TRAVEL));
            btnTravel.setIcon(FontIcon.of(FontAwesomeSolid.MAP_MARKED_ALT, 16));
            btnTravel.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_TRAVEL_TOOLTIP));
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
            btnCalculate.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_SOLVE_TOOLTIP));
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
            btnClearSolution.setToolTipText(messages.getString(MessageKeys.SIDE_PANEL_CLEAR_SOLUTION_TOOLTIP));
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
            btnMultipleCalculate.setToolTipText(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST_TOOLTIP));
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
        gaParameters.setLocalOptimizationType((GAParameters.LocalOptimizationType) localOptTypeCombo.getSelectedItem());
        gaParameters.setSelectionType((GAParameters.SelectionType) selectionTypeCombo.getSelectedItem());
        gaParameters.setStagnationType((GAParameters.StagnationType) stagnationTypeCombo.getSelectedItem());
        gaParameters.setElitismType((GAParameters.ElitismType) elitismTypeCombo.getSelectedItem());
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
        localOptTypeCombo.setSelectedItem(gaParameters.getLocalOptimizationType());
        selectionTypeCombo.setSelectedItem(gaParameters.getSelectionType());
        stagnationTypeCombo.setSelectedItem(gaParameters.getStagnationType());
        elitismTypeCombo.setSelectedItem(gaParameters.getElitismType());
        syncAllTooltips();
        this.updateUI();
    }
}