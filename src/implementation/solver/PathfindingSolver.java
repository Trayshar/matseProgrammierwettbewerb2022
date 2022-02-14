package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSet;
import implementation.PuzzleSolution;
import implementation.cube.set.CachedCubeSet;

public class PathfindingSolver implements IPuzzleSolver {

    @Override
    public IPuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, ICubeSet givenCubes) {
        PuzzleSolution s = new PuzzleSolution(dimensionX, dimensionY, dimensionZ);

        CachedCubeSet cubes;
        if(givenCubes.getClass() != CachedCubeSet.class) {
            cubes = new CachedCubeSet(givenCubes.stream().toArray(ICube[]::new));
        }else cubes = (CachedCubeSet) givenCubes;




        // Steps:
        // 1. find a seed
        // 2. Try to build around this seed, keeping track of all operations along the way
        // 3. If we hit a dead end, go one step back.
        return null;
    }
}
