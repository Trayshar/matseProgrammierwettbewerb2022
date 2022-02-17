package abstractions;

import abstractions.cube.ICubeSet;

public interface IPuzzleSolver {
    IPuzzleSolution solve(int dimensionX, int dimensionY, int dimensionZ, ICubeSet cubes) throws PuzzleNotSolvableException;
}
