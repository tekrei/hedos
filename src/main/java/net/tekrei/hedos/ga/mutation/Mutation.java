package net.tekrei.hedos.ga.mutation;

import net.tekrei.hedos.ga.utilities.Chromosome;
import net.tekrei.hedos.ga.utilities.GAParameters;

public abstract class Mutation {
    public Chromosome[] mutate(Chromosome[] toplum) {
        for (int i = 0; i < toplum.length; i = i + 2) {
            try {
                if (GAParameters.getInstance().mutasyon()) {
                    mutate(toplum[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return toplum;
    }

    abstract void mutate(Chromosome chromosome);
}
