/*
 * tekrei tarafindan Jan 20, 2006 tarihinde yaratilmistir.
 */
package net.tekrei.hedos;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.Point;
import net.tekrei.hedos.graphics.X3DUtility;
import net.tekrei.hedos.ui.PropertiesPanel;
import net.tekrei.hedos.utility.Messages;
import net.tekrei.hedos.utility.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class HedosFrame extends JFrame {
    private PropertiesPanel pnlProperties;

    private JMenuBar menu;

    private HedosFrame() {
        init();
    }

    public static void main(String[] args) {
        new HedosFrame();
    }

    private void init() {
        uiInit();
        initScene();
    }

    private void uiInit() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                X3DUtility.shutdown();
                System.exit(0);
            }
        });

        Messages.getInstance().init(Messages.Language.en);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(0);

        pnlProperties = new PropertiesPanel(this);
        splitPane.setLeftComponent(pnlProperties);
        splitPane.setRightComponent(X3DUtility.createBrowser());

        this.add(splitPane, BorderLayout.CENTER);
        this.setJMenuBar(getMenu());

        setSize(1280, 768);
        setVisible(true);
    }


    private JMenuBar getMenu() {
        if (menu == null) {
            menu = new JMenuBar();

            JMenu mnDosya = new JMenu(Messages.getInstance().getString("HedosFrame.Dosya"));
            JMenuItem mnItem = new JMenuItem(Messages.getInstance().getString("HedosFrame.AyarlariKaydet"));
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString("HedosFrame.AyarlariYukle"));
            mnItem.addActionListener(e -> {
                // TODO loadFile();
            });
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString("HedosFrame.AyarlariFarkliKaydet"));
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString("HedosFrame.CokluTest"));
            mnItem.addActionListener(e -> multipleCalculation());
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString("HedosFrame.Cikis"));
            mnItem.addActionListener(e -> System.exit(0));
            mnDosya.add(mnItem);

            menu.add(mnDosya);
        }

        return menu;
    }

    private void initScene() {
        ArrayList<Point> gecici = Settings.getInstance().getHedefler();

        for (Point nokta : gecici) {
            addTarget(nokta);
        }

        pnlProperties.updatePanel();
    }

    private void drawPath(int[] path) {
        float[] coordinates = new float[path.length * 3];
        int[] index = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            Point target = pnlProperties.getTarget(path[i]);
            coordinates[i * 3] = target.x;
            coordinates[i * 3 + 1] = target.y;
            coordinates[i * 3 + 2] = target.z;
            index[i] = i;
        }
        X3DUtility.addLineSet(coordinates, index);
    }

    public void addTarget(Point target) {
        X3DUtility.createTargetAt(target);
        pnlProperties.addTarget(target);
    }

    public void deleteTarget(Point target) {
        X3DUtility.deleteNode(target.getName());
    }

    public void calculate() {
        pnlProperties.updateGPParameter();
        try {
            Chromosome enIyi = new GeneticAlgorithm().run(pnlProperties.getTargets());
            drawPath(enIyi.getGenes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void travel() {
        X3DUtility.setRoute(pnlProperties.getTargets());
        X3DUtility.setTimer(pnlProperties.getTargetCount());
    }

    public void multipleCalculation() {
        String deger = JOptionPane.showInputDialog(null, Messages.getInstance()
                .getString("HedosFrame.CokluTest"), "10");
        if (deger != null && !deger.equals("")) {
            int denemeSayisi = Integer.parseInt(deger);
            for (int i = 0; i < denemeSayisi; i++) {
                calculate();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
