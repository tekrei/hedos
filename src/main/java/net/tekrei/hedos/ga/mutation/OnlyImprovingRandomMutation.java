package net.tekrei.hedos.ga.mutation;

import net.tekrei.hedos.ga.GeneticAlgorithm;
import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.GAParameters;

/**
 * Bu sinifin amaci sadece gelistiren rastgele ("Random, only improving")
 * mutasyon uygulamak
 *
 * @author emre
 */
public class OnlyImprovingRandomMutation extends Mutation {

    @Override
    void mutate(Chromosome degisecek) {
        // Mutasyon olacaksa eger rastgele bireyleri secip
        // rastgele kromozomlarindan iki degerin yerini degistirecegiz
        int[] tempDeger = degisecek.getGenes().clone();

        if (GAParameters.getInstance().nextFloat() < GAParameters
                .getInstance().getMutationProbability()) {
            int bir = GAParameters.getInstance().nextInt(tempDeger.length);
            int iki = GAParameters.getInstance().nextInt(tempDeger.length);

            // Rastgele secilen iki elemanin yerlerini degistiriyoruz
            int temp = tempDeger[bir];
            tempDeger[bir] = tempDeger[iki];
            tempDeger[iki] = temp;
        }
        // Eger mutasyon sonucu olusan kromozom oncekinden daha uygun ise
        // degistirecegiz
        if (GeneticAlgorithm.calculateCost(tempDeger) < degisecek.getCost()) {
            degisecek.setGenes(tempDeger, GeneticAlgorithm.calculateCost(tempDeger));
        }
    }
}
