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

    private final static ICubeFilter defaultFilter = CubeFilterFactory.from(Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone, Triangle.AnyNotNone);

    /**
     * Returns a filter with "Triangle.AnyNotNone" on each side
     */
    public static ICubeFilter defaultFilter() {
        return defaultFilter.cloneFilter();
    }
}
