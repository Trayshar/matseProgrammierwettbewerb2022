package implementation.solver;

import abstractions.IPuzzleSolver;
import implementation.PuzzleSolution;
import implementation.cube.StaticCubeSet;

public class PathfindingSolver implements IPuzzleSolver {

    @Override
    public PuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, StaticCubeSet cubes) {
        // Steps:
        // 1. find a seed
        // 2. Try to build around this seed, keeping track of all operations along the way
        // 3. If we hit a dead end, go one step back.
        return null;
    }
}
