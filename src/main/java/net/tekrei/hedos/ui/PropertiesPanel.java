package net.tekrei.hedos.ui;

import net.tekrei.hedos.HedosFrame;
import net.tekrei.hedos.ga.data.GAParameters;
import net.tekrei.hedos.ga.data.Point;
import net.tekrei.hedos.utility.Messages;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PropertiesPanel extends JPanel {

    private final ArrayList<Point> targets = new ArrayList<>();
    private final HedosFrame hedosFrame;
    private JTextField nesilSayisi = null;
    private JTextField toplumBuyuklugu = null;
    private JTextField mutasyonOlasilik = null;
    private JTextField caprazlamaOlasilik = null;
    private JRadioButton seckinlik;
    private JComboBox<String> caprazlamaTipi = null;
    private JComboBox<String> mutasyonTipi = null;
    private JTextField hedefX = null;
    private JTextField hedefY = null;
    private JTextField hedefZ = null;
    private JButton btnHedefEkle = null;
    private JComboBox<Point> cmbTargets = null;
    private JButton btnSil = null;
    private JButton btnDolas = null;
    private JButton btnHesapla = null;
    private JButton btnCokluHesapla = null;
    public PropertiesPanel(HedosFrame _hedosFrame) {
        super();
        hedosFrame = _hedosFrame;
        initialize();
    }

    private void initialize() {
        this.setLayout(null);

        int yKonum = 0;
        int yukseklik = 20;
        int xBaslangic = 5;
        int uzunluk = 190;

        int lblUzunluk = 130;

        JLabel label = new JLabel(Messages.getInstance().getString(
                "SolPanel.ToplumBuyukluk"));
        label.setBounds(xBaslangic, yKonum, lblUzunluk, yukseklik);
        this.add(label);
        this.add(getTxtPopulasyonBuyukluk(new Rectangle(
                lblUzunluk + xBaslangic, yKonum, uzunluk - lblUzunluk,
                yukseklik)));

        yKonum += yukseklik;

        label = new JLabel(Messages.getInstance().getString(
                "SolPanel.NesilSayisi"));
        label.setBounds(xBaslangic, yKonum, lblUzunluk, yukseklik);
        this.add(label);
        this.add(getTxtNesilSayisi(new Rectangle(lblUzunluk + xBaslangic,
                yKonum, uzunluk - lblUzunluk, yukseklik)));

        yKonum += yukseklik;

        label = new JLabel(Messages.getInstance().getString(
                "SolPanel.CaprazlamaOlasiligi"));
        label.setBounds(xBaslangic, yKonum, lblUzunluk, yukseklik);
        this.add(label);
        this.add(getTxtCaprazlamaOlasilik(new Rectangle(
                lblUzunluk + xBaslangic, yKonum, uzunluk - lblUzunluk,
                yukseklik)));

        yKonum += yukseklik;

        label = new JLabel(Messages.getInstance().getString(
                "SolPanel.MutasyonOlasiligi"));
        label.setBounds(xBaslangic, yKonum, lblUzunluk, yukseklik);
        this.add(label);
        this.add(getTxtMutasyonOlasilik(new Rectangle(lblUzunluk + xBaslangic,
                yKonum, uzunluk - lblUzunluk, yukseklik)));

        yKonum += yukseklik;

        this.add(getElitism(new Rectangle(xBaslangic, yKonum, uzunluk,
                yukseklik)));

        yKonum += yukseklik;

        label = new JLabel(Messages.getInstance().getString(
                "SolPanel.MutasyonTipi"));
        label.setBounds(xBaslangic, yKonum, uzunluk, yukseklik);
        this.add(label);

        yKonum += yukseklik;

        this.add(getMutasyonTipi(new Rectangle(xBaslangic, yKonum, uzunluk,
                yukseklik)));

        yKonum += yukseklik;

        label = new JLabel(Messages.getInstance().getString(
                "SolPanel.CaprazlamaTipi"));
        label.setBounds(xBaslangic, yKonum, uzunluk, yukseklik);
        this.add(label);

        yKonum += yukseklik;

        this.add(getCaprazlamaTipi(new Rectangle(xBaslangic, yKonum, uzunluk,
                yukseklik)));

        yKonum += yukseklik;

        this.add(
                getTxtHedefX(new Rectangle(xBaslangic, yKonum, 40, yukseklik)),
                null);
        this.add(getTxtHedefY(new Rectangle(40 + xBaslangic, yKonum, 40,
                yukseklik)), null);
        this.add(getTxtHedefZ(new Rectangle(80 + xBaslangic, yKonum, 40,
                yukseklik)), null);

        this.add(getBtnHedefEkle(new Rectangle(120 + xBaslangic, yKonum, 70,
                yukseklik)), null);

        yKonum += yukseklik;

        this.add(getCmbHedefler(new Rectangle(xBaslangic, yKonum, 120,
                yukseklik)), null);

        this.add(getBtnSil(new Rectangle(120 + xBaslangic, yKonum, 70,
                yukseklik)), null);

        yKonum += yukseklik;

        this.add(getBtnHesapla(new Rectangle(xBaslangic, yKonum, uzunluk,
                yukseklik)), null);

        yKonum += yukseklik;

        this.add(getBtnCokluHesapla(new Rectangle(xBaslangic, yKonum, uzunluk,
                yukseklik)), null);

        yKonum += yukseklik;

        this.add(getBtnDolas(new Rectangle(xBaslangic, yKonum, uzunluk,
                yukseklik)), null);

        yKonum += yukseklik;

        this.setSize(uzunluk, yKonum + 350);
    }

    private JTextField getTxtNesilSayisi(Rectangle rect) {
        if (nesilSayisi == null) {
            nesilSayisi = new JTextField();
            nesilSayisi.setBounds(rect);
        }

        return nesilSayisi;
    }

    private JTextField getTxtPopulasyonBuyukluk(Rectangle rect) {
        if (toplumBuyuklugu == null) {
            toplumBuyuklugu = new JTextField();
            toplumBuyuklugu.setBounds(rect);
        }

        return toplumBuyuklugu;
    }

    private JTextField getTxtCaprazlamaOlasilik(Rectangle rect) {
        if (caprazlamaOlasilik == null) {
            caprazlamaOlasilik = new JTextField();
            caprazlamaOlasilik.setBounds(rect);
        }

        return caprazlamaOlasilik;
    }

    private JTextField getTxtMutasyonOlasilik(Rectangle rect) {
        if (mutasyonOlasilik == null) {
            mutasyonOlasilik = new JTextField();
            mutasyonOlasilik.setBounds(rect);
        }

        return mutasyonOlasilik;
    }

    private JComboBox getMutasyonTipi(Rectangle bounds) {
        if (mutasyonTipi == null) {
            mutasyonTipi = new JComboBox<>(Messages.getMutations());
            mutasyonTipi.setBounds(bounds);
        }

        return mutasyonTipi;
    }

    private JRadioButton getElitism(Rectangle bounds) {
        if (seckinlik == null) {
            seckinlik = new JRadioButton(Messages.getInstance().getString(
                    "SolPanel.Seckinlik"));
            seckinlik.setBounds(bounds);
        }

        return seckinlik;
    }

    private JComboBox getCaprazlamaTipi(Rectangle bounds) {
        if (caprazlamaTipi == null) {
            caprazlamaTipi = new JComboBox<>(Messages.getCrossovers());
            caprazlamaTipi.setBounds(bounds);
        }

        return caprazlamaTipi;
    }

    private JComboBox getCmbHedefler(Rectangle bounds) {
        if (cmbTargets == null) {
            cmbTargets = new JComboBox<>(new ArrayListComboBoxModel());
            cmbTargets.setBounds(bounds);
        }

        return cmbTargets;
    }

    private JTextField getTxtHedefX(Rectangle bounds) {
        if (hedefX == null) {
            hedefX = new JTextField();
            hedefX.setBounds(bounds);
        }

        return hedefX;
    }

    private JTextField getTxtHedefY(Rectangle bounds) {
        if (hedefY == null) {
            hedefY = new JTextField();
            hedefY.setBounds(bounds);
        }

        return hedefY;
    }

    private JTextField getTxtHedefZ(Rectangle bounds) {
        if (hedefZ == null) {
            hedefZ = new JTextField();
            hedefZ.setBounds(bounds);
        }

        return hedefZ;
    }

    private JButton getBtnHedefEkle(Rectangle bounds) {
        if (btnHedefEkle == null) {
            btnHedefEkle = new JButton();
            btnHedefEkle.setBounds(bounds);
            btnHedefEkle.setText(Messages.getInstance().getString(
                    "SolPanel.Ekle"));
            btnHedefEkle.setMnemonic('e');
            btnHedefEkle.addActionListener(e -> addTarget());
        }

        return btnHedefEkle;
    }

    private void addTarget() {
        float _hedefX = Float.parseFloat(hedefX.getText());
        float _hedefY = Float.parseFloat(hedefY.getText());
        float _hedefZ = Float.parseFloat(hedefZ.getText());
        hedosFrame.addTarget(new Point(_hedefX, _hedefY, _hedefZ));
    }

    private JButton getBtnSil(Rectangle bounds) {
        if (btnSil == null) {
            btnSil = new JButton();
            btnSil.setBounds(bounds);
            btnSil.setText(Messages.getInstance().getString("SolPanel.Sil"));
            btnSil.setMnemonic('s');
            btnSil.addActionListener(e -> deleteTarget());
        }

        return btnSil;
    }

    private void deleteTarget() {
        Point silinecek = (Point) cmbTargets.getSelectedItem();
        hedosFrame.deleteTarget(silinecek);
        targets.remove(silinecek);
        cmbTargets.setSelectedIndex(0);
        cmbTargets.updateUI();
    }

    private JButton getBtnDolas(Rectangle bounds) {
        if (btnDolas == null) {
            btnDolas = new JButton();
            btnDolas.setBounds(bounds);
            btnDolas
                    .setText(Messages.getInstance().getString("SolPanel.Dolas"));
            btnDolas.setMnemonic('d');
            btnDolas.addActionListener(e -> dolasTiklandi());
        }

        return btnDolas;
    }

    private JButton getBtnHesapla(Rectangle bounds) {
        if (btnHesapla == null) {
            btnHesapla = new JButton();
            btnHesapla.setBounds(bounds);
            btnHesapla.setText(Messages.getInstance().getString(
                    "SolPanel.Hesapla"));
            btnHesapla.setMnemonic('h');
            btnHesapla.addActionListener(e -> hedosFrame.calculate());
        }

        return btnHesapla;
    }

    private JButton getBtnCokluHesapla(Rectangle bounds) {
        if (btnCokluHesapla == null) {
            btnCokluHesapla = new JButton();
            btnCokluHesapla.setBounds(bounds);
            btnCokluHesapla.setText(Messages.getInstance().getString(
                    "HedosFrame.CokluTest"));
            btnCokluHesapla.setMnemonic('e');
            btnCokluHesapla.addActionListener(e -> hedosFrame.multipleCalculation());
        }

        return btnCokluHesapla;
    }

    public void addTarget(Point nokta) {
        targets.add(nokta);
    }

    public void updateGPParameter() {
        GAParameters.getInstance().setCaprazlamaOlasiligi(
                Float.parseFloat(caprazlamaOlasilik.getText()));
        GAParameters.getInstance().setMutationProbability(
                Float.parseFloat(mutasyonOlasilik.getText()));
        GAParameters.getInstance().setToplumBuyuklugu(
                Integer.parseInt(toplumBuyuklugu.getText()));
        GAParameters.getInstance().setNesilSayisi(
                Integer.parseInt(nesilSayisi.getText()));
        GAParameters.getInstance().setCaprazlamaTipi(
                caprazlamaTipi.getSelectedItem().toString());
        GAParameters.getInstance().setMutasyonTipi(
                mutasyonTipi.getSelectedItem().toString());
        GAParameters.getInstance().setElitism(seckinlik.isSelected());
    }

    public void updatePanel() {
        this.nesilSayisi.setText("" +
                GAParameters.getInstance().getNesilSayisi());
        this.toplumBuyuklugu.setText("" +
                GAParameters.getInstance().getToplumBuyuklugu());
        this.mutasyonOlasilik.setText("" +
                GAParameters.getInstance().getMutationProbability());
        this.caprazlamaOlasilik.setText("" +
                GAParameters.getInstance().getCaprazlamaOlasiligi());
        this.updateUI();
    }

    public int getTargetCount() {
        return targets.size();
    }

    private void dolasTiklandi() {
        hedosFrame.travel();
    }

    public Point getTarget(int i) {
        return targets.get(i);
    }

    public ArrayList<Point> getTargets() {
        return targets;
    }

    class ArrayListComboBoxModel extends AbstractListModel<Point> implements ComboBoxModel<Point> {
        private Point selectedItem;

        public Point getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(Object newValue) {
            selectedItem = (Point) newValue;
        }

        public int getSize() {
            return targets.size();
        }

        public Point getElementAt(int i) {
            return targets.get(i);
        }
    }
}