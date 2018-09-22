package net.tekrei.hedos.utility;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateRandomTargets extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Random generator = new Random(System.currentTimeMillis());
    private JTextField txtHedefSayisi = null;
    private JButton btnUret = null;

    private GenerateRandomTargets() {
        super();
        initialize();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new GenerateRandomTargets();
    }

    private void initialize() {
        JLabel jLabel = new JLabel();
        jLabel.setText("Hedef Sayisi:");
        this.getContentPane().setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(360, 75));
        this.getContentPane().add(jLabel, java.awt.BorderLayout.WEST);
        this.getContentPane().add(getTxtHedefSayisi(),
                java.awt.BorderLayout.CENTER);
        this.getContentPane().add(getBtnUret(), java.awt.BorderLayout.SOUTH);
    }

    private JTextField getTxtHedefSayisi() {
        if (txtHedefSayisi == null) {
            txtHedefSayisi = new JTextField();
        }

        return txtHedefSayisi;
    }

    private JButton getBtnUret() {
        if (btnUret == null) {
            btnUret = new JButton();
            btnUret.setText("Üret");
            btnUret.addActionListener(e -> uret());
        }

        return btnUret;
    }

    private void uret() {
        try {
            int hedefSayisi = Integer.parseInt(txtHedefSayisi.getText());
            BufferedWriter writer = new BufferedWriter(new FileWriter("H"
                    + hedefSayisi + ".txt"));
            writer.write("HedefSayisi=" + hedefSayisi + "\n");

            for (int i = 0; i < hedefSayisi; i++) {
                writer.write("Hedef" + i + "=" + generator.nextInt(100) + "|"
                        + generator.nextInt(100) + "|" + generator.nextInt(100)
                        + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} // @jve:decl-index=0:visual-constraint="10,10"
