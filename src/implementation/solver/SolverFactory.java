package implementation.solver;

import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import abstractions.cube.ICubeSorter;

import static abstractions.cube.CubeType.*;

public final class SolverFactory {

    public static IPuzzleSolver getSolver(int dimensionX, int dimensionY, int dimensionZ, ICube[] cubes) {
        return new StagedSolver(dimensionX, dimensionY, dimensionZ, cubes);
    }

    /**
     * Checks whether a cube solution with the given dimensions is possible with the given cubes.
     * Throws a PuzzleNotSolvableException if not.
     */
    public static void checkSolvable(int dimX, int dimY, int dimZ, ICubeSorter sorter) throws PuzzleNotSolvableException {
        if(dimX * dimY * dimZ != sorter.getNumCubes()) throw new PuzzleNotSolvableException("Expected number of cubes doesn't match given number");

        // X ≥ Y ≥ Z
        if (dimZ > dimY) {
            int tmp = dimZ;
            dimZ = dimY;
            dimY = tmp;
        }
        if (dimY > dimX) {
            int tmp = dimX;
            dimX = dimY;
            dimY = tmp;
        }
        if (dimZ > dimY) {
            int tmp = dimZ;
            dimZ = dimY;
            dimY = tmp;
        }

        if(dimZ == 1) {
            if(dimY == 1) {
                if(dimX == 1) { // x = y = z = 1
                    // + -- +
                    // | 0  |
                    // + -- +
                    check(sorter, Zero, 1);
                }else{ // y = z = 1, x > 1
                    // + -- + -- + ... + -- + -- +
                    // | 1  | 2o | ... | 2o | 1  |
                    // + -- + -- + ... + -- + -- +
                    check(sorter, One, 2);
                    check(sorter, TwoOpposite, dimX - 2);
                }
            } else if(dimY == 2) { // x ≥ 2, y = 2, z = 1
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                check(sorter, TwoConnected, 4);
                check(sorter, ThreeConnected, 2*dimX - 4);
            } else { // x, y ≥ 3, z = 1
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                // | 3c | 4r | ... | 4r | 3c |
                // + -- + -- + ... + -- + -- +
                // .    .    .     .    .    .
                // .    .    .     .    .    .
                // + -- + -- + ... + -- + -- +
                // | 3c | 4r | ... | 4r | 3c |
                // + -- + -- + ... + -- + -- +
                // | 2c | 3c | ... | 3c | 2c |
                // + -- + -- + ... + -- + -- +
                check(sorter, TwoConnected, 4);
                check(sorter, ThreeConnected, 2 * (dimX + dimY - 4) );
                check(sorter, FourRound, (dimX - 2) * (dimY - 2) );
            }
        } else {// x, y, z ≥ 2
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 3e | 4c | ... | 4c | 3e |    | 4c | 5  | ... | 5  | 4c |    | 3e | 4c | ... | 4c | 3e |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 4c | 5  | ... | 5  | 4c |    | 5  | 6  | ... | 6  | 5  |    | 4c | 5  | ... | 5  | 4c |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // .    .    .     .    .    .    .    .    .     .    .    .    .    .    .     .    .    .
            // .    .    .     .    .    .    .    .    .     .    .    .    .    .    .     .    .    .
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 4c | 5  | ... | 5  | 4c |    | 5  | 6  | ... | 6  | 5  |    | 4c | 5  | ... | 5  | 4c |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            // | 3e | 4c | ... | 4c | 3e |    | 4c | 5  | ... | 5  | 4c |    | 3e | 4c | ... | 4c | 3e |
            // + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +    + -- + -- + ... + -- + -- +
            check(sorter, ThreeEdge, 8);
            // 4*(dimX-2)+4*(dimY-2)+4*(dimZ-2)
            check(sorter, FourConnected, 4 * (dimX + dimY + dimZ - 6) );
            // 2 * (dimX - 2) * (dimY - 2) + 2 * (dimZ - 2) * (dimX - 2) + 2 * (dimZ - 2) * (dimY - 2)
            check(sorter, Five, 2 * ((dimX - 2) * (dimY - 2) + (dimZ - 2) * (dimX - 2) + (dimZ - 2) * (dimY - 2)) );
            check(sorter, Six, (dimX - 2) * (dimY - 2) * (dimZ - 2) );
        }
    }

    private static void check(ICubeSorter sorter, CubeType type, int expected) throws PuzzleNotSolvableException {
        int i = sorter.unique(type);
        if(i != expected) throw new PuzzleNotSolvableException("Type " + type + " required " + expected + " cubes, but " + i + " were found!");
        System.out.println("Type " + type + " found " + i + " times");
    }
}
