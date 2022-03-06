package implementation.solver;

import abstractions.CoordinateGenerator;
import abstractions.IPuzzleSolution;
import abstractions.IPuzzleSolver;
import abstractions.PuzzleNotSolvableException;
import abstractions.cube.CubeType;
import abstractions.cube.ICube;
import implementation.LinearCoordinateGenerator;
import implementation.solution.DynamicPuzzleSolution;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.*;

public final class SolverFactory {
    /**
     * Returns the solver for a solution of type "Zero", meaning all side lengths are 1 and the only cube has no triangles.
     */
    private static PuzzleSolverContainer zero(ICube cube) {
        IPuzzleSolution s = new DynamicPuzzleSolution(1,1 ,1);
        s.set(0, 0, 0, cube);

        return new PuzzleSolverContainer(){
            @Override
            public IPuzzleSolution solve() {
                return s;
            }

            @Override
            public IPuzzleSolution solveWithLogging() {
                return s;
            }

            @Override
            public IPuzzleSolution solveWithTimeout(ExecutorService solverExecutor, ScheduledExecutorService loggingExecutor, int seconds) {
                return s;
            }
        };
    }

    /**
     * Returns the solver for a solution of type "Line", meaning all but one dimensions are 1.
     */
    private static PuzzleSolverContainer line(int dimX, int dimY, int dimZ, ICube[] cubes) {
        return new SingleSolverContainer(new SimpleSolver(dimX, dimY, dimZ, cubes));
    }

    /**
     * Returns the solver for a solution of type "Plane", meaning one dimension is 1 and the other two are greater than 1.
     */
    private static PuzzleSolverContainer plane(int dimX, int dimY, int dimZ, ICube[] twoConnected, ICube[] threeConnected, ICube[] fourRound) {
        return new SingleSolverContainer(new StagedSolver(dimX, dimY, dimZ, twoConnected, threeConnected, fourRound));
    }

    /**
     * Returns the solver for a solution of type "Cuboid", meaning all dimensions are greater than 1.
     */
    private static PuzzleSolverContainer cuboid(int dimX, int dimY, int dimZ, ICube[] threeEdge, ICube[] fourConnected, ICube[] five, ICube[] six) {
        EnumMap<CubeType, ICube[]> cubeMap = new EnumMap<>(CubeType.class);
        cubeMap.put(CubeType.ThreeEdge, threeEdge);
        cubeMap.put(CubeType.FourConnected, fourConnected);
        cubeMap.put(CubeType.Five, five);
        cubeMap.put(CubeType.Six, six);

        return new TreeSolverContainer(dimX, dimY, dimZ, cubeMap, new LinearCoordinateGenerator(dimX, dimY, dimZ));
    }

