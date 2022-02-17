package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.cube.ICube;
import abstractions.cube.ICubeFilter;
import abstractions.cube.ICubeSet;
import abstractions.cube.Triangle;
import implementation.cube.filter.CubeFilterFactory;
import implementation.cube.set.CachedCubeSet;
import implementation.solution.DynamicPuzzleSolution;

import java.util.ArrayDeque;
import java.util.HashSet;

@Deprecated
public class PathfindingSolver implements IPuzzleSolver {

    @Override
    public IPuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, ICubeSet givenCubes) {
        DynamicPuzzleSolution s = new DynamicPuzzleSolution(dimensionX, dimensionY, dimensionZ);

        HashSet<ICube> used_cubes = new HashSet<>();
        ArrayDeque<Position> free = new ArrayDeque<>();
        CachedCubeSet cubes;
        if(givenCubes.getClass() != CachedCubeSet.class) {
            cubes = new CachedCubeSet(givenCubes.stream().toArray(ICube[]::new));
        }else cubes = (CachedCubeSet) givenCubes;

        ICube seed = cubes.matching(s.getFilterAt(0,0,0)).findAny().orElseThrow();
        s.set(0, 0, 0, seed);
        used_cubes.add(seed);

        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                for (int z = 0; z < dimensionZ; z++) {
                    free.add(new Position(x, y, z));
                }
            }
        }
        free.remove(new Position(0, 0, 0));

        while(!free.isEmpty()) {

        }



        // Steps:
        // 1. find a seed
        // 2. Try to build around this seed, keeping track of all operations along the way
        // 3. If we hit a dead end, go one step back.
        return s;
    }

    private record Position(int x, int y, int z) {}
}
