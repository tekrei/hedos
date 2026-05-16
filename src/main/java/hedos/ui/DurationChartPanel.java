package hedos.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class DurationChartPanel extends JPanel {
    private final List<Integer> generations = new ArrayList<>();
    private final List<Long> durations = new ArrayList<>();
    private final List<Long> lsDurations = new ArrayList<>();
    private final List<Float> fitnesses = new ArrayList<>();
    private final List<String> lsoNames = new ArrayList<>();
    private final List<Boolean> neighborhoodMarkers = new ArrayList<>();
    private Integer hoverIndex = null;
    private boolean showBubble = false;

    public DurationChartPanel() {
        setPreferredSize(new Dimension(0, 100));
        setBackground(new Color(40, 40, 40));
        setToolTipText(""); // Register with ToolTipManager

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                synchronized (DurationChartPanel.this) {
                    showBubble = false;
                    updateHover(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                synchronized (DurationChartPanel.this) {
                    updateHover(e);
                    if (hoverIndex != null) {
                        showBubble = true;
                        // Trigger tooltip bubble immediately without hover delay
                        ToolTipManager manager = ToolTipManager.sharedInstance();
                        int originalDelay = manager.getInitialDelay();
                        manager.setInitialDelay(0);
                        manager.mouseMoved(e);
                        manager.setInitialDelay(originalDelay);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                synchronized (DurationChartPanel.this) {
                    hoverIndex = null;
                    showBubble = false;
                    repaint();
                }
            }

            private void updateHover(MouseEvent e) {
                Integer newHoverIndex = null;
                if (durations.size() >= 2) {
                    float xScale = (float) getWidth() / (durations.size() - 1);
                    int calculatedIdx = Math.round(e.getX() / xScale);
                    if (calculatedIdx >= 0 && calculatedIdx < durations.size()) {
                        newHoverIndex = calculatedIdx;
                    }
                }
                if (!Objects.equals(hoverIndex, newHoverIndex)) {
                    hoverIndex = newHoverIndex;
                    repaint();
                }
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public synchronized void addData(int gen, long duration, long lsDuration, float fitness, String lsoName, boolean marker) {
        generations.add(gen);
        durations.add(duration);
        lsDurations.add(lsDuration);
        fitnesses.add(fitness);
        lsoNames.add(lsoName);
        neighborhoodMarkers.add(marker);
        repaint();
    }

    public synchronized void clear() {
        generations.clear();
        durations.clear();
        lsDurations.clear();
        fitnesses.clear();
        lsoNames.clear();
        neighborhoodMarkers.clear();
        repaint();
    }

    public synchronized void exportCSV(PrintWriter writer) {
        writer.println();
        writer.println("--- Performance Data ---");
        writer.println("Generation,DurationMs,BestFitness");
        for (int i = 0; i < durations.size(); i++) {
            writer.println(generations.get(i) + "," + durations.get(i) + "," + lsDurations.get(i) + "," + fitnesses.get(i));
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        synchronized (this) {
            if (!showBubble || hoverIndex == null || hoverIndex < 0 || hoverIndex >= durations.size()) {
                return null;
            }

            return String.format("<html><b>Generation %d</b><br/>LSO: %s<br/>Total Time: %d ms<br/>LS Time: %d ms<br/>Best Fitness: %.2f</html>",
                    generations.get(hoverIndex),
                    lsoNames.get(hoverIndex),
                    durations.get(hoverIndex),
                    lsDurations.get(hoverIndex),
                    fitnesses.get(hoverIndex));
        }
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        synchronized (this) {
            if (!showBubble || hoverIndex == null || durations.size() < 2) return null;
            float xScale = (float) getWidth() / (durations.size() - 1);
            int x = Math.round(hoverIndex * xScale);
            // Position the tooltip at the top of the highlight line for better visibility
            return new Point(x, 25);
        }
    }

    public void saveAsImage(File file) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        this.paint(g2);
        g2.dispose();
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save chart: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (durations.size() < 2 || fitnesses.size() < 2) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Duration Scaling (Blue)
        long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(1);
        float xScale = (float) getWidth() / (durations.size() - 1);
        float yScaleDuration = (float) (getHeight() - 30) / maxDuration;

        // Fitness Scaling (Green)
        float maxFitness = (float) fitnesses.stream().mapToDouble(Float::doubleValue).max().orElse(1.0);
        float minFitness = (float) fitnesses.stream().mapToDouble(Float::doubleValue).min().orElse(0.0);
        float fitnessRange = Math.max(1.0f, maxFitness - minFitness);
        float yScaleFitness = (float) (getHeight() - 30) / fitnessRange;

        int[] xPoints = new int[durations.size()];
        int[] yDurPoints = new int[durations.size()];
        int[] yLsPoints = new int[lsDurations.size()];
        int[] yFitPoints = new int[fitnesses.size()];

        for (int i = 0; i < durations.size(); i++) {
            xPoints[i] = (int) (i * xScale);
            yDurPoints[i] = getHeight() - 10 - (int) (durations.get(i) * yScaleDuration);
            yLsPoints[i] = getHeight() - 10 - (int) (lsDurations.get(i) * yScaleDuration);
            yFitPoints[i] = getHeight() - 10 - (int) ((fitnesses.get(i) - minFitness) * yScaleFitness);
        }

        // Draw Neighborhood Increase Markers
        g2.setColor(new Color(255, 50, 50, 150));
        for (int i = 0; i < neighborhoodMarkers.size(); i++) {
            if (neighborhoodMarkers.get(i)) {
                g2.drawLine(xPoints[i], 30, xPoints[i], getHeight() - 10);
            }
        }

        // Draw vertical marker for hover
        if (hoverIndex != null && hoverIndex < durations.size()) {
            int x = xPoints[hoverIndex];
            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2.drawLine(x, 30, x, getHeight() - 10);

            // Highlight data points with circles
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0, 150, 255)); g2.drawOval(x - 3, yDurPoints[hoverIndex] - 3, 6, 6);
            g2.setColor(new Color(255, 150, 0)); g2.drawOval(x - 3, yLsPoints[hoverIndex] - 3, 6, 6);
            g2.setColor(new Color(50, 200, 50)); g2.drawOval(x - 3, yFitPoints[hoverIndex] - 3, 6, 6);
        }

        // Draw Local Search Area (Orange) - Filled area for better visibility of component time
        Polygon lsArea = new Polygon();
        lsArea.addPoint(0, getHeight() - 10);
        for (int i = 0; i < durations.size(); i++) {
            lsArea.addPoint(xPoints[i], yLsPoints[i]);
        }
        lsArea.addPoint(xPoints[durations.size() - 1], getHeight() - 10);
        
        g2.setColor(new Color(255, 150, 0, 60)); // Transparent fill
        g2.fillPolygon(lsArea);
        g2.setColor(new Color(255, 150, 0, 180)); // Solid border
        g2.drawPolyline(xPoints, yLsPoints, lsDurations.size());

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

        g2.setColor(new Color(255, 150, 0));
        g2.fillRect(legendX, 18, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString("LS Time", legendX + 12, 26);

        g2.setColor(new Color(50, 200, 50));
        g2.fillRect(legendX, 31, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString("Best Fitness", legendX + 12, 39);

        // Current Values
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        if (!durations.isEmpty()) {
            g2.drawString(String.format("Time (Blue): %d ms | Best Fitness (Green): %.2f", 
                durations.get(durations.size() - 1), fitnesses.get(fitnesses.size() - 1)), 5, 12);
        }
    }
}