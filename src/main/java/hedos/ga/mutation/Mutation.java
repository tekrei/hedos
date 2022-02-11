package hedos.ga.mutation;

import hedos.ga.data.Chromosome;
import hedos.ga.data.GAParameters;

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
