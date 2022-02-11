package hedos.ga.data;

import hedos.ga.crossover.Crossover;
import hedos.ga.crossover.SinglePointCrossover;
import hedos.ga.crossover.TwoPointCrossover;
import hedos.ga.crossover.UniformCrossover;
import hedos.ga.mutation.Mutation;
import hedos.ga.mutation.OnlyImprovingRandomMutation;
import hedos.ga.mutation.OnlyImprovingSystematicMutation;
import hedos.ga.mutation.RandomMutation;
import hedos.utility.Messages;
import hedos.utility.Settings;

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

    private final Random generator;
    private int nesilSayisi;
    private int toplumBuyuklugu;
    private float mutationProbability;
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
        this.mutationProbability = Float.parseFloat(Settings.getInstance()
                .getString("mutasyonOlasiligi"));
        this.elitism = Boolean.parseBoolean(Settings.getInstance().getString(
                "elitism"));
        this.caprazlamaOlasiligi = Float.parseFloat(Settings.getInstance()
                .getString("caprazlamaOlasiligi"));
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

    public boolean isElitism() {
        return elitism;
    }

    public void setElitism(boolean _elitism) {
        this.elitism = _elitism;
    }

    public float getMutationProbability() {
        return mutationProbability;
    }

    public void setMutationProbability(float _mutasyonOlasiligi) {
        this.mutationProbability = _mutasyonOlasiligi;
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
        return nextFloat() < mutationProbability;
    }

    public float nextFloat() {
        return generator.nextFloat();
    }

    public int nextInt(int deger) {
        return generator.nextInt(deger);
    }
}
