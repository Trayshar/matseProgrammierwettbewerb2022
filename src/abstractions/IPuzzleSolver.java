package abstractions;

import implementation.PuzzleSolution;
import abstractions.cube.ICube;

public interface IPuzzleSolver {
    PuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes);
}
