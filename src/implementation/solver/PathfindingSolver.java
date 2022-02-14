package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.cube.ICube;
import implementation.PuzzleSolution;
import implementation.cube.StaticCubeSet;

public class PathfindingSolver implements IPuzzleSolver {

    @Override
    public IPuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, StaticCubeSet cubes) {
        PuzzleSolution s = new PuzzleSolution(dimensionX, dimensionY, dimensionZ);

        //TODO: Setze Seed
        ICube seed = null;


        // Steps:
        // 1. find a seed
        // 2. Try to build around this seed, keeping track of all operations along the way
        // 3. If we hit a dead end, go one step back.
        return null;
    }
}
