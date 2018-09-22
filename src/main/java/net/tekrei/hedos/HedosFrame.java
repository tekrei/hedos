/*
 * tekrei tarafindan Jan 20, 2006 tarihinde yaratilmistir.
 */
package net.tekrei.hedos;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.GAParameters;
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
    private static final long serialVersionUID = 1L;

    private PropertiesPanel pnlSol;

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

        pnlSol = new PropertiesPanel(this);
        splitPane.setLeftComponent(pnlSol);
        splitPane.setRightComponent(X3DUtility.createBrowser());

        this.add(splitPane, BorderLayout.CENTER);
        this.setJMenuBar(getMenu());

        setSize(1280, 768);
        setVisible(true);
    }


    private JMenuBar getMenu() {
        if (menu == null) {
            menu = new JMenuBar();

            JMenu mnDosya = new JMenu(Messages.getInstance().getString(
                    "HedosFrame.Dosya"));

            JMenuItem mnItem = new JMenuItem(Messages.getInstance().getString(
                    "HedosFrame.AyarlariKaydet"));
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString(
                    "HedosFrame.AyarlariYukle"));
            mnItem.addActionListener(e -> {
                // TODO loadFile();
            });
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString(
                    "HedosFrame.AyarlariFarkliKaydet"));
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString(
                    "HedosFrame.CokluTest"));
            mnItem.addActionListener(e -> multipleCalculation());
            mnDosya.add(mnItem);

            mnItem = new JMenuItem(Messages.getInstance().getString(
                    "HedosFrame.Cikis"));
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

        pnlSol.updatePanel();
    }

    private void drawPath(int[] path) {

        float[] coordinates = new float[path.length * 3];
        int[] index = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            coordinates[i * 3] = GAParameters.getInstance().getHedef(i).x;
            coordinates[i * 3 + 1] = GAParameters.getInstance().getHedef(i).y;
            coordinates[i * 3 + 2] = GAParameters.getInstance().getHedef(i).z;
            index[i] = i;
        }

        X3DUtility.addLineSet(coordinates, index);
    }

    public void addTarget(Point target) {
        X3DUtility.createTargetAt(target);
        GAParameters.getInstance().addTarget(target);
        pnlSol.addTarget(target);
    }

    public boolean deleteTarget(Point target) {
        X3DUtility.deleteNode(target.getName());
        return GAParameters.getInstance().deleteTarget(target);
    }

    public void calculate() {
        long hesaplamaBaslangic = System.currentTimeMillis();
        pnlSol.updateGPParameter();

        try {
            GeneticAlgorithm isleyici = new GeneticAlgorithm();
            Chromosome enIyi = isleyici.isle();
            String duyuru = GAParameters.getInstance().getHedefSayi()
                    + "\t" + GAParameters.getInstance().getMutasyonTipi()
                    + "\t" + GAParameters.getInstance().getCaprazlamaTipi()
                    + "\t"
                    + GAParameters.getInstance().getMutasyonOlasiligi()
                    + "\t"
                    + GAParameters.getInstance().getCaprazlamaOlasiligi()
                    + "\t" + enIyi.toString() + "\t"
                    + (System.currentTimeMillis() - hesaplamaBaslangic);
            pnlSol.duyuruEkle(duyuru);
            System.out.println(duyuru);
            drawPath(enIyi.getGenes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void travel() {
        X3DUtility.setRoute();
        X3DUtility.setTimer();
    }

    public void multipleCalculation() {
        String deger = JOptionPane.showInputDialog(null, Messages.getInstance()
                .getString("HedosFrame.CokluTest"), "100");
        if (deger != null && !deger.equals("")) {
            int denemeSayisi = Integer.parseInt(deger);
            for (int i = 0; i < denemeSayisi; i++) {
                calculate();
            }
        }
    }
}
