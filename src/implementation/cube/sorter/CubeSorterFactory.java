package implementation.cube.sorter;

import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSorter;

import java.util.Arrays;

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

    public static ArrayCubeSorter from(ICube[] cubes, CubeType t) {
        System.out.println("Making sorter for " + t + ": " + Arrays.toString(cubes));
        return new ArrayCubeSorter(cubes);
    }
}
