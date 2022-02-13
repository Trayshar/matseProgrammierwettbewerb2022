package abstractions;

import abstractions.cube.ICubeSet;
import implementation.PuzzleSolution;

public interface IPuzzleSolver {
    PuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, ICubeSet cubes);
}
