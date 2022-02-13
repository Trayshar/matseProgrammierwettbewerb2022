package implementation.cube;

import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;

public class CubeFilter {

    private final Triangle[] sides;

    public CubeFilter(Triangle[] sides) {
        this.sides = sides;
    }

    public boolean match(ICube cube) {
        return false;
    }
}
