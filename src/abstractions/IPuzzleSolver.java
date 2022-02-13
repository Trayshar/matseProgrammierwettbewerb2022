package abstractions;

import implementation.cube.StaticCubeSet;

public interface IPuzzleSolver {
    IPuzzelSolution solve(int dimensionX, int dimensionY, int dimensionZ, StaticCubeSet cubes);
}
