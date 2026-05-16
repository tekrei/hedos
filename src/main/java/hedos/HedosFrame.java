package hedos;

import hedos.ga.GeneticAlgorithm;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import hedos.ga.data.CostCalculator;
import hedos.ga.data.TSPCostCalculator;
import hedos.ga.data.Point;
import hedos.utility.MessageKeys;
import hedos.utility.EventBus;
import hedos.utility.PathUtils;
import hedos.graphics.X3DEngine;
import hedos.ui.PropertiesPanel;
import hedos.ui.TargetManagementDialog;
import hedos.ui.GenerateRandomTargetsDialog;
import hedos.ui.DurationChartPanel;
import hedos.utility.HedosModule;
import hedos.utility.Messages;
import hedos.utility.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class HedosFrame extends JFrame {
    private final List<Point> targets = new ArrayList<>();
    private final PropertiesPanel propertiesPanel;
    private final Messages messages;
    private final GAParameters gaParams;
    private final X3DEngine engine;
    private final Settings settings;
    private final CostCalculator calculator;
    private final EventBus eventBus;
    private final Provider<GeneticAlgorithm> gaProvider;
    private final TargetManagementDialog.Factory targetMgmtFactory;
    private final GenerateRandomTargetsDialog.Factory generateTargetsFactory;

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel lastLogLabel;
    private DurationChartPanel chartPanel;
    private JMenuBar menu;

    public record ProgressData(int current, int total, float bestCost, long duration, String message) {}

    @Inject
    public HedosFrame(PropertiesPanel propertiesPanel, Messages messages, GAParameters gaParams, 
                      X3DEngine engine, Settings settings, CostCalculator calculator, 
                      EventBus eventBus, Provider<GeneticAlgorithm> gaProvider,
                      TargetManagementDialog.Factory targetMgmtFactory,
                      GenerateRandomTargetsDialog.Factory generateTargetsFactory) {
        this.propertiesPanel = propertiesPanel;
        this.messages = messages;
        this.gaParams = gaParams;
        this.engine = engine;
        this.settings = settings;
        this.calculator = calculator;
        this.eventBus = eventBus;
        this.gaProvider = gaProvider;
        this.targetMgmtFactory = targetMgmtFactory;
        this.generateTargetsFactory = generateTargetsFactory;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();
            Injector injector = Guice.createInjector(new HedosModule());
            HedosFrame frame = injector.getInstance(HedosFrame.class);
            frame.init();
        });
    }

    private void init() {
        uiInit();
        initScene();
        
        // Subscribe to global settings changes
        eventBus.subscribe(EventBus.SettingsChangedEvent.class, e -> SwingUtilities.invokeLater(this::refreshTargets));
    }

    private void uiInit() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                engine.shutdown();
                System.exit(0);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(0);

        splitPane.setLeftComponent(propertiesPanel);
        splitPane.setRightComponent(engine.getBrowserPanel());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel(messages.getString(MessageKeys.STATUS_BAR_READY));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        lastLogLabel = new JLabel(" ");
        lastLogLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));

        chartPanel = new DurationChartPanel();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(statusLabel, BorderLayout.EAST);
        statusPanel.add(chartPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 2));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(lastLogLabel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        this.add(bottomPanel, BorderLayout.SOUTH);

        this.add(splitPane, BorderLayout.CENTER);
        this.setJMenuBar(getMenu());

        setSize(1440, 900);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private JMenuBar getMenu() {
        if (menu == null) {
            menu = new JMenuBar();

            JMenu fileMenu = new JMenu(messages.getString(MessageKeys.HEDOS_FRAME_FILE));
            JMenuItem menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_SAVE_SETTINGS));
            menuItem.addActionListener(e -> saveFile());
            fileMenu.add(menuItem);

            menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_LOAD_SETTINGS));
            menuItem.addActionListener(e -> loadFile());
            fileMenu.add(menuItem);

            menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_SAVE_SETTINGS_AS));
            menuItem.addActionListener(e -> saveFile());
            fileMenu.add(menuItem);

            menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_EXIT));
            menuItem.addActionListener(e -> System.exit(0));
            fileMenu.add(menuItem);

            menu.add(fileMenu);

            JMenu settingsMenu = new JMenu(messages.getString(MessageKeys.HEDOS_FRAME_SETTINGS));
            menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST));
            menuItem.addActionListener(e -> multipleCalculation());
            settingsMenu.add(menuItem);

            menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_MANAGE_TARGETS));
            menuItem.addActionListener(e -> targetMgmtFactory.create(this).setVisible(true));
            settingsMenu.add(menuItem);

            menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_GENERATE_TARGETS));
            menuItem.addActionListener(e -> {
                GenerateRandomTargetsDialog dialog = generateTargetsFactory.create(this);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            });
            settingsMenu.add(menuItem);

            menu.add(settingsMenu);
        }
        
        // Add Language Menu
        JMenu langMenu = new JMenu(messages.getString(MessageKeys.HEDOS_FRAME_LANGUAGE));
        JMenuItem englishItem = new JMenuItem("English");
        englishItem.addActionListener(e -> switchLanguage(Messages.Language.ENGLISH));
        JMenuItem turkishItem = new JMenuItem("Türkçe");
        turkishItem.addActionListener(e -> switchLanguage(Messages.Language.TURKISH));
        
        langMenu.add(englishItem);
        langMenu.add(turkishItem);
        menu.add(langMenu);

        return menu;
    }

    private void switchLanguage(String langCode) {
        messages.setLocale(langCode);
        refreshLabels();
        propertiesPanel.refreshLabels();
    }

    public void refreshLabels() {
        // Recreate menu to refresh all labels
        menu = null;
        setJMenuBar(getMenu());
        
        statusLabel.setText(messages.getString(MessageKeys.STATUS_BAR_READY));
        progressBar.setString(null); // Reset progress string if any
        
        validate();
        repaint();
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Assuming settings.load returns true on success
            eventBus.publish(new EventBus.LoadSettingsRequest(fileChooser.getSelectedFile()));
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            gaParams.saveToSettings();
            settings.save(fileChooser.getSelectedFile());
        }
    }

    private void refreshTargets() {
        // Re-sync application state after settings change
        gaParams.resetToDefaults();
        propertiesPanel.updatePanel();
        clearSolution();
        // Clear existing visual and logical targets
        for (Point p : new ArrayList<>(targets)) {
            deleteTarget(p);
        }
        initScene();
    }

    private void initScene() {
        List<Point> initialTargets = settings.getTargets();

        for (Point point : initialTargets) {
            addTarget(point);
        }

        propertiesPanel.updatePanel();
    }

    private void drawPath(int[] path) {
        float[] coordinates = PathUtils.getPathCoordinates(path, targets);
        int[] index = PathUtils.getLineIndices(path.length);
        int[] colorIndex = PathUtils.detectSharpTurns(path, targets);
        engine.addLineSet(coordinates, index, colorIndex);
    }

    public void addTarget(Point target) {
        engine.createTargetAt(target);
        targets.add(target);
        propertiesPanel.updatePanel();
    }

    public void deleteTarget(Point target) {
        engine.deleteNode(target.name());
        targets.remove(target);
        propertiesPanel.updatePanel();
    }

    public void calculate() {
        if (targets.isEmpty()) {
            JOptionPane.showMessageDialog(this, messages.getString(MessageKeys.DIALOG_NO_TARGETS_MSG), messages.getString(MessageKeys.DIALOG_NO_TARGETS_TITLE), JOptionPane.WARNING_MESSAGE);
            return;
        }

        chartPanel.clear();
        propertiesPanel.updateGPParameter();
        
        if (calculator instanceof TSPCostCalculator tspCalc) {
            tspCalc.init(targets);
        }
        GeneticAlgorithm ga = gaProvider.get();
        ga.setCalculator(calculator);

        log(String.format(messages.getString(MessageKeys.LOG_CALC_START), 
            gaParams.getGenerationCount(), gaParams.getPopulationSize(), gaParams.getMutationProbability()));

        // Using SwingWorker to keep the UI responsive
        SwingWorker<Chromosome, HedosFrame.ProgressData> worker = new SwingWorker<Chromosome, HedosFrame.ProgressData>() {
            @Override
            protected Chromosome doInBackground() {
                ga.setProgressListener((current, total, bestCost, duration) -> {
                    String msg = (current % 10 == 0 || current == total) ? 
                        String.format("Generation %d (%d ms): Best Fitness = %.2f", current, duration, bestCost) : null;
                    publish(new HedosFrame.ProgressData(current, total, bestCost, duration, msg));
                });
                return ga.run(targets);
            }

            @Override
            protected void process(List<HedosFrame.ProgressData> chunks) {
                HedosFrame.ProgressData latest = chunks.get(chunks.size() - 1);
                
                for (HedosFrame.ProgressData data : chunks) {
                    if (data.message() != null) log(data.message());
                    chartPanel.addData(data.duration(), data.bestCost());
                }

                int percent = (int) (((float) latest.current() / latest.total()) * 100);
                progressBar.setValue(percent);
                statusLabel.setText(String.format("%s: %d/%d | %s: %.2f", 
                    messages.getString(MessageKeys.STATUS_BAR_GENERATION), latest.current(), latest.total(),
                    messages.getString(MessageKeys.STATUS_BAR_BEST_FITNESS), latest.bestCost()));
            }

            @Override
            protected void done() {
                try {
                    Chromosome best = this.get();
                    drawPath(best.genes());
                    log(messages.getString(MessageKeys.LOG_CALC_COMPLETE) + best.cost());
                    progressBar.setValue(0);
                    statusLabel.setText(messages.getString(MessageKeys.STATUS_BAR_READY));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }

    private void log(String message) {
        lastLogLabel.setText(message);
    }

    public void travel() {
        engine.setRoute(targets, settings.getStartPoint());
        engine.startTour(targets.size());
    }

    public void clearSolution() {
        engine.deleteNode("P_SOLUTION");
        progressBar.setValue(0);
        statusLabel.setText(messages.getString(MessageKeys.STATUS_BAR_READY));
    }

    public void multipleCalculation() {
        String input = JOptionPane.showInputDialog(null, messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST), "10");
        if (input == null || input.isBlank()) return;

        try {
            int trialCount = Integer.parseInt(input.trim());
            log(String.format(messages.getString(MessageKeys.LOG_TRIALS_START), trialCount));
            
            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() {
                    for (int i = 0; i < trialCount; i++) {
                        publish(String.format(messages.getString(MessageKeys.LOG_TRIAL_START), i + 1, trialCount));
                        if (calculator instanceof TSPCostCalculator tspCalc) {
                            tspCalc.init(targets);
                        }
                        GeneticAlgorithm ga = gaProvider.get();
                        ga.setCalculator(calculator);
                        Chromosome best = ga.run(targets);
                        publish(String.format(messages.getString(MessageKeys.LOG_TRIAL_FINISH), i + 1, best.cost()));
                    }
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String msg : chunks) log(msg);
                }

                @Override
                protected void done() {
                    log(messages.getString(MessageKeys.LOG_TRIALS_COMPLETE));
                }
            }.execute();
        } catch (NumberFormatException e) {
            log(messages.getString(MessageKeys.LOG_INVALID_COUNT));
        }
    }

    public List<Point> getTargets() {
        return targets;
    }
}
