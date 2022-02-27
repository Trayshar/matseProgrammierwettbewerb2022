package implementation.cube.sorter;

import abstractions.cube.ICube;
import abstractions.cube.ICubeSorter;

public final class CubeSorterFactory {

    public static ICubeSorter from(ICube[] cubes) {
        if(cubes.length < 20 ) return new HashCubeSorter(cubes);
        return new ArrayCubeSorter(cubes);
    }

    public static HashCubeSorter makeHashCubeSorter(ICube[] cubes) {
        return new HashCubeSorter(cubes);
    }

    public static ArrayCubeSorter makePrimitiveCubeSorter(ICube[] cubes) {
        return new ArrayCubeSorter(cubes);
    }
}
