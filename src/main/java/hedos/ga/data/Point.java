package hedos.ga.data;

import javax.vecmath.Point3f;

public class Point extends Point3f {
    private String name;

    public Point(float _x, float _y, float _z) {
        super(_x, _y, _z);
        setName();
    }

    private void setName() {
        name = "N_" + getX() + "_" + getY() + "_" + getZ();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
