package hedos.utility;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;
import hedos.ui.DurationChartPanel;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.Component;

@Singleton
public class PersistenceService {
    private final Settings settings;
    private final EventBus eventBus;

    @Inject
    public PersistenceService(Settings settings, EventBus eventBus) {
        this.settings = settings;
        this.eventBus = eventBus;
    }

    public void saveResults(Component parent, Chromosome solution, DurationChartPanel chart) {
        if (solution == null) return;
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                writer.println("HeDoS Genetic Algorithm Report");
                writer.println("==============================");
                writer.println("Best Solution found: " + solution.toString());
                chart.exportCSV(writer);
            } catch (IOException e) {
                showError(parent, "Error saving results: " + e.getMessage());
            }
        }
    }

    public void saveChart(Component parent, DurationChartPanel chart) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("ga_performance.png"));
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            chart.saveAsImage(fileChooser.getSelectedFile());
        }
    }

    public void saveSettings(Component parent, GAParameters params) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            params.saveToSettings();
            settings.save(fileChooser.getSelectedFile());
        }
    }

    public void loadSettings(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            eventBus.publish(new EventBus.LoadSettingsRequest(fileChooser.getSelectedFile()));
        }
    }

    private void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Persistence Error", JOptionPane.ERROR_MESSAGE);
    }
}