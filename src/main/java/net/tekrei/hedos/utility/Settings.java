package net.tekrei.hedos.utility;

import net.tekrei.hedos.ga.data.Point;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class Settings {

    private static ResourceBundle RESOURCE_BUNDLE = null;

    private static Settings ornek;
    private Point baslangic = null;
    private ArrayList<Point> hedefler = null;

    private Settings() {
        init();
    }

    public static Settings getInstance() {
        if (ornek == null) {
            ornek = new Settings();
        }

        return ornek;
    }

    private void init() {
        RESOURCE_BUNDLE = ResourceBundle.getBundle("settings");
    }

    public String getString(String key) {
        return RESOURCE_BUNDLE.getString(key);
    }

    public Point getBaslangic() {
        if (baslangic == null) {
            float[] koordinatlar = getKoordinatlar(RESOURCE_BUNDLE
                    .getString("Baslangic"));
            baslangic = new Point(koordinatlar[0], koordinatlar[1],
                    koordinatlar[2]
            );
        }

        return baslangic;
    }

    public ArrayList<Point> getHedefler() {
        if (hedefler == null) {
            int hedefSayisi = Integer.parseInt(RESOURCE_BUNDLE.getString("HedefSayisi"));
            hedefler = new ArrayList<>();

            for (int i = 0; i < hedefSayisi; i++) {
                float[] koordinat = getKoordinatlar(RESOURCE_BUNDLE.getString("Hedef" + i));
                hedefler.add(new Point(koordinat[0], koordinat[1], koordinat[2]));
            }
        }

        return hedefler;
    }

    private float[] getKoordinatlar(String koordinat) {
        float[] sonuc = new float[3];
        StringTokenizer tok = new StringTokenizer(koordinat, RESOURCE_BUNDLE
                .getString("KoordinatAyrac"));
        int i = 0;

        while (tok.hasMoreElements()) {
            sonuc[i++] = Float.parseFloat(tok.nextToken());
        }

        return sonuc;
    }
}