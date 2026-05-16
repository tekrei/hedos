package hedos.ui;

import hedos.HedosFrame;
import hedos.ga.data.GAParameters;
import hedos.utility.EventBus;
import hedos.utility.MessageKeys;
import hedos.utility.Messages;
import hedos.utility.PersistenceService;
import javax.swing.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Encapsulates the menu bar logic for the HeDoS application.
 */
@Singleton
public class HedosMenu extends JMenuBar {
    private final Messages messages;
    private final PersistenceService persistenceService;
    private final GAParameters gaParams;
    private final TargetManagementDialog.Factory targetMgmtFactory;
    private final GenerateRandomTargetsDialog.Factory generateTargetsFactory;
    private final Provider<HedosFrame> frameProvider;
    private final BenchmarkService benchmarkService;

    @Inject
    public HedosMenu(Messages messages, PersistenceService persistenceService, GAParameters gaParams, EventBus eventBus,
                     TargetManagementDialog.Factory targetMgmtFactory,
                     GenerateRandomTargetsDialog.Factory generateTargetsFactory,
                     Provider<HedosFrame> frameProvider,
                     BenchmarkService benchmarkService) {
        this.messages = messages;
        this.persistenceService = persistenceService;
        this.gaParams = gaParams;
        this.targetMgmtFactory = targetMgmtFactory;
        this.generateTargetsFactory = generateTargetsFactory;
        this.frameProvider = frameProvider;
        this.benchmarkService = benchmarkService;

        eventBus.subscribe(EventBus.LocaleChangedEvent.class, e -> setupMenu());
    }

    public void setupMenu() {
        this.removeAll();

        JMenu fileMenu = new JMenu(messages.getString(MessageKeys.HEDOS_FRAME_FILE));
        JMenuItem menuItem;
        
        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_LOAD_SETTINGS));
        menuItem.addActionListener(e -> persistenceService.loadSettings(frameProvider.get()));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_SAVE_RESULTS));
        menuItem.addActionListener(e -> {
            HedosFrame frame = frameProvider.get();
            persistenceService.saveResults(frame, frame.getLastBestSolution(), frame.getChartPanel());
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_SAVE_CHART));
        menuItem.addActionListener(e -> {
            HedosFrame frame = frameProvider.get();
            persistenceService.saveChart(frame, frame.getChartPanel());
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_SAVE_SETTINGS_AS));
        menuItem.addActionListener(e -> persistenceService.saveSettings(frameProvider.get(), gaParams));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_EXIT));
        menuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(menuItem);

        this.add(fileMenu);

        JMenu settingsMenu = new JMenu(messages.getString(MessageKeys.HEDOS_FRAME_SETTINGS));
        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_MULTIPLE_TEST));
        menuItem.addActionListener(e -> frameProvider.get().multipleCalculation());
        settingsMenu.add(menuItem);

        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_MANAGE_TARGETS));
        menuItem.addActionListener(e -> targetMgmtFactory.create(frameProvider.get()).setVisible(true));
        settingsMenu.add(menuItem);

        menuItem = new JMenuItem(messages.getString(MessageKeys.HEDOS_FRAME_GENERATE_TARGETS));
        menuItem.addActionListener(e -> {
            HedosFrame frame = frameProvider.get();
            GenerateRandomTargetsDialog dialog = generateTargetsFactory.create(frame);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        });
        settingsMenu.add(menuItem);

        this.add(settingsMenu);

        JMenu benchmarkMenu = new JMenu("Benchmark");
        JMenuItem runBenchItem = new JMenuItem("Run JMH Benchmarks");
        runBenchItem.addActionListener(e -> benchmarkService.runAllBenchmarks(frameProvider.get()::showStatus, this::displayBenchmarkResults));
        benchmarkMenu.add(runBenchItem);
        this.add(benchmarkMenu);

        // Add Language Menu
        JMenu langMenu = new JMenu(messages.getString(MessageKeys.HEDOS_FRAME_LANGUAGE));
        JMenuItem englishItem = new JMenuItem("English");
        englishItem.addActionListener(e -> messages.setLocale(Messages.Language.ENGLISH));
        JMenuItem turkishItem = new JMenuItem("Türkçe");
        turkishItem.addActionListener(e -> messages.setLocale(Messages.Language.TURKISH));
        
        langMenu.add(englishItem);
        langMenu.add(turkishItem);
        this.add(langMenu);
        
        this.revalidate();
    }

    private void displayBenchmarkResults(String results) {
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = new JTextArea(results);
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));
            JOptionPane.showMessageDialog(frameProvider.get(), scrollPane, "JMH Benchmark Results", JOptionPane.PLAIN_MESSAGE);
        });
    }
}
