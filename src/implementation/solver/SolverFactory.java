package implementation.solver;

import abstractions.IPuzzleSolver;
import abstractions.cube.ICube;

public final class SolverFactory {

    public static IPuzzleSolver getSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        return new StagedSolver(dimensionX, dimensionY, dimensionZ, cubes);
    }
}
