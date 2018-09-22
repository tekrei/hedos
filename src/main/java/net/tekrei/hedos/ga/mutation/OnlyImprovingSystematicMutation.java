package net.tekrei.hedos.ga.mutation;

import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.GAParameters;

/**
 * Bu sinifin amaci gelistiren bir mutasyon bulana kadar sistematik bir sekilde
 * mutasyon denemeleri yapmaktir.
 *
 * @author emre
 */
public class OnlyImprovingSystematicMutation extends Mutation {

    @Override
    void mutate(Chromosome degisecek) {
        // Mutasyon olacaksa eger rastgele bireyleri secip
        // sirayla kromozomlardan iki degerin yerini degistirecegiz
        // eger olusan yeni birey onceki bireyden daha iyi ise dongu bitecek
        // degilse
        // dongu devam edecek
        int[] tempDeger = degisecek.getGenes().clone();

        if (GAParameters.getInstance().nextFloat() < GAParameters
                .getInstance().getMutasyonOlasiligi()) {
            boolean devam = true;

            for (int i = 0; (i < tempDeger.length) && devam; i++) {
                for (int j = i + 1; (j < tempDeger.length) && devam; j++) {
                    // Eger iki eleman sayisindan buyuk ise
                    // Sirayla secilen iki elemanin yerlerini degistiriyoruz
                    int temp = tempDeger[i];
                    tempDeger[i] = tempDeger[j];
                    tempDeger[i] = temp;

                    // Eger mutasyon sonucu olusan kromozom oncekinden daha
                    // uygun ise
                    // degistirecegiz ve cikacagiz
                    if (Chromosome.calculateCost(tempDeger) < degisecek.getCost()) {
                        degisecek.setGenes(tempDeger);
                        devam = false;
                    }
                }
            }
        }
    }
}
