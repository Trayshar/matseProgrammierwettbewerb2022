package implementation.solver;

import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import implementation.LinearCoordinateGenerator;
import implementation.solution.DynamicPuzzleSolution;

public final class SolverFactory {
    /**
     * Returns the solver for a solution of type "Zero", meaning all side lengths are 1 and the only cube has no triangles.
     */
    private static IPuzzleSolver zero(ICube cube) {
        IPuzzleSolution s = new DynamicPuzzleSolution(1,1 ,1);
        s.set(0, 0, 0, cube);

        return new IPuzzleSolver() {
            @Override
            public void prepare() {}

            @Override
            public IPuzzleSolution solve() {
                return s;
            }

            @Override
            public IPuzzleSolution solveConcurrent() {
                return s;
            }

            @Override
            public String getCurrentStatus() {
                return "";
            }

            @Override
            public IPuzzleSolver deepClone() {
                return this;
            }
        };
    }

    /**
     * Returns the solver for a solution of type "Line", meaning all but one dimensions are 1.
     */
    private static IPuzzleSolver line(int dimX, int dimY, int dimZ, ICube[] cubes) {
        return new SimpleSolver(dimX, dimY, dimZ, cubes);
    }

    /**
     * Returns the solver for a solution of type "Plane", meaning one dimension is 1 and the other two are greater than 1.
     */
    private static IPuzzleSolver plane(int dimX, int dimY, int dimZ, ICube[] twoConnected, ICube[] threeConnected, ICube[] fourRound) {
        return new StagedSolver(dimX, dimY, dimZ, twoConnected, threeConnected, fourRound);
    }

    /**
     * Returns the solver for a solution of type "Cuboid", meaning all dimensions are greater than 1.
     */
    private static IPuzzleSolver cuboid(int dimX, int dimY, int dimZ, ICube[] threeEdge, ICube[] fourConnected, ICube[] five, ICube[] six) {
        return new TreeSolver(dimX, dimY, dimZ, threeEdge, fourConnected, five, six, new LinearCoordinateGenerator(dimX, dimY, dimZ));
    }

    /**
     * Returns the matching solver for the problem...
     * Throws a PuzzleNotSolvableException if the given data has no solutions by design (missing cubes).
     */
    public static IPuzzleSolver of(int dimX, int dimY, int dimZ, ICube[] cubes) throws PuzzleNotSolvableException {
        if(dimX * dimY * dimZ != cubes.length) throw new PuzzleNotSolvableException("Expected number of cubes doesn't match given number");

        // Sort X, Y and Z so that X ≥ Y ≥ Z
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
                    if(cubes[0].getUniqueCubeId() != 0) throw new PuzzleNotSolvableException("CubeType Zero not satisfied!");
                    return zero(cubes[0]);
                }else{ // y = z = 1, x > 1
                    // + -- + -- + ... + -- + -- +
                    // | 1  | 2o | ... | 2o | 1  |
                    // + -- + -- + ... + -- + -- +
                    // One: 2
                    // TwoOpposite: dimX - 2
                    int i1 = 0;
                    int i2 = 0;
                    int size2 = dimX - 2;

                    for(ICube cube : cubes) {
                        switch (CubeType.get(cube.getTriangles())) {
                            case One -> {
                                if(i1 < 2) i1++;
                                else throw new PuzzleNotSolvableException("More than 2 cubes of type \"One\" found!");
                            }
                            case TwoOpposite -> {
                                if(i2 < size2) i2++;
                                else throw new PuzzleNotSolvableException("More than " + size2 + " cubes of type \"TwoOpposite\" found!");
                            }
                            default -> throw new PuzzleNotSolvableException("Unexpected cube type!");
                        }
                    }
                    return line(dimX, dimY, dimZ, cubes);
                }
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
                // TwoConnected: 4
                // ThreeConnected: 2 * (dimX + dimY - 4)
                // FourRound: (dimX - 2) * (dimY - 2)

                ICube[] twoConnected = new ICube[4];
                int i2 = 0;

                int size3 = 2 * (dimX + dimY - 4);
                ICube[] threeConnected = new ICube[size3];
                int i3 = 0;

                int size4 = (dimX - 2) * (dimY - 2);
                ICube[] fourRound = new ICube[size4];
                int i4 = 0;

                for(ICube cube : cubes) {
                    switch (CubeType.get(cube.getTriangles())) {
                        case TwoConnected -> {
                            if(i2 < 4) twoConnected[i2++] = cube;
                            else throw new PuzzleNotSolvableException("More than 4 cubes of type \"TwoConnected\" found!");
                        }
                        case ThreeConnected -> {
                            if(i3 < size3) threeConnected[i3++] = cube;
                            else throw new PuzzleNotSolvableException("More than " + size3 + " cubes of type \"ThreeConnected\" found!");
                        }
                        case FourRound -> {
                            if(i4 < size4) fourRound[i4++] = cube;
                            else throw new PuzzleNotSolvableException("More than " + size4 + " cubes of type \"FourRound\" found!");
                        }
                        default -> throw new PuzzleNotSolvableException("Unexpected cube type!");
                    }
                }
                return plane(dimX, dimY, dimZ, twoConnected, threeConnected, fourRound);
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
            // ThreeEdge: 8
            //
            // 4*(dimX-2)+4*(dimY-2)+4*(dimZ-2)
            // FourConnected: 4 * (dimX + dimY + dimZ - 6)
            //
            // 2 * (dimX - 2) * (dimY - 2) + 2 * (dimZ - 2) * (dimX - 2) + 2 * (dimZ - 2) * (dimY - 2)
            // Five: 2 * ((dimX - 2) * (dimY - 2) + (dimZ - 2) * (dimX - 2) + (dimZ - 2) * (dimY - 2))
            //
            // Six: (dimX - 2) * (dimY - 2) * (dimZ - 2)

            ICube[] threeEdge = new ICube[8];
            int i3 = 0;

            int size4 = 4 * (dimX + dimY + dimZ - 6);
            ICube[] fourConnected = new ICube[size4];
            int i4 = 0;

            int size5 = 2 * ((dimX - 2) * (dimY - 2) + (dimZ - 2) * (dimX - 2) + (dimZ - 2) * (dimY - 2));
            ICube[] five = new ICube[size5];
            int i5 = 0;

            int size6 = (dimX - 2) * (dimY - 2) * (dimZ - 2);
            ICube[] six = new ICube[size6];
            int i6 = 0;

            for(ICube cube : cubes) {
                switch (CubeType.get(cube.getTriangles())) {
                    case ThreeEdge -> {
                        if(i3 < 8) threeEdge[i3++] = cube;
                        else throw new PuzzleNotSolvableException("More than 8 cubes of type \"ThreeEdge\" found!");
                    }
                    case FourConnected -> {
                        if(i4 < size4) fourConnected[i4++] = cube;
                        else throw new PuzzleNotSolvableException("More than " + size4 + " cubes of type \"FourConnected\" found!");
                    }
                    case Five -> {
                        if(i5 < size5) five[i5++] = cube;
                        else throw new PuzzleNotSolvableException("More than " + size5 + " cubes of type \"Five\" found!");
                    }
                    case Six -> {
                        if(i6 < size6) six[i6++] = cube;
                        else throw new PuzzleNotSolvableException("More than " + size6 + " cubes of type \"Six\" found!");
                    }
                    default -> throw new PuzzleNotSolvableException("Unexpected cube type!");
                }
            }
            return cuboid(dimX, dimY, dimZ, threeEdge, fourConnected, five, six);
        }
    }
}
