package implementation.cube.filter;

import abstractions.cube.ICubeFilter;
import abstractions.cube.Triangle;

public final class CubeFilterFactory {

    public static ICubeFilter from(Triangle... triangles) {
        return new ByteCubeFilter(triangles);
    }

    public static ICubeFilter from(byte... triangles) {
        return new ByteCubeFilter(triangles);
    }
}
