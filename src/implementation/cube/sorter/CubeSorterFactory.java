package implementation.cube.sorter;

import abstractions.cube.CubeType;
import abstractions.cube.ICube;

public final class CubeSorterFactory {

    public static HashCubeSorter makeHashCubeSorter(ICube[] cubes) {
        return new HashCubeSorter(cubes);
    }

    public static ArrayCubeSorter makePrimitiveCubeSorter(ICube[] cubes) {
        return new ArrayCubeSorter(cubes);
    }

    public static ArrayCubeSorter from(ICube[] cubes, CubeType t) {
        return new ArrayCubeSorter(cubes);
    }
}
