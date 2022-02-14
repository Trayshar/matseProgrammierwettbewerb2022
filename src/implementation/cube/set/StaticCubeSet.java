package implementation.cube.set;

import abstractions.cube.ICube;
import abstractions.cube.ICubeSet;

import java.util.Arrays;
import java.util.stream.Stream;

public class StaticCubeSet implements ICubeSet {
    private final ICube[] cubes;

    public StaticCubeSet(ICube... cubes) {
        this.cubes = cubes;
    }

    @Override
    public int size() {
        return cubes.length;
    }

    @Override
    public ICube getAny() {
        return cubes[0];
    }

    @Override
    public Stream<ICube> stream() {
        return Arrays.stream(cubes);
    }
}
