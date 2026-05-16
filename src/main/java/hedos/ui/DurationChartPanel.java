package hedos.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DurationChartPanel extends JPanel {
    private final List<Long> durations = new ArrayList<>();
    private final List<Float> fitnesses = new ArrayList<>();
    private static final int MAX_DATA_POINTS = 200;

    public DurationChartPanel() {
        setPreferredSize(new Dimension(0, 100));
        setBackground(new Color(40, 40, 40));
    }

    public synchronized void addData(long duration, float fitness) {
        durations.add(duration);
        fitnesses.add(fitness);
        if (durations.size() > MAX_DATA_POINTS) durations.remove(0);
        if (fitnesses.size() > MAX_DATA_POINTS) fitnesses.remove(0);
        repaint();
    }

    public synchronized void clear() {
        durations.clear();
        fitnesses.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (durations.size() < 2 || fitnesses.size() < 2) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Duration Scaling (Blue)
        long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(1); // This line was already correct in the context, but the error implies it was not. Re-adding for clarity.
        float xScale = (float) getWidth() / (durations.size() - 1);
        float yScaleDuration = (float) (getHeight() - 30) / maxDuration;

        // Fitness Scaling (Green)
        float maxFitness = (float) fitnesses.stream().mapToDouble(Float::doubleValue).max().orElse(1.0);
        float minFitness = (float) fitnesses.stream().mapToDouble(Float::doubleValue).min().orElse(0.0);
        float fitnessRange = Math.max(1.0f, maxFitness - minFitness);
        float yScaleFitness = (float) (getHeight() - 30) / fitnessRange;

        int[] xPoints = new int[durations.size()];
        int[] yDurPoints = new int[durations.size()];
        int[] yFitPoints = new int[fitnesses.size()];

        for (int i = 0; i < durations.size(); i++) {
            xPoints[i] = (int) (i * xScale);
            yDurPoints[i] = getHeight() - 10 - (int) (durations.get(i) * yScaleDuration);
            yFitPoints[i] = getHeight() - 10 - (int) ((fitnesses.get(i) - minFitness) * yScaleFitness);
        }

        // Draw Duration Line
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0, 150, 255));
        g2.drawPolyline(xPoints, yDurPoints, durations.size());

        // Draw Fitness Line
        g2.setColor(new Color(50, 200, 50));
        g2.drawPolyline(xPoints, yFitPoints, fitnesses.size());
        
        // Legend
        int legendX = getWidth() - 130;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(0, 150, 255));
        g2.fillRect(legendX, 5, 8, 8);
        g2.setColor(Color.WHITE); // Set color for text
        g2.drawString("Time (ms)", legendX + 12, 13);

        g2.setColor(new Color(50, 200, 50));
        g2.fillRect(legendX, 18, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString("Best Fitness", legendX + 12, 26);

        // Current Values
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        if (!durations.isEmpty()) {
            g2.drawString(String.format("Time (Blue): %d ms | Best Fitness (Green): %.2f", 
                durations.get(durations.size() - 1), fitnesses.get(fitnesses.size() - 1)), 5, 12);
        }
    }
}