package hedos.ga.data;

import java.util.UUID;

public record Point(float x, float y, float z, String name) {
    
    public Point(float x, float y, float z) {
        this(x, y, z, "P_" + UUID.randomUUID().toString().substring(0, 8));
    }
    
}