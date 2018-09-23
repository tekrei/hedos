package net.tekrei.hedos.utility;

import java.lang.reflect.Field;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static Messages _instance = null;

    private static ResourceBundle RESOURCE_BUNDLE = null;

    private Messages() {
    }

    public static Messages getInstance() {
        if (_instance == null) {
            _instance = new Messages();
        }

        return _instance;
    }

    public static String[] getMutations() {
        return getFieldsAsStringArray(Mutasyonlar.class);
    }

    public static String[] getCrossovers() {
        return getFieldsAsStringArray(Caprazlamalar.class);
    }

    private static String[] getFieldsAsStringArray(Class snf) {
        Field[] sahalar = snf.getDeclaredFields();
        String[] sonuc = new String[sahalar.length];

        for (int i = 0; i < sonuc.length; i++) {
            try {
                sonuc[i] = sahalar[i].get(snf).toString();
            } catch (Exception e) {
                System.err.println(sahalar[i].getName()
                        + Messages.getInstance()
                        .getString("Ayarlar.eklenemedi"));
            }
        }

        return sonuc;
    }

    public void init(String _language) {
        if (_language != null) {
            RESOURCE_BUNDLE = ResourceBundle.getBundle(_language);
        } else {
            RESOURCE_BUNDLE = ResourceBundle.getBundle(Language.tr);
        }
    }

    public String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public final static class Mutasyonlar {

        public final static String normalRastgele = Messages.getInstance()
                .getString("Ayarlar.NormalRastgele");

        public final static String sadeceGelistirenRastgele = Messages
                .getInstance().getString("Ayarlar.SadeceGelistirenRastgele");

        public final static String sadeceGelistirenSistematik = Messages
                .getInstance().getString("Ayarlar.SadeceGelistirenSistematik");
    }

    public final static class Caprazlamalar {
        public final static String tekNoktali = Messages.getInstance()
                .getString("Ayarlar.TekNoktali");
        public final static String ciftNoktali = Messages.getInstance()
                .getString("Ayarlar.CiftNoktali");
        public final static String uniform = Messages.getInstance().getString(
                "Ayarlar.uniform");
    }

    public final static class Language {
        public final static String en = "en";
        final static String tr = "tr";
    }
}