    /**
     * Returns the matching solver for the problem...
     * Throws a PuzzleNotSolvableException if the given data has no solutions by design (missing cubes).
     */
    public static PuzzleSolverContainer of(int dimX, int dimY, int dimZ, ICube[] cubes) throws PuzzleNotSolvableException {
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

    /**
     * Abstract logic to execute a solver
     */
    public abstract static class PuzzleSolverContainer {
        /**
         * Executes the given solver. If a solution is found it is returned. In case of an error null is returned.
         */
        public abstract IPuzzleSolution solve();

        /**
         * Executes the given solver with logging enabled. If a solution is found it is returned. In case of an error null is returned.
         */
        public abstract IPuzzleSolution solveWithLogging();

        /**
         * Executes the given solver with logging enabled and a specific timeout. If a solution is found it is returned. In case of an error null is returned.
         * The given executors will not get shut down.
         * @throws TimeoutException If the timeout is reached.
         */
        public abstract IPuzzleSolution solveWithTimeout(ExecutorService solverExecutor, ScheduledExecutorService loggingExecutor, int seconds) throws TimeoutException;
    }

    /**
     * Abstract logic to execute a solver on the main thread
     */
    public static class SingleSolverContainer extends PuzzleSolverContainer {
        private final IPuzzleSolver solver;

        private SingleSolverContainer(IPuzzleSolver solver) {
            this.solver = solver;
        }

        @Override
        public IPuzzleSolution solve() {
            try {
                this.solver.prepare();
                return this.solver.solve();
            } catch (PuzzleNotSolvableException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public IPuzzleSolution solveWithLogging() {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(this.solver, 1, 1, TimeUnit.SECONDS);
            try {
                this.solver.prepare();
                var solution = this.solver.solve();
                executorService.shutdownNow();
                return solution;
            } catch (PuzzleNotSolvableException e) {
                e.printStackTrace();
                executorService.shutdownNow();
                return null;
            }
        }

        @Override
        public IPuzzleSolution solveWithTimeout(ExecutorService solverExecutor, ScheduledExecutorService loggingExecutor, int seconds) throws TimeoutException {
            Future<IPuzzleSolution> solverHandle = null;
            ScheduledFuture<?> loggingHandle = loggingExecutor.scheduleAtFixedRate(this.solver, 1, 1, TimeUnit.SECONDS);
            IPuzzleSolution solution = null;
            try {
                this.solver.prepare();
                solverHandle = solverExecutor.submit((Callable<IPuzzleSolution>) this.solver);
                solution = solverHandle.get(seconds, TimeUnit.SECONDS);
            } catch (PuzzleNotSolvableException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                if(solverHandle != null) solverHandle.cancel(true);
                loggingHandle.cancel(true);
            }

            return solution;
        }
    }

    /**
     * Execution logic for a concurrent TreeSolver
     */
    public static class TreeSolverContainer extends PuzzleSolverContainer {
        private final List<IPuzzleSolver> solvers;

        public TreeSolverContainer(int dimensionX, int dimensionY, int dimensionZ, EnumMap<CubeType, ICube[]> cubeMap, CoordinateGenerator generator) {
            TreeSolver s = new TreeSolver(dimensionX, dimensionY, dimensionZ, cubeMap, generator);
            TreeSolver s1 = new TreeSolver(dimensionX, dimensionY, dimensionZ, cubeMap, generator);
            TreeSolver s2 = new TreeSolver(dimensionX, dimensionY, dimensionZ, cubeMap, generator);
            TreeSolver s3 = new TreeSolver(dimensionX, dimensionY, dimensionZ, cubeMap, generator);

            s.prepare();
            s1.syncStartingNode(s);
            s2.syncStartingNode(s);
            s3.syncStartingNode(s);

            solvers = List.of(s, s1, s2, s3);
        }

        @Override
        public IPuzzleSolution solve() {
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            IPuzzleSolution solution = null;
            try {
                solution = executorService.invokeAny(this.solvers);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                executorService.shutdownNow();
            }

            return solution;
        }

        @Override
        public IPuzzleSolution solveWithLogging() {
            ExecutorService solverExecutor = Executors.newFixedThreadPool(4);
            ScheduledExecutorService loggingExecutor = Executors.newSingleThreadScheduledExecutor();
            IPuzzleSolution solution = null;
            try {
                loggingExecutor.scheduleAtFixedRate(() -> {
                    for (int i = 0; i < 4; i++) {
                        System.out.println("[T" + i + "] " + solvers.get(i).getCurrentStatus());
                    }
                }, 1, 1, TimeUnit.SECONDS);

                solution = solverExecutor.invokeAny(solvers);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                solverExecutor.shutdownNow();
                loggingExecutor.shutdownNow();
            }

            return solution;
        }

        @Override
        public IPuzzleSolution solveWithTimeout(ExecutorService solverExecutor, ScheduledExecutorService loggingExecutor, int seconds) throws TimeoutException {
            ScheduledFuture<?> loggingHandle = null;
            IPuzzleSolution solution = null;
            try {
                loggingHandle = loggingExecutor.scheduleAtFixedRate(() -> {
                    for (int i = 0; i < 4; i++) {
                        System.out.println("[T" + i + "] " + solvers.get(i).getCurrentStatus());
                    }
                }, 1, 1, TimeUnit.SECONDS);

                solution = solverExecutor.invokeAny(solvers, seconds, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                if (loggingHandle != null) loggingHandle.cancel(true);
            }

            return solution;
        }
    }
}
