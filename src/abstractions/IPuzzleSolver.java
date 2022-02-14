package abstractions;

import implementation.cube.StaticCubeSet;

public interface IPuzzleSolver {
    IPuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, StaticCubeSet cubes);
}
