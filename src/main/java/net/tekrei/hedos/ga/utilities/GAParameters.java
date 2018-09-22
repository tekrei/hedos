package net.tekrei.hedos.ga.utilities;

import net.tekrei.hedos.ga.crossover.Crossover;
import net.tekrei.hedos.ga.crossover.SinglePointCrossover;
import net.tekrei.hedos.ga.crossover.TwoPointCrossover;
import net.tekrei.hedos.ga.crossover.UniformCrossover;
import net.tekrei.hedos.ga.mutation.Mutation;
import net.tekrei.hedos.ga.mutation.OnlyImprovingRandomMutation;
import net.tekrei.hedos.ga.mutation.OnlyImprovingSystematicMutation;
import net.tekrei.hedos.ga.mutation.RandomMutation;
import net.tekrei.hedos.utility.Messages;
import net.tekrei.hedos.utility.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GAParameters {
    private static final HashMap<String, Crossover> crossovers = new HashMap<>();
    private static final HashMap<String, Mutation> mutations = new HashMap<>();
    private static GAParameters instance = null;

    static {
        crossovers.put(Messages.Caprazlamalar.tekNoktali, new SinglePointCrossover());
        crossovers.put(Messages.Caprazlamalar.ciftNoktali, new TwoPointCrossover());
        crossovers.put(Messages.Caprazlamalar.uniform, new UniformCrossover());

        mutations.put(Messages.Mutasyonlar.normalRastgele, new RandomMutation());
        mutations.put(Messages.Mutasyonlar.sadeceGelistirenRastgele, new OnlyImprovingRandomMutation());
        mutations.put(Messages.Mutasyonlar.sadeceGelistirenSistematik, new OnlyImprovingSystematicMutation());
    }

    private final ArrayList<Point> hedef;
    private final Random generator;
    private int nesilSayisi;
    private int toplumBuyuklugu;
    private float mutasyonOlasiligi;
    private boolean elitism;
    private float caprazlamaOlasiligi;
    private String mutasyonTipi;
    private String caprazlamaTipi;

    private GAParameters() {
        super();
        this.nesilSayisi = Integer.parseInt(Settings.getInstance().getString(
                "nesilSayisi"));
        this.toplumBuyuklugu = Integer.parseInt(Settings.getInstance()
                .getString("toplumBuyuklugu"));
        this.mutasyonOlasiligi = Float.parseFloat(Settings.getInstance()
                .getString("mutasyonOlasiligi"));
        this.elitism = Boolean.parseBoolean(Settings.getInstance().getString(
                "elitism"));
        this.caprazlamaOlasiligi = Float.parseFloat(Settings.getInstance()
                .getString("caprazlamaOlasiligi"));
        this.hedef = new ArrayList<>();

        generator = new Random();
    }

    public static Crossover getCrossover() {
        return crossovers.get(GAParameters.getInstance().getCaprazlamaTipi());
    }

    public static Mutation getMutation() {
        return mutations.get(GAParameters.getInstance().getMutasyonTipi());
    }

    public static GAParameters getInstance() {
        if (instance == null) {
            instance = new GAParameters();
        }
        return instance;
    }

    public void addTarget(Point _nokta) {
        hedef.add(_nokta);
    }

    public boolean deleteTarget(Point _nokta) {
        return hedef.remove(_nokta);
    }

    public Point getHedef(int i) {
        return hedef.get(i);
    }

    public int getHedefSayi() {
        return hedef.size();
    }

    /**
     * iki hedef arasindaki uzakligi bulup donduren metod
     */
    float getDistance(int i, int j) {
        return hedef.get(i).distance(hedef.get(j));
    }

    public boolean isElitism() {
        return elitism;
    }

    public void setElitism(boolean _elitism) {
        this.elitism = _elitism;
    }

    public float getMutasyonOlasiligi() {
        return mutasyonOlasiligi;
    }

    public void setMutasyonOlasiligi(float _mutasyonOlasiligi) {
        this.mutasyonOlasiligi = _mutasyonOlasiligi;
    }

    public int getNesilSayisi() {
        return nesilSayisi;
    }

    public void setNesilSayisi(int _nesilSayisi) {
        this.nesilSayisi = _nesilSayisi;
    }

    public int getToplumBuyuklugu() {
        return toplumBuyuklugu;
    }

    public void setToplumBuyuklugu(int _toplumBuyuklugu) {
        this.toplumBuyuklugu = _toplumBuyuklugu;
    }

    public float getCaprazlamaOlasiligi() {
        return caprazlamaOlasiligi;
    }

    public void setCaprazlamaOlasiligi(float _caprazlamaOlasiligi) {
        this.caprazlamaOlasiligi = _caprazlamaOlasiligi;
    }

    public String getCaprazlamaTipi() {
        return caprazlamaTipi;
    }

    public void setCaprazlamaTipi(String _caprazlamaTipi) {
        this.caprazlamaTipi = _caprazlamaTipi;
    }

    public String getMutasyonTipi() {
        return mutasyonTipi;
    }

    public void setMutasyonTipi(String _mutasyonTipi) {
        this.mutasyonTipi = _mutasyonTipi;
    }

    public boolean caprazla() {
        return nextFloat() < caprazlamaOlasiligi;
    }

    public boolean mutasyon() {
        return nextFloat() < mutasyonOlasiligi;
    }

    public float nextFloat() {
        return generator.nextFloat();
    }

    public int nextInt(int deger) {
        return generator.nextInt(deger);
    }
}
